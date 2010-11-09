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
package com.smartitengineering.cms.spi.impl.content;

import com.google.inject.Inject;
import com.smartitengineering.cms.api.content.Content;
import com.smartitengineering.cms.api.content.Filter;
import com.smartitengineering.cms.api.type.ContentStatus;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.cms.spi.content.ContentSearcher;
import com.smartitengineering.common.dao.search.CommonFreeTextSearchDao;
import com.smartitengineering.dao.common.queryparam.BiOperandQueryParameter;
import com.smartitengineering.dao.common.queryparam.OperatorType;
import com.smartitengineering.dao.common.queryparam.ParameterType;
import com.smartitengineering.dao.common.queryparam.QueryParameter;
import com.smartitengineering.dao.common.queryparam.QueryParameterCastHelper;
import com.smartitengineering.dao.common.queryparam.QueryParameterFactory;
import com.smartitengineering.dao.common.queryparam.StringLikeQueryParameter;
import com.smartitengineering.dao.common.queryparam.UniOperandQueryParameter;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.util.ClientUtils;

/**
 *
 * @author kaisar
 */
public class ContentSearcherImpl implements ContentSearcher {

  @Inject
  private CommonFreeTextSearchDao<Content> textSearchDao;
  private StringBuilder query = new StringBuilder();

  @Override
  public Collection<Content> search(Filter filter) {
    String orSeperator = " OR ";
    String andSeperator = " AND ";
    int count = 0;
    Set<ContentTypeId> contentTypeIds = filter.getContentTypeFilters();
    for (ContentTypeId contentTypeId : contentTypeIds) {
      if (count == 0) {
        query.append("(");
      }
      else {
        query.append(orSeperator + "(");
      }
      WorkspaceId workspaceId = contentTypeId.getWorkspace();
      if (workspaceId != null) {
        query.append("+workspaceId: ").append(workspaceId);
      }
      query.append(")");
      count++;
    }
    if (filter.getCreationDateFilter() != null) {
      query.append(orSeperator);
      QueryParameter<Date> creationDateFilter = filter.getCreationDateFilter();
      String queryStr = generateDateQuery(creationDateFilter);
      query.append("creationDate: ").append(queryStr);
    }
    if (filter.getLastModifiedDateFilter() != null) {
      query.append(orSeperator);
      QueryParameter<Date> lastModifiedDateFilter = filter.getLastModifiedDateFilter();
      String queryStr = generateDateQuery(lastModifiedDateFilter);
      query.append("lastModifiedDate: ").append(queryStr);
    }
    Set<ContentStatus> statuses = filter.getStatusFilters();
    for (ContentStatus contentStatus : statuses) {
      query.append(orSeperator);
      if (StringUtils.isNotBlank(contentStatus.getName())) {
        query.append("status: ").append(ClientUtils.escapeQueryChars(contentStatus.getName()));
      }
    }
    Collection<QueryParameter> fieldQuery = filter.getFieldFilters();
    if (fieldQuery != null && !fieldQuery.isEmpty()) {
      for (QueryParameter parameter : fieldQuery) {
        if (parameter.getParameterType().equals(ParameterType.PARAMETER_TYPE_PROPERTY)
            && parameter instanceof StringLikeQueryParameter) {
          query.append(orSeperator);
          StringLikeQueryParameter param = QueryParameterCastHelper.STRING_PARAM_HELPER.cast(parameter);
          query.append(param.getPropertyName()).append(": ").append(ClientUtils.escapeQueryChars(param.getValue()));
        }
      }
    }

    System.out.println(query.toString());
    return textSearchDao.search(QueryParameterFactory.getStringLikePropertyParam("q", query.toString()));
  }

  private String generateDateQuery(QueryParameter<Date> creationDateFilter) {
    String dateQuery = "";
    switch (creationDateFilter.getParameterType()) {
      case PARAMETER_TYPE_PROPERTY:
        if (creationDateFilter instanceof UniOperandQueryParameter) {
          UniOperandQueryParameter<Date> param =
                                         (UniOperandQueryParameter<Date>) creationDateFilter;
          switch (param.getOperatorType()) {
            case OPERATOR_EQUAL:
              dateQuery = param.getValue().toString();
              break;
            case OPERATOR_LESSER:
              dateQuery = "NOT [" + param.getValue() + " TO *]";
//              dateQuery = "-[" + param.getValue() + " TO *]";
              break;
            case OPERATOR_GREATER_EQUAL:
              dateQuery = "[" + param.getValue() + " TO *]";
              break;
            case OPERATOR_GREATER:
              dateQuery = "NOT [* TO " + param.getValue() + "]";
//              dateQuery = "-[* TO " + param.getValue() + "]";
              break;
            case OPERATOR_LESSER_EQUAL:
              dateQuery = "[* TO " + param.getValue() + "]";
              break;
            default:
              dateQuery = "[* TO *]";
          }
        }
        if (creationDateFilter instanceof BiOperandQueryParameter) {
          BiOperandQueryParameter<Date> param =
                                        (BiOperandQueryParameter<Date>) creationDateFilter;
          if (param.getOperatorType().equals(OperatorType.OPERATOR_BETWEEN)) {
            dateQuery = "[" + param.getFirstValue() + " TO " + param.getSecondValue() + "]";
          }
        }
        break;
      default:
        UniOperandQueryParameter<Date> param =
                                       (UniOperandQueryParameter<Date>) creationDateFilter;
        dateQuery = param.getPropertyName() + ": [* TO *]";
        break;
    }
    return dateQuery;
  }
}
