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
package com.smartitengineering.cms.ws.resources.type;

import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.api.workspace.Workspace;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.util.rest.atom.server.AbstractResource;
import com.smartitengineering.util.rest.server.ServerResourceInjectables;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;

/**
 *
 * @author imyousuf
 */
public class FriendlyContentTypesResource extends AbstractResource {

  private final WorkspaceId workspaceId;

  public FriendlyContentTypesResource(ServerResourceInjectables injectables, WorkspaceId workspaceId) {
    super(injectables);
    if (workspaceId == null) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }
    this.workspaceId = workspaceId;
  }

  @GET
  @Produces(MediaType.APPLICATION_ATOM_XML)
  public Response get() {
    Workspace workspace = workspaceId.getWorkspae();
    List<ContentType> types = new ArrayList<ContentType>();
    Collection<WorkspaceId> friends = workspace.getFriendlies();
    if (friends != null && !friends.isEmpty()) {
      for (WorkspaceId wId : friends) {
        Workspace friend = wId.getWorkspae();
        if (friend != null) {
          types.addAll(friend.getContentDefintions());
        }
      }
    }
    if (types == null || types.isEmpty()) {
      return Response.noContent().build();
    }
    ContentType type = Collections.max(types, ContentTypesResource.CONTENT_TYPE_COMPRATOR);
    Date lastChangeDate = type.getLastModifiedDate();
    Feed feed = getFeed(new StringBuilder("friendly-content-types.").append(workspace.getId()).toString(),
                        "Friendly Content Types",
                        lastChangeDate);
    String wsNS = workspace.getId().getGlobalNamespace(), wsName = workspace.getId().getName();
    for (ContentType contentType : types) {
      final ContentTypeId contentTypeId = contentType.getContentTypeID();
      URI uri = getAbsoluteURIBuilder().path(ContentTypesResource.class).path(
          ContentTypesResource.PATH_TO_FRIENDLY_CONTENT_TYPE).build(wsNS, wsName, contentTypeId.getWorkspace().
          getGlobalNamespace(), contentTypeId.getWorkspace().getName(), contentTypeId.getNamespace(), contentTypeId.
          getName());
      final String toString = contentTypeId.toString();
      Entry entry = getEntry(toString, toString, lastChangeDate, getLink(uri, "friendlyContentType",
                                                                         MediaType.APPLICATION_ATOM_XML));
      feed.addEntry(entry);
    }
    Response.ResponseBuilder builder = Response.ok(feed);
    CacheControl control = new CacheControl();
    control.setMaxAge(180);
    builder.cacheControl(control);
    return builder.build();
  }

  @Override
  protected String getAuthor() {
    return "Smart CMS";
  }
}
