/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smartitengineering.cms.ws.resources.content;

import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.util.rest.server.AbstractResource;
import com.smartitengineering.util.rest.server.ServerResourceInjectables;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author imyousuf
 */
public class ReIndexResource extends AbstractResource {

  private WorkspaceId workspaceId;

  public ReIndexResource(ServerResourceInjectables injectables) {
    super(injectables);
  }

  public WorkspaceId getWorkspaceId() {
    return workspaceId;
  }

  public void setWorkspaceId(WorkspaceId workspaceId) {
    this.workspaceId = workspaceId;
  }

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String get() {
    return "POST to this resource to perform reindexing in asynchronous thread";
  }

  @POST
  public Response reIndex(@QueryParam("workspaceId") @DefaultValue("") final String workspaceIdStr) {
    WorkspaceId cWorkspaceId;
    if (workspaceId == null) {
      if (StringUtils.isBlank(workspaceIdStr)) {
        cWorkspaceId = null;
      }
      else {
        try {
          String[] splits = workspaceIdStr.split(":");
          String ns = splits[0], name = splits[1];
          cWorkspaceId = SmartContentAPI.getInstance().getWorkspaceApi().createWorkspaceId(ns, name);
        }
        catch (Exception ex) {
          cWorkspaceId = null;
        }
      }
    }
    else {
      cWorkspaceId = workspaceId;
    }
    Response.ResponseBuilder builder = Response.status(Response.Status.ACCEPTED);
    SmartContentAPI.getInstance().getContentLoader().reIndex(cWorkspaceId);
    return builder.build();
  }
}
