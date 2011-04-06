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
package com.smartitengineering.cms.ws.resources;

import com.smartitengineering.cms.ws.resources.workspace.WorkspaceResource;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.workspace.Workspace;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.cms.ws.resources.content.ReIndexResource;
import com.smartitengineering.cms.ws.resources.content.searcher.ContentSearcherResource;
import com.smartitengineering.util.rest.atom.server.AbstractResource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;

/**
 *
 * @author imyousuf
 */
@Path("/")
public class RootResource extends AbstractResource {

  private static final Comparator<Workspace> WORKSPACE_COMPRATOR = new Comparator<Workspace>() {

    @Override
    public int compare(Workspace o1, Workspace o2) {
      return o1.getCreationDate().compareTo(o2.getCreationDate());
    }
  };
  public static final String PATH_TO_SEARCH = "search";
  public static final String PATH_TO_REINDEX = "reindex";
  public static final String PATH_TO_TEMPLATES = "templates";

  @GET
  @Produces(MediaType.APPLICATION_ATOM_XML)
  public Response get(@HeaderParam(HttpHeaders.IF_MODIFIED_SINCE) Date ifModifiedSince) {
    ArrayList<Workspace> workspaces = new ArrayList<Workspace>(SmartContentAPI.getInstance().getWorkspaceApi().
        getWorkspaces());
    if (workspaces.isEmpty()) {
      return Response.noContent().build();
    }
    Workspace maxWorkspace = Collections.max(workspaces, WORKSPACE_COMPRATOR);
    final ResponseBuilder response;
    final Date lastModifiedDate = maxWorkspace.getCreationDate();
    if (ifModifiedSince != null && !lastModifiedDate.after(ifModifiedSince)) {
      response = Response.status(Response.Status.NOT_MODIFIED);
    }
    else {
      response = Response.status(Response.Status.OK);
      Feed feed = getFeed("workspaces", "Workspaces", lastModifiedDate);
      Collections.sort(workspaces, WORKSPACE_COMPRATOR);
      Collections.reverse(workspaces);
      for (Workspace workspace : workspaces) {
        final WorkspaceId id = workspace.getId();
        Link link = getLink(
            getRelativeURIBuilder().path(WorkspaceResource.class).build(id.getGlobalNamespace(), id.getName()),
            WorkspaceResource.REL_WORKSPACE_CONTENT, MediaType.APPLICATION_JSON);
        Entry entry = getEntry(id.toString(), id.getName(), workspace.getCreationDate(), link);
        link = getLink(
            getRelativeURIBuilder().path(WorkspaceResource.class).build(id.getGlobalNamespace(), id.getName()),
            WorkspaceResource.REL_WORKSPACE_CONTENT, MediaType.APPLICATION_ATOM_XML);
        entry.addLink(link);
        feed.addEntry(entry);
      }
      feed.addLink(getLink(getRelativeURIBuilder().path(PATH_TO_SEARCH).build(), "search",
                           com.smartitengineering.util.opensearch.jaxrs.MediaType.APPLICATION_OPENSEARCHDESCRIPTION_XML));
      feed.addLink(getLink(getRelativeURIBuilder().path(PATH_TO_REINDEX).build(), "re-index",
                           MediaType.TEXT_PLAIN));
      feed.addLink(getLink(getRelativeURIBuilder().path(PATH_TO_TEMPLATES).build(), "templates",
                           com.smartitengineering.util.opensearch.jaxrs.MediaType.APPLICATION_OPENSEARCHDESCRIPTION_XML));
      response.entity(feed);
      response.lastModified(lastModifiedDate);
    }
    return response.build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response createWorkspace(@FormParam("namespace") final String namespace, @FormParam("name") final String name) {
    try {
      WorkspaceId id = SmartContentAPI.getInstance().getWorkspaceApi().createWorkspace(namespace, name);
      return Response.created(getAbsoluteURIBuilder().path(WorkspaceResource.class).build(id.getGlobalNamespace(), id.
          getName())).build();
    }
    catch (IllegalArgumentException exception) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
  }

  @Path("/" + PATH_TO_SEARCH)
  public ContentSearcherResource search() {
    return new ContentSearcherResource(getInjectables());
  }

  @Path("/" + PATH_TO_REINDEX)
  public ReIndexResource reIndex() {
    return new ReIndexResource(getInjectables());
  }

  @Path("/" + PATH_TO_TEMPLATES)
  public UriTemplatesResource templates() {
    return new UriTemplatesResource(getInjectables());
  }

  @Override
  protected String getAuthor() {
    return "Smart CMS";
  }
}
