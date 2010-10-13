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
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.factory.content.WriteableContent;
import com.smartitengineering.cms.ws.common.utils.Utils;
import com.smartitengineering.cms.ws.resources.domains.Factory;
import com.smartitengineering.util.rest.server.AbstractResource;
import com.smartitengineering.util.rest.server.ServerResourceInjectables;
import java.net.URI;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriBuilder;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class ContentResource extends AbstractResource {

  private final Content content;
  private final EntityTag tag;
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  ContentResource(ServerResourceInjectables injectables) {
    super(injectables);
    content = null;
    tag = null;
  }

  public ContentResource(ServerResourceInjectables injectables, Content content) {
    super(injectables);
    if (content == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    this.content = content;
    tag = new EntityTag(DigestUtils.md5Hex(new StringBuilder(Utils.getFormattedDate(content.getLastModifiedDate())).
        append('~').append(content.getOwnFields().toString()).append('~').append(content.getStatus()).toString()));
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response get() {
    ResponseBuilder builder = getContext().getRequest().evaluatePreconditions(tag);
    if (builder == null) {
      builder = Response.ok(Factory.getContent(getContent()));
      builder.tag(tag);
      builder.lastModified(getContent().getLastModifiedDate());
      CacheControl control = new CacheControl();
      control.setMaxAge(300);
      builder.cacheControl(control);
    }
    return builder.build();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  public Response put(com.smartitengineering.cms.ws.common.domains.Content content) {
    return null;
  }

  @DELETE
  public Response delete() {
    ResponseBuilder builder = getContext().getRequest().evaluatePreconditions(tag);
    if (builder == null) {
      WriteableContent writeableContent = SmartContentAPI.getInstance().getContentLoader().getWritableContent(content);
      try {
        writeableContent.delete();
        builder = Response.ok();
      }
      catch (Exception ex) {
        logger.error("Could not delete due to server error!", ex);
        builder = Response.serverError();
      }
    }
    return builder.build();
  }

  public Content getContent() {
    return content;
  }

  public static URI getContentUri(ContentId contentId) {
    UriBuilder builder = UriBuilder.fromResource(ContentsResource.class).path(ContentsResource.PATH_TO_CONTENT);
    return builder.build(contentId.getWorkspaceId().getGlobalNamespace(), contentId.getWorkspaceId().getName(),
                         StringUtils.newStringUtf8(contentId.getId()));

  }
}
