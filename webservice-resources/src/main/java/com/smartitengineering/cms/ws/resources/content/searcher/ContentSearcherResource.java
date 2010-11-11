/*
 *
 * This is a simple Content Management System (CMS)
 * Copyright (C) 2010  Imran M Yousuf (imyousuf@smartitengineering.com)
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
package com.smartitengineering.cms.ws.resources.content.searcher;

import com.smartitengineering.cms.api.content.Content;
import com.smartitengineering.cms.api.content.Filter;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.type.ContentStatus;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.api.type.MutableContentStatus;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.cms.spi.SmartContentSPI;
import com.smartitengineering.cms.spi.content.ContentSearcher;
import com.smartitengineering.dao.common.queryparam.QueryParameter;
import com.smartitengineering.dao.common.queryparam.QueryParameterFactory;
import com.smartitengineering.util.rest.atom.server.AbstractResource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author kaisar
 */
@Path("/search")
public class ContentSearcherResource extends AbstractResource {

  @QueryParam("content")
  private String content;
  @QueryParam("TypeId")
  private List<String> contentTypeId;
  @QueryParam("id")
  private String id;
  @QueryParam("instanceOf")
  private String instanceOf;
  @QueryParam("status")
  private List<String> statuses;
  @QueryParam("WSId")
  private String workspaceId;
  @QueryParam("field")
  private List<String> fieldQuery;
  @QueryParam("creationDate")
  private String creationDate;
  @QueryParam("lastModifiedDate")
  private String lastModifiedDate;
  @QueryParam("start")
  private int start;
  @QueryParam("count")
  private int count;
  @QueryParam("disjunction")
  private boolean disJunction;
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response get() {
    ContentSearcher searcher = SmartContentSPI.getInstance().getContentSearcher();
    Collection<Content> searchContent;
    ResponseBuilder responseBuilder;
    Filter filter = getFilter();
    searchContent = searcher.search(filter);
    if (searchContent.isEmpty() || searchContent == null) {
      responseBuilder = Response.status(Response.Status.NO_CONTENT);
    }
    else {
      responseBuilder = Response.ok(searchContent);
    }
    return responseBuilder.build();
  }

  private Filter getFilter() {
    Filter filter = SmartContentAPI.getInstance().getContentLoader().craeteFilter();
    if (contentTypeId != null && !contentTypeId.isEmpty()) {
      filter.addContentTypeToFilter(parseCollectionContentTypeId(contentTypeId));
    }
    if (statuses != null && !statuses.isEmpty()) {
      filter.addStatusFilter(parseContentStatus(statuses));
    }
    if (fieldQuery != null && !fieldQuery.isEmpty()) {
      filter.addFieldFilter(parseFieldQuery(fieldQuery));
    }
    logger.info(":::WORKSPACE ID : " + workspaceId);
    if (StringUtils.isNotBlank(workspaceId)) {
      filter.setWorkspaceId(parseWorkspaceId(workspaceId));
    }
    logger.info(":::START FROM : " + String.valueOf(start));
    filter.setStartFrom(start);
    logger.info(":::NUMBER OF ITEM : " + String.valueOf(count));
    filter.setMaxContents(count);
    logger.info(String.valueOf(":::VAULE OF DISJUNCTION : " + disJunction));
    filter.setDisjunction(disJunction);
    logger.info(":::CREATION DATE : " + creationDate);
    if (creationDate != null) {
      filter.setCreationDateFilter(formatDate(creationDate));
    }
    logger.info(":::LAST MODIFIED DATE : " + lastModifiedDate);
    if (lastModifiedDate != null) {
      filter.setLastModifiedDateFilter(formatDate(lastModifiedDate));
    }
    return filter;
  }

  private ContentTypeId[] parseCollectionContentTypeId(List<String> strCollectionContentTypeId) {
    Collection<ContentTypeId> contentTypeIds = new ArrayList<ContentTypeId>();
    for (String strContentTypeId : strCollectionContentTypeId) {
      logger.info(":::CONTENT TYPE ID AS STRING : " + strContentTypeId);
      if (StringUtils.isBlank(strContentTypeId)) {
        continue;
      }
      final ContentTypeId id = parseContentTypeId(strContentTypeId);
      if (id != null) {
        contentTypeIds.add(id);
      }
    }
    ContentTypeId[] retContentTypeIds = contentTypeIds.toArray(new ContentTypeId[contentTypeIds.size()]);
    return retContentTypeIds;
  }

