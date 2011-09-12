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
package com.smartitengineering.cms.ws.resources.workspace;

import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.workspace.Workspace;
import com.smartitengineering.cms.api.factory.workspace.WorkspaceAPI;
import com.smartitengineering.cms.ws.common.providers.TextURIListProvider;
import com.smartitengineering.cms.ws.common.utils.SimpleFeedExtensions;
import com.smartitengineering.cms.ws.common.utils.Utils;
import com.smartitengineering.cms.ws.resources.content.ContentsResource;
import com.smartitengineering.cms.ws.resources.content.searcher.ContentSearcherResource;
import com.smartitengineering.cms.ws.resources.domains.Factory;
import com.smartitengineering.cms.ws.resources.type.ContentTypesResource;
import com.smartitengineering.util.rest.atom.server.AbstractResource;
import java.net.URI;
import java.util.Date;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriBuilder;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
@Path("/w/{" + WorkspaceResource.PARAM_NAMESPACE + "}/{" + WorkspaceResource.PARAM_NAME + "}")
public class WorkspaceResource extends AbstractResource {

  public static final int MAX_AGE = 1 * 60 * 60;
  public static final String PARAM_NAMESPACE = "ns";
  public static final String PARAM_NAME = "wsName";
  public static final String PATH_FRIENDLIES = "friendlies";
  public static final String PATH_REPRESENTATIONS = "representations";
  public static final String PATH_CONTENT_CO_PROCESSORS = "content-co-processors";
  public static final String PATH_VARIATIONS = "variations";
  public static final String PATH_VALIDATORS = "validators";
  public static final String PATH_SEARCH = "search";
  public static final String PATH_REINDEX = "reindex";
  public static final String REL_FRIENDLIES = "friendlies";
  public static final String REL_REPRESENTATIONS = "representations";
  public static final String REL_CONTENT_CO_PROCESSORS = "contentCoProcessors";
  public static final String REL_VARIATIONS = "variations";
  public static final String REL_VALIDATORS = "validators";
  public static final String REL_CONTENT_TYPES = "content-types";
  public static final String REL_CONTENTS = "contents";
  public static final String REL_SEARCH = "search";
  public static final String REL_REINDEX = "re-index-all";
  public static final String REL_REINDEX_CONTENTS = "re-index-contents";
  public static final String REL_REINDEX_TYPES = "re-index-types";
  public static final String REL_WORKSPACE_CONTENT = "workspaceContent";
  private final String namespace;
  private final String workspaceName;
  private final Workspace workspace;
  @HeaderParam(HttpHeaders.IF_MODIFIED_SINCE)
  private Date ifModifiedSince;
  @HeaderParam(HttpHeaders.IF_NONE_MATCH)
  private EntityTag entityTag;
  private final static transient Logger LOGGER = LoggerFactory.getLogger(WorkspaceResource.class);

