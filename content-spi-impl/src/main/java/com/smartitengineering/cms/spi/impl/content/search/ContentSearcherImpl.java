/*
 *
 * This is a simple Content Management System (CMS)
 * Copyright (C) 2009  Imran M Yousuf (imyousuf@smartitengineering.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.smartitengineering.cms.spi.impl.content.search;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.smartitengineering.cms.api.content.Content;
import com.smartitengineering.cms.api.content.ContentId;
import com.smartitengineering.cms.api.content.Filter;
import com.smartitengineering.cms.api.common.SearchResult;
import com.smartitengineering.cms.api.event.Event.EventType;
import com.smartitengineering.cms.api.event.Event.Type;
import com.smartitengineering.cms.api.event.EventListener;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.type.ContentStatus;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.cms.spi.content.ContentSearcher;
import com.smartitengineering.cms.spi.impl.content.PersistentContent;
import com.smartitengineering.cms.spi.impl.events.SolrFieldNames;
import com.smartitengineering.common.dao.search.CommonFreeTextSearchDao;
import com.smartitengineering.dao.common.CommonReadDao;
import com.smartitengineering.dao.common.queryparam.BiOperandQueryParameter;
import com.smartitengineering.dao.common.queryparam.MatchMode;
import com.smartitengineering.dao.common.queryparam.OperatorType;
import com.smartitengineering.dao.common.queryparam.ParameterType;
import com.smartitengineering.dao.common.queryparam.QueryParameter;
import com.smartitengineering.dao.common.queryparam.QueryParameterCastHelper;
import com.smartitengineering.dao.common.queryparam.QueryParameterFactory;
import com.smartitengineering.dao.common.queryparam.StringLikeQueryParameter;
import com.smartitengineering.dao.common.queryparam.UniOperandQueryParameter;
import com.smartitengineering.dao.impl.hbase.spi.SchemaInfoProvider;
import com.smartitengineering.events.async.api.EventSubscriber;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author kaisar
 */
public class ContentSearcherImpl implements ContentSearcher {

  public static final String REINDEX_LISTENER_NAME = "contentReindexEventListener";
  protected final transient Logger logger = LoggerFactory.getLogger(getClass());
  @Inject
  private CommonFreeTextSearchDao<Content> textSearchDao;
  @Inject
  private CommonReadDao<PersistentContent, ContentId> readDao;
  @Inject
  private SchemaInfoProvider<PersistentContent, ContentId> schemaInfoProvider;
  // Injected so that the quartz service starts
  @Inject(optional = true)
  private EventSubscriber subscriber;
  @Inject
  @Named(REINDEX_LISTENER_NAME)
  private EventListener reindexListener;
  private final ExecutorService executorService = Executors.newSingleThreadExecutor();
  private static final String SOLR_DATE_FORMAT = DateFormatUtils.ISO_DATETIME_FORMAT.getPattern() + "'Z'";

