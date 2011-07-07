/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smartitengineering.cms.ws.resources.workspace;

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

  public static final String CONTENTS = "contents", TYPES = "types";
  private WorkspaceId workspaceId;
  private boolean contentsOnly;
  private boolean typesOnly;

  public ReIndexResource(ServerResourceInjectables injectables) {
    super(injectables);
  }

  public WorkspaceId getWorkspaceId() {
    return workspaceId;
  }

  public void setWorkspaceId(WorkspaceId workspaceId) {
    this.workspaceId = workspaceId;
  }

  public boolean isContentsOnly() {
    return contentsOnly;
  }

  public void setContentsOnly(boolean contentsOnly) {
    this.contentsOnly = contentsOnly;
  }

  public boolean isTypesOnly() {
    return typesOnly;
  }

  public void setTypesOnly(boolean typesOnly) {
    this.typesOnly = typesOnly;
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
    if (contentsOnly) {
      return reIndexContents(cWorkspaceId);
    }
    else if (typesOnly) {
      return reIndexTypes(cWorkspaceId);
    }
    else {
      SmartContentAPI.getInstance().getContentLoader().reIndex(cWorkspaceId);
      SmartContentAPI.getInstance().getContentTypeLoader().reIndexTypes(cWorkspaceId);
      Response.ResponseBuilder builder = Response.status(Response.Status.ACCEPTED);
      return builder.build();
    }
  }

  private Response reIndexTypes(final WorkspaceId cWorkspaceId) {
    SmartContentAPI.getInstance().getContentTypeLoader().reIndexTypes(cWorkspaceId);
    Response.ResponseBuilder builder = Response.status(Response.Status.ACCEPTED);
    return builder.build();
  }

  private Response reIndexContents(final WorkspaceId cWorkspaceId) {
    SmartContentAPI.getInstance().getContentLoader().reIndex(cWorkspaceId);
    Response.ResponseBuilder builder = Response.status(Response.Status.ACCEPTED);
    return builder.build();
  }
}
