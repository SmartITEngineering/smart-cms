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
package com.smartitengineering.cms.ws.resources.content;

import com.smartitengineering.cms.api.content.Content;
import com.smartitengineering.cms.api.content.ContentId;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.workspace.Workspace;
import com.smartitengineering.cms.ws.common.providers.TextURIListProvider;
import com.smartitengineering.util.rest.atom.server.AbstractResource;
import com.smartitengineering.util.rest.server.ServerResourceInjectables;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class WorkspaceContentContainerResource extends AbstractResource {

  private final Workspace workspace;
  private static final String CONTENT_URI = "contentUri";
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  public WorkspaceContentContainerResource(ServerResourceInjectables injectables, Workspace workspace) {
    super(injectables);
    this.workspace = workspace;
    if (workspace == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
  }

  @GET
  @Produces(MediaType.APPLICATION_ATOM_XML)
  public Response getFeedMedia() {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setMaxAge(600);
    ResponseBuilder builder = Response.ok().cacheControl(cacheControl);
    Feed feed = getFeed(new StringBuilder("container-").append(workspace.getId()).toString(), "Root contents",
                        new Date());
    Collection<ContentId> contents = workspace.getRootContents();
    for (ContentId contentId : contents) {
      Content content = SmartContentAPI.getInstance().getContentLoader().loadContent(contentId);
      if (content != null) {
        feed.addEntry(getEntry(contentId.toString(), contentId.toString(), content.getLastModifiedDate(),
                               getLink(ContentResource.getContentUri(contentId), Link.REL_ALTERNATE,
                                       MediaType.APPLICATION_JSON)));
      }
    }
    feed.addLink(getLink(getUriInfo().getRequestUri(), Link.REL_ALTERNATE, TextURIListProvider.TEXT_URI_LIST));
    builder.entity(feed);
    return builder.build();
  }

  @GET
  @Produces(TextURIListProvider.TEXT_URI_LIST)
  public Response getUriList() {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setMaxAge(600);
    ResponseBuilder builder = Response.ok().cacheControl(cacheControl);
    Collection<ContentId> contents = workspace.getRootContents();
    List<URI> uris = new ArrayList<URI>(contents.size());
    for (ContentId contentId : contents) {
      uris.add(ContentResource.getContentUri(contentId));
    }
    builder.entity(uris);
    return builder.build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response addContainerContent(final @FormParam(CONTENT_URI) String contentUri) {
    return writeRootContents(contentUri, true);
  }

  @PUT
  @Consumes(TextURIListProvider.TEXT_URI_LIST)
  public Response replaceContainerContents(final Collection<URI> contentUris) {
    SmartContentAPI.getInstance().getWorkspaceApi().removeAllRootContents(workspace.getId());
    if (contentUris != null && contentUris.size() > 0) {
      List<ContentId> ids = new ArrayList<ContentId>(contentUris.size());
      for (URI uri : contentUris) {
        if (logger.isDebugEnabled()) {
          logger.debug("URI to parse " + uri.toASCIIString());
        }
        ContentId id = getResourceContext().matchResource(uri, ContentResource.class).getContent().getContentId();
        if (id == null) {
          return Response.status(Response.Status.BAD_REQUEST).entity("Some URIs could not be resolved internally!").
              build();
        }
        ids.add(id);
      }
      SmartContentAPI.getInstance().getWorkspaceApi().addRootContent(workspace.getId(), ids.toArray(
          new ContentId[ids.size()]));
    }
    ResponseBuilder builder = Response.status(Response.Status.OK);
    builder.location(getUriInfo().getAbsolutePath());
    return builder.build();
  }

  @DELETE
  public Response removeContainerContent(final @QueryParam(CONTENT_URI) String contentUri) {
    return writeRootContents(contentUri, false);
  }

  @Override
  protected String getAuthor() {
    return "Smart CMS";
  }

  protected Response writeRootContents(final String uri, final boolean add) {
    final ResponseBuilder builder;
    boolean error = false;
    String entity = "";
    if (StringUtils.isBlank(uri) && add) {
      error = true;
      entity = "URI is blank";
    }
    else if (StringUtils.isBlank(uri) && !add) {
      SmartContentAPI.getInstance().getWorkspaceApi().removeAllRootContents(workspace.getId());
    }
    else {
      ContentId contentId = null;
      try {
        if (logger.isDebugEnabled()) {
          logger.debug("Trying to add " + uri);
        }
        ContentResource resource = getResourceContext().matchResource(new URI(uri), ContentResource.class);
        contentId = resource.getContent().getContentId();
      }
      catch (Exception ex) {
        logger.warn(ex.getMessage(), ex);
        error = true;
        entity = ex.getMessage() + ". ";
      }
      if (contentId == null) {
        error = true;
        entity = entity + " Content ID Null.";
      }
      else {
        if (add) {
          SmartContentAPI.getInstance().getWorkspaceApi().addRootContent(workspace.getId(), contentId);
        }
        else {
          SmartContentAPI.getInstance().getWorkspaceApi().removeRootContent(workspace.getId(), contentId);
        }
      }
    }
    if (error) {
      builder = Response.status(Response.Status.BAD_REQUEST).entity(entity);
    }
    else {
      builder = Response.status(Response.Status.ACCEPTED);
      builder.location(getUriInfo().getAbsolutePath());
    }
    return builder.build();
  }
}
