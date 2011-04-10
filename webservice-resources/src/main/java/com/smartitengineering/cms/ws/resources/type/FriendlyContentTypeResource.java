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
import com.smartitengineering.cms.api.factory.workspace.WorkspaceAPI;
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.api.workspace.Workspace;
import com.smartitengineering.cms.ws.resources.content.searcher.ContentSearcherResource;
import com.smartitengineering.cms.ws.resources.workspace.WorkspaceResource;
import com.smartitengineering.util.rest.atom.server.AbstractResource;
import java.net.URI;
import java.util.Collections;
import java.util.Date;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.abdera.model.Feed;

/**
 *
 * @author imyousuf
 */
public class FriendlyContentTypeResource extends AbstractResource {

  public static final String SEARCH = "search";
  @PathParam(WorkspaceResource.PARAM_NAMESPACE)
  private String ownerWorkspaceNS;
  @PathParam(WorkspaceResource.PARAM_NAME)
  private String ownerWorkspaceName;
  @PathParam(ContentTypesResource.PARAM_FRIENDLY_WORKSPACE_NS)
  private String friendlyWorkspaceNS;
  @PathParam(ContentTypesResource.PARAM_FRIENDLY_WORKSPACE_NAME)
  private String friendlyWorkspaceName;
  @PathParam(ContentTypesResource.PARAM_FRIENDLY_CONTENT_TYPE_NS)
  private String friendlyContentTypeNS;
  @PathParam(ContentTypesResource.PARAM_FRIENDLY_CONTENT_TYPE_NAME)
  private String friendlyContentTypeName;
  private Workspace ownerWorkspace;

  @GET
  @Produces(MediaType.APPLICATION_ATOM_XML)
  public Response get() {
    ContentType contentType = validate();
    ContentTypeId friendlyContentTypeId = contentType.getContentTypeID();
    final Date lastChangeDate = contentType.getLastModifiedDate();
    Feed feed = getFeed(new StringBuilder("content-type.").append(friendlyContentTypeId).toString(),
                        "Friendly Content Type", lastChangeDate);
    URI uri = getAbsoluteURIBuilder().path(ContentTypesResource.class).path(ContentTypesResource.PATH_TO_CONTENT_TYPE).
        build(friendlyWorkspaceNS, friendlyWorkspaceName, friendlyContentTypeNS, friendlyContentTypeName);
    feed.addLink(getLink(uri, ContentTypesResource.REL_CONTENT_TYPE, MediaType.APPLICATION_XML));
    feed.addLink(getLink(uri, ContentTypesResource.REL_CONTENT_TYPE_FEED, MediaType.APPLICATION_ATOM_XML));
    URI searchUri = getAbsoluteURIBuilder().path(ContentTypesResource.class).path(
        ContentTypesResource.PATH_TO_FRIENDLY_CONTENT_TYPE).path(SEARCH).build(ownerWorkspaceNS, ownerWorkspaceName,
                                                                               friendlyWorkspaceNS,
                                                                               friendlyWorkspaceName,
                                                                               friendlyContentTypeNS,
                                                                               friendlyContentTypeName);
    feed.addLink(getLink(searchUri, SEARCH,
                         com.smartitengineering.util.opensearch.jaxrs.MediaType.APPLICATION_OPENSEARCHDESCRIPTION_XML));
    return Response.ok(feed).build();
  }

  protected ContentType validate() throws WebApplicationException {
    final WorkspaceAPI workspaceApi =
                       SmartContentAPI.getInstance().getWorkspaceApi();
    Workspace workspace =
              workspaceApi.getWorkspace(workspaceApi.createWorkspaceId(ownerWorkspaceNS, ownerWorkspaceName));
    if (workspace == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    this.ownerWorkspace = workspace;
    Workspace friendlyWorkspace =
              workspaceApi.getWorkspace(workspaceApi.createWorkspaceId(friendlyWorkspaceNS, friendlyWorkspaceName));
    if (friendlyWorkspace == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    if (!workspace.getFriendlies().contains(friendlyWorkspace.getId())) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    final ContentType contentType =
                      SmartContentAPI.getInstance().getContentTypeLoader().
        createContentTypeId(friendlyWorkspace.getId(), friendlyContentTypeNS, friendlyContentTypeName).getContentType();
    if (contentType == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    return contentType;
  }

  @Path(SEARCH)
  public ContentSearcherResource search() {
    ContentType contentType = validate();
    final ContentSearcherResource contentSearcherResource = new ContentSearcherResource(getInjectables());
    contentSearcherResource.setWorkspaceId(ownerWorkspace.getId().toString());
    contentSearcherResource.setContentTypeId(Collections.singletonList(contentType.getContentTypeID().toString()));
    return contentSearcherResource;
  }

  @Override
  protected String getAuthor() {
    return "Smart CMS";
  }
}
