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
import com.smartitengineering.cms.api.type.MutableRepresentationDef;
import com.smartitengineering.cms.api.type.MutableValidatorDef;
import com.smartitengineering.cms.api.type.MutableVariationDef;
import com.smartitengineering.cms.api.type.MutableResourceUri;
import com.smartitengineering.cms.spi.SmartContentSPI;
import com.smartitengineering.cms.spi.type.ContentTypeDefinitionParser;
import com.smartitengineering.cms.spi.type.TypeValidator;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author kaisar
 */
public class ContentTypeLoaderImpl implements ContentTypeLoader {

  @Override
  public ContentType loadContentType(ContentTypeId contentTypeID) throws NullPointerException {
    final Collection<? extends ContentType> reads =
                                  SmartContentSPI.getInstance().getContentTypeReader().readContentTypeFromPersistentStorage(
        contentTypeID);
    if (reads.size() > 0) {
      return reads.iterator().next();
    }
    else {
      return null;
    }
  }

  @Override
  public Collection<MutableContentType> parseContentTypes(InputStream contentTypeDefinitionStream, MediaType mediaType)
      throws NullPointerException, IOException {
    TypeValidator validator = SmartContentSPI.getInstance().getTypeValidators().getValidators().get(mediaType);
    ContentTypeDefinitionParser parser = SmartContentSPI.getInstance().getContentTypeDefinitionParsers().getParsers().get(
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
      final Collection<MutableContentType> types = parser.parseStream(contentTypeDefinitionStream);
      List<ContentTypeImpl> resultingTypes = mergeWithStoredContentTypes(types);
      return Collections.<MutableContentType>unmodifiableCollection(resultingTypes);
    }
    catch (Exception ex) {
      throw new IOException(ex);
    }
  }

  protected List<ContentTypeImpl> mergeWithStoredContentTypes(final Collection<MutableContentType> types) throws
      IllegalArgumentException {
    final List<ContentTypeImpl> resultingTypes =
                                new ArrayList<ContentTypeImpl>(types.size());
    final Collection<? extends ContentType> storedContentTypes;
    final ContentTypeId[] ids = new ContentTypeId[types.size()];
    int i = 0;
    for (ContentType type : types) {
      ids[i++] = type.getContentTypeID();
    }
    storedContentTypes =
    SmartContentSPI.getInstance().getContentTypeReader().readContentTypeFromPersistentStorage(ids);
    for (ContentType type : storedContentTypes) {
      final ContentTypeImpl contentTypeImpl = getContentTypeImpl(type);
      contentTypeImpl.setFromPersistentStorage(true);
      resultingTypes.add(contentTypeImpl);
    }
    for (MutableContentType type : types) {
      int index = resultingTypes.indexOf(type);
      if (index >= 0) {
        merge(resultingTypes.get(index), type);
      }
      else {
        resultingTypes.add(getContentTypeImpl(type));
      }
    }
    return resultingTypes;
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
      return getContentTypeImpl(contentType);
    }
    else {
      throw new IllegalArgumentException("Argument can not be null.");
    }
  }

  protected ContentTypeImpl getContentTypeImpl(ContentType contentType) throws IllegalArgumentException {
    ContentTypeImpl typeImpl = new ContentTypeImpl();
    merge(typeImpl, contentType);
    return typeImpl;
  }

  protected void merge(ContentTypeImpl typeImpl, ContentType contentType) throws IllegalArgumentException {
    typeImpl.setContentTypeID(contentType.getContentTypeID());
    typeImpl.setCreationDate(contentType.getCreationDate());
    typeImpl.setDisplayName(contentType.getDisplayName());
    typeImpl.setFromPersistentStorage(contentType instanceof ContentTypeImpl ? ((ContentTypeImpl) contentType).
        isFromPersistentStorage() : false);
    typeImpl.setLastModifiedDate(contentType.getLastModifiedDate());
    typeImpl.setParent(contentType.getParent());
    typeImpl.getMutableFieldDefs().clear();
    typeImpl.getMutableFieldDefs().addAll(contentType.getFieldDefs().values());
    typeImpl.getMutableRepresentationDefs().clear();
    typeImpl.getMutableRepresentationDefs().addAll(contentType.getRepresentationDefs().values());
    typeImpl.getMutableStatuses().clear();
    typeImpl.getMutableStatuses().addAll(contentType.getStatuses().values());
  }

  @Override
  public MutableRepresentationDef createMutableRepresentationDef() {
    return new RepresentationDefImpl();
  }

  @Override
  public MutableVariationDef createMutableVariationDef() {
    return new VariationDefImpl();
  }

  @Override
  public MutableValidatorDef createMutableValidatorDef() {
    return new ValidatorDefImpl();
  }

  @Override
  public MutableResourceUri createMutableResourceUri() {
    return new ResourceUriImpl();
  }
}
