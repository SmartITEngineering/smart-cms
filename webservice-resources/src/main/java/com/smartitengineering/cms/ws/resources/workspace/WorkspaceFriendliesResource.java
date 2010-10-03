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
import com.smartitengineering.cms.ws.providers.TextURIListProvider;
import com.smartitengineering.util.rest.server.AbstractResource;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author imyousuf
 */
public class WorkspaceFriendliesResource extends AbstractResource {

  private final Workspace workspace;

  public WorkspaceFriendliesResource(String namespace, String name) {
    final WorkspaceAPI workspaceApi = SmartContentAPI.getInstance().getWorkspaceApi();
    workspace = workspaceApi.getWorkspace(workspaceApi.createWorkspaceId(namespace, name));
  }

  @GET
  @Produces(TextURIListProvider.TEXT_URI_LIST)
  public Response getFriendlies() {
    final ResponseBuilder builder;
    if (workspace == null) {
      builder = Response.status(Response.Status.NOT_FOUND);
    }
    else {
      final Collection<WorkspaceId> friendlies = workspace.getFriendlies();
      final Collection<URI> uris = new ArrayList<URI>(friendlies.size());
      for (WorkspaceId id : friendlies) {
        uris.add(WorkspaceResource.getWorkspaceURI(getAbsoluteURIBuilder(), id.getGlobalNamespace(), id.getName()));
      }
      builder = Response.ok(uris);
      CacheControl control = new CacheControl();
      control.setMaxAge(300);
      builder.cacheControl(control);
    }
    return builder.build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response addWorkspaceAsFriendly(@FormParam("workspaceUri") final String uri) {
    return writeFriends(uri, true);
  }

  @DELETE
  public Response deleteWorkspaceAsFriendly(@QueryParam("workspaceUri") final String uri) {
    return writeFriends(uri, false);
  }

  protected Response writeFriends(final String uri, final boolean add) {
    final ResponseBuilder builder;
    boolean error = false;
    String entity = "";
    if (StringUtils.isBlank(uri) && add) {
      error = true;
      entity = "URI is blank";
    }
    else if (StringUtils.isBlank(uri) && !add) {
      SmartContentAPI.getInstance().getWorkspaceApi().removeAllFriendlies(workspace.getId());
    }
    else {
      WorkspaceId workspaceId = null;
      try {
        workspaceId = WorkspaceResource.parseWorkspaceId(getUriInfo(), new URI(uri));
      }
      catch (URISyntaxException ex) {
        error = true;
        entity = ex.getMessage() + ". ";
      }
      if (workspaceId == null) {
        error = true;
        entity = entity + " Workspace ID Null.";
      }
      else {
        if (add) {
          SmartContentAPI.getInstance().getWorkspaceApi().addFriend(workspace.getId(), workspaceId);
        }
        else {
          SmartContentAPI.getInstance().getWorkspaceApi().removeFriend(workspace.getId(), workspaceId);
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
