/*
 *
 * This is a simple Content Management System (CMS)
 * Copyright (C) 2011  Imran M Yousuf (imyousuf@smartitengineering.com)
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
package com.smartitengineering.cms.spi.impl.type.search;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.smartitengineering.cms.api.common.SearchResult;
import com.smartitengineering.cms.api.event.Event.EventType;
import com.smartitengineering.cms.api.event.Event.Type;
import com.smartitengineering.cms.api.event.EventListener;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.api.type.Filter;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.cms.spi.impl.SearchBeanLoader;
import com.smartitengineering.cms.spi.impl.content.search.ContentSearcherImpl;
import com.smartitengineering.cms.spi.impl.events.SolrFieldNames;
import com.smartitengineering.cms.spi.type.ContentTypeSearcher;
import com.smartitengineering.common.dao.search.CommonFreeTextSearchDao;
import com.smartitengineering.dao.common.queryparam.MatchMode;
import com.smartitengineering.dao.common.queryparam.QueryParameter;
import com.smartitengineering.dao.common.queryparam.QueryParameterFactory;
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
 * @author imyousuf
 */
public class ContentTypeSearcherImpl implements ContentTypeSearcher {

  public static final String REINDEX_LISTENER_NAME = "typeReindexEventListener";
  protected final transient Logger logger = LoggerFactory.getLogger(getClass());
  @Inject
  private CommonFreeTextSearchDao<ContentType> textSearchDao;
  @Inject
  private SearchBeanLoader<ContentType, ContentTypeId> contentTypeLoader;
  @Inject
  @Named(REINDEX_LISTENER_NAME)
  private EventListener reindexListener;
  private final ExecutorService executorService = Executors.newSingleThreadExecutor();
  private static final String SOLR_DATE_FORMAT = DateFormatUtils.ISO_DATETIME_FORMAT.getPattern() + "'Z'";

  @Override
  public void reIndex(ContentTypeId typeId) {
    if (typeId != null) {
      ContentType contentType = contentTypeLoader.loadById(typeId);
      if (contentType != null) {
        reindexListener.notify(SmartContentAPI.getInstance().getEventRegistrar().<ContentType>createEvent(
            EventType.UPDATE, Type.CONTENT_TYPE, contentType));
      }
    }
  }

  @Override
  public void reIndex(final WorkspaceId workspaceId) {
    executorService.submit(new Runnable() {

      @Override
      public void run() {
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
        ContentTypeId lastId = null;
        List<QueryParameter> params = new ArrayList<QueryParameter>();
        while (hasMore) {
          params.clear();
          if (param != null) {
            params.add(param);
          }
          params.add(maxResultsParam);
          if (lastId != null) {
            try {
              params.add(QueryParameterFactory.getGreaterThanPropertyParam("id", contentTypeLoader.getByteArrayFromId(
                  lastId)));
            }
            catch (Exception ex) {
              logger.warn("Could not add last id clause " + lastId.toString(), ex);
            }
          }
          List<ContentType> list = contentTypeLoader.getQueryResult(params);
          if (list == null || list.isEmpty()) {
            hasMore = false;
          }
          else {
            final ContentType[] contents = new ContentType[list.size()];
            int index = 0;
            for (ContentType content : list) {
              reindexListener.notify(SmartContentAPI.getInstance().getEventRegistrar().<ContentType>createEvent(
                  EventType.UPDATE, Type.CONTENT_TYPE, content));
              contents[index++] = content;
            }

            lastId = contents[contents.length - 1].getContentTypeID();
          }
        }
      }
    });
  }

  @Override
  public SearchResult<ContentType> search(Filter filter) {
    final StringBuilder finalQuery = new StringBuilder();
    String disjunctionSeperator = " OR ";
    String conjunctionSeperator = " AND ";
    String seperator = filter.isDisjunction() ? disjunctionSeperator : conjunctionSeperator;
    int count = 0;
    finalQuery.append(SolrFieldNames.TYPE).append(": ").append(ContentTypeHelper.CONTENT_TYPE);

    final WorkspaceId workspaceId = filter.getWorkspaceId();
    if (workspaceId != null) {
      finalQuery.append(conjunctionSeperator);
      finalQuery.append((" ("));
      finalQuery.append(SolrFieldNames.WORKSPACEID).append(": ").append(ClientUtils.escapeQueryChars(
          workspaceId.toString()));
      if (filter.isFriendliesIncluded()) {
        Collection<WorkspaceId> friendlies = workspaceId.getWorkspace().getFriendlies();
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
    ContentTypeId parentId = filter.getChildOf();
    if (parentId != null) {
      if (query.length() > 0) {
        query.append(seperator);
      }
      query.append(SolrFieldNames.CONTENTTYPEID).append(": ").append(ClientUtils.escapeQueryChars(parentId.toString()));
    }

    Set<ContentTypeId> contentTypeIds = filter.getInstanceOfContentTypeFilters();
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

    if (StringUtils.isNotBlank(filter.getSearchTerms())) {
      if (query.length() > 0) {
        query.append(seperator);
      }
      query.append(SolrFieldNames.ALL_TEXT).append(": ").append(ClientUtils.escapeQueryChars(filter.getSearchTerms()));
    }
    if (filter.getCreationDateFilter() != null) {
      if (query.length() > 0) {
        query.append(seperator);
      }
      else {
        query.append("workspaceId: [* TO *]").append(seperator);
      }
      QueryParameter<Date> creationDateFilter = filter.getCreationDateFilter();
      String queryStr = ContentSearcherImpl.generateDateQuery(SolrFieldNames.CREATIONDATE, creationDateFilter);
      query.append(queryStr);
    }

    if (filter.getLastModifiedDateFilter() != null) {
      if (query.length() > 0) {
        query.append(seperator);
      }
      else {
        query.append("workspaceId: [* TO *]").append(seperator);
      }
      QueryParameter<Date> lastModifiedDateFilter = filter.getLastModifiedDateFilter();
      String queryStr = ContentSearcherImpl.generateDateQuery(SolrFieldNames.LASTMODIFIEDDATE, lastModifiedDateFilter);
      query.append(queryStr);
    }

    if (query.length() > 0) {
      finalQuery.append(conjunctionSeperator).append('(').append(query.toString()).append(')');
    }
    if (logger.isDebugEnabled()) {
      logger.debug("Query q = " + finalQuery.toString());
    }
    final com.smartitengineering.common.dao.search.SearchResult<ContentType> searchResult =
                                                                             textSearchDao.detailedSearch(QueryParameterFactory.
        getStringLikePropertyParam("q", finalQuery.toString()), QueryParameterFactory.getFirstResultParam(filter.
        getStartFrom()), QueryParameterFactory.getMaxResultsParam(filter.getMaxContents()));
    final Collection<ContentType> result;
    if (searchResult == null || searchResult.getResult() == null || searchResult.getResult().isEmpty()) {
      result = Collections.emptyList();
    }
    else {
      result = new ArrayList<ContentType>();
      for (ContentType content : searchResult.getResult()) {
        if (content != null) {
          result.add(content);
        }
      }
    }
    return SmartContentAPI.getInstance().getContentTypeLoader().createSearchResult(result,
                                                                                   searchResult.getTotalResults());
  }
}
