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

import com.smartitengineering.cms.api.SmartContentAPI;
import com.smartitengineering.cms.api.workspace.Workspace;
import com.smartitengineering.cms.api.workspace.WorkspaceAPI;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.cms.ws.common.providers.TextURIListProvider;
import com.smartitengineering.cms.ws.resources.domains.Factory;
import com.smartitengineering.util.rest.atom.server.AbstractResource;
import java.net.URI;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author imyousuf
 */
@Path("/ws/{" + WorkspaceResource.PARAM_NAMESPACE + "}/{" + WorkspaceResource.PARAM_NAME + "}")
public class WorkspaceResource extends AbstractResource {

  public static final int MAX_AGE = 1 * 60 * 60;
  public static final String PARAM_NAMESPACE = "ns";
  public static final String PARAM_NAME = "wsName";
  public static final String PATH_FRIENDLIES = "friendlies";
  public static final String PATH_REPRESENTATIONS = "representations";
  public static final String PATH_VARIATIONS = "variations";
  public static final String REL_FRIENDLIES = "friendlies";
  public static final String REL_REPRESENTATIONS = "representations";
  public static final String REL_VARIATIONS = "variations";
  public static final String REL_WORKSPACE_CONTENT = "workspaceContent";
  public static final Pattern PATTERN = Pattern.compile("(/)?ws/([\\w\\._-]+)/(\\w+)");
  private final String namespace;
  private final String workspaceName;
  private final Workspace workspace;
  @HeaderParam(HttpHeaders.IF_MODIFIED_SINCE)
  private Date ifModifiedSince;

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
      return builder.build();
    }
    else {
      return Response.status(Response.Status.NOT_MODIFIED).build();
    }
  }

  @GET
  @Produces(MediaType.APPLICATION_ATOM_XML)
  public Response getWorkspace() {
    final Date creationDate = workspace.getCreationDate();
    if (ifModifiedSince == null || ifModifiedSince.before(creationDate)) {
      Feed feed = getFeed(workspace.getId().toString(), workspaceName, creationDate);
      feed.addLink(getLink(
          getAbsoluteURIBuilder().path(WorkspaceResource.class).path(PATH_FRIENDLIES).build(namespace, workspaceName),
          REL_FRIENDLIES, TextURIListProvider.TEXT_URI_LIST));
      feed.addLink(getLink(getUriInfo().getRequestUri(), Link.REL_ALTERNATE, MediaType.APPLICATION_JSON));
      ResponseBuilder builder = Response.ok(feed);
      builder.lastModified(creationDate);
      CacheControl control = new CacheControl();
      control.setMaxAge(MAX_AGE);
      builder.cacheControl(control);
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

  @Path(PATH_VARIATIONS)
  public WorkspaceVariationsResource getVariationsResource(@QueryParam("count") @DefaultValue("10") int count) {
    return new WorkspaceVariationsResource(workspace, count, getInjectables());
  }

  @Path(PATH_REPRESENTATIONS + "/name/{name}")
  public WorkspaceRepresentationResource getRepresentationsResource(@PathParam("name") String name) {
    return new WorkspaceRepresentationResource(name, workspace, getInjectables());
  }

  @Path(PATH_VARIATIONS + "/name/{name}")
  public WorkspaceVariationResource getVariationResource(@PathParam("name") String name) {
    return new WorkspaceVariationResource(name, workspace, getInjectables());
  }

  public static URI getWorkspaceURI(UriBuilder builder, String namespace, String name) {
    if (builder != null) {
      builder.path(WorkspaceResource.class);
      return builder.build(namespace, name);
    }
    return null;
  }

  public static WorkspaceId parseWorkspaceId(UriInfo uriInfo, URI uri) {
    String path = uri.getPath();
    String basePath = uriInfo.getBaseUri().getPath();
    String fullPath = uriInfo.getBaseUri().toASCIIString();
    if (StringUtils.isBlank(path)) {
      return null;
    }
    final String pathToWorkspace;
    if (path.startsWith(fullPath)) {
      pathToWorkspace = path.substring(fullPath.length());
    }
    else if (path.startsWith(basePath)) {
      pathToWorkspace = path.substring(basePath.length());
    }
    else {
      pathToWorkspace = path;
    }
    Matcher matcher = PATTERN.matcher(pathToWorkspace);
    if (matcher.matches()) {
      final WorkspaceAPI workspaceApi = SmartContentAPI.getInstance().getWorkspaceApi();
      final String namespace = matcher.group(2);
      final String name = matcher.group(3);
      return workspaceApi.getWorkspaceIdIfExists(workspaceApi.createWorkspaceId(namespace, name));
    }
    else {
      return null;
    }
  }

  @Override
  protected String getAuthor() {
    return "Smart CMS";
  }
}
