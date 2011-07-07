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
package com.smartitengineering.cms.ws.resources.type;

import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.type.Filter;
import com.smartitengineering.cms.ws.resources.content.searcher.ContentSearcherResource;
import com.smartitengineering.util.opensearch.api.Url.RelEnum;
import com.smartitengineering.util.opensearch.impl.OpenSearchDescriptorBuilder;
import com.smartitengineering.util.opensearch.impl.UrlBuilder;
import com.smartitengineering.util.rest.atom.server.AbstractResource;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriBuilder;
import org.apache.abdera.ext.opensearch.OpenSearchConstants;
import org.apache.abdera.ext.opensearch.model.IntegerElement;
import org.apache.abdera.ext.opensearch.model.Query;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class ContentTypeSearcherResource extends AbstractResource {

  private final transient Logger logger = LoggerFactory.getLogger(getClass());
  private List<String> contentTypeId;
  private String parentId;
  private String workspaceId;
  private String searchTerms;
  private String creationDate;
  private String lastModifiedDate;
  private int start;
  private int count;
  private boolean disjunction;
  private Boolean includeFriendlies = null;
  private final static String WORKSPACE_ID = "workspaceId", TYPE_ID = "instanceOf", CREATION_DATE = "creationDate",
      LAST_MODIFIED_DATE = "lastModifiedDate", START = "start", COUNT = "count", DISJUNCTION = "disjunction",
      SEARCH_TERMS = "q", INCLUDE_FRIENDLIES = "includeFriendlies", CHILD_OF = "parentId";

  public List<String> getContentTypeId() {
    return contentTypeId;
  }

  public void setContentTypeId(List<String> contentTypeId) {
    this.contentTypeId = contentTypeId;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public String getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(String creationDate) {
    this.creationDate = creationDate;
  }

  public boolean isDisjunction() {
    return disjunction;
  }

  public void setDisjunction(boolean disjunction) {
    this.disjunction = disjunction;
  }

  public String getLastModifiedDate() {
    return lastModifiedDate;
  }

  public void setLastModifiedDate(String lastModifiedDate) {
    this.lastModifiedDate = lastModifiedDate;
  }

  public String getParentId() {
    return parentId;
  }

  public void setParentId(String parentId) {
    this.parentId = parentId;
  }

  public String getSearchTerms() {
    return searchTerms;
  }

  public void setSearchTerms(String searchTerms) {
    this.searchTerms = searchTerms;
  }

  public int getStart() {
    return start;
  }

  public void setStart(int start) {
    this.start = start;
  }

  public String getWorkspaceId() {
    return workspaceId;
  }

  public void setWorkspaceId(String workspaceId) {
    this.workspaceId = workspaceId;
  }

  public Boolean getIncludeFriendlies() {
    return includeFriendlies;
  }

  public void setIncludeFriendlies(Boolean includeFriendlies) {
    this.includeFriendlies = includeFriendlies;
  }

  protected void initParams(List<String> contentTypeId, String searchTerms, String workspaceId, String creationDate,
                            String lastModifiedDate, int start, int count, boolean disJunction,
                            boolean includeFriendlies, String parentId) {
    if (contentTypeId != null && !contentTypeId.isEmpty()) {
      this.contentTypeId = contentTypeId;
    }
    else if (this.contentTypeId == null) {
      this.contentTypeId = Collections.emptyList();
    }
    if (StringUtils.isNotBlank(parentId)) {
      this.parentId = parentId;
    }
    if (StringUtils.isNotBlank(workspaceId)) {
      this.workspaceId = workspaceId;
    }
    this.creationDate = creationDate;
    this.lastModifiedDate = lastModifiedDate;
    this.start = start;
    this.count = count;
    this.disjunction = disJunction;
    this.searchTerms = searchTerms;
    if (this.includeFriendlies == null) {
      this.includeFriendlies = includeFriendlies;
    }
  }

  protected URI getNextPage() {
    return getPage(1);
  }

  protected URI getPreviousPage() {
    if (start - count < 0) {
      return null;
    }
    return getPage(-1);
  }

  protected URI getPage(int offset) {
    return UriBuilder.fromUri(getUriInfo().getRequestUri()).replaceQueryParam(START, start + offset * count).build();
  }

  private Filter getFilter() {
    Filter filter = SmartContentAPI.getInstance().getContentTypeLoader().createFilter();
    if (StringUtils.isNotBlank(searchTerms)) {
      filter.setSearchTerms(searchTerms);
    }
    if (contentTypeId != null && !contentTypeId.isEmpty()) {
      filter.addInstanceOfContentTypeToFilter(ContentSearcherResource.parseCollectionContentTypeId(contentTypeId));
    }
    if (StringUtils.isNotBlank(parentId)) {
      filter.setChildOf(ContentSearcherResource.parseContentTypeId(parentId));
    }
    if (StringUtils.isNotBlank(workspaceId)) {
      filter.setWorkspaceId(ContentSearcherResource.parseWorkspaceId(workspaceId));
    }

    filter.setStartFrom(start);
    filter.setMaxContents(count);

    filter.setDisjunction(disjunction);
    if (includeFriendlies != null) {
      filter.setFriendliesIncluded(includeFriendlies);
    }
    else {
      logger.info(String.valueOf(":::VAULE OF Inclue Friendlies is true"));
      filter.setFriendliesIncluded(true);
    }
    if (creationDate != null) {
      filter.setCreationDateFilter(ContentSearcherResource.formatDate(creationDate));
    }
    if (lastModifiedDate != null) {
      filter.setLastModifiedDateFilter(ContentSearcherResource.formatDate(lastModifiedDate));
    }

    if (logger.isInfoEnabled()) {
      logger.info(":::Workspace ID : " + workspaceId + " " + filter.getWorkspaceId());
      logger.info(":::Parent ID : " + parentId + " " + filter.getChildOf());
      logger.info(":::START FROM : " + String.valueOf(start));
      logger.info(":::NUMBER OF ITEM : " + String.valueOf(count));
      logger.info(String.valueOf(":::VAULE OF DISJUNCTION : " + disjunction));
      logger.info(":::CREATION DATE : " + creationDate + " " + filter.getCreationDateFilter());
      logger.info(":::LAST MODIFIED DATE : " + lastModifiedDate + " " + filter.getLastModifiedDateFilter());
      logger.info(String.valueOf(":::VAULE OF Inclue Friendlies : " + includeFriendlies));
      logger.info("Instance of " + contentTypeId + " " + filter.getInstanceOfContentTypeFilters());
    }
    return filter;
  }

  protected StringBuilder getSearchUri(boolean withTemplate) {
    StringBuilder templateBuilder = new StringBuilder(getUriInfo().getRequestUri().toASCIIString());
    if (withTemplate) {
      templateBuilder.append('?').append(SEARCH_TERMS).append("=").
          append(StringUtils.isBlank(searchTerms) ? "{searchTerms}" : searchTerms);
      templateBuilder.append('&').append(START).append("=").append(start <= 0 ? "{startIndex?}" : start);
      templateBuilder.append('&').append(COUNT).append("=").append(count <= 0 ? "{count?}" : count);
      templateBuilder.append('&').append(WORKSPACE_ID).append("=").
          append(StringUtils.isBlank(workspaceId) ? "{workspaceId?}" : workspaceId);
      templateBuilder.append('&').append(CHILD_OF).append("=").
          append(StringUtils.isBlank(parentId) ? "{parentId?}" : parentId);
      templateBuilder.append('&').append(CREATION_DATE).append("=").
          append(StringUtils.isBlank(creationDate) ? "{creationModifiedDateSpec?}" : creationDate);
      templateBuilder.append('&').append(LAST_MODIFIED_DATE).append("=").
          append(StringUtils.isBlank(lastModifiedDate) ? "{lastModifiedDateSpec?}" : lastModifiedDate);
      templateBuilder.append('&').append(INCLUDE_FRIENDLIES).append("=").
          append(includeFriendlies == null ? "{includeFriendlies?}" : includeFriendlies.booleanValue());
      if (contentTypeId != null && !contentTypeId.isEmpty()) {
        for (String typeId : contentTypeId) {
          templateBuilder.append('&').append(TYPE_ID).append("=").append(typeId);
        }
      }
      else {
        templateBuilder.append('&').append(TYPE_ID).append("=").append("{contentTypeId?}");
      }
      templateBuilder.append('&').append(DISJUNCTION).append("=").append("{disjunction?}");
    }
    else {
      templateBuilder.append('?');
      if (StringUtils.isNotBlank(searchTerms)) {
        templateBuilder.append(SEARCH_TERMS).append("=").append(searchTerms).append('&');
      }
      if (StringUtils.isNotBlank(workspaceId)) {
        templateBuilder.append(WORKSPACE_ID).append("=").append(workspaceId).append('&');
      }
      if (StringUtils.isNotBlank(creationDate)) {
        templateBuilder.append(CREATION_DATE).append("=").append(creationDate).append('&');
      }
      if (StringUtils.isNotBlank(parentId)) {
        templateBuilder.append(CHILD_OF).append("=").append(parentId).append('&');
      }
      if (StringUtils.isNotBlank(lastModifiedDate)) {
        templateBuilder.append(LAST_MODIFIED_DATE).append("=").append(lastModifiedDate).append('&');
      }
      if (includeFriendlies != null) {
        templateBuilder.append(INCLUDE_FRIENDLIES).append("=").append(includeFriendlies.booleanValue()).append('&');
      }
      if (start >= 0) {
        templateBuilder.append(START).append("=").append(start).append('&');
      }
      if (count >= 0) {
        templateBuilder.append(COUNT).append("=").append(count).append('&');
      }
      if (disjunction) {
        templateBuilder.append(DISJUNCTION).append("=").append(disjunction).append('&');
      }
      if (contentTypeId != null && !contentTypeId.isEmpty()) {
        for (String typeId : contentTypeId) {
          templateBuilder.append(TYPE_ID).append("=").append(typeId).append('&');
        }
      }
    }
    return templateBuilder;
  }

  @Override
  protected String getAuthor() {
    return "Smart CMS";
  }

  @GET
  @Produces(com.smartitengineering.util.opensearch.jaxrs.MediaType.APPLICATION_OPENSEARCHDESCRIPTION_XML)
  public Response getSpec() {
    OpenSearchDescriptorBuilder descBuilder = OpenSearchDescriptorBuilder.getBuilder();
    descBuilder.shortName("Content Search");
    descBuilder.description("Search the content repository for contents!");
    StringBuilder templateBuilder = getSearchUri(true);
    final String urlTemplate = templateBuilder.toString();
    if (logger.isInfoEnabled()) {
      logger.info("Template URL: " + urlTemplate);
    }
    UrlBuilder xmlBuilder = UrlBuilder.getBuilder().rel(RelEnum.RESULTS).indexOffset(start).template(urlTemplate).type(
        MediaType.APPLICATION_ATOM_XML);
    descBuilder.urls(xmlBuilder.build());
    ResponseBuilder builder = Response.ok(descBuilder.build());
    return builder.build();
  }

  @GET
  @Produces(MediaType.APPLICATION_ATOM_XML)
  public Response getResultFeed(@QueryParam(TYPE_ID) List<String> contentTypeId,
                                @QueryParam(SEARCH_TERMS) String searchTerms,
                                @QueryParam(WORKSPACE_ID) String workspaceId,
                                @QueryParam(CHILD_OF) String parentId,
                                @QueryParam(CREATION_DATE) String creationDate,
                                @QueryParam(LAST_MODIFIED_DATE) String lastModifiedDate,
                                @QueryParam(INCLUDE_FRIENDLIES) @DefaultValue("true") boolean includeFriendlies,
                                @QueryParam(START) int start,
                                @QueryParam(COUNT) @DefaultValue("5") int count,
                                @QueryParam(DISJUNCTION) boolean disJunction) {
    initParams(contentTypeId, searchTerms, workspaceId, creationDate, lastModifiedDate, start,
               count, disJunction, includeFriendlies, parentId);
    ResponseBuilder responseBuilder;
    Filter filter = getFilter();
    final com.smartitengineering.cms.api.common.SearchResult result = SmartContentAPI.getInstance().getContentTypeLoader().
        search(filter);
    final Collection<ContentType> searchContent = result.getResult();
    Feed feed = getFeed("search", "Content Search Result", new Date());
    feed.addLink(getLink(getUriInfo().getRequestUri().toASCIIString(), Link.REL_ALTERNATE, MediaType.APPLICATION_JSON));
    feed.addLink(getLink(new StringBuilder(getUriInfo().getBaseUri().toASCIIString()).append(getUriInfo().getPath()).
        toString(), "search",
                         com.smartitengineering.util.opensearch.jaxrs.MediaType.APPLICATION_OPENSEARCHDESCRIPTION_XML));
    Query query = feed.<Query>addExtension(OpenSearchConstants.QUERY);
    query.setRole(Query.Role.REQUEST);
    query.setCount(count);
    query.setStartIndex(start);
    query.setSearchTerms(searchTerms);
    IntegerElement countElem = feed.<IntegerElement>addExtension(OpenSearchConstants.ITEMS_PER_PAGE);
    countElem.setValue(count);
    IntegerElement startIndexElem = feed.<IntegerElement>addExtension(OpenSearchConstants.START_INDEX);
    startIndexElem.setValue(start);
    IntegerElement totalResultsElem = feed.<IntegerElement>addExtension(OpenSearchConstants.TOTAL_RESULTS);
    totalResultsElem.setValue(Long.valueOf(result.getTotalResultsCount()).intValue());
    if (searchContent != null && !searchContent.isEmpty()) {
      feed.addLink(getLink(getNextPage().toASCIIString(), Link.REL_NEXT, MediaType.APPLICATION_ATOM_XML));
      if (getPreviousPage() != null) {
        feed.addLink(getLink(getPreviousPage().toASCIIString(), Link.REL_PREVIOUS, MediaType.APPLICATION_ATOM_XML));
      }
      for (ContentType type : searchContent) {
        final URI contentUri = ContentTypeResource.getContentTypeRelativeURI(getUriInfo(), type.getContentTypeID());
        Entry entry = getEntry(type.getContentTypeID().toString(), new StringBuilder("ContentType ").append(type.
            getContentTypeID().toString()).toString(), type.getLastModifiedDate(),
                               getLink(contentUri, Link.REL_ALTERNATE, MediaType.APPLICATION_ATOM_XML), getLink(
            contentUri, Link.REL_ALTERNATE, MediaType.APPLICATION_XML));
        feed.addEntry(entry);
      }
    }
    responseBuilder = Response.ok(feed);
    return responseBuilder.build();
  }
}
