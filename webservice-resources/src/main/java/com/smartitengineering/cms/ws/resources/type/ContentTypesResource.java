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
package com.smartitengineering.cms.ws.resources.type;

import com.smartitengineering.cms.api.SmartContentAPI;
import com.smartitengineering.cms.api.exception.InvalidReferenceException;
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.api.type.ContentTypeLoader;
import com.smartitengineering.cms.api.type.MutableContentType;
import com.smartitengineering.cms.api.workspace.Workspace;
import com.smartitengineering.cms.api.workspace.WorkspaceAPI;
import com.smartitengineering.cms.ws.resources.workspace.WorkspaceResource;
import com.smartitengineering.util.rest.atom.server.AbstractResource;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
@Path("/type/{" + WorkspaceResource.PARAM_NAMESPACE + "}/{" + WorkspaceResource.PARAM_NAME + "}")
public class ContentTypesResource extends AbstractResource {

  public static final String PATH_TO_CONTENT_TYPE = "{typeNS}/{typeName}";
  public static final String REL_CONTENT_TYPE = "contentType";
  private static final Comparator<ContentType> CONTENT_TYPE_COMPRATOR = new Comparator<ContentType>() {

    @Override
    public int compare(ContentType o1, ContentType o2) {
      return o1.getLastModifiedDate().compareTo(o2.getLastModifiedDate());
    }
  };
  protected final Logger logger = LoggerFactory.getLogger(getClass());
  private final Workspace workspace;

  public ContentTypesResource(@PathParam(WorkspaceResource.PARAM_NAMESPACE) String namespace,
                              @PathParam(WorkspaceResource.PARAM_NAME) String workspaceName) {
    final WorkspaceAPI workspaceApi = SmartContentAPI.getInstance().getWorkspaceApi();
    this.workspace = workspaceApi.getWorkspace(workspaceApi.createWorkspaceId(namespace, workspaceName));
    if (this.workspace == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
  }

  @GET
  @Produces(MediaType.APPLICATION_ATOM_XML)
  public Response get() {
    Collection<ContentType> types = workspace.getContentDefintions();
    if (types == null || types.isEmpty()) {
      return Response.noContent().build();
    }
    ContentType type = Collections.max(types, CONTENT_TYPE_COMPRATOR);
    Date lastChangeDate = type.getLastModifiedDate();
    Feed feed = getFeed(new StringBuilder("content-type.").append(workspace.getId()).toString(), "Content Types",
                        lastChangeDate);
    String wsNS = workspace.getId().getGlobalNamespace(), wsName = workspace.getId().getName();
    for (ContentType contentType : types) {
      final ContentTypeId contentTypeId = contentType.getContentTypeID();
      URI uri = getAbsoluteURIBuilder().path(ContentTypesResource.class).path(PATH_TO_CONTENT_TYPE).build(wsNS, wsName, contentTypeId.
          getNamespace(), contentTypeId.getName());
      final String toString = contentTypeId.toString();
      Entry entry = getEntry(toString, toString, lastChangeDate, getLink(uri, REL_CONTENT_TYPE,
                                                                         MediaType.APPLICATION_XML));
      feed.addEntry(entry);
    }
    Response.ResponseBuilder builder = Response.ok(feed);
    CacheControl control = new CacheControl();
    control.setMaxAge(180);
    builder.cacheControl(control);
    return builder.build();
  }

  @Path(PATH_TO_CONTENT_TYPE)
  public ContentTypeResource getContentType(@PathParam("typeNS") String typeNS, @PathParam("typeName") String typeName) {
    final ContentTypeLoader loader = SmartContentAPI.getInstance().getContentTypeLoader();
    ContentType type = loader.loadContentType(loader.createContentTypeId(workspace.getId(), typeNS, typeName));
    return new ContentTypeResource(getInjectables(), type);
  }

  @POST
  @Consumes(MediaType.APPLICATION_XML)
  public Response post(InputStream stream) {
    try {
      final ContentTypeLoader contentTypeLoader = SmartContentAPI.getInstance().getContentTypeLoader();
      final Collection<MutableContentType> types;
      types = contentTypeLoader.parseContentTypes(workspace.getId(), stream,
                                                  com.smartitengineering.cms.api.common.MediaType.APPLICATION_XML);
      for (MutableContentType type : types) {
        type.put();
      }
      return Response.status(Response.Status.ACCEPTED).build();
    }
    catch (InvalidReferenceException ex) {
      logger.warn(ex.getMessage(), ex);
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
    catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
    }
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
  }

  @Override
  protected String getAuthor() {
    return "Smart CMS";
  }
}
