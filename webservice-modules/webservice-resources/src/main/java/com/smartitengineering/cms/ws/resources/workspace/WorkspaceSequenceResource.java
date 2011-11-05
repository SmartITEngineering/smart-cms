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
package com.smartitengineering.cms.ws.resources.workspace;

import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.workspace.Sequence;
import com.smartitengineering.cms.api.workspace.Workspace;
import com.smartitengineering.util.rest.atom.server.AbstractResource;
import com.smartitengineering.util.rest.server.ServerResourceInjectables;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author imyousuf
 */
public class WorkspaceSequenceResource extends AbstractResource {

  private final Sequence sequence;

  public WorkspaceSequenceResource(Workspace workspace, String sequenceName, ServerResourceInjectables injectables) {
    super(injectables);
    this.sequence = SmartContentAPI.getInstance().getWorkspaceApi().getSequence(workspace.getId(), sequenceName);
    if (this.sequence == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
  }

  @Override
  protected String getAuthor() {
    return "Smart CMS";
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getSequence() {
    return Response.ok(Collections.singletonMap("value", sequence.getCurrentValue())).build();
  }

  @DELETE
  public Response delete() {
    try {
      SmartContentAPI.getInstance().getWorkspaceApi().deleteSequence(sequence.getWorkspace(), sequence.getName());
      return Response.status(Response.Status.ACCEPTED).build();
    }
    catch (Exception ex) {
      return Response.serverError().entity(ex).build();
    }
  }

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response changeSequence(@DefaultValue("1") @FormParam("delta") long delta) {
    long newVal = SmartContentAPI.getInstance().getWorkspaceApi().modifySequenceValue(sequence, delta);
    Map<String, Long> currentValue = new HashMap<String, Long>();
    currentValue.put("value", sequence.getCurrentValue());
    return Response.status(Response.Status.OK).entity(Collections.singletonMap("value", newVal)).build();
  }
}
