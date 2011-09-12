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

import com.smartitengineering.cms.api.common.TemplateType;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.workspace.Workspace;
import com.smartitengineering.cms.api.factory.workspace.WorkspaceAPI.ResourceSortCriteria;
import com.smartitengineering.cms.ws.common.domains.ResourceTemplate;
import com.smartitengineering.cms.ws.common.domains.ResourceTemplateImpl;
import com.smartitengineering.util.rest.atom.server.AbstractResource;
import com.smartitengineering.util.rest.server.ServerResourceInjectables;
import com.sun.jersey.multipart.FormDataParam;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author imyousuf
 */
public class WorkspaceContentCoProcessorsResource extends AbstractResource {

  private final Workspace workspace;
  private final int count;

  public WorkspaceContentCoProcessorsResource(Workspace workspace, int count, ServerResourceInjectables injectables) {
    super(injectables);
    this.workspace = workspace;
    this.count = count;
  }

  @GET
  public Response getFirstPage() {
    return getAfter("");
  }

  @GET
  @Path("after/{name}")
  public Response getAfter(@PathParam("name") @DefaultValue("") final String startPointName) {
    return getResponseForContentCoProcNames(SmartContentAPI.getInstance().getWorkspaceApi().getContentCoProcessorNames(
        workspace.getId(), ResourceSortCriteria.BY_NAME, startPointName, count));
  }

  @GET
  @Path("before/{name}")
  public Response getBefore(@PathParam("name") @DefaultValue("") String startPointName) {
    return getResponseForContentCoProcNames(SmartContentAPI.getInstance().getWorkspaceApi().getContentCoProcessorNames(
        workspace.getId(), ResourceSortCriteria.BY_NAME, startPointName, -1 * count));
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Response putResource(ResourceTemplate template) {
    if (StringUtils.isBlank(template.getName())) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
    WorkspaceContentCoProcessorResource resource = new WorkspaceContentCoProcessorResource(template.getName(), workspace,
                                                                                   getInjectables());
    return resource.put(template, null);
  }

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public Response postForm(@FormDataParam("name") String name,
                                          @FormDataParam("templateType") String templateType,
                                          @FormDataParam("templateData") byte[] templateData) {
    if (StringUtils.isBlank(name) || StringUtils.isBlank(templateType) || templateData == null || templateData.length <=
        0) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
    try {
      TemplateType.valueOf(templateType);
    }
    catch (Exception ex) {
      return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
    }
    ResourceTemplateImpl template = new ResourceTemplateImpl();
    template.setName(name);
    template.setTemplate(templateData);
    template.setTemplateType(templateType);
    WorkspaceContentCoProcessorResource resource = new WorkspaceContentCoProcessorResource(template.getName(), workspace,
                                                                                   getInjectables());
    return resource.put(template, null);
  }

  protected Response getResponseForContentCoProcNames(Collection<String> names) {
    if (names == null || names.isEmpty()) {
      return Response.noContent().build();
    }
    final Date date = new Date();
    Feed feed = getFeed(new StringBuilder("reps-").append(workspace.getId().toString()).toString(),
                        "Content co processors of a feed", date);
    ArrayList<String> nameList = new ArrayList<String>(names);
    final String first = nameList.get(0);
    final String last = nameList.get(nameList.size() - 1);
    Link previousLink = getLink(getUriInfo().getBaseUriBuilder().path(WorkspaceResource.class).path(
        WorkspaceResource.PATH_CONTENT_CO_PROCESSORS).path("before").path(first).build(workspace.getId().getGlobalNamespace(), workspace.
        getId().getName()), Link.REL_PREVIOUS, MediaType.APPLICATION_ATOM_XML);
    Link nextLink = getLink(getUriInfo().getBaseUriBuilder().path(WorkspaceResource.class).path(
        WorkspaceResource.PATH_CONTENT_CO_PROCESSORS).path("after").path(last).build(workspace.getId().getGlobalNamespace(), workspace.
        getId().getName()), Link.REL_NEXT, MediaType.APPLICATION_ATOM_XML);
    Link firstLink = getLink(
        getUriInfo().getBaseUriBuilder().path(WorkspaceResource.class).path(
        WorkspaceResource.PATH_CONTENT_CO_PROCESSORS).build(workspace.getId().getGlobalNamespace(), workspace.getId().getName()),
        Link.REL_FIRST, MediaType.APPLICATION_ATOM_XML);
    feed.addLink(firstLink);
    feed.addLink(previousLink);
    feed.addLink(nextLink);
    for (String name : nameList) {
      Link nameLink = getLink(getUriInfo().getBaseUriBuilder().path(WorkspaceResource.class).path(
          WorkspaceResource.PATH_CONTENT_CO_PROCESSORS).path("name").path(name).build(workspace.getId().getGlobalNamespace(), workspace.
          getId().getName()), Link.REL_ALTERNATE, MediaType.APPLICATION_JSON);
      Entry entry = getEntry(name, name, date, nameLink);
      feed.addEntry(entry);
    }
    return Response.ok(feed).build();
  }

  @Override
  protected String getAuthor() {
    return "Smart CMS";
  }
}