  private ContentStatus[] parseContentStatus(List<String> strCollectionContentStatus) {
    Collection<ContentStatus> contentStatuses = new ArrayList<ContentStatus>();
    for (String strContentStatus : strCollectionContentStatus) {
      if (StringUtils.isBlank(strContentStatus)) {
        continue;
      }
      logger.info(":::CONTENT STATUS : " + strContentStatus);
      MutableContentStatus contentStatus = SmartContentAPI.getInstance().getContentTypeLoader().
          createMutableContentStatus();
      contentStatus.setName(strContentStatus);
      contentStatuses.add(contentStatus);
    }
    ContentStatus[] retContentStatus = contentStatuses.toArray(new ContentStatus[contentStatuses.size()]);
    return retContentStatus;
  }

  private WorkspaceId parseWorkspaceId(String strWorkspaceId) {
    if (StringUtils.isBlank(strWorkspaceId)) {
      return null;
    }
    logger.info(":::WORKSPACE ID : " + strWorkspaceId);
    String[] workspaceParam = splitStr(strWorkspaceId, ",");
    if (workspaceParam.length < 2) {
      return null;
    }
    WorkspaceId parsedWorkspaceId = SmartContentAPI.getInstance().getWorkspaceApi().createWorkspaceId(workspaceParam[0],
                                                                                                      workspaceParam[1]);
    return parsedWorkspaceId;
  }

  private QueryParameter[] parseFieldQuery(List<String> strFieldQuery) {
    Collection<QueryParameter> parseQuery = new ArrayList<QueryParameter>();
    for (String strSingleFieldQuery : strFieldQuery) {
      if (StringUtils.isBlank(strSingleFieldQuery)) {
        continue;
      }
      logger.info(":::FIELD QUERY : " + strSingleFieldQuery);
      String[] values = splitStr(strSingleFieldQuery, ",");
      if (values.length < 2) {
        continue;
      }
      parseQuery.add(QueryParameterFactory.getStringLikePropertyParam(values[0], values[1]));
    }
    QueryParameter[] parsedQueryParameter = parseQuery.toArray(new QueryParameter[parseQuery.size()]);
    return parsedQueryParameter;
  }

  private ContentTypeId parseContentTypeId(String strContentTypeId) {
    String[] contentTypeIdStr = splitStr(strContentTypeId, ",");
    if (contentTypeIdStr.length < 4) {
      return null;
    }
    WorkspaceId contentWorkspaceId =
                SmartContentAPI.getInstance().getWorkspaceApi().createWorkspaceId(contentTypeIdStr[0],
                                                                                  contentTypeIdStr[1]);
    ContentTypeId parsedContentTypeId = SmartContentAPI.getInstance().getContentTypeLoader().createContentTypeId(
        contentWorkspaceId, contentTypeIdStr[2], contentTypeIdStr[3]);
    return parsedContentTypeId;
  }

  private String[] splitStr(String string, String splitChar) {
    String[] retStr;
    retStr = string.split(splitChar);
    return retStr;
  }

  private QueryParameter<Date> formatDate(String strDate) {
    if (StringUtils.isBlank(strDate)) {
      return null;
    }
    QueryParameter<Date> queryParameter = null;
    if (strDate.startsWith(">")) {
      String date = strDate.replace(">", "");
      queryParameter = QueryParameterFactory.getGreaterThanPropertyParam("graterThan", new Date(Long.parseLong(date)));
    }
    else if (strDate.startsWith("<")) {
      String date = strDate.replace("<", "");
      queryParameter = QueryParameterFactory.getLesserThanPropertyParam("lessThan", new Date(Long.parseLong(date)));
    }
    else if (strDate.startsWith(">=")) {
      String date = strDate.replaceAll(">=", "");
      queryParameter = QueryParameterFactory.getGreaterThanEqualToPropertyParam("graterOrEqual", new Date(Long.parseLong(
          date)));
    }
    else if (strDate.startsWith("<=")) {
      String date = strDate.replaceAll("<=", "");
      queryParameter = QueryParameterFactory.getLesserThanEqualToPropertyParam("lessOrEqual", new Date(Long.parseLong(
          date)));
    }
    else if (strDate.contains(",")) {
      String[] date = strDate.split(",");
      queryParameter = QueryParameterFactory.getBetweenPropertyParam("between", new Date(Long.parseLong(date[0])), new Date(Long.
          parseLong(date[1])));
    }
    else {
      queryParameter = null;
    }
    return queryParameter;
  }

  @Override
  protected String getAuthor() {
    return "Smart CMS";
  }
}
