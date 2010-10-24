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

import com.smartitengineering.cms.api.content.CollectionFieldValue;
import com.smartitengineering.cms.api.content.Content;
import com.smartitengineering.cms.api.content.ContentFieldValue;
import com.smartitengineering.cms.api.content.ContentId;
import com.smartitengineering.cms.api.content.Field;
import com.smartitengineering.cms.api.content.FieldValue;
import com.smartitengineering.cms.api.content.MutableCollectionFieldValue;
import com.smartitengineering.cms.api.content.MutableField;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.factory.content.WriteableContent;
import com.smartitengineering.cms.api.type.CollectionDataType;
import com.smartitengineering.cms.api.type.ContentStatus;
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.type.DataType;
import com.smartitengineering.cms.api.type.FieldDef;
import com.smartitengineering.cms.api.type.OtherDataType;
import com.smartitengineering.cms.api.type.RepresentationDef;
import com.smartitengineering.cms.api.type.VariationDef;
import com.smartitengineering.cms.ws.common.domains.CollectionFieldValueImpl;
import com.smartitengineering.cms.ws.common.domains.ContentImpl;
import com.smartitengineering.cms.ws.common.domains.FieldImpl;
import com.smartitengineering.cms.ws.common.domains.FieldValueImpl;
import com.smartitengineering.cms.ws.common.domains.OtherFieldValueImpl;
import com.smartitengineering.cms.ws.resources.type.ContentTypeResource;
import com.smartitengineering.util.bean.adapter.AbstractAdapterHelper;
import com.smartitengineering.util.bean.adapter.GenericAdapter;
import com.smartitengineering.util.bean.adapter.GenericAdapterImpl;
import com.smartitengineering.util.rest.server.AbstractResource;
import com.smartitengineering.util.rest.server.ServerResourceInjectables;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriBuilder;
import org.apache.commons.codec.binary.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class ContentResource extends AbstractResource {

  private final Content content;
  private final ContentId contentId;
  private final EntityTag tag;
  protected final Logger logger = LoggerFactory.getLogger(getClass());
  protected final GenericAdapter<Content, com.smartitengineering.cms.ws.common.domains.Content> adapter;

  public ContentResource(ServerResourceInjectables injectables, ContentId contentId) {
    super(injectables);
    if (contentId == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    this.contentId = contentId;
    if (logger.isDebugEnabled()) {
      logger.debug("Content ID " + contentId);
    }
    if (contentId.getId() != null && contentId.getId().length > 0) {
      //An existing content
      this.content = SmartContentAPI.getInstance().getContentLoader().loadContent(contentId);
    }
    else {
      //New content is being created
      this.content = null;
    }
    if (content != null) {
      tag = new EntityTag(content.getEntityTagValue());
    }
    else {
      tag = null;
    }
    GenericAdapterImpl adapterImpl =
                       new GenericAdapterImpl<Content, com.smartitengineering.cms.ws.common.domains.Content>();
    adapterImpl.setHelper(new ContentAdapterHelper());
    adapter = adapterImpl;
  }

  @Path("f/{fieldName}")
  public FieldResource getFieldResource(@PathParam("fieldName") String fieldName) {
    if (logger.isDebugEnabled()) {
      logger.debug("Trying to get field resource with name " + fieldName);
    }
    FieldDef fieldDef = content.getContentDefinition().getFieldDefs().get(fieldName);
    FieldResource resource = new FieldResource(getInjectables(), content, fieldDef, tag);
    return resource;
  }

  @Path("r/{repName}")
  public RepresentationResource getRepresentationResource(@PathParam("repName") String repName) {
    return new RepresentationResource(getInjectables(), repName, content);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response get() {
    if (content == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    ResponseBuilder builder = getContext().getRequest().evaluatePreconditions(tag);
    if (builder == null) {
      builder = Response.ok(adapter.convert(getContent()));
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
  public Response put(com.smartitengineering.cms.ws.common.domains.Content jsonContent,
                      @HeaderParam(HttpHeaders.IF_MATCH) EntityTag etag) {
    Content newContent;
    try {
      newContent = adapter.convertInversely(jsonContent);
    }
    catch (Exception ex) {
      logger.warn("Could not convert to content!", ex);
      return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).type(MediaType.TEXT_PLAIN).build();
    }
    final WriteableContent writeableContent;
    if (this.content == null) {
      //Create new content
      writeableContent = SmartContentAPI.getInstance().getContentLoader().getWritableContent(newContent);
    }
    else {
      //Update new content with etag checking
      if (etag == null) {
        return Response.status(Response.Status.PRECONDITION_FAILED).build();
      }
      ResponseBuilder builder = getContext().getRequest().evaluatePreconditions(this.tag);
      if (builder != null) {
        return builder.build();
      }
      //Merge new contents into the old one in case of update
      writeableContent = SmartContentAPI.getInstance().getContentLoader().getWritableContent(this.content);
      writeableContent.setContentDefinition(newContent.getContentDefinition());
      writeableContent.setStatus(newContent.getStatus());
      writeableContent.setParentId(newContent.getParentId());
      for (Field field : newContent.getOwnFields().values()) {
        writeableContent.setField(field);
      }
    }
    //Set content id for new content with valid id
    if (this.content == null && contentId.getId() != null && contentId.getId().length > 0) {
      writeableContent.setContentId(contentId);
    }
    //Create new content id for new content with no specified id
    else if (this.content == null) {
      writeableContent.createContentId(contentId.getWorkspaceId());
    }
    try {
      //Save or update the content, will be decided by writeable content implementation
      writeableContent.put();
    }
    catch (IOException ex) {
      logger.error("Could save/update content!", ex);
      return Response.serverError().build();
    }
    final ResponseBuilder builder;
    if (this.content == null) {
      //Send 201
      builder = Response.created(getContentUri(getAbsoluteURIBuilder(), writeableContent.getContentId()));
    }
    else {
      //Send 202
      builder = Response.status(Response.Status.ACCEPTED);
    }
    return builder.build();
  }

  @DELETE
  public Response delete(@HeaderParam(HttpHeaders.IF_MATCH) EntityTag etag) {
    if (content == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    if (etag == null) {
      return Response.status(Response.Status.PRECONDITION_FAILED).build();
    }
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

  public static URI getContentUri(UriBuilder builder, ContentId contentId) {
    builder.path(ContentsResource.class).path(ContentsResource.PATH_TO_CONTENT);
    return builder.build(contentId.getWorkspaceId().getGlobalNamespace(), contentId.getWorkspaceId().getName(),
                         StringUtils.newStringUtf8(contentId.getId()));
  }

  protected class ContentAdapterHelper extends AbstractAdapterHelper<Content, com.smartitengineering.cms.ws.common.domains.Content> {

    @Override
    protected com.smartitengineering.cms.ws.common.domains.Content newTInstance() {
      return new ContentImpl();
    }

    @Override
    protected void mergeFromF2T(Content fromBean, com.smartitengineering.cms.ws.common.domains.Content toBean) {
      ContentImpl contentImpl = (ContentImpl) toBean;
      ContentType type = fromBean.getContentDefinition();
      contentImpl.setContentTypeUri(
          ContentTypeResource.getContentTypeRelativeURI(type.getContentTypeID()).toASCIIString());
      if (fromBean.getParentId() != null) {
        contentImpl.setParentContentUri(ContentResource.getContentUri(getRelativeURIBuilder(), fromBean.getParentId()).
            toASCIIString());
      }
      contentImpl.setCreationDate(fromBean.getCreationDate());
      contentImpl.setLastModifiedDate(fromBean.getLastModifiedDate());
      final ContentStatus status = fromBean.getStatus();
      if (status != null) {
        contentImpl.setStatus(status.getName());
      }
      Map<String, Field> fields = fromBean.getFields();
      if (logger.isDebugEnabled()) {
        logger.debug("FIELDS: " + fields);
      }
      String contentUri = ContentResource.getContentUri(getRelativeURIBuilder(), fromBean.getContentId()).toASCIIString();
      for (FieldDef fieldDef : type.getFieldDefs().values()) {
        final String fieldName = fieldDef.getName();
        Field field = fields.get(fieldName);
        FieldImpl fieldImpl = new FieldImpl();
        fieldImpl.setName(fieldName);
        getDomainField(getRelativeURIBuilder(), field, contentUri, fieldImpl);
        if (logger.isDebugEnabled()) {
          logger.debug("Converting field " + field.getName() + " with value " + field.getValue().toString());
        }
        contentImpl.getFields().add(fieldImpl);
      }
      Collection<RepresentationDef> defs = type.getRepresentationDefs().values();
      String currentContext = new StringBuilder(contentUri).append("/r/").toString();
      Map<String, String> repUris = contentImpl.getRepresentations();
      for (RepresentationDef def : defs) {
        String uri = new StringBuilder(currentContext).append(def.getName()).toString();
        repUris.put(uri, def.getMIMEType());
      }
    }

    @Override
    protected Content convertFromT2F(com.smartitengineering.cms.ws.common.domains.Content toBean) {
      final ContentType contentType;
      try {
        ContentTypeResource resource = getResourceContext().matchResource(new URI(toBean.getContentTypeUri()),
                                                                          ContentTypeResource.class);
        if (resource == null) {
          throw new NullPointerException("No such content type!");
        }
        contentType = resource.getType();
      }
      catch (Exception ex) {
        throw new RuntimeException(ex.getMessage(), ex);
      }
      WriteableContent writeableContent = SmartContentAPI.getInstance().getContentLoader().createContent(contentType);
      ContentStatus status = contentType.getStatuses().get(toBean.getStatus());
      if (status == null) {
        throw new IllegalArgumentException("No such status in content type!");
      }
      writeableContent.setStatus(status);
      Content parentContent;
      final String parentContentUri = toBean.getParentContentUri();
      if (org.apache.commons.lang.StringUtils.isNotBlank(parentContentUri)) {
        try {
          final ContentResource resource = getResourceContext().matchResource(new URI(parentContentUri),
                                                                              ContentResource.class);
          if (resource == null) {
            throw new NullPointerException("No such content type!");
          }
          parentContent = resource.getContent();
        }
        catch (Exception ex) {
          throw new RuntimeException(ex.getMessage(), ex);
        }
        writeableContent.setParentId(parentContent.getContentId());
      }
      for (com.smartitengineering.cms.ws.common.domains.Field field : toBean.getFields()) {
        MutableField mutableField = getField(contentType.getFieldDefs().get(field.getName()), field);
        writeableContent.setField(mutableField);
      }
      return writeableContent;
    }
  }

  protected static FieldValue getFieldValue(final DataType dataType,
                                            com.smartitengineering.cms.ws.common.domains.FieldValue value) {
    FieldValue fieldValue;
    switch (dataType.getType()) {
      case COLLECTION:
        MutableCollectionFieldValue collectionFieldValue = SmartContentAPI.getInstance().getContentLoader().
            createCollectionFieldValue();
        CollectionDataType collectionDataType = (CollectionDataType) dataType;
        com.smartitengineering.cms.ws.common.domains.CollectionFieldValue cFieldValue =
                                                                          (com.smartitengineering.cms.ws.common.domains.CollectionFieldValue) value;
        ArrayList<FieldValue> list = new ArrayList<FieldValue>(cFieldValue.getValues().size());
        for (com.smartitengineering.cms.ws.common.domains.FieldValue v : cFieldValue.getValues()) {
          list.add(getFieldValue(collectionDataType.getItemDataType(), v));
        }
        collectionFieldValue.setValue(list);
        fieldValue = collectionFieldValue;
        break;
      default:
        fieldValue = SmartContentAPI.getInstance().getContentLoader().getValueFor(value.getValue(), dataType);
    }
    return fieldValue;
  }

  protected static MutableField getField(final FieldDef fieldDef,
                                         com.smartitengineering.cms.ws.common.domains.Field field) throws
      IllegalArgumentException {
    if (fieldDef == null) {
      throw new IllegalArgumentException("No field in content type with name " + field.getName());
    }
    final DataType dataType = fieldDef.getValueDef();
    if (org.apache.commons.lang.StringUtils.isNotBlank(field.getValue().getType()) &&
        !org.apache.commons.lang.StringUtils.equalsIgnoreCase(dataType.getType().name(), field.getValue().getType())) {
      throw new IllegalArgumentException("Type mismatch! NOTE: type of valus in field is optional in this case. " +
          "Field is " + field.getName());
    }
    final MutableField mutableField =
                       SmartContentAPI.getInstance().getContentLoader().createMutableField(fieldDef);
    final FieldValue fieldValue;
    fieldValue = getFieldValue(dataType, field.getValue());
    mutableField.setValue(fieldValue);
    return mutableField;
  }

  protected static void getDomainField(UriBuilder builder, Field field, String contentUri, FieldImpl fieldImpl) {

    final String fieldUri = new StringBuilder(contentUri).append("/f/").append(field.getName()).toString();
    fieldImpl.setFieldUri(fieldUri);
    fieldImpl.setFieldRawContentUri(new StringBuilder(fieldUri).append("/raw").toString());
    if (field != null) {
      fieldImpl.setName(field.getName());
      final FieldValueImpl value;
      final FieldValue contentFieldValue = field.getValue();
      final DataType valueDef = field.getFieldDef().getValueDef();
      value = getFieldvalue(builder, valueDef, contentFieldValue);
      Map<String, String> variations = fieldImpl.getVariations();
      Collection<VariationDef> defs = field.getFieldDef().getVariations().values();
      for (VariationDef def : defs) {
        variations.put(new StringBuilder(fieldUri).append("/v/").append(def.getName()).toString(), def.getMIMEType());
      }
      fieldImpl.setValue(value);
    }
  }

  private static FieldValueImpl getFieldvalue(final UriBuilder builder, final DataType valueDef, final FieldValue contentFieldValue) {
    final FieldValueImpl value;
    switch (valueDef.getType()) {
      case CONTENT: {
        FieldValueImpl valueImpl = new FieldValueImpl();
        valueImpl.setValue(ContentResource.getContentUri(builder, ((ContentFieldValue) contentFieldValue).getValue()).toASCIIString());
        value = valueImpl;
        break;
      }
      case COLLECTION: {
        CollectionFieldValueImpl valueImpl =
                                 new CollectionFieldValueImpl();
        Collection<FieldValue> contentValues =
                               ((CollectionFieldValue) contentFieldValue).getValue();
        final DataType itemDataType = ((CollectionDataType) valueDef).getItemDataType();
        for (FieldValue contentValue : contentValues) {
          valueImpl.getValues().add(getFieldvalue(builder, itemDataType, contentValue));
        }
        value = valueImpl;
        break;
      }
      case OTHER:
      case STRING: {
        OtherFieldValueImpl valueImpl = new OtherFieldValueImpl();
        valueImpl.setValue(contentFieldValue.toString());
        valueImpl.setMimeType(((OtherDataType) valueDef).getMIMEType());
        value = valueImpl;
        break;
      }
      default: {
        FieldValueImpl valueImpl = new FieldValueImpl();
        valueImpl.setValue(contentFieldValue.toString());
        value = valueImpl;
      }
    }
    value.setType(contentFieldValue.getDataType().name());
    return value;
  }
}