  @Override
  public SearchResult<Content> search(Filter filter) {
    final StringBuilder finalQuery = new StringBuilder();
    String disjunctionSeperator = " OR ";
    String conjunctionSeperator = " AND ";
    String seperator = filter.isDisjunction() ? disjunctionSeperator : conjunctionSeperator;
    int count = 0;
    Set<ContentTypeId> contentTypeIds = filter.getContentTypeFilters();
    finalQuery.append(SolrFieldNames.TYPE).append(": ").append(ContentHelper.CONTENT);
    final WorkspaceId workspaceId = filter.getWorkspaceId();
    if (workspaceId != null) {
      finalQuery.append(conjunctionSeperator);
      finalQuery.append((" ("));
      finalQuery.append(SolrFieldNames.WORKSPACEID).append(": ").append(ClientUtils.escapeQueryChars(
          workspaceId.toString()));
      if (filter.isFriendliesIncluded()) {
        Collection<WorkspaceId> friendlies = workspaceId.getWorkspae().getFriendlies();
        if (friendlies != null && !friendlies.isEmpty()) {
          finalQuery.append(disjunctionSeperator).append("(private: false AND (");
          boolean first = true;
          for (WorkspaceId friendly : friendlies) {
            if (friendly != null) {
              if (first) {
                first = false;
              }
              else {
                finalQuery.append(disjunctionSeperator);
              }
              finalQuery.append(SolrFieldNames.WORKSPACEID).append(": ").append(ClientUtils.escapeQueryChars(friendly.
                  toString()));
            }
          }
          finalQuery.append("))");
        }
      }
      finalQuery.append((") "));
    }
    final StringBuilder query = new StringBuilder();
    if (contentTypeIds != null && !contentTypeIds.isEmpty()) {
      if (query.length() > 0) {
        query.append(seperator);
      }
      query.append("(");
    }
    for (ContentTypeId contentTypeId : contentTypeIds) {
      if (count > 0) {
        query.append(disjunctionSeperator);
      }
      if (contentTypeId != null) {
        query.append(SolrFieldNames.INSTANCE_OF).append(": ").append(ClientUtils.escapeQueryChars(
            contentTypeId.toString()));
      }
      count++;
    }
    if (contentTypeIds != null && !contentTypeIds.isEmpty()) {
      query.append(")");
    }
    if (filter.getCreationDateFilter() != null) {
      if (query.length() > 0) {
        query.append(seperator);
      }
      QueryParameter<Date> creationDateFilter = filter.getCreationDateFilter();
      String queryStr = generateDateQuery(SolrFieldNames.CREATIONDATE, creationDateFilter);
      query.append(queryStr);
    }
    if (filter.getLastModifiedDateFilter() != null) {
      if (query.length() > 0) {
        query.append(seperator);
      }
      QueryParameter<Date> lastModifiedDateFilter = filter.getLastModifiedDateFilter();
      String queryStr = generateDateQuery(SolrFieldNames.LASTMODIFIEDDATE, lastModifiedDateFilter);
      query.append(queryStr);
    }
    if (StringUtils.isNotBlank(filter.getSearchTerms())) {
      if (query.length() > 0) {
        query.append(seperator);
      }
      query.append(SolrFieldNames.ALL_TEXT).append(": ").append(ClientUtils.escapeQueryChars(filter.getSearchTerms()));
    }
    Set<ContentStatus> statuses = filter.getStatusFilters();
    for (ContentStatus contentStatus : statuses) {
      if (query.length() > 0) {
        query.append(seperator);
      }
      if (StringUtils.isNotBlank(contentStatus.getName())) {
        query.append(SolrFieldNames.STATUS).append(": ").append(ClientUtils.escapeQueryChars(contentStatus.getName()));
      }
    }
    Collection<QueryParameter> fieldQuery = filter.getFieldFilters();
    if (fieldQuery != null && !fieldQuery.isEmpty()) {
      for (QueryParameter parameter : fieldQuery) {
        if (parameter.getParameterType().equals(ParameterType.PARAMETER_TYPE_PROPERTY) &&
            parameter instanceof StringLikeQueryParameter) {
          if (query.length() > 0) {
            query.append(seperator);
          }
          StringLikeQueryParameter param = QueryParameterCastHelper.STRING_PARAM_HELPER.cast(parameter);
          query.append(param.getPropertyName()).append(": ").append(ClientUtils.escapeQueryChars(param.getValue()));
        }
      }
    }
    if (query.length() > 0) {
      finalQuery.append(conjunctionSeperator).append('(').append(query.toString()).append(')');
    }
    if (logger.isInfoEnabled()) {
      logger.info("Query q = " + finalQuery.toString());
    }
    final com.smartitengineering.common.dao.search.SearchResult<Content> searchResult = textSearchDao.detailedSearch(QueryParameterFactory.
        getStringLikePropertyParam("q", finalQuery.toString()), QueryParameterFactory.getFirstResultParam(filter.
        getStartFrom()), QueryParameterFactory.getMaxResultsParam(filter.getMaxContents()));
    final Collection<Content> result;
    if (searchResult == null || searchResult.getResult() == null || searchResult.getResult().isEmpty()) {
      result = Collections.emptyList();
    }
    else {
      result = new ArrayList<Content>();
      for (Content content : searchResult.getResult()) {
        if (content != null) {
          result.add(content);
        }
      }
    }
    return SmartContentAPI.getInstance().getContentLoader().createSearchResult(result, searchResult.getTotalResults());
  }

