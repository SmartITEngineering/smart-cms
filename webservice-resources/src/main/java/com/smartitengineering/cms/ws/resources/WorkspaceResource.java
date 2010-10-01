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

import com.smartitengineering.cms.api.SmartContentAPI;
import com.smartitengineering.cms.api.workspace.Workspace;
import com.smartitengineering.cms.api.workspace.WorkspaceAPI;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author imyousuf
 */
@Path("/ws/{" + WorkspaceResource.PARAM_NAMESPACE + "}/{" + WorkspaceResource.PARAM_NAME + "}")
public class WorkspaceResource {

  public static final String PARAM_NAMESPACE = "ns";
  public static final String PARAM_NAME = "wsName";
  public static final String REL_WORKSPACE_CONTENT = "workspaceContent";
  @PathParam(PARAM_NAMESPACE)
  private String namespace;
  @PathParam(PARAM_NAME)
  private String workspaceName;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Workspace getWorkspace() {
    final WorkspaceAPI workspaceApi = SmartContentAPI.getInstance().getWorkspaceApi();
    return workspaceApi.getWorkspace(workspaceApi.createWorkspaceId(namespace, workspaceName));
  }
}
