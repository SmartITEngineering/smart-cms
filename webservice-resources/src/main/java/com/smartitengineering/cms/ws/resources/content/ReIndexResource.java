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

  public ReIndexResource(ServerResourceInjectables injectables) {
    super(injectables);
  }

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String get() {
    return "POST to this resource to perform reindexing in asynchronous thread";
  }

  @POST
  public Response reIndex(@QueryParam("workspaceId") @DefaultValue("") final String workspaceIdStr) {
    WorkspaceId workspaceId;
    if (StringUtils.isBlank(workspaceIdStr)) {
      workspaceId = null;
    }
    else {
      try {
        String[] splits = workspaceIdStr.split(":");
        String ns = splits[0], name = splits[1];
        workspaceId = SmartContentAPI.getInstance().getWorkspaceApi().createWorkspaceId(ns, name);
      }
      catch (Exception ex) {
        workspaceId = null;
      }
    }
    Response.ResponseBuilder builder = Response.status(Response.Status.ACCEPTED);
    SmartContentAPI.getInstance().getContentLoader().reIndex(workspaceId);
    return builder.build();
  }
}
