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
import com.smartitengineering.cms.api.content.Field;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.factory.content.WriteableContent;
import com.smartitengineering.cms.api.type.FieldDef;
import com.smartitengineering.cms.ws.common.domains.FieldImpl;
import com.smartitengineering.util.bean.adapter.AbstractAdapterHelper;
import com.smartitengineering.util.bean.adapter.GenericAdapter;
import com.smartitengineering.util.bean.adapter.GenericAdapterImpl;
import com.smartitengineering.util.rest.server.AbstractResource;
import com.smartitengineering.util.rest.server.ServerResourceInjectables;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class FieldResource extends AbstractResource {

  private final Content content;
  private final FieldDef fieldDef;
  private final EntityTag entityTag;
  protected final GenericAdapter<Field, com.smartitengineering.cms.ws.common.domains.Field> adapter;
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  public FieldResource(ServerResourceInjectables injectables, Content content, FieldDef fieldDef, EntityTag eTag) {
    super(injectables);
    if (content == null || fieldDef == null) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }
    this.content = content;
    this.fieldDef = fieldDef;
    GenericAdapterImpl adapterImpl = new GenericAdapterImpl<Field, com.smartitengineering.cms.ws.common.domains.Field>();
    adapterImpl.setHelper(new FieldAdapterHelper());
    this.adapter = adapterImpl;
    this.entityTag = eTag;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response get() {
    ResponseBuilder builder = getContext().getRequest().evaluatePreconditions(content.getLastModifiedDate(), entityTag);
    if (builder == null) {
      Field field = content.getField(fieldDef.getName());
      if (field == null) {
        builder = Response.status(Status.NOT_FOUND);
      }
      else {
        builder = Response.ok(adapter.convert(field)).lastModified(content.getLastModifiedDate()).tag(entityTag);
        CacheControl control = new CacheControl();
        control.setMaxAge(300);
        builder.cacheControl(control);
      }
    }
    return builder.build();
  }

  @DELETE
  public Response delete(@HeaderParam(HttpHeaders.IF_MATCH) EntityTag ifMatchHeader) {
    if (!fieldDef.isFieldStandaloneUpdateAble()) {
      return Response.status(Status.FORBIDDEN).build();
    }
    final boolean isAvailable = content.getField(fieldDef.getName()) != null;
    if (!isAvailable) {
      return Response.status(Status.NOT_FOUND).build();
    }
    if (ifMatchHeader == null) {
      return Response.status(Status.PRECONDITION_FAILED).build();
    }
    ResponseBuilder builder = getContext().getRequest().evaluatePreconditions(content.getLastModifiedDate(), entityTag);
    if (builder == null) {
      WriteableContent writeableContent = SmartContentAPI.getInstance().getContentLoader().getWritableContent(content);
      writeableContent.removeField(fieldDef.getName());
      boolean error = false;
      try {
        writeableContent.put();
      }
      catch (Exception ex) {
        logger.error("Could not update field by updating content!", ex);
        builder = Response.status(Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage());
        error = true;
      }
      if (!error) {
        builder = Response.ok();
      }
    }
    return builder.build();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  public Response put(com.smartitengineering.cms.ws.common.domains.Field field,
                      @HeaderParam(HttpHeaders.IF_MATCH) EntityTag ifMatchHeader) {
    if (!fieldDef.isFieldStandaloneUpdateAble()) {
      return Response.status(Status.FORBIDDEN).build();
    }
    final boolean isUpdate = content.getField(field.getName()) != null;
    if (isUpdate && ifMatchHeader == null) {
      return Response.status(Status.PRECONDITION_FAILED).build();
    }
    ResponseBuilder builder = getContext().getRequest().evaluatePreconditions(content.getLastModifiedDate(), entityTag);
    if (builder == null) {
      boolean error = false;
      WriteableContent writeableContent = SmartContentAPI.getInstance().getContentLoader().getWritableContent(content);
      try {
        writeableContent.setField(adapter.convertInversely(field));
      }
      catch (Exception ex) {
        logger.warn("Could not convert to field!", ex);
        builder = Response.status(Status.BAD_REQUEST).entity(ex.getMessage());
        error = true;
      }
      try {
        writeableContent.put();
      }
      catch (Exception ex) {
        logger.error("Could not update field by updating content!", ex);
        builder = Response.status(Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage());
        error = true;
      }
      if (!error) {
        if (isUpdate) {
          builder = Response.status(Status.ACCEPTED);
        }
        else {
          builder = Response.created(getAbsoluteURIBuilder().uri(ContentResource.getContentUri(content.getContentId())).
              path(field.getName()).build());
        }
      }
    }
    return builder.build();
  }

  class FieldAdapterHelper extends AbstractAdapterHelper<Field, com.smartitengineering.cms.ws.common.domains.Field> {

    @Override
    protected com.smartitengineering.cms.ws.common.domains.Field newTInstance() {
      return new FieldImpl();
    }

    @Override
    protected void mergeFromF2T(Field fromBean, com.smartitengineering.cms.ws.common.domains.Field toBean) {
      ContentResource.getDomainField(fromBean, ContentResource.getContentUri(content.getContentId()).toASCIIString(),
                                     (FieldImpl) toBean);
    }

    @Override
    protected Field convertFromT2F(com.smartitengineering.cms.ws.common.domains.Field toBean) {
      return ContentResource.getField(fieldDef, toBean);
    }
  }
}
