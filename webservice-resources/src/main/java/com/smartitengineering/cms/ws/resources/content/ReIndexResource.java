/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smartitengineering.cms.ws.resources.content;

import com.smartitengineering.cms.api.content.ContentId;
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
  private ContentId contentId;

  public ReIndexResource(ServerResourceInjectables injectables) {
    super(injectables);
  }

  public ContentId getContentId() {
    return contentId;
  }

  public WorkspaceId getWorkspaceId() {
    return workspaceId;
  }

  public void setContentId(ContentId contentId) {
    this.contentId = contentId;
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
    if (contentId != null) {
      SmartContentAPI.getInstance().getContentLoader().reIndex(contentId);
    }
    else {
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
      SmartContentAPI.getInstance().getContentLoader().reIndex(cWorkspaceId);
    }
    Response.ResponseBuilder builder = Response.status(Response.Status.ACCEPTED);
    return builder.build();
  }
}
