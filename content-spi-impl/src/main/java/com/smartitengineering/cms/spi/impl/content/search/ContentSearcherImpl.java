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
import com.smartitengineering.cms.api.content.Content;
import com.smartitengineering.cms.api.content.Filter;
import com.smartitengineering.cms.api.type.ContentStatus;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.spi.content.ContentSearcher;
import com.smartitengineering.cms.spi.impl.content.PersistentContent;
import com.smartitengineering.common.dao.search.CommonFreeTextSearchDao;
import com.smartitengineering.dao.common.queryparam.BiOperandQueryParameter;
import com.smartitengineering.dao.common.queryparam.OperatorType;
import com.smartitengineering.dao.common.queryparam.ParameterType;
import com.smartitengineering.dao.common.queryparam.QueryParameter;
import com.smartitengineering.dao.common.queryparam.QueryParameterCastHelper;
import com.smartitengineering.dao.common.queryparam.QueryParameterFactory;
import com.smartitengineering.dao.common.queryparam.StringLikeQueryParameter;
import com.smartitengineering.dao.common.queryparam.UniOperandQueryParameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
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

  protected final Logger logger = LoggerFactory.getLogger(getClass());
  @Inject
  private CommonFreeTextSearchDao<PersistentContent> textSearchDao;
  private static final String SOLR_DATE_FORMAT = DateFormatUtils.ISO_DATETIME_FORMAT.getPattern() + "'Z'";

  @Override
  public Collection<Content> search(Filter filter) {
    final StringBuilder finalQuery = new StringBuilder();
    String disjunctionSeperator = " OR ";
    String conjunctionSeperator = " AND ";
    String seperator = filter.isDisjunction() ? disjunctionSeperator : conjunctionSeperator;
    int count = 0;
    Set<ContentTypeId> contentTypeIds = filter.getContentTypeFilters();
    finalQuery.append(ContentHelper.TYPE).append(": ").append(ContentHelper.CONTENT);
    if (filter.getWorkspaceId() != null) {
      finalQuery.append(disjunctionSeperator);
      finalQuery.append(ContentHelper.WORKSPACEID).append(": ").append(ClientUtils.escapeQueryChars(filter.
          getWorkspaceId().toString()));
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
        query.append(ContentHelper.INSTANCE_OF).append(": ").append(ClientUtils.escapeQueryChars(
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
      String queryStr = generateDateQuery(ContentHelper.CREATIONDATE, creationDateFilter);
      query.append(queryStr);
    }
    if (filter.getLastModifiedDateFilter() != null) {
      if (query.length() > 0) {
        query.append(seperator);
      }
      QueryParameter<Date> lastModifiedDateFilter = filter.getLastModifiedDateFilter();
      String queryStr = generateDateQuery(ContentHelper.LASTMODIFIEDDATE, lastModifiedDateFilter);
      query.append(queryStr);
    }
    if (StringUtils.isNotBlank(filter.getSearchTerms())) {
      if (query.length() > 0) {
        query.append(seperator);
      }
      query.append(ContentHelper.ALL_TEXT).append(": ").append(ClientUtils.escapeQueryChars(filter.getSearchTerms()));
    }
    Set<ContentStatus> statuses = filter.getStatusFilters();
    for (ContentStatus contentStatus : statuses) {
      if (query.length() > 0) {
        query.append(seperator);
      }
      if (StringUtils.isNotBlank(contentStatus.getName())) {
        query.append(ContentHelper.STATUS).append(": ").append(ClientUtils.escapeQueryChars(contentStatus.getName()));
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
      finalQuery.append(disjunctionSeperator).append('(').append(query.toString()).append(')');
    }
    if (logger.isInfoEnabled()) {
      logger.info("Query q = " + finalQuery.toString());
    }
    final Collection<PersistentContent> searchResult = textSearchDao.search(QueryParameterFactory.
        getStringLikePropertyParam("q", finalQuery.toString()), QueryParameterFactory.getFirstResultParam(filter.
        getStartFrom()), QueryParameterFactory.getMaxResultsParam(filter.getMaxContents()));
    final Collection<Content> result;
    if (searchResult == null || searchResult.isEmpty()) {
      result = Collections.emptyList();
    }
    else {
      result = new ArrayList<Content>();
      for (PersistentContent content : searchResult) {
        if (content != null && content.getMutableContent() != null) {
          result.add(content.getMutableContent());
        }
      }
    }
    return result;
  }

  private String generateDateQuery(String fieldName, QueryParameter<Date> creationDateFilter) {
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
}