  public WorkspaceResource(@PathParam(PARAM_NAMESPACE) String namespace, @PathParam(PARAM_NAME) String workspaceName) {
    this.namespace = namespace;
    this.workspaceName = workspaceName;
    final WorkspaceAPI workspaceApi = SmartContentAPI.getInstance().getWorkspaceApi();
    this.workspace = workspaceApi.getWorkspace(workspaceApi.createWorkspaceId(namespace, workspaceName));
    if (this.workspace == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getWorkspaceContent() {
    if (ifModifiedSince == null || ifModifiedSince.before(workspace.getCreationDate())) {
      ResponseBuilder builder = Response.ok(Factory.getWorkspace(workspace));
      builder.lastModified(workspace.getCreationDate());
      CacheControl control = new CacheControl();
      control.setMaxAge(MAX_AGE);
      builder.cacheControl(control);
      builder.header(HttpHeaders.VARY, HttpHeaders.ACCEPT);
      return builder.build();
    }
    else {
      return Response.status(Response.Status.NOT_MODIFIED).build();
    }
  }

  @Path(PATH_SEARCH)
  public ContentSearcherResource searchWithinWorkspace() {
    ContentSearcherResource resource = new ContentSearcherResource(getInjectables());
    resource.setWorkspaceId(workspace.getId().toString().replaceAll(":", ","));
    return resource;
  }

  @Path(PATH_REINDEX)
  public ReIndexResource reindexForWorkspace() {
    ReIndexResource resource = new ReIndexResource(getInjectables());
    resource.setWorkspaceId(workspace.getId());
    return resource;
  }

  @Path(PATH_REINDEX + "/" + ReIndexResource.CONTENTS)
  public ReIndexResource reindexForWorkspaceContents() {
    ReIndexResource resource = new ReIndexResource(getInjectables());
    resource.setWorkspaceId(workspace.getId());
    resource.setContentsOnly(true);
    return resource;
  }

  @Path(PATH_REINDEX + "/" + ReIndexResource.TYPES)
  public ReIndexResource reindexForWorkspaceTypes() {
    ReIndexResource resource = new ReIndexResource(getInjectables());
    resource.setWorkspaceId(workspace.getId());
    resource.setTypesOnly(true);
    return resource;
  }

  @GET
  @Produces(MediaType.APPLICATION_ATOM_XML)
  public Response getWorkspaceFeed() {
    final Date creationDate = workspace.getCreationDate();
    final EntityTag tag = new EntityTag(DigestUtils.md5Hex(Utils.getFormattedDate(creationDate)));
    if ((ifModifiedSince == null || ifModifiedSince.before(creationDate)) && (entityTag == null ||
                                                                              !entityTag.equals(tag))) {
      Feed feed = getFeed(workspace.getId().toString(), workspaceName, creationDate);
      feed.addLink(getLink(
          getAbsoluteURIBuilder().path(WorkspaceResource.class).path(PATH_FRIENDLIES).build(namespace, workspaceName),
          REL_FRIENDLIES, TextURIListProvider.TEXT_URI_LIST));
      feed.addLink(getLink(
          getAbsoluteURIBuilder().path(WorkspaceResource.class).path(PATH_REPRESENTATIONS).build(namespace,
                                                                                                 workspaceName),
          REL_REPRESENTATIONS, MediaType.APPLICATION_ATOM_XML));
      feed.addLink(getLink(
          getAbsoluteURIBuilder().path(WorkspaceResource.class).path(PATH_CONTENT_CO_PROCESSORS).build(namespace,
                                                                                                       workspaceName),
          REL_CONTENT_CO_PROCESSORS, MediaType.APPLICATION_ATOM_XML));
      feed.addLink(getLink(
          getAbsoluteURIBuilder().path(WorkspaceResource.class).path(PATH_VARIATIONS).build(namespace, workspaceName),
          REL_VARIATIONS, MediaType.APPLICATION_ATOM_XML));
      feed.addLink(getLink(
          getAbsoluteURIBuilder().path(WorkspaceResource.class).path(PATH_VALIDATORS).build(namespace, workspaceName),
          REL_VALIDATORS, MediaType.APPLICATION_ATOM_XML));
      feed.addLink(getLink(
          getAbsoluteURIBuilder().path(WorkspaceResource.class).path(PATH_SEARCH).build(namespace, workspaceName),
          REL_SEARCH, com.smartitengineering.util.opensearch.jaxrs.MediaType.APPLICATION_OPENSEARCHDESCRIPTION_XML));
      feed.addLink(getLink(
          getAbsoluteURIBuilder().path(WorkspaceResource.class).path(PATH_REINDEX).build(namespace, workspaceName),
          REL_REINDEX, MediaType.TEXT_PLAIN));
      feed.addLink(getLink(
          getAbsoluteURIBuilder().path(WorkspaceResource.class).path(PATH_REINDEX).path(ReIndexResource.CONTENTS).build(
          namespace, workspaceName), REL_REINDEX_CONTENTS, MediaType.TEXT_PLAIN));
      feed.addLink(getLink(
          getAbsoluteURIBuilder().path(WorkspaceResource.class).path(PATH_REINDEX).path(ReIndexResource.TYPES).build(
          namespace, workspaceName), REL_REINDEX_TYPES, MediaType.TEXT_PLAIN));
      feed.addLink(getLink(
          getAbsoluteURIBuilder().path(ContentTypesResource.class).build(namespace, workspaceName), REL_CONTENT_TYPES,
          MediaType.APPLICATION_ATOM_XML));
      feed.addLink(getLink(getAbsoluteURIBuilder().path(ContentsResource.class).build(namespace, workspaceName),
                           REL_CONTENTS, MediaType.APPLICATION_ATOM_XML));
      feed.addLink(getLink(getUriInfo().getRequestUri(), Link.REL_ALTERNATE, MediaType.APPLICATION_JSON));
      feed.addSimpleExtension(SimpleFeedExtensions.WORKSPACE_NAME_SPACE, namespace);
      feed.addSimpleExtension(SimpleFeedExtensions.WORKSPACE_NAME, workspaceName);
      ResponseBuilder builder = Response.ok(feed);
      builder.lastModified(creationDate);
      CacheControl control = new CacheControl();
      control.setMaxAge(MAX_AGE);
      builder.cacheControl(control);
      builder.tag(tag);
      builder.header(HttpHeaders.VARY, HttpHeaders.ACCEPT);
      return builder.build();
    }
    else {
      return Response.status(Response.Status.NOT_MODIFIED).build();
    }
  }

  @Path(PATH_FRIENDLIES)
  public WorkspaceFriendliesResource getFriendliesResource() {
    return new WorkspaceFriendliesResource(workspace, getInjectables());
  }

  @Path(PATH_REPRESENTATIONS)
  public WorkspaceRepresentationsResource getRepresentationsResource(@QueryParam("count") @DefaultValue("10") int count) {
    return new WorkspaceRepresentationsResource(workspace, count, getInjectables());
  }

  @Path(PATH_CONTENT_CO_PROCESSORS)
  public WorkspaceContentCoProcessorsResource getContentCoProcessorsResource(
      @QueryParam("count") @DefaultValue("10") int count) {
    return new WorkspaceContentCoProcessorsResource(workspace, count, getInjectables());
  }

  @Path(PATH_VARIATIONS)
  public WorkspaceVariationsResource getVariationsResource(@QueryParam("count") @DefaultValue("10") int count) {
    return new WorkspaceVariationsResource(workspace, count, getInjectables());
  }

  @Path(PATH_VALIDATORS)
  public WorkspaceValidatorsResource getValidatorsResource(@QueryParam("count") @DefaultValue("10") int count) {
    return new WorkspaceValidatorsResource(workspace, count, getInjectables());
  }

  @Path(PATH_REPRESENTATIONS + "/name/{name}")
  public WorkspaceRepresentationResource getRepresentationsResource(@PathParam("name") String name) {
    return new WorkspaceRepresentationResource(name, workspace, getInjectables());
  }

  @Path(PATH_CONTENT_CO_PROCESSORS + "/name/{name}")
  public WorkspaceContentCoProcessorResource getContentCoProcessorResource(@PathParam("name") String name) {
    return new WorkspaceContentCoProcessorResource(name, workspace, getInjectables());
  }

  @Path(PATH_VARIATIONS + "/name/{name}")
  public WorkspaceVariationResource getVariationResource(@PathParam("name") String name) {
    return new WorkspaceVariationResource(name, workspace, getInjectables());
  }

  @Path(PATH_VALIDATORS + "/name/{name}")
  public WorkspaceValidatorResource getValidatorResource(@PathParam("name") String name) {
    return new WorkspaceValidatorResource(name, workspace, getInjectables());
  }

  public static URI getWorkspaceURI(UriBuilder builder, String namespace, String name) {
    if (builder != null) {
      builder.path(WorkspaceResource.class);
      return builder.build(namespace, name);
    }
    return null;
  }

  public Workspace getWorkspace() {
    return workspace;
  }

  @Override
  protected String getAuthor() {
    return "Smart CMS";
  }
}
