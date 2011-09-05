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

import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.factory.type.WritableContentType;
import com.smartitengineering.cms.api.type.CollectionDataType;
import com.smartitengineering.cms.api.type.CompositeDataType;
import com.smartitengineering.cms.api.type.ContentDataType;
import com.smartitengineering.cms.api.type.ContentStatus;
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.api.type.DataType;
import com.smartitengineering.cms.api.type.EnumDataType;
import com.smartitengineering.cms.api.type.FieldDef;
import com.smartitengineering.cms.api.type.OtherDataType;
import com.smartitengineering.cms.ws.common.domains.CollectionFieldDefImpl;
import com.smartitengineering.cms.ws.common.domains.CompositeFieldDefImpl;
import com.smartitengineering.cms.ws.common.domains.ContentFieldDefImpl;
import com.smartitengineering.cms.ws.common.domains.EnumFieldDefImpl;
import com.smartitengineering.cms.ws.common.domains.FieldDefImpl;
import com.smartitengineering.cms.ws.common.domains.OtherFieldDefImpl;
import com.smartitengineering.cms.ws.common.utils.SimpleFeedExtensions;
import com.smartitengineering.cms.ws.resources.content.searcher.ContentSearcherResource;
import com.smartitengineering.util.rest.atom.server.AbstractResource;
import com.smartitengineering.util.rest.server.ServerResourceInjectables;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
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
  public static transient final String PATH_TO_SEARCH = "search";
  public static final String PATH_TO_REINDEX = "reindex";
  public static final String PATH_TO_CHILDREN = "children";
  public static final String PATH_TO_INSTANCES = "instances";
  public static final String PATH_TO_STATUSES = "statuses";

  public ContentTypeResource(ServerResourceInjectables injectables, ContentType type) {
    super(injectables);
    if (type == null) {
      throw new WebApplicationException(Response.status(Status.NOT_FOUND).build());
    }
    this.type = type;
    lastModified = type.getLastModifiedDate();
    tag = new EntityTag(type.getEntityTagValue());
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
      builder.header(HttpHeaders.VARY, HttpHeaders.ACCEPT);
    }
    return builder.build();
  }

  @Path(PATH_TO_SEARCH)
  public ContentSearcherResource search() {
    ContentSearcherResource resource = new ContentSearcherResource(getInjectables());
    resource.setContentTypeId(Collections.singletonList(type.getContentTypeID().toString()));
    return resource;
  }

  @Path(PATH_TO_REINDEX)
  public ContentTypeReIndexResource reIndex() {
    ContentTypeReIndexResource resource = new ContentTypeReIndexResource(getInjectables());
    resource.setTypeId(type.getContentTypeID());
    return resource;
  }

  @GET
  @Produces(MediaType.APPLICATION_ATOM_XML)
  public Response getAtomFeed() {
    ResponseBuilder builder = getContext().getRequest().evaluatePreconditions(lastModified, tag);
    if (builder == null) {
      final String id = type.getContentTypeID().toString();
      Feed feed = getFeed(id, StringUtils.isBlank(type.getDisplayName()) ? id : type.getDisplayName(), lastModified);
      if (type.getParent() != null) {
        feed.addLink(getLink(getContentTypeRelativeURI(getUriInfo(), type.getParent()), "parent",
                             MediaType.APPLICATION_ATOM_XML));
      }
      feed.addLink(getLink(getRelativeURIBuilder().path(ContentTypesResource.class).path(
          ContentTypesResource.PATH_TO_CONTENT_TYPE).path(PATH_TO_SEARCH).build(type.getContentTypeID().getWorkspace().
          getGlobalNamespace(), type.getContentTypeID().getWorkspace().getName(), type.getContentTypeID().getNamespace(),
                                                                                type.getContentTypeID().getName()),
                           "search",
                           com.smartitengineering.util.opensearch.jaxrs.MediaType.APPLICATION_OPENSEARCHDESCRIPTION_XML));
      feed.addLink(getLink(getRelativeURIBuilder().path(ContentTypesResource.class).path(
          ContentTypesResource.PATH_TO_CONTENT_TYPE).path(PATH_TO_REINDEX).build(type.getContentTypeID().getWorkspace().
          getGlobalNamespace(), type.getContentTypeID().getWorkspace().getName(), type.getContentTypeID().getNamespace(),
                                                                                 type.getContentTypeID().getName()),
                           PATH_TO_REINDEX, MediaType.TEXT_PLAIN));
      final URI childrenUri = getRelativeURIBuilder().path(ContentTypesResource.class).path(
          ContentTypesResource.PATH_TO_CONTENT_TYPE).path(PATH_TO_CHILDREN).build(type.getContentTypeID().getWorkspace().
          getGlobalNamespace(), type.getContentTypeID().getWorkspace().getName(), type.getContentTypeID().getNamespace(),
                                                                                  type.getContentTypeID().getName());
      final URI instancesUri = getRelativeURIBuilder().path(ContentTypesResource.class).path(
          ContentTypesResource.PATH_TO_CONTENT_TYPE).path(PATH_TO_INSTANCES).build(type.getContentTypeID().getWorkspace().
          getGlobalNamespace(), type.getContentTypeID().getWorkspace().getName(), type.getContentTypeID().getNamespace(),
                                                                                   type.getContentTypeID().getName());

      feed.addLink(getLink(childrenUri, PATH_TO_CHILDREN, MediaType.APPLICATION_ATOM_XML));
      feed.addLink(getLink(instancesUri, PATH_TO_INSTANCES, MediaType.APPLICATION_ATOM_XML));
      feed.addLink(getLink(childrenUri, PATH_TO_CHILDREN,
                           com.smartitengineering.util.opensearch.jaxrs.MediaType.APPLICATION_OPENSEARCHDESCRIPTION_XML));
      feed.addLink(getLink(instancesUri, PATH_TO_INSTANCES,
                           com.smartitengineering.util.opensearch.jaxrs.MediaType.APPLICATION_OPENSEARCHDESCRIPTION_XML));
      feed.addLink(getLink(getUriInfo().getRequestUri(), Link.REL_ALTERNATE, MediaType.APPLICATION_XML));
      final URI statusesUri = getRelativeURIBuilder().path(ContentTypesResource.class).path(
          ContentTypesResource.PATH_TO_CONTENT_TYPE).path(PATH_TO_STATUSES).build(type.getContentTypeID().getWorkspace().
          getGlobalNamespace(), type.getContentTypeID().getWorkspace().getName(), type.getContentTypeID().getNamespace(),
                                                                                  type.getContentTypeID().getName());
      feed.addLink(getLink(statusesUri, "statuses", MediaType.APPLICATION_ATOM_XML));
      Map<String, FieldDef> fieldDefs = type.getFieldDefs();
      feed.addSimpleExtension(SimpleFeedExtensions.WORKSPACE_NAME_SPACE, type.getContentTypeID().getWorkspace().
          getGlobalNamespace());
      feed.addSimpleExtension(SimpleFeedExtensions.WORKSPACE_NAME, type.getContentTypeID().getWorkspace().getName());
      feed.addSimpleExtension(SimpleFeedExtensions.CONTENT_TYPE_NAME_SPACE, type.getContentTypeID().getNamespace());
      feed.addSimpleExtension(SimpleFeedExtensions.CONTENT_TYPE_NAME, type.getContentTypeID().getName());
      if (fieldDefs != null && !fieldDefs.isEmpty()) {
        ObjectMapper objectMapper = new ObjectMapper();
        for (Entry<String, FieldDef> fieldDef : fieldDefs.entrySet()) {
          org.apache.abdera.model.Entry entry = getEntry(fieldDef.getKey(), fieldDef.getValue().getDisplayName(),
                                                         lastModified);
          StringWriter writer = new StringWriter();
          final FieldDefImpl def;
          def = convertFieldDef(fieldDef.getValue());
          try {
            objectMapper.writeValue(writer, def);
            entry.setContent(writer.toString(), MediaType.APPLICATION_JSON);
            feed.addEntry(entry);
          }
          catch (Exception ex) {
            logger.error("Could not add field entry", ex);
          }
        }
      }
      builder = Response.ok(feed);
      builder.lastModified(lastModified);
      builder.tag(tag);
      builder.header(HttpHeaders.VARY, HttpHeaders.ACCEPT);
    }
    return builder.build();
  }

  protected FieldDefImpl convertFieldDef(FieldDef fieldDef) {
    final FieldDefImpl def;
    final DataType dataType = fieldDef.getValueDef();
    switch (dataType.getType()) {
      case OTHER:
      case STRING:
        def = new OtherFieldDefImpl();
        ((OtherFieldDefImpl) def).setMimeType(((OtherDataType) fieldDef.getValueDef()).getMIMEType());
        break;
      case COLLECTION:
        def = new CollectionFieldDefImpl();
        CollectionDataType collectionDataType = ((CollectionDataType) fieldDef.getValueDef());
        FieldDefImpl itemFieldDef;
        switch (collectionDataType.getItemDataType().getType()) {
          case OTHER:
          case STRING:
            itemFieldDef = new OtherFieldDefImpl();
            ((OtherFieldDefImpl) itemFieldDef).setMimeType(((OtherDataType) collectionDataType.getItemDataType()).
                getMIMEType());
            break;
          case CONTENT:
            itemFieldDef = new ContentFieldDefImpl();
            if (logger.isInfoEnabled()) {
              logger.info("Content inner data type (" + fieldDef.getName() + "): " +
                  collectionDataType.getItemDataType());
              if (collectionDataType.getItemDataType() != null) {
                logger.info("Content inner data type type def (" + fieldDef.getName() + "): " +
                    ((ContentDataType) collectionDataType.getItemDataType()).getTypeDef());
              }
            }
            ((ContentFieldDefImpl) itemFieldDef).setInstanceOfId(((ContentDataType) collectionDataType.getItemDataType()).
                getTypeDef().toString());
            break;
          case ENUM:
            EnumDataType enumDataType = (EnumDataType) collectionDataType.getItemDataType();
            EnumFieldDefImpl enumFieldDefImpl = new EnumFieldDefImpl();
            itemFieldDef = enumFieldDefImpl;
            if (logger.isInfoEnabled()) {
              logger.info("Enum choices " + enumDataType.getChoices());
            }
            enumFieldDefImpl.setChoices(enumDataType.getChoices());
            break;
          case COMPOSITE: {
            itemFieldDef = new CompositeFieldDefImpl();
            CompositeFieldDefImpl defImpl = (CompositeFieldDefImpl) itemFieldDef;
            CompositeDataType compositeDataType = (CompositeDataType) collectionDataType.getItemDataType();
            final Collection<FieldDef> composition = compositeDataType.getComposition();
            if (composition != null && !composition.isEmpty()) {
              List<com.smartitengineering.cms.ws.common.domains.FieldDef> composedFields =
                                                                          new ArrayList<com.smartitengineering.cms.ws.common.domains.FieldDef>(composition.
                  size());
              for (FieldDef composedField : composition) {
                composedFields.add(convertFieldDef(composedField));
              }
              defImpl.setComposedFields(composedFields);
            }
            break;
          }
          default:
            itemFieldDef = new FieldDefImpl();
            break;
        }
        itemFieldDef.setType(collectionDataType.getItemDataType().getType().name());
        itemFieldDef.setName(fieldDef.getName());
        itemFieldDef.setRequired(fieldDef.isRequired());
        ((CollectionFieldDefImpl) def).setItemDef(itemFieldDef);
        ((CollectionFieldDefImpl) def).setMaxSize(collectionDataType.getMaxSize());
        ((CollectionFieldDefImpl) def).setMinSize(collectionDataType.getMinSize());
        break;
      case CONTENT:
        def = new ContentFieldDefImpl();
        ((ContentFieldDefImpl) def).setInstanceOfId(((ContentDataType) fieldDef.getValueDef()).getTypeDef().
            toString());
        break;
      case ENUM: {
        EnumDataType enumDataType = (EnumDataType) fieldDef.getValueDef();
        EnumFieldDefImpl enumFieldDefImpl = new EnumFieldDefImpl();
        def = enumFieldDefImpl;
        if (logger.isInfoEnabled()) {
          logger.info("Enum choices " + enumDataType.getChoices());
        }
        enumFieldDefImpl.setChoices(enumDataType.getChoices());
        break;
      }
      case COMPOSITE: {
        def = new CompositeFieldDefImpl();
        CompositeFieldDefImpl defImpl = (CompositeFieldDefImpl) def;
        CompositeDataType compositeDataType = (CompositeDataType) dataType;
        final Collection<FieldDef> composition = compositeDataType.getComposition();
        if (composition != null && !composition.isEmpty()) {
          List<com.smartitengineering.cms.ws.common.domains.FieldDef> composedFields =
                                                                      new ArrayList<com.smartitengineering.cms.ws.common.domains.FieldDef>(composition.
              size());
          for (FieldDef composedField : composition) {
            composedFields.add(convertFieldDef(composedField));
          }
          defImpl.setComposedFields(composedFields);
        }
        break;
      }
      default:
        def = new FieldDefImpl();
        break;
    }
    def.setName(fieldDef.getName());
    def.setRequired(fieldDef.isRequired());
    def.setParameters(fieldDef.getParameters());
    def.setType(fieldDef.getValueDef().getType().name());
    return def;
  }

  @DELETE
  public Response delete(@HeaderParam(HttpHeaders.IF_MATCH) EntityTag etag) {
    if (logger.isDebugEnabled()) {
      logger.debug("Delete content type with id " + type.getContentTypeID().toString() + " with last-modified " +
          lastModified + " " + tag.getValue());
    }
    if (etag == null) {
      return Response.status(Response.Status.PRECONDITION_FAILED).build();
    }
    ResponseBuilder builder = getContext().getRequest().evaluatePreconditions(lastModified, tag);
    if (builder != null) {
      return builder.build();
    }
    WritableContentType contentType = SmartContentAPI.getInstance().getContentTypeLoader().getWritableContentType(type);
    try {
      contentType.delete();
      return Response.ok().build();
    }
    catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @Path(PATH_TO_CHILDREN)
  public ContentTypeSearcherResource getChildren() {
    ContentTypeSearcherResource resource = getResourceContext().getResource(ContentTypeSearcherResource.class);
    resource.setWorkspaceId(type.getContentTypeID().getWorkspace().toString());
    resource.setParentId(type.getContentTypeID().toString());
    resource.setIncludeFriendlies(false);
    return resource;
  }

  @Path(PATH_TO_INSTANCES)
  public ContentTypeSearcherResource getInstances() {
    ContentTypeSearcherResource resource = getResourceContext().getResource(ContentTypeSearcherResource.class);
    resource.setWorkspaceId(type.getContentTypeID().getWorkspace().toString());
    resource.setContentTypeId(Collections.singletonList(type.getContentTypeID().toString()));
    resource.setIncludeFriendlies(false);
    return resource;
  }

  @GET
  @Path(PATH_TO_STATUSES)
  @Produces(MediaType.APPLICATION_ATOM_XML)
  public Response getStatuses() {
    ResponseBuilder builder = getContext().getRequest().evaluatePreconditions(lastModified, tag);
    if (builder == null) {
      Map<String, ContentStatus> statuses = type.getStatuses();
      Feed feed = getFeed("Statuses", lastModified);
      for (String name : statuses.keySet()) {
        if (StringUtils.isNotBlank(name)) {
          if (logger.isInfoEnabled()) {
            logger.info("Adding status " + name);
          }
          org.apache.abdera.model.Entry entry = getEntry(name, name, lastModified);
          entry.setContent(name);
          feed.addEntry(entry);
        }
      }
      builder = Response.ok(feed);
      builder.lastModified(lastModified);
      builder.tag(tag);
      builder.header(HttpHeaders.VARY, HttpHeaders.ACCEPT);
    }
    return builder.build();
  }

  public static URI getContentTypeRelativeURI(UriInfo info, ContentTypeId typeId) {
    UriBuilder builder = UriBuilder.fromPath(info.getBaseUri().getPath());
    builder.path(ContentTypesResource.class);
    builder.path(ContentTypesResource.PATH_TO_CONTENT_TYPE);
    return builder.build(typeId.getWorkspace().getGlobalNamespace(), typeId.getWorkspace().getName(), typeId.
        getNamespace(), typeId.getName());
  }

  public ContentType getType() {
    return type;
  }

  @Override
  protected String getAuthor() {
    return "Smart CMS";
  }
}
