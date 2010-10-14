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
package com.smartitengineering.cms.ws.resources.content;

import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.factory.content.ContentLoader;
import com.smartitengineering.cms.api.factory.workspace.WorkspaceAPI;
import com.smartitengineering.cms.api.workspace.Workspace;
import com.smartitengineering.cms.ws.common.domains.Content;
import com.smartitengineering.util.rest.server.AbstractResource;
import java.io.UnsupportedEncodingException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.codec.binary.StringUtils;

/**
 *
 * @author imyousuf
 */
@Path("/c/{wsNS}/{wsName}")
public class ContentsResource extends AbstractResource {

  private final Workspace workspace;
  static final String PARAM_CONTENT = "contentId";
  public static final String PATH_TO_CONTENT = "{" + PARAM_CONTENT + "}";

  public ContentsResource(@PathParam("wsNS") String namespace, @PathParam("wsName") String name) {
    final WorkspaceAPI workspaceApi = SmartContentAPI.getInstance().getWorkspaceApi();
    workspace = workspaceApi.getWorkspace(workspaceApi.createWorkspaceId(namespace, name));
    if (workspace == null) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }
  }

  @Path(ContentsResource.PATH_TO_CONTENT)
  public ContentResource getContentResource(@PathParam(PARAM_CONTENT) String contentId) throws
      UnsupportedEncodingException {
    final ContentLoader contentLoader = SmartContentAPI.getInstance().getContentLoader();
    return new ContentResource(getInjectables(), contentLoader.createContentId(workspace.getId(), StringUtils.
        getBytesUtf8(contentId)));
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createContent(Content content) {
    final ContentLoader contentLoader = SmartContentAPI.getInstance().getContentLoader();
    ContentResource r = new ContentResource(getInjectables(), contentLoader.createContentId(workspace.getId(),
                                                                                            new byte[0]));
    return r.put(content, null);
  }

  @GET
  public Response get() {
    return Response.noContent().build();
  }
}
