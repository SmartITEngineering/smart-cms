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
import com.smartitengineering.cms.api.content.MutableCompositeFieldValue;
import com.smartitengineering.cms.api.content.MutableContentFieldValue;
import com.smartitengineering.cms.api.content.MutableField;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.factory.content.WriteableContent;
import com.smartitengineering.cms.api.type.CollectionDataType;
import com.smartitengineering.cms.api.type.CompositeDataType;
import com.smartitengineering.cms.api.type.ContentStatus;
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.type.DataType;
import com.smartitengineering.cms.api.type.FieldDef;
import com.smartitengineering.cms.api.type.OtherDataType;
import com.smartitengineering.cms.api.type.RepresentationDef;
import com.smartitengineering.cms.api.type.VariationDef;
import com.smartitengineering.cms.ws.common.domains.CollectionFieldValueImpl;
import com.smartitengineering.cms.ws.common.domains.CompositeFieldValue;
import com.smartitengineering.cms.ws.common.domains.CompositeFieldValueImpl;
import com.smartitengineering.cms.ws.common.domains.ContentImpl;
import com.smartitengineering.cms.ws.common.domains.FieldImpl;
import com.smartitengineering.cms.ws.common.domains.FieldValueImpl;
import com.smartitengineering.cms.ws.common.domains.OtherFieldValueImpl;
import com.smartitengineering.cms.ws.common.utils.SimpleFeedExtensions;
import com.smartitengineering.cms.ws.resources.type.ContentTypeResource;
import com.smartitengineering.util.bean.adapter.AbstractAdapterHelper;
import com.smartitengineering.util.bean.adapter.GenericAdapter;
import com.smartitengineering.util.bean.adapter.GenericAdapterImpl;
import com.smartitengineering.util.rest.atom.server.AbstractResource;
import com.smartitengineering.util.rest.server.ServerResourceInjectables;
import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.core.ResourceContext;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
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
import javax.ws.rs.core.UriBuilderException;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
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
  private final boolean importMode;
  private final static transient Logger LOGGER = LoggerFactory.getLogger(ContentResource.class);
  protected final GenericAdapter<Content, com.smartitengineering.cms.ws.common.domains.Content> adapter;
  public static final String PATH_TO_REP = "r/{repName}";
  public static final String PATH_TO_FIELD = "f/{fieldName}";
  public static final String PATH_TO_REINDEX = "reindex";

  public ContentResource(ServerResourceInjectables injectables, ContentId contentId) {
    this(injectables, contentId, false);
  }

  public ContentResource(ServerResourceInjectables injectables, ContentId contentId, boolean importMode) {
    super(injectables);
    this.importMode = importMode;
    if (contentId == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    this.contentId = contentId;
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Content ID " + contentId);
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
    adapterImpl.setHelper(new ContentAdapterHelper(injectables, importMode));
    adapter = adapterImpl;
  }

  @Path(PATH_TO_FIELD)
  public FieldResource getFieldResource(@PathParam("fieldName") String fieldName) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Trying to get field resource with name " + fieldName);
    }
    FieldDef fieldDef = content.getContentDefinition().getFieldDefs().get(fieldName);
    FieldResource resource = new FieldResource(getInjectables(), content, fieldDef, tag);
    return resource;
  }

  @Path(PATH_TO_REP)
  public RepresentationResource getRepresentationResource(@PathParam("repName") String repName) {
    return new RepresentationResource(getInjectables(), repName, content);
  }

  @Path(PATH_TO_REINDEX)
  public ContentReIndexResource reindex() {
    ContentReIndexResource resource = new ContentReIndexResource(getInjectables());
    resource.setContentId(contentId);
    return resource;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response get() {
    if (content == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    ResponseBuilder builder = getContext().getRequest().evaluatePreconditions(content.getLastModifiedDate(), tag);
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

  @GET
  @Produces(MediaType.APPLICATION_ATOM_XML)
  public Response getAtomFeed() {
    if (content == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    ResponseBuilder builder = getContext().getRequest().evaluatePreconditions(content.getLastModifiedDate(), tag);
    if (builder == null) {
      final String idStr = content.getContentId().toString();
      Feed feed = getFeed(idStr, idStr, content.getLastModifiedDate());
      feed.addLink(getLink(getUriInfo().getRequestUri(), Link.REL_ALTERNATE, MediaType.APPLICATION_JSON));
      feed.addSimpleExtension(SimpleFeedExtensions.WORKSPACE_NAME_SPACE, content.getContentId().getWorkspaceId().
          getGlobalNamespace());
      feed.addSimpleExtension(SimpleFeedExtensions.WORKSPACE_NAME, content.getContentId().getWorkspaceId().getName());
      feed.addSimpleExtension(SimpleFeedExtensions.CONTENT_ID_IN_WORKSPACAE,
                              org.apache.commons.codec.binary.StringUtils.newStringUtf8(content.getContentId().getId()));
      Map<String, Field> fields = content.getFields();
      final String contentUri = ContentResource.getContentUri(getRelativeURIBuilder(), content.getContentId()).
          toASCIIString();
      final ContentType contentDefinition = content.getContentDefinition();
      if (contentDefinition != null && fields != null && !fields.isEmpty()) {
        final Map<String, FieldDef> fieldDefs = contentDefinition.getFieldDefs();
        ObjectMapper objectMapper = new ObjectMapper();
        for (Entry<String, Field> field : fields.entrySet()) {
          final FieldDef def = fieldDefs.get(field.getKey());
          if (def != null) {
            final URI fieldURI = FieldResource.getFieldURI(getRelativeURIBuilder(), content, def);
            org.apache.abdera.model.Entry entry = getEntry(field.getKey(), field.getKey(), content.getLastModifiedDate(),
                                                           getLink(fieldURI, Link.REL_ALTERNATE,
                                                                   MediaType.APPLICATION_JSON));
            FieldImpl jsonField = new FieldImpl();
            ContentResource.getDomainField(getRelativeURIBuilder(), field.getValue(), contentUri, jsonField);
            StringWriter writer = new StringWriter();
            try {
              objectMapper.writeValue(writer, jsonField);
              entry.setContent(writer.toString(), MediaType.APPLICATION_JSON);
              feed.addEntry(entry);
            }
            catch (Exception ex) {
              LOGGER.warn("Error adding content field json", ex);
            }
          }
        }
      }
      builder = Response.ok(feed);
      builder.tag(tag);
      builder.lastModified(getContent().getLastModifiedDate());
      CacheControl control = new CacheControl();
      control.setMaxAge(300);
      builder.cacheControl(control);
    }
    return builder.build();
  }

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public Response post(FormDataMultiPart multiPart) {
    com.smartitengineering.cms.ws.common.domains.Content contentImpl = parseMultipartFormData(multiPart,
                                                                                              getInjectables());
    if (LOGGER.isInfoEnabled()) {
      try {
        ObjectMapper mapper = new ObjectMapper();
        LOGGER.info("Serialized content impl is " + mapper.writeValueAsString(contentImpl));
      }
      catch (Exception ex) {
        LOGGER.warn("Could not output info log ", ex);
      }
    }
    return put(contentImpl, this.content == null ? null : new EntityTag("*"));
  }

  public static com.smartitengineering.cms.ws.common.domains.Content parseMultipartFormData(
      FormDataMultiPart multiPart, ServerResourceInjectables injectables) throws WebApplicationException {
    ContentImpl contentImpl = new ContentImpl();
    contentImpl.setContentTypeUri(multiPart.getField("contentTypeUri").getValue());
    final FormDataBodyPart field = multiPart.getField("parentContentUri");
    if (field != null) {
      contentImpl.setParentContentUri(field.getValue());
    }
    contentImpl.setStatus(multiPart.getField("status").getValue());
    final FormDataBodyPart part = multiPart.getField("private");
    contentImpl.setPrivateContent(part == null || org.apache.commons.lang.StringUtils.isBlank(part.getValue()) ?
        false : (part.getValue().equals("on") ? true : false));
    if (org.apache.commons.lang.StringUtils.isNotBlank(contentImpl.getContentTypeUri())) {
      final ContentType contentType;
      try {
        contentType = getContentTypeResource(contentImpl.getContentTypeUri(), injectables).getType();
      }
      catch (Exception ex) {
        LOGGER.warn("Count not extract content type info!", ex);
        throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).build());
      }
      final Map<String, FieldDef> allDefs = contentType.getFieldDefs();
      final Collection<com.smartitengineering.cms.ws.common.domains.Field> fields = new ArrayList();
      final Map<String, List<FormDataBodyPart>> bodyParts = multiPart.getFields();
      formFields(allDefs, bodyParts, fields);
      contentImpl.getFields().addAll(fields);
    }
    return contentImpl;
  }

  protected static void formFields(final Map<String, FieldDef> allDefs,
                                   final Map<String, List<FormDataBodyPart>> bodyParts,
                                   final Collection<com.smartitengineering.cms.ws.common.domains.Field> fields) {
    for (Entry<String, FieldDef> fieldDef : allDefs.entrySet()) {
      if (bodyParts != null && !bodyParts.isEmpty()) {
        if (LOGGER.isInfoEnabled()) {
          LOGGER.info("Creating field for " + fieldDef.getKey() + " with type " + fieldDef.getValue().getValueDef().
              getType());
        }
        FieldImpl fieldImpl = new FieldImpl();
        fieldImpl.setName(fieldDef.getKey());
        final boolean containsKey = bodyParts.containsKey(fieldDef.getKey());
        switch (fieldDef.getValue().getValueDef().getType()) {
          case COMPOSITE: {
            boolean hasCompositeValue = false;
            Map<String, List<FormDataBodyPart>> composites = new LinkedHashMap<String, List<FormDataBodyPart>>();
            final String prefix = new StringBuilder(fieldDef.getKey()).append('.').toString();
            for (String key : bodyParts.keySet()) {
              if (key.startsWith(prefix)) {
                hasCompositeValue = true;
                composites.put(key.substring(prefix.length()), bodyParts.get(key));
              }
            }
            if (hasCompositeValue) {
              Collection<com.smartitengineering.cms.ws.common.domains.Field> composedFields =
                                                                             new ArrayList<com.smartitengineering.cms.ws.common.domains.Field>();
              CompositeDataType compositeDataType = (CompositeDataType) fieldDef.getValue().getValueDef();
              formFields(compositeDataType.getComposedFieldDefs(), composites, composedFields);
              CompositeFieldValueImpl valueImpl = new CompositeFieldValueImpl();
              valueImpl.setValuesAsCollection(composedFields);
              fieldImpl.setValue(valueImpl);
            }
            break;
          }
          case COLLECTION: {
            CollectionDataType collectionFieldDef = (CollectionDataType) fieldDef.getValue().getValueDef();
            CollectionFieldValueImpl fieldValueImpl = new CollectionFieldValueImpl();
            switch (collectionFieldDef.getItemDataType().getType()) {
              case COMPOSITE: {
                boolean hasCompositeValue = false;
                Map<String, Map<String, List<FormDataBodyPart>>> compositesCollection =
                                                                 new HashMap<String, Map<String, List<FormDataBodyPart>>>();
                final String prefixPattern = new StringBuilder(fieldDef.getKey()).append("\\.([a-z0-9]+)\\..+").
                    toString();
                Pattern pattern = Pattern.compile(prefixPattern);
                for (String key : bodyParts.keySet()) {
                  Matcher matcher = pattern.matcher(key);
                  if (matcher.matches()) {
                    hasCompositeValue = true;
                    final Map<String, List<FormDataBodyPart>> composites;
                    String groupKey = matcher.group(1);
                    if (compositesCollection.containsKey(groupKey)) {
                      composites = compositesCollection.get(groupKey);
                    }
                    else {
                      composites = new LinkedHashMap<String, List<FormDataBodyPart>>();
                      compositesCollection.put(groupKey, composites);
                    }
                    composites.put(key.substring(matcher.end(1) + 1), bodyParts.get(key));
                  }
                }
                if (hasCompositeValue) {
                  CompositeDataType compositeDataType = (CompositeDataType) collectionFieldDef.getItemDataType();
                  for (Entry<String, Map<String, List<FormDataBodyPart>>> cols : compositesCollection.entrySet()) {
                    Collection<com.smartitengineering.cms.ws.common.domains.Field> composedFields =
                                                                                   new ArrayList<com.smartitengineering.cms.ws.common.domains.Field>();
                    formFields(compositeDataType.getComposedFieldDefs(), cols.getValue(), composedFields);
                    CompositeFieldValueImpl valueImpl = new CompositeFieldValueImpl();
                    valueImpl.setValuesAsCollection(composedFields);
                    fieldValueImpl.getValues().add(valueImpl);
                  }
                }
                break;
              }
              default:
                if (containsKey) {
                  for (FormDataBodyPart bodyPart : bodyParts.get(fieldDef.getKey())) {
                    if (bodyPart == null || org.apache.commons.lang.StringUtils.isBlank(bodyPart.getValue())) {
                      continue;
                    }
                    FieldValueImpl valueImpl = addFieldFromBodyPart(bodyPart, collectionFieldDef.getItemDataType());
                    if (valueImpl != null) {
                      fieldValueImpl.getValues().add(valueImpl);
                    }
                  }
                }
            }
            if (fieldValueImpl.getValues().isEmpty()) {
              continue;
            }
            fieldImpl.setValue(fieldValueImpl);
            break;
          }

          case OTHER: {
            if (containsKey) {
              final FormDataBodyPart singleBodyPart = bodyParts.get(fieldDef.getKey()).get(0);
              FieldValueImpl valueImpl = addFieldFromBodyPart(singleBodyPart, fieldDef.getValue().getValueDef());
              fieldImpl.setValue(valueImpl);
            }
            break;
          }
          default: {
            if (containsKey) {
              final FormDataBodyPart singleBodyPart = bodyParts.get(fieldDef.getKey()).get(0);
              if (singleBodyPart == null || org.apache.commons.lang.StringUtils.isBlank(singleBodyPart.getValue())) {
                continue;
              }
              FieldValueImpl valueImpl = addFieldFromBodyPart(singleBodyPart, fieldDef.getValue().getValueDef());
              fieldImpl.setValue(valueImpl);
            }
            break;
          }
        }
        fields.add(fieldImpl);
      }
    }
  }

  protected static ContentTypeResource getContentTypeResource(String uri, ServerResourceInjectables injectables) throws
      ClassCastException, ContainerException {
    final URI checkUri;
    if (uri.startsWith("http:")) {
      checkUri = URI.create(uri);
    }
    else {
      URI absUri = injectables.getUriInfo().getBaseUriBuilder().build();
      checkUri =
      UriBuilder.fromPath(uri).host(absUri.getHost()).port(absUri.getPort()).scheme(absUri.getScheme()).build();
    }
    return injectables.getResourceContext().matchResource(checkUri, ContentTypeResource.class);
  }
  private static final byte[] TMP = new byte[0];

  protected static FieldValueImpl addFieldFromBodyPart(FormDataBodyPart bodyPart, DataType dataType) {
    switch (dataType.getType()) {
      case STRING:
        OtherFieldValueImpl stringFieldValueImpl = new OtherFieldValueImpl();
        stringFieldValueImpl.setType(dataType.getType().name());
        stringFieldValueImpl.setMimeType(bodyPart.getMediaType().toString());
        stringFieldValueImpl.setValue(bodyPart.getValue());
        return stringFieldValueImpl;
      case OTHER:
        OtherFieldValueImpl otherFieldValueImpl = new OtherFieldValueImpl();
        otherFieldValueImpl.setType(dataType.getType().name());
        otherFieldValueImpl.setMimeType(bodyPart.getMediaType().toString());
        if (LOGGER.isInfoEnabled()) {
          LOGGER.info("Body Part " + bodyPart.getMediaType());
        }
        try {
          otherFieldValueImpl.setValue(Base64.encodeBase64String(bodyPart.getValueAs(TMP.getClass())));
          if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Field value " + otherFieldValueImpl.getValue());
          }
        }
        catch (Exception ex) {
          LOGGER.warn("Entity not found!", ex);
        }
        return otherFieldValueImpl;
      case BOOLEAN: {
        FieldValueImpl valueImpl = new FieldValueImpl();
        valueImpl.setType(dataType.getType().name());
        if (org.apache.commons.lang.StringUtils.isNotBlank(bodyPart.getValue())) {
          valueImpl.setValue(bodyPart.getValue().equals("on") ? "true" : "false");
        }
        else {
          valueImpl.setValue(bodyPart.getValue());
        }
        return valueImpl;
      }
      default: {
        FieldValueImpl valueImpl = new FieldValueImpl();
        valueImpl.setType(dataType.getType().name());
        valueImpl.setValue(bodyPart.getValue());
        return valueImpl;
      }
    }
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
      LOGGER.warn("Could not convert to content!", ex);
      return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).type(MediaType.TEXT_PLAIN).build();
    }
    final WriteableContent writeableContent;
    if (this.content == null) {
      //Create new content
      writeableContent = SmartContentAPI.getInstance().getContentLoader().getWritableContent(newContent, importMode);
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
      writeableContent = SmartContentAPI.getInstance().getContentLoader().getWritableContent(this.content, importMode);
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
      LOGGER.error("Could save/update content!", ex);
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
        LOGGER.error("Could not delete due to server error!", ex);
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

  @Override
  protected String getAuthor() {
    return "Smart CMS";
  }

  public static class ContentAdapterHelper extends AbstractAdapterHelper<Content, com.smartitengineering.cms.ws.common.domains.Content> {

    private final ServerResourceInjectables injectables;
    private final boolean importMode;

    public ContentAdapterHelper(ServerResourceInjectables injectables, boolean importMode) {
      this.injectables = injectables;
      this.importMode = importMode;
    }

    @Override
    protected com.smartitengineering.cms.ws.common.domains.Content newTInstance() {
      return new ContentImpl();
    }

    protected UriBuilder getRelativeURIBuilder() {
      return UriBuilder.fromPath(injectables.getUriInfo().getBaseUriBuilder().build().getPath());
    }

    protected UriBuilder getAbsoluteURIBuilder() {
      return injectables.getUriInfo().getBaseUriBuilder();
    }

    @Override
    protected void mergeFromF2T(Content fromBean, com.smartitengineering.cms.ws.common.domains.Content toBean) {
      ContentImpl contentImpl = (ContentImpl) toBean;
      contentImpl.setPrivateContent(fromBean.isPrivate());
      ContentType type = fromBean.getContentDefinition();
      if (fromBean.getContentId() != null) {
        contentImpl.setContentId(fromBean.getContentId().toString());
        contentImpl.setSelfUri(ContentResource.getContentUri(getRelativeURIBuilder(), fromBean.getContentId()).
            toASCIIString());
        contentImpl.setReindexUri(new StringBuilder(contentImpl.getSelfUri()).append('/').append(PATH_TO_REINDEX).
            toString());
      }
      contentImpl.setContentTypeUri(
          ContentTypeResource.getContentTypeRelativeURI(injectables.getUriInfo(), type.getContentTypeID()).toASCIIString());
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
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("FIELDS: " + fields);
      }
      String contentUri =
             ContentResource.getContentUri(getRelativeURIBuilder(), fromBean.getContentId()).toASCIIString();
      for (FieldDef fieldDef : type.getFieldDefs().values()) {
        FieldImpl fieldImpl = convertToDomainField(getRelativeURIBuilder(), fieldDef, fields, contentUri);
        if (fieldImpl != null) {
          contentImpl.getFields().add(fieldImpl);
        }
      }
      Collection<RepresentationDef> defs = type.getRepresentationDefs().values();
      String currentContext = new StringBuilder(contentUri).append("/r/").toString();
      Map<String, String> repUris = contentImpl.getRepresentations();
      Map<String, String> repNames = contentImpl.getRepresentationsByName();
      for (RepresentationDef def : defs) {
        final String uri = new StringBuilder(currentContext).append(def.getName()).toString();
        repUris.put(uri, def.getMIMEType());
        repNames.put(def.getName(), uri);
      }
    }

    @Override
    protected Content convertFromT2F(com.smartitengineering.cms.ws.common.domains.Content toBean) {
      final ContentType contentType;
      try {
        ContentTypeResource resource = getContentTypeResource(toBean.getContentTypeUri(), injectables);
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
      writeableContent.setPrivate(toBean.isPrivateContent());
      ContentId parentContentId;
      final String parentContentUri = toBean.getParentContentUri();
      if (org.apache.commons.lang.StringUtils.isNotBlank(parentContentUri)) {
        try {
          final ContentResource resource = getContentResource(parentContentUri, getAbsoluteURIBuilder(),
                                                              injectables.getResourceContext());
          if (resource == null) {
            throw new NullPointerException("No such parent content!");
          }
          if (!importMode && resource.getContent() == null) {
            throw new NullPointerException("No such parent content!");
          }
          parentContentId = resource.getContentId();
        }
        catch (Exception ex) {
          throw new RuntimeException(ex.getMessage(), ex);
        }
        writeableContent.setParentId(parentContentId);
      }
      for (com.smartitengineering.cms.ws.common.domains.Field field : toBean.getFields()) {
        MutableField mutableField = getField(writeableContent.getContentId(), contentType.getFieldDefs().get(
            field.getName()), field, injectables.getResourceContext(), getAbsoluteURIBuilder(), importMode);
        writeableContent.setField(mutableField);
      }
      return writeableContent;
    }
  }

  protected static FieldValue getFieldValue(final DataType dataType,
                                            com.smartitengineering.cms.ws.common.domains.FieldValue value,
                                            ResourceContext context, UriBuilder absBuilder, boolean importMode) {
    FieldValue fieldValue;
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Parsing value as " + dataType.getType().name());
    }
    switch (dataType.getType()) {
      case COMPOSITE: {
        LOGGER.info("Parsing as composite");
        MutableCompositeFieldValue compositeFieldValue = SmartContentAPI.getInstance().getContentLoader().
            createCompositeFieldValue();
        CompositeFieldValue compositeValue = (CompositeFieldValue) value;
        CompositeDataType compositeDataType = (CompositeDataType) dataType;
        Collection<Field> composedOf = new ArrayList<Field>(compositeValue.getValues().size());
        Map<String, FieldDef> fieldDefs = compositeDataType.getComposedFieldDefs();
        for (Entry<String, com.smartitengineering.cms.ws.common.domains.Field> field : compositeValue.getValues().
            entrySet()) {
          FieldDef def = fieldDefs.get(field.getKey());
          if (def == null) {
            throw new NullPointerException("No such field definition within composed field");
          }
          MutableField mutableField = getField(null, def, field.getValue(), context, absBuilder);
          composedOf.add(mutableField);
        }
        compositeFieldValue.setValue(composedOf);
        fieldValue = compositeFieldValue;
        break;
      }
      case COLLECTION:
        LOGGER.info("Parsing as collection");
        MutableCollectionFieldValue collectionFieldValue = SmartContentAPI.getInstance().getContentLoader().
            createCollectionFieldValue();
        CollectionDataType collectionDataType = (CollectionDataType) dataType;
        com.smartitengineering.cms.ws.common.domains.CollectionFieldValue cFieldValue =
                                                                          (com.smartitengineering.cms.ws.common.domains.CollectionFieldValue) value;
        ArrayList<FieldValue> list = new ArrayList<FieldValue>(cFieldValue.getValues().size());
        for (com.smartitengineering.cms.ws.common.domains.FieldValue v : cFieldValue.getValues()) {
          list.add(getFieldValue(collectionDataType.getItemDataType(), v, context, absBuilder, importMode));
        }
        collectionFieldValue.setValue(list);
        fieldValue = collectionFieldValue;
        break;
      case CONTENT:
        LOGGER.info("Parsing string as Content!");
        MutableContentFieldValue contentFieldValue = SmartContentAPI.getInstance().getContentLoader().
            createContentFieldValue();
        String contentUrl = value.getValue();
        if (LOGGER.isInfoEnabled()) {
          LOGGER.info("URL to content is " + contentUrl);
        }
        try {
          ContentResource resource = getContentResource(contentUrl, absBuilder, context);
          if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Resource " + resource);
          }
          if (resource == null) {
            throw new NullPointerException();
          }
          if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Content ID " + resource.getContentId());
          }
          if (!importMode && resource.getContent() == null) {
            throw new NullPointerException("No such content! " + resource.getContentId());
          }
          contentFieldValue.setValue(resource.getContentId());
        }
        catch (Exception ex) {
          LOGGER.warn("Error getting content!", ex);
          throw new IllegalArgumentException("Invalid Content URI!");
        }
        fieldValue = contentFieldValue;
        break;
      default:
        LOGGER.info("Parsing as default or all other than content and collection");
        fieldValue = SmartContentAPI.getInstance().getContentLoader().getValueFor(value.getValue(), dataType);
    }
    return fieldValue;
  }

  protected static MutableField getField(final ContentId contentId, final FieldDef fieldDef,
                                         com.smartitengineering.cms.ws.common.domains.Field field,
                                         ResourceContext context, UriBuilder absBuilder) {
    return getField(contentId, fieldDef, field, context, absBuilder, false);
  }

  protected static MutableField getField(final ContentId contentId, final FieldDef fieldDef,
                                         com.smartitengineering.cms.ws.common.domains.Field field,
                                         ResourceContext context, UriBuilder absBuilder, boolean importMode) throws
      IllegalArgumentException {
    if (fieldDef == null) {
      throw new IllegalArgumentException("No field in content type with name " + field.getName());
    }
    final DataType dataType = fieldDef.getValueDef();
    LOGGER.info("Working with field " + field.getName() + " of type " + dataType.getType().name() + " with value " +
        field.getValue());
    if (field.getValue() != null && org.apache.commons.lang.StringUtils.isNotBlank(field.getValue().getType()) &&
        !org.apache.commons.lang.StringUtils.equalsIgnoreCase(dataType.getType().name(), field.getValue().getType())) {
      throw new IllegalArgumentException("Type mismatch! NOTE: type of values in field is optional in this case. " +
          "Field is " + field.getName() + " - " + dataType.getType().name() + " " + field.getValue().getType());
    }
    final MutableField mutableField =
                       SmartContentAPI.getInstance().getContentLoader().createMutableField(contentId, fieldDef);
    final FieldValue fieldValue;
    if (field.getValue() != null) {
      fieldValue = getFieldValue(dataType, field.getValue(), context, absBuilder, importMode);
    }
    else {
      fieldValue = null;
    }
    mutableField.setValue(fieldValue);
    return mutableField;
  }

  protected static ContentResource getContentResource(String contentUrl, UriBuilder absBuilder, ResourceContext context)
      throws ContainerException, IllegalArgumentException, ClassCastException, UriBuilderException {
    final URI uri;
    if (contentUrl.startsWith("http:")) {
      uri = URI.create(contentUrl);
    }
    else {
      URI absUri = absBuilder.build();
      uri =
      UriBuilder.fromPath(contentUrl).host(absUri.getHost()).port(absUri.getPort()).scheme(absUri.getScheme()).build();
    }
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("URI to content is " + uri);
    }
    ContentResource resource = context.matchResource(uri, ContentResource.class);
    return resource;
  }

  protected static void getDomainField(UriBuilder builder, Field field, String contentUri, FieldImpl fieldImpl) {

    if (field != null && org.apache.commons.lang.StringUtils.isNotBlank(field.getName()) && field.getValue() != null) {
      fieldImpl.setName(field.getName());
      final FieldValueImpl value;
      final FieldValue contentFieldValue = field.getValue();
      final DataType valueDef = field.getFieldDef().getValueDef();
      value = getFieldvalue(builder, valueDef, contentFieldValue);
      if (org.apache.commons.lang.StringUtils.isNotBlank(contentUri)) {
        Collection<VariationDef> defs = field.getFieldDef().getVariations().values();
        Map<String, String> variations = fieldImpl.getVariations();
        Map<String, String> variationsByNames = fieldImpl.getVariationsByNames();
        final String fieldUri = new StringBuilder(contentUri).append("/f/").append(field.getName()).toString();
        fieldImpl.setFieldUri(fieldUri);
        fieldImpl.setFieldRawContentUri(new StringBuilder(fieldUri).append("/raw").toString());
        for (VariationDef def : defs) {
          final String uri = new StringBuilder(fieldUri).append("/v/").append(def.getName()).toString();
          variations.put(uri, def.getMIMEType());
          variationsByNames.put(def.getName(), uri);
        }
      }
      fieldImpl.setValue(value);
    }
    else {
      if (LOGGER.isWarnEnabled()) {
        String fieldError = ((field == null) ? "NULL Field" : ((org.apache.commons.lang.StringUtils.isBlank(field.
                                                                getName()) ? "NULL Name" : ((field.getValue() == null) ?
                                                                                            "NULL Value" :
                                                                                            "Indeterministic error!"))));
        if (!fieldError.contains("Name")) {
          fieldError = new StringBuilder(fieldError).append(' ').append(field.getName()).toString();
        }
        LOGGER.warn("Invalid field! " + fieldError);
      }
    }
  }

  private static FieldValueImpl getFieldvalue(final UriBuilder builder, final DataType valueDef,
                                              final FieldValue contentFieldValue) {

    final FieldValueImpl value;
    switch (valueDef.getType()) {
      case CONTENT: {
        FieldValueImpl valueImpl = new FieldValueImpl();
        valueImpl.setValue(ContentResource.getContentUri(builder, ((ContentFieldValue) contentFieldValue).getValue()).
            toASCIIString());
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
          valueImpl.getValues().add(getFieldvalue(builder.clone(), itemDataType, contentValue));
        }
        value = valueImpl;
        break;
      }
      case COMPOSITE: {
        CompositeFieldValueImpl valueImpl = new CompositeFieldValueImpl();
        Map<String, Field> composedFields =
                           ((com.smartitengineering.cms.api.content.CompositeFieldValue) contentFieldValue).
            getValueAsMap();
        final Map<String, FieldDef> composedFieldDefs = ((CompositeDataType) valueDef).getComposedFieldDefs();
        for (Entry<String, Field> composeeField : composedFields.entrySet()) {
          valueImpl.getValues().put(composeeField.getKey(),
                                    convertToDomainField(builder, composedFieldDefs.get(composeeField.getKey()),
                                                         composedFields, ""));
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
    value.setType(valueDef.getType().toString());
    return value;
  }

  protected static FieldImpl convertToDomainField(UriBuilder builder, FieldDef fieldDef,
                                                  Map<String, Field> fields, String contentUri) {
    final String fieldName = fieldDef.getName();
    Field field = fields.get(fieldName);
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Field " + field);
      if (field != null) {
        LOGGER.info("Converting field " + field.getName() + " with value " + field.getValue() +
            " and URI " + contentUri);
      }
    }
    FieldImpl fieldImpl;
    if (field != null) {
      fieldImpl = new FieldImpl();
      fieldImpl.setName(fieldName);
      getDomainField(builder, field, contentUri, fieldImpl);

    }
    else {
      fieldImpl = null;
    }
    return fieldImpl;
  }

  protected ContentId getContentId() {
    return contentId;
  }
}
