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
import com.smartitengineering.cms.ws.resources.domains.Factory;
import java.util.Date;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

/**
 *
 * @author imyousuf
 */
@Path("/ws/{" + WorkspaceResource.PARAM_NAMESPACE + "}/{" + WorkspaceResource.PARAM_NAME + "}")
public class WorkspaceResource {

  public static final int MAX_AGE = 1 * 60 * 60;
  public static final String PARAM_NAMESPACE = "ns";
  public static final String PARAM_NAME = "wsName";
  public static final String REL_WORKSPACE_CONTENT = "workspaceContent";
  @PathParam(PARAM_NAMESPACE)
  private String namespace;
  @PathParam(PARAM_NAME)
  private String workspaceName;
  @HeaderParam(HttpHeaders.IF_MODIFIED_SINCE)
  private Date ifModifiedSince;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getWorkspace() {
    final WorkspaceAPI workspaceApi = SmartContentAPI.getInstance().getWorkspaceApi();
    final Workspace workspace = workspaceApi.getWorkspace(workspaceApi.createWorkspaceId(namespace, workspaceName));
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
}
