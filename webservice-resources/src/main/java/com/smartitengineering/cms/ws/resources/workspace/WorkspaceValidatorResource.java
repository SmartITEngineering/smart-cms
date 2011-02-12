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

import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.workspace.Workspace;
import com.smartitengineering.cms.api.factory.workspace.WorkspaceAPI;
import com.smartitengineering.cms.api.type.ValidatorType;
import com.smartitengineering.cms.api.workspace.ValidatorTemplate;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.cms.ws.common.domains.ResourceTemplateImpl;
import com.smartitengineering.cms.ws.resources.domains.Factory;
import com.smartitengineering.util.rest.server.AbstractResource;
import com.smartitengineering.util.rest.server.ServerResourceInjectables;
import com.sun.jersey.multipart.FormDataParam;
import java.util.Date;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author imyousuf
 */
public class WorkspaceValidatorResource extends AbstractResource {

  private final String valName;
  private final ValidatorTemplate template;
  private final Workspace workspace;

  public WorkspaceValidatorResource(String valName, Workspace workspace, ServerResourceInjectables injectables) {
    super(injectables);
    this.valName = valName;
    this.workspace = workspace;
    template = SmartContentAPI.getInstance().getWorkspaceApi().getValidatorTemplate(workspace.getId(), valName);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response get() {
    if (template == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    Date lastModifiedDate = template.getLastModifiedDate();
    EntityTag tag = new EntityTag(template.getEntityTagValue());
    ResponseBuilder builder = getContext().getRequest().evaluatePreconditions(lastModifiedDate, tag);
    if (builder == null) {
      builder = Response.ok();
      builder.entity(Factory.getValidatorTemplate(template));
      builder.lastModified(template.getLastModifiedDate());
      builder.tag(tag);
      CacheControl control = new CacheControl();
      control.setMaxAge(300);
      builder.cacheControl(control);
    }
    return builder.build();
  }

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public Response postValidatorForm(@FormDataParam("templateType") String templateType,
                                     @FormDataParam("templateData") byte[] templateData) {
    if (StringUtils.isBlank(templateType) || templateData == null || templateData.length <=
        0) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
    try {
      ValidatorType.valueOf(templateType);
    }
    catch (Exception ex) {
      return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
    }
    ResourceTemplateImpl newTemplate = new ResourceTemplateImpl();
    newTemplate.setName(valName);
    newTemplate.setTemplate(templateData);
    newTemplate.setTemplateType(templateType);
    WorkspaceValidatorResource resource = new WorkspaceValidatorResource(newTemplate.getName(), workspace,
                                                                         getInjectables());
    return resource.put(newTemplate, "*");
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  public Response put(com.smartitengineering.cms.ws.common.domains.ResourceTemplate template, @HeaderParam(
      HttpHeaders.IF_MATCH) String ifMatchHeader) {
    ResponseBuilder builder;
    WorkspaceId id = workspace.getId();
    if (this.template == null) {
      final WorkspaceAPI workspaceApi = SmartContentAPI.getInstance().getWorkspaceApi();
      ValidatorTemplate created = workspaceApi.putValidatorTemplate(workspaceApi.createWorkspaceId(
          id.getGlobalNamespace(), id.getName()), valName, ValidatorType.valueOf(template.getTemplateType()), template.
          getTemplate());
      if (created != null) {
        UriBuilder uriBuilder = getAbsoluteURIBuilder().path(WorkspaceResource.class).path(
            WorkspaceResource.PATH_VALIDATORS).path("name").path(valName);
        builder = Response.created(uriBuilder.build(id.getGlobalNamespace(), id.getName()));
      }
      else {
        builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
      }
    }
    else {
      if (StringUtils.isBlank(ifMatchHeader)) {
        return Response.status(Status.PRECONDITION_FAILED).build();
      }
      Date lastModifiedDate = this.template.getLastModifiedDate();
      EntityTag entityTag = new EntityTag(this.template.getEntityTagValue());
      builder = getContext().getRequest().evaluatePreconditions(lastModifiedDate, entityTag);
      if (builder == null) {
        final WorkspaceAPI workspaceApi = SmartContentAPI.getInstance().getWorkspaceApi();
        ValidatorTemplate put = workspaceApi.putValidatorTemplate(workspaceApi.createWorkspaceId(id.getGlobalNamespace(), id.
            getName()), valName, ValidatorType.valueOf(template.getTemplateType()), template.getTemplate());
        if (put != null) {
          builder = Response.status(Status.ACCEPTED).location(getUriInfo().getRequestUri());
        }
        else {
          builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
        }
      }
    }
    return builder.build();
  }

  @DELETE
  public Response delete(@HeaderParam(HttpHeaders.IF_MATCH) String ifMatchHeader) {
    if (template == null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    if (StringUtils.isBlank(ifMatchHeader)) {
      return Response.status(Status.PRECONDITION_FAILED).build();
    }
    Date lastModifiedDate = template.getLastModifiedDate();
    EntityTag entityTag = new EntityTag(template.getEntityTagValue());
    ResponseBuilder builder = getContext().getRequest().evaluatePreconditions(lastModifiedDate, entityTag);
    if (builder == null) {
      final WorkspaceAPI workspaceApi = SmartContentAPI.getInstance().getWorkspaceApi();
      try {
        workspaceApi.delete(template);
        builder = Response.status(Status.ACCEPTED);
      }
      catch (Exception ex) {
        builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
      }
    }
    return builder.build();
  }
}
