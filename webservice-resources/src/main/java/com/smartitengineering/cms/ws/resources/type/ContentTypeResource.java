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
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.type.MutableContentType;
import com.smartitengineering.cms.ws.common.utils.Utils;
import com.smartitengineering.util.rest.server.AbstractResource;
import com.smartitengineering.util.rest.server.ServerResourceInjectables;
import java.util.Date;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class ContentTypeResource extends AbstractResource {

  private final ContentType type;
  private final Date lastModified;
  private final EntityTag tag;
  private final Logger logger = LoggerFactory.getLogger(getClass());

  public ContentTypeResource(ServerResourceInjectables injectables, ContentType type) {
    super(injectables);
    if (type == null) {
      throw new WebApplicationException(Response.status(Status.NOT_FOUND).build());
    }
    this.type = type;
    lastModified = type.getLastModifiedDate();
    tag = new EntityTag(DigestUtils.md5Hex(Utils.getFormattedDate(lastModified)));
  }

  @GET
  @Produces(MediaType.APPLICATION_XML)
  public Response get() {
    ResponseBuilder builder = getContext().getRequest().evaluatePreconditions(lastModified, tag);
    if (builder == null) {
      builder = Response.ok(type.getRepresentations().get(
          com.smartitengineering.cms.api.common.MediaType.APPLICATION_XML));
      builder.lastModified(lastModified);
      builder.tag(tag);
    }
    return builder.build();
  }

  @DELETE
  public Response delete() {
    ResponseBuilder builder = getContext().getRequest().evaluatePreconditions(lastModified, tag);
    if (builder != null) {
      return builder.build();
    }
    MutableContentType contentType = SmartContentAPI.getInstance().getContentTypeLoader().getMutableContentType(type);
    try {
      contentType.delete();
      return Response.ok().build();
    }
    catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }
}
