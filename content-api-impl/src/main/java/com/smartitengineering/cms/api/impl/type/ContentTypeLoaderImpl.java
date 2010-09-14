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
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

/**
 *
 * @author kaisar
 */
public class ContentTypeLoaderImpl implements ContentTypeLoader {

  @Override
  public ContentType loadContentType(ContentTypeId contentTypeID) throws NullPointerException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Collection<MutableContentType> parseContentTypes(InputStream contentTypeDefinitionStream) throws
      NullPointerException,
      IOException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public MutableContentDataType getMutableContentDataType(ContentDataType contentDataType) {
    ContentDataTypeImpl contentDataTypeImpl = new ContentDataTypeImpl();
    contentDataTypeImpl.setTypeDef(contentDataType.getTypeDef());
    contentDataTypeImpl.setBiBidirectionalFieldName(contentDataType.getBidirectionalFieldName());
    return contentDataTypeImpl;
  }

  @Override
  public MutableCollectionDataType getMutableCollectionDataType(CollectionDataType collectionDataType) {
    CollectionDataTypeImpl collectionDataTypeImpl = new CollectionDataTypeImpl();
    collectionDataTypeImpl.setItemDataType(collectionDataType.getItemDataType());
    collectionDataTypeImpl.setMaxSize(collectionDataType.getMaxSize());
    collectionDataTypeImpl.setMinSeize(collectionDataType.getMinSize());
    return collectionDataTypeImpl;
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
    FieldDefImpl fieldDefImpl = new FieldDefImpl();
    fieldDefImpl.setName(fieldDef.getName());
    fieldDefImpl.setSearchDefinition(fieldDef.getSearchDefinition());
    fieldDefImpl.setValueDef(fieldDef.getValueDef());
    fieldDefImpl.setVariations(fieldDef.getVariations());
    fieldDefImpl.setFieldStandaloneUpdateAble(true);
    fieldDefImpl.setRequired(true);
    return fieldDefImpl;
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
    ContentTypeIdImpl contentTypeIdImpl = new ContentTypeIdImpl();
    contentTypeIdImpl.setName(name);
    contentTypeIdImpl.setNamespace(namespace);
    contentTypeIdImpl.setWorkspace(workspaceId);
    return contentTypeIdImpl;
  }
}
