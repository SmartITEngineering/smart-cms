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
package com.smartitengineering.cms.api.impl.type;

import com.smartitengineering.cms.api.WorkspaceId;
import com.smartitengineering.cms.api.common.MediaType;
import com.smartitengineering.cms.api.type.CollectionDataType;
import com.smartitengineering.cms.api.type.ContentDataType;
import com.smartitengineering.cms.api.type.ContentStatus;
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.api.type.ContentTypeLoader;
import com.smartitengineering.cms.api.type.FieldDef;
import com.smartitengineering.cms.api.type.MutableCollectionDataType;
import com.smartitengineering.cms.api.type.MutableContentDataType;
import com.smartitengineering.cms.api.type.MutableContentStatus;
import com.smartitengineering.cms.api.type.MutableContentType;
import com.smartitengineering.cms.api.type.MutableContentTypeId;
import com.smartitengineering.cms.api.type.MutableFieldDef;
import com.smartitengineering.cms.spi.SmartSPI;
import com.smartitengineering.cms.spi.type.ContentTypeDefinitionParser;
import com.smartitengineering.cms.spi.type.TypeValidator;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author kaisar
 */
public class ContentTypeLoaderImpl implements ContentTypeLoader {

  @Override
  public ContentType loadContentType(ContentTypeId contentTypeID) throws NullPointerException {
    return SmartSPI.getInstance().getContentTypeReader().readContentTypeFromPersistentStorage(contentTypeID);
  }

  @Override
  public Collection<MutableContentType> parseContentTypes(InputStream contentTypeDefinitionStream, MediaType mediaType)
      throws NullPointerException, IOException {
    TypeValidator validator = SmartSPI.getInstance().getTypeValidators().getValidators().get(mediaType);
    ContentTypeDefinitionParser parser = SmartSPI.getInstance().getContentTypeDefinitionParsers().getParsers().get(
        mediaType);
    if (validator == null || parser == null) {
      throw new IOException("Media type " + mediaType.toString() + " is not supported!");
    }
    if (!contentTypeDefinitionStream.markSupported()) {
      contentTypeDefinitionStream = new BufferedInputStream(contentTypeDefinitionStream);
    }
    try {
      if (!validator.isValid(contentTypeDefinitionStream)) {
        throw new IOException("Content does not meet definition!");
      }
      return parser.parseStream(contentTypeDefinitionStream);
    }
    catch (Exception ex) {
      throw new IOException(ex);
    }
  }

  @Override
  public MutableContentDataType getMutableContentDataType(ContentDataType contentDataType) {
    if (contentDataType.getTypeDef() != null) {
      ContentDataTypeImpl contentDataTypeImpl = new ContentDataTypeImpl();
      contentDataTypeImpl.setTypeDef(contentDataType.getTypeDef());
      contentDataTypeImpl.setBiBidirectionalFieldName(contentDataType.getBidirectionalFieldName());
      return contentDataTypeImpl;
    }
    else {
      throw new IllegalArgumentException("Argument can not be null.");
    }
  }

  @Override
  public MutableCollectionDataType getMutableCollectionDataType(CollectionDataType collectionDataType) {
    if (collectionDataType.getItemDataType() != null && collectionDataType.getMinSize() >= 0 && collectionDataType.
        getMaxSize() >= collectionDataType.getMinSize()) {
      CollectionDataTypeImpl collectionDataTypeImpl = new CollectionDataTypeImpl();
      collectionDataTypeImpl.setItemDataType(collectionDataType.getItemDataType());
      collectionDataTypeImpl.setMaxSize(collectionDataType.getMaxSize());
      collectionDataTypeImpl.setMinSeize(collectionDataType.getMinSize());
      return collectionDataTypeImpl;
    }
    else {
      throw new IllegalArgumentException("Argument can not be null or min size has to be non-negative or max size can" +
          " not be smaller than min zie.");
    }
  }