  public static String generateDateQuery(String fieldName, QueryParameter<Date> creationDateFilter) {
    StringBuilder query = new StringBuilder(fieldName).append(": ");
    String dateQuery = "";
    switch (creationDateFilter.getParameterType()) {
      case PARAMETER_TYPE_PROPERTY:
        if (creationDateFilter instanceof UniOperandQueryParameter) {
          UniOperandQueryParameter<Date> param =
                                         (UniOperandQueryParameter<Date>) creationDateFilter;
          switch (param.getOperatorType()) {
            case OPERATOR_EQUAL:
              dateQuery = formatDateInSolrFormat(param.getValue());
              break;
            case OPERATOR_LESSER:
              query.insert(0, "NOT ");
              dateQuery = "[" + formatDateInSolrFormat(param.getValue()) + " TO *]";
//              dateQuery = "-[" + param.getValue() + " TO *]";
              break;
            case OPERATOR_GREATER_EQUAL:
              dateQuery = "[" + formatDateInSolrFormat(param.getValue()) + " TO *]";
              break;
            case OPERATOR_GREATER:
              query.insert(0, "NOT ");
              dateQuery = "[* TO " + formatDateInSolrFormat(param.getValue()) + "]";
//              dateQuery = "-[* TO " + param.getValue() + "]";
              break;
            case OPERATOR_LESSER_EQUAL:
              dateQuery = "[* TO " + formatDateInSolrFormat(param.getValue()) + "]";
              break;
            default:
              dateQuery = "[* TO *]";
          }
        }
        if (creationDateFilter instanceof BiOperandQueryParameter) {
          BiOperandQueryParameter<Date> param =
                                        (BiOperandQueryParameter<Date>) creationDateFilter;
          if (param.getOperatorType().equals(OperatorType.OPERATOR_BETWEEN)) {
            dateQuery = "[" + formatDateInSolrFormat(param.getFirstValue()) + " TO " + formatDateInSolrFormat(param.
                getSecondValue()) + "]";
          }
        }
        break;
      default:
        UniOperandQueryParameter<Date> param =
                                       (UniOperandQueryParameter<Date>) creationDateFilter;
        dateQuery = param.getPropertyName() + ": [* TO *]";
        break;
    }
    query.append(dateQuery);
    return query.toString();
  }

  public static String formatDateInSolrFormat(Date date) {
    return DateFormatUtils.formatUTC(date, SOLR_DATE_FORMAT);
  }

  @Override
  public void reIndex(ContentId contentId) {
    if (contentId != null) {
      PersistentContent content = readDao.getById(contentId);
      if (content != null) {
        reindexListener.notify(SmartContentAPI.getInstance().getEventRegistrar().<Content>createEvent(
            EventType.CREATE, Type.CONTENT, content.getMutableContent()));
      }
    }
  }

  @Override
  public void reIndex(final WorkspaceId workspaceId) {
    if (logger.isInfoEnabled()) {
      logger.info("Re-Indexing " + workspaceId);
    }
    executorService.submit(new Runnable() {

      @Override
      public void run() {
        try {
          final QueryParameter param;
          if (workspaceId == null) {
            param = null;
          }
          else {
            param = QueryParameterFactory.getStringLikePropertyParam("id", new StringBuilder(workspaceId.toString()).
                append(':').toString(), MatchMode.START);
          }
          final QueryParameter<Integer> maxResultsParam = QueryParameterFactory.getMaxResultsParam(100);
          boolean hasMore = true;
          ContentId lastId = null;
          List<QueryParameter> params = new ArrayList<QueryParameter>();
          logger.info("Beginning iteration over contents");
          while (hasMore) {
            if (logger.isInfoEnabled()) {
              logger.info("Trying with Last ID " + lastId);
            }
            params.clear();
            if (param != null) {
              params.add(param);
            }
            params.add(maxResultsParam);
            if (lastId != null) {
              try {
                params.add(QueryParameterFactory.getGreaterThanPropertyParam("id", schemaInfoProvider.getRowIdFromId(
                    lastId)));
              }
              catch (Exception ex) {
                logger.warn("Could not add last id clause " + lastId.toString(), ex);
              }
            }
            List<PersistentContent> list = readDao.getList(params);
            if (logger.isInfoEnabled()) {
              logger.info("Has More " + hasMore);
              logger.info("Content numbers in current iteration " + (list != null ? list.size() : -1));
            }
            if (list == null || list.isEmpty()) {
              hasMore = false;
            }
            else {
              final PersistentContent[] contents = new PersistentContent[list.size()];
              int index = 0;
              for (PersistentContent content : list) {
                if (logger.isInfoEnabled()) {
                  logger.info("Attempting to index " + content.getId());
                }
                reindexListener.notify(SmartContentAPI.getInstance().getEventRegistrar().<Content>createEvent(
                    EventType.UPDATE, Type.CONTENT, content.getMutableContent()));
                contents[index++] = content;
              }
              lastId = contents[contents.length - 1].getId();
            }
            if (logger.isInfoEnabled()) {
              logger.info("Has More " + hasMore);
              logger.info("Content numbers in current iteration " + (list != null ? list.size() : -1));
              logger.info("Last ID " + lastId);
              logger.info("Going for next iteration " + hasMore);
            }
          }
        }
        catch (Exception ex) {
          logger.error("Error reindexing", ex);
        }
      }
    });
  }
}
