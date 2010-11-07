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
import com.smartitengineering.common.dao.search.CommonFreeTextSearchDao;
import com.smartitengineering.dao.common.queryparam.QueryParameter;
import com.smartitengineering.dao.common.queryparam.QueryParameterFactory;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.solr.client.solrj.util.ClientUtils;

/**
 *
 * @author kaisar
 */
public class ContentSearcherImpl {

  @Inject
  private CommonFreeTextSearchDao<Content> textSearchDao;

  public Collection<Content> search(Filter filter) {
    String orSeperator = " OR ";
    String andSeperator = " AND ";
    int i = 0;
    StringBuilder query = new StringBuilder();
    Set<ContentTypeId> contentTypeIds = filter.getContentTypeFilters();
    for (ContentTypeId contentTypeId : contentTypeIds) {
      if (i == 0) {
        query.append("(");
      }
      else {
        query.append(andSeperator + "(");
      }
      String contentName = contentTypeId.getName();
      if (StringUtils.isNotBlank(contentName)) {
        query.append("+contentName: ").append(ClientUtils.escapeQueryChars(contentName));
      }
      String contentNamespace = contentTypeId.getNamespace();
      if (StringUtils.isNotBlank(contentNamespace)) {
        query.append(orSeperator);
        query.append("+contentNamespace: ").append(ClientUtils.escapeQueryChars(contentNamespace));
      }
      WorkspaceId contentWorkspaceId = contentTypeId.getWorkspace();
      String contentWorkspaceName = contentWorkspaceId.getName();
      if (StringUtils.isNotBlank(contentWorkspaceName)) {
        query.append(orSeperator);
        query.append("+contentWorkspaceName: ").append(ClientUtils.escapeQueryChars(contentWorkspaceName));
      }
      String contentGlobalNamespace = contentWorkspaceId.getGlobalNamespace();
      if (StringUtils.isNotBlank(contentGlobalNamespace)) {
        query.append(orSeperator);
        query.append("+contentGlobalNamespace: ").append(ClientUtils.escapeQueryChars(contentGlobalNamespace));
      }
      query.append(") ");
      i++;
    }

    i = 0;

    QueryParameter<Date> cDate = filter.getCreationDateFilter();
    String contentCreationDate = "";
    if (cDate != null) {
      contentCreationDate = DateFormatUtils.ISO_DATETIME_FORMAT.format(cDate);
    }
    if (StringUtils.isNotBlank(contentCreationDate)) {
      query.append("+contentCreationDate: ").append(ClientUtils.escapeQueryChars(contentCreationDate));
    }

    Collection<QueryParameter> fieldFilters = filter.getFieldFilters();
    for (QueryParameter queryParms : fieldFilters) {
      String contentFieldQuery = queryParms.toString();
      if (i != 0) {
        query.append(orSeperator);
      }
      query.append("+contentFieldQuery: ").append(ClientUtils.escapeQueryChars(contentFieldQuery));
      i++;
    }

    i = 0;

    QueryParameter<Date> lmDate = filter.getLastModifiedDateFilter();
    String contentLastModifiedDate = "";
    if (lmDate != null) {
      contentLastModifiedDate = DateFormatUtils.ISO_DATETIME_FORMAT.format(lmDate);
      query.append("+contentLastModifiedDate: ").append(ClientUtils.escapeQueryChars(contentLastModifiedDate));
    }

    Set<ContentStatus> contentStatuses = filter.getStatusFilters();
    for (ContentStatus contentStatus : contentStatuses) {
      if (i == 0) {
        query.append("(");
      }
      else {
        query.append(andSeperator + "(");
      }
      int contentStatusId = contentStatus.getId();
      String statusName = contentStatus.getName();
      if (StringUtils.isNotBlank(statusName)) {
        query.append("+contentStatusId: ").append(String.valueOf(contentStatusId));
      }
      if (StringUtils.isNotBlank(statusName)) {
        query.append(orSeperator);
        query.append("+statusName: ").append(ClientUtils.escapeQueryChars(statusName));
      }
      query.append(")");
      i++;
    }
    return textSearchDao.search(QueryParameterFactory.getStringLikePropertyParam("q", query.toString()));
  }
}
