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

import com.smartitengineering.cms.api.workspace.Workspace;
import com.smartitengineering.cms.ws.common.domains.ResourceTemplate;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author imyousuf
 */
public class WorkspaceRepresentationResource {

  private final String repName;
  private final Workspace workspace;
  private final UriInfo uriInfo;

  public WorkspaceRepresentationResource(String repName, Workspace workspace, UriInfo uriInfo) {
    this.repName = repName;
    this.workspace = workspace;
    this.uriInfo = uriInfo;
  }
}