  @Override
  public MutableContentStatus getMutableContentStatus(ContentStatus contentStatus) {
    ContentStatusImpl contentStatusImpl = new ContentStatusImpl();
    contentStatusImpl.setContentTypeID(contentStatus.getContentType());
    contentStatusImpl.setId(contentStatus.getId());
    contentStatusImpl.setName(contentStatus.getName());
    return contentStatusImpl;
  }

  @Override
  public MutableContentTypeId getMutableContentTypeID(ContentTypeId contentTypeID) {
    ContentTypeIdImpl contentTypeIdImpl = new ContentTypeIdImpl();
    contentTypeIdImpl.setName(contentTypeID.getName());
    contentTypeIdImpl.setNamespace(contentTypeID.getNamespace());
    contentTypeIdImpl.setWorkspace(contentTypeID.getWorkspace());
    return contentTypeIdImpl;
  }

  @Override
  public MutableFieldDef getMutableFieldDef(FieldDef fieldDef) {
    if (fieldDef != null) {
      FieldDefImpl fieldDefImpl = new FieldDefImpl();
      fieldDefImpl.setName(fieldDef.getName());
      fieldDefImpl.setSearchDefinition(fieldDef.getSearchDefinition());
      fieldDefImpl.setValueDef(fieldDef.getValueDef());
      fieldDefImpl.setVariations(fieldDef.getVariations());
      fieldDefImpl.setFieldStandaloneUpdateAble(true);
      fieldDefImpl.setRequired(true);
      return fieldDefImpl;
    }
    else {
      throw new IllegalArgumentException("Argument can not be null.");
    }
  }

  @Override
  public MutableCollectionDataType createMutableCollectionDataType() {
    return new CollectionDataTypeImpl();
  }

  @Override
  public MutableContentDataType createMutableContentDataType() {
    return new ContentDataTypeImpl();
  }

  @Override
  public MutableContentStatus createMutableContentStatus() {
    return new ContentStatusImpl();
  }

  @Override
  public MutableContentTypeId createMutableContentTypeID() {
    return new ContentTypeIdImpl();
  }

  @Override
  public MutableFieldDef createMutableFieldDef() {
    return new FieldDefImpl();
  }

  @Override
  public ContentTypeId createContentTypeId(WorkspaceId workspaceId, String namespace, String name) {
    if (workspaceId != null && StringUtils.isNotBlank(namespace) && StringUtils.isNotBlank(name)) {
      ContentTypeIdImpl contentTypeIdImpl = new ContentTypeIdImpl();
      contentTypeIdImpl.setName(name);
      contentTypeIdImpl.setNamespace(namespace);
      contentTypeIdImpl.setWorkspace(workspaceId);
      return contentTypeIdImpl;
    }
    else {
      throw new IllegalArgumentException("Any of arguments can not be null or blank.");
    }
  }

  @Override
  public MutableContentType getMutableContentType(ContentType contentType) {
    if (contentType != null) {
      ContentTypeImpl typeImpl = new ContentTypeImpl();
      merge(typeImpl, contentType);
      return typeImpl;
    }
    else {
      throw new IllegalArgumentException("Argument can not be null.");
    }
  }

  protected void merge(ContentTypeImpl typeImpl, ContentType contentType) throws IllegalArgumentException {
    typeImpl.setContentTypeID(contentType.getContentTypeID());
    typeImpl.setCreationDate(contentType.getCreationDate());
    typeImpl.setDisplayName(contentType.getDisplayName());
    typeImpl.setFromPersistentStorage(contentType instanceof ContentTypeImpl
        ? ((ContentTypeImpl) contentType).isFromPersistentStorage() : false);
    typeImpl.setLastModifiedDate(contentType.getLastModifiedDate());
    typeImpl.setParent(contentType.getParent());
    typeImpl.getMutableFields().addAll(contentType.getFields().values());
    typeImpl.getMutableRepresentationDefs().addAll(contentType.getRepresentations().values());
    typeImpl.getMutableStatuses().addAll(contentType.getStatuses().values());
  }
}
