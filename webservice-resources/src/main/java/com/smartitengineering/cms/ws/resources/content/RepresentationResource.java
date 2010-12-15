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

import com.smartitengineering.cms.api.content.Content;
import com.smartitengineering.cms.api.content.ContentId;
import com.smartitengineering.cms.api.content.Representation;
import com.smartitengineering.util.rest.server.AbstractResource;
import com.smartitengineering.util.rest.server.ServerResourceInjectables;
import java.net.URI;
import java.util.Date;
import javax.ws.rs.GET;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriBuilder;

/**
 *
 * @author imyousuf
 */
public class RepresentationResource extends AbstractResource {

  private final String representationName;
  private final Content content;

  public RepresentationResource(ServerResourceInjectables injectables, String representationName, Content content) {
    super(injectables);
    this.representationName = representationName;
    this.content = content;
  }

  @GET
  public Response get() {
    ResponseBuilder builder;
    Representation rep = content.getRepresentation(representationName);
    if (rep == null) {
      builder = Response.status(Response.Status.NOT_FOUND);
    }
    else {
      final Date lastModifiedDate = rep.getLastModifiedDate();
      builder = getContext().getRequest().evaluatePreconditions(lastModifiedDate);
      if (builder == null) {
        builder = Response.ok(rep.getRepresentation()).type(MediaType.valueOf(rep.getMimeType())).lastModified(
            lastModifiedDate);
        CacheControl control = new CacheControl();
        control.setMaxAge(900);
        builder.cacheControl(control);
      }
    }
    return builder.build();
  }

  public static URI getUri(UriBuilder baseBuilder, ContentId content, String repName) {
    return UriBuilder.fromUri(ContentResource.getContentUri(baseBuilder, content)).path(ContentResource.PATH_TO_REP).
        build(repName);
  }
}
