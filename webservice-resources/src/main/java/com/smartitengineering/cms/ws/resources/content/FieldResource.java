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

import com.smartitengineering.cms.api.content.BooleanFieldValue;
import com.smartitengineering.cms.api.content.CollectionFieldValue;
import com.smartitengineering.cms.api.content.Content;
import com.smartitengineering.cms.api.content.ContentFieldValue;
import com.smartitengineering.cms.api.content.ContentId;
import com.smartitengineering.cms.api.content.DateTimeFieldValue;
import com.smartitengineering.cms.api.content.Field;
import com.smartitengineering.cms.api.content.FieldValue;
import com.smartitengineering.cms.api.content.NumberFieldValue;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.factory.content.WriteableContent;
import com.smartitengineering.cms.api.type.DataType;
import com.smartitengineering.cms.api.type.FieldDef;
import com.smartitengineering.cms.api.type.FieldValueType;
import com.smartitengineering.cms.api.type.OtherDataType;
import com.smartitengineering.cms.api.type.StringDataType;
import com.smartitengineering.cms.ws.common.domains.FieldImpl;
import com.smartitengineering.cms.ws.common.providers.TextURIListProvider;
import com.smartitengineering.cms.ws.common.utils.Utils;
import com.smartitengineering.util.bean.adapter.AbstractAdapterHelper;
import com.smartitengineering.util.bean.adapter.GenericAdapter;
import com.smartitengineering.util.bean.adapter.GenericAdapterImpl;
import com.smartitengineering.util.rest.atom.server.AbstractResource;
import com.smartitengineering.util.rest.server.ServerResourceInjectables;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
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
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Variant;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.commons.lang.StringUtils;
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
  public static final String PATH_TO_VAR = "v/{varName}";

  public FieldResource(ServerResourceInjectables injectables, Content content, FieldDef fieldDef, EntityTag eTag) {
    super(injectables);
    if (content == null || fieldDef == null) {
      logger.warn("No content or field def", new NullPointerException());
      throw new WebApplicationException(Status.NOT_FOUND);
    }
    this.content = content;
    this.fieldDef = fieldDef;
    GenericAdapterImpl adapterImpl = new GenericAdapterImpl<Field, com.smartitengineering.cms.ws.common.domains.Field>();
    adapterImpl.setHelper(new FieldAdapterHelper());
    this.adapter = adapterImpl;
    this.entityTag = eTag;
  }

  @Path(PATH_TO_VAR)
  public VariationResource getVariation(@PathParam("varName") String varName) {
    Field field = content.getField(fieldDef.getName());
    if (field != null) {
      return new VariationResource(getInjectables(), content, field, varName);
    }
    else {
      throw new WebApplicationException(Status.NOT_FOUND);
    }
  }

  @GET
  @Path("/raw/abs")
  public Response getAbsoluteRaw() {
    ResponseBuilder builder = getContext().getRequest().evaluatePreconditions(content.getLastModifiedDate(), entityTag);
    if (builder == null) {
      Field field = content.getField(fieldDef.getName());
      if (field == null) {
        builder = Response.status(Status.NOT_FOUND);
      }
      else {
        builder = Response.ok().tag(entityTag);
        processDefaultRawContent(builder);
      }
      CacheControl control = new CacheControl();
      control.setMaxAge(300);
      builder.cacheControl(control);
    }
    return builder.build();
  }

  @GET
  @Path("/raw")
  public Response getRaw() {
    boolean useDefault = false;
    List<MediaType> accepts = getContext().getRequest().getAcceptableMediaTypes();
    if (accepts == null || accepts.isEmpty()) {
      useDefault = true;
    }
    ResponseBuilder builder =
                    getContext().getRequest().evaluatePreconditions(content.getLastModifiedDate(), entityTag);
    if (builder == null) {
      Field field = content.getField(fieldDef.getName());
      if (field == null) {
        builder = Response.status(Status.NOT_FOUND);
      }
      else {
        builder = Response.ok().tag(entityTag);
        if (useDefault) {
          processDefaultRawContent(builder);
        }
        else {
          final MediaType fieldValueDefaultMimeType = getFieldValueDefaultMimeType(fieldDef.getValueDef());
          MediaType type = getUserPreferredType(fieldValueDefaultMimeType);
          if (type == null) {
            final List<Variant> variants = getVariants(MediaType.APPLICATION_ATOM_XML_TYPE, fieldValueDefaultMimeType,
                                                       MediaType.APPLICATION_JSON_TYPE, MediaType.APPLICATION_XML_TYPE,
                                                       MediaType.APPLICATION_XML_TYPE, MediaType.TEXT_XML_TYPE);
            return Response.notAcceptable(variants).build();
          }
          else if (type.equals(MediaType.APPLICATION_ATOM_XML_TYPE) || type.equals(MediaType.TEXT_XML_TYPE) || type.
              equals(
              MediaType.APPLICATION_XML_TYPE)) {
            processAtomFeedAsRawContent(builder);
          }
          else if (type.toString().equals(MediaType.MEDIA_TYPE_WILDCARD) ||
              type.equals(fieldValueDefaultMimeType)) {
            processDefaultRawContent(builder);
          }
          else {
            return Response.seeOther(getFieldUri()).build();
          }
        }
        CacheControl control = new CacheControl();
        control.setMaxAge(300);
        builder.cacheControl(control);
      }
    }
    return builder.build();
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
    ResponseBuilder builder =
                    getContext().getRequest().evaluatePreconditions(content.getLastModifiedDate(), entityTag);
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
    ResponseBuilder builder =
                    getContext().getRequest().evaluatePreconditions(content.getLastModifiedDate(), entityTag);
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
          builder = Response.created(UriBuilder.fromUri(ContentResource.getContentUri(getAbsoluteURIBuilder(), content.
              getContentId())).path(field.getName()).build());
        }
      }
    }
    return builder.build();
  }

  protected Collection<Entry> getEntries(final FieldValue value, final Date lastModifiedDate, String... id)
      throws IllegalArgumentException {
    final List<Entry> entries = new ArrayList<Entry>();
    final String mimeType = getFieldValueDefaultMimeType(fieldDef.getValueDef()).toString();
    switch (fieldDef.getValueDef().getType()) {
      case BOOLEAN:
        BooleanFieldValue booleanFieldValue = (BooleanFieldValue) value;
        Entry entry = getEntry("value", "Value", lastModifiedDate);
        entry.setContent(Boolean.toString(booleanFieldValue.getValue()), mimeType);
        entries.add(entry);
        break;
      case COLLECTION:
        CollectionFieldValue collectionFieldValue = (CollectionFieldValue) value;
        int index = 0;
        for (FieldValue fieldValue : collectionFieldValue.getValue()) {
          entries.addAll(
              getEntries(fieldValue, lastModifiedDate, new StringBuilder("value-").append(index++).toString()));
        }
        break;
      case CONTENT:
        ContentFieldValue contentFieldValue = (ContentFieldValue) value;
        entry = getEntry(StringUtils.defaultIfEmpty(id[0], "value"), "Value", lastModifiedDate, getLink(ContentResource.
            getContentUri(getRelativeURIBuilder(), contentFieldValue.getValue()), Link.REL_ALTERNATE, mimeType));
        entries.add(entry);
        break;
      case DATE_TIME:
        DateTimeFieldValue dateTimeFieldValue = (DateTimeFieldValue) value;
        entry = getEntry("value", "Value", lastModifiedDate);
        entry.setContent(Utils.getFormattedDate(dateTimeFieldValue.getValue()), mimeType);
        entries.add(entry);
        break;
      case DOUBLE:
      case INTEGER:
      case LONG:
        NumberFieldValue numberFieldValue = (NumberFieldValue) value;
        entry = getEntry("value", "Value", lastModifiedDate);
        entry.setContent(numberFieldValue.getValue().toString(), mimeType);
        entries.add(entry);
        break;
      case STRING:
      case OTHER:
        OtherDataType otherDataType = (OtherDataType) fieldDef.getValueDef();
        entry = getEntry("value", "Value", lastModifiedDate);
        entry.setContent(String.valueOf(value), mimeType);
        entries.add(entry);
        break;
    }
    return entries;
  }

  public static MediaType getFieldValueDefaultMimeType(DataType value) {
    switch (value.getType()) {
      case COLLECTION:
        return MediaType.APPLICATION_ATOM_XML_TYPE;
      case CONTENT:
        return TextURIListProvider.TEXT_URI_LIST_TYPE;
      case DATE_TIME:
      case DOUBLE:
      case INTEGER:
      case LONG:
      case BOOLEAN:
      default:
        return MediaType.TEXT_PLAIN_TYPE;
      case STRING:
      case OTHER:
        OtherDataType otherDataType = (OtherDataType) value;
        return MediaType.valueOf(otherDataType.getMIMEType());
    }
  }

  private void processDefaultRawContent(ResponseBuilder builder) {
    final Field field = content.getField(fieldDef.getName());
    final FieldValue value = field.getValue();
    final MediaType mimeType = getFieldValueDefaultMimeType(fieldDef.getValueDef());
    switch (fieldDef.getValueDef().getType()) {
      case BOOLEAN:
        BooleanFieldValue booleanFieldValue = (BooleanFieldValue) value;
        builder.entity(booleanFieldValue.getValue()).type(mimeType);
        break;
      case COLLECTION:
        processAtomFeedAsRawContent(builder);
        break;
      case CONTENT:
        ContentFieldValue contentFieldValue = (ContentFieldValue) value;
        builder.entity(Collections.singleton(ContentResource.getContentUri(getRelativeURIBuilder(), contentFieldValue.
            getValue()))).type(mimeType);
        break;
      case DATE_TIME:
        DateTimeFieldValue dateTimeFieldValue = (DateTimeFieldValue) value;
        builder.entity(Utils.getFormattedDate(dateTimeFieldValue.getValue())).type(mimeType);
        break;
      case DOUBLE:
      case INTEGER:
      case LONG:
        NumberFieldValue numberFieldValue = (NumberFieldValue) value;
        builder.entity(numberFieldValue.getValue().toString()).type(mimeType);
        break;
      case STRING:
        StringDataType stringDataType = (StringDataType) fieldDef.getValueDef();
        final String encoding = stringDataType.getEncoding();
        if (StringUtils.isNotBlank(encoding)) {
          builder.header(HttpHeaders.CONTENT_ENCODING, encoding);
        }
      case OTHER:
        builder.entity(value.getValue());
        builder.type(mimeType);
        break;
    }
  }

  private void processAtomFeedAsRawContent(ResponseBuilder builder) {
    final String toString =
                 new StringBuilder(content.getContentId().toString()).append(':').append(fieldDef.getName()).toString();
    final Date lastModifiedDate = content.getLastModifiedDate();
    Feed feed = getFeed(toString, toString, lastModifiedDate);
    feed.addLink(getLink(getFieldUri(), Link.REL_EDIT, MediaType.APPLICATION_JSON));
    final Field field = content.getField(fieldDef.getName());
    final FieldValue value = field.getValue();
    Collection<Entry> entries = getEntries(value, lastModifiedDate);
    for (Entry entry : entries) {
      feed.addEntry(entry);
    }
    if (!value.getDataType().equals(FieldValueType.COLLECTION)) {
      feed.addLink(getLink(UriBuilder.fromUri(getFieldUri()).path("raw").build(), Link.REL_ALTERNATE,
                           getFieldValueDefaultMimeType(fieldDef.getValueDef()).toString()));
    }
    builder.entity(feed).type(MediaType.APPLICATION_ATOM_XML);
  }

  private URI getFieldUri() {
    return getFieldURI(getAbsoluteURIBuilder(), content, fieldDef);
  }

  public static URI getFieldURI(UriBuilder builder, Content content, FieldDef fieldDef) {
    return getFieldURI(builder, content.getContentId(), fieldDef);
  }

  public static URI getFieldRawURI(UriBuilder builder, Content content, FieldDef fieldDef) {
    return getFieldRawURI(builder, content.getContentId(), fieldDef);
  }

  public static URI getFieldAbsRawURI(UriBuilder builder, Content content, FieldDef fieldDef) {
    return getFieldAbsRawURI(builder, content.getContentId(), fieldDef);
  }

  public static URI getFieldURI(UriBuilder builder, ContentId contentId, FieldDef fieldDef) {
    return UriBuilder.fromUri(ContentResource.getContentUri(builder, contentId)).path("f").path(fieldDef.getName()).
        build();
  }

  public static URI getFieldRawURI(UriBuilder builder, ContentId contentId, FieldDef fieldDef) {
    return UriBuilder.fromUri(ContentResource.getContentUri(builder, contentId)).path("f").path(fieldDef.getName()).path(
        "raw").build();
  }

  public static URI getFieldAbsRawURI(UriBuilder builder, ContentId contentId, FieldDef fieldDef) {
    return UriBuilder.fromUri(ContentResource.getContentUri(builder, contentId)).path("f").path(fieldDef.getName()).path(
        "raw").path("abs").build();
  }

  @Override
  protected String getAuthor() {
    return "Smart CMS";
  }

  private MediaType getUserPreferredType(MediaType defaultType) {
    return getContext().getRequest().selectVariant(getVariants(defaultType, MediaType.APPLICATION_ATOM_XML_TYPE,
                                                               MediaType.APPLICATION_JSON_TYPE,
                                                               MediaType.APPLICATION_XML_TYPE,
                                                               MediaType.TEXT_XML_TYPE,
                                                               MediaType.WILDCARD_TYPE)).getMediaType();
  }

  private List<Variant> getVariants(MediaType... mediaTypes) {
    return Variant.mediaTypes(mediaTypes).add().build();
  }

  class FieldAdapterHelper extends AbstractAdapterHelper<Field, com.smartitengineering.cms.ws.common.domains.Field> {

    @Override
    protected com.smartitengineering.cms.ws.common.domains.Field newTInstance() {
      return new FieldImpl();
    }

    @Override
    protected void mergeFromF2T(Field fromBean, com.smartitengineering.cms.ws.common.domains.Field toBean) {
      ContentResource.getDomainField(getRelativeURIBuilder(), fromBean, ContentResource.getContentUri(
          getRelativeURIBuilder(), content.getContentId()).toASCIIString(), (FieldImpl) toBean);
    }

    @Override
    protected Field convertFromT2F(com.smartitengineering.cms.ws.common.domains.Field toBean) {
      return ContentResource.getField(content.getContentId(), fieldDef, toBean, getResourceContext());
    }
  }
}
