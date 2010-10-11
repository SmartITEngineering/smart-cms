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
package com.smartitengineering.cms.spi.impl.content;

import com.google.inject.Inject;
import com.smartitengineering.cms.api.content.ContentId;
import com.smartitengineering.cms.api.content.Field;
import com.smartitengineering.cms.api.content.MutableField;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.factory.content.WriteableContent;
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.api.type.FieldDef;
import com.smartitengineering.cms.api.type.FieldValueType;
import com.smartitengineering.cms.api.type.MutableContentStatus;
import com.smartitengineering.cms.spi.SmartContentSPI;
import com.smartitengineering.cms.spi.content.PersistableContent;
import com.smartitengineering.cms.spi.impl.Utils;
import com.smartitengineering.cms.spi.impl.type.PersistentContentType;
import com.smartitengineering.dao.impl.hbase.spi.ExecutorService;
import com.smartitengineering.dao.impl.hbase.spi.SchemaInfoProvider;
import com.smartitengineering.dao.impl.hbase.spi.impl.AbstactObjectRowConverter;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

/**
 *
 * @author imyousuf
 */
public class ContentObjectConverter extends AbstactObjectRowConverter<PersistentContent, ContentId> {

  public final static byte[] FAMILY_SELF = Bytes.toBytes("self");
  public final static byte[] FAMILY_SIMPLE_FIELDS = Bytes.toBytes("simple-fields");
  public final static byte[] FAMILY_COLLECTION = Bytes.toBytes("collections");
  public final static byte[] FAMILY_STRING = Bytes.toBytes("strings");
  public final static byte[] FAMILY_OTHER = Bytes.toBytes("others");
  public final static byte[] FAMILY_FIELD_TYPE = Bytes.toBytes("fieldType");
  public final static byte[] CELL_PARENT_ID = Bytes.toBytes("parent");
  public final static byte[] CELL_CONTENT_TYPE_ID = Bytes.toBytes("contentType");
  public final static byte[] CELL_STATUS = Bytes.toBytes("status");
  public final static byte[] CELL_CREATED = Bytes.toBytes("created");
  public final static byte[] CELL_LAST_MODIFIED = Bytes.toBytes("lastModified");
  @Inject
  private SchemaInfoProvider<PersistentContentType, ContentTypeId> contentTypeSchemaProvider;

  @Override
  protected String[] getTablesToAttainLock() {
    return new String[]{getInfoProvider().getMainTableName()};
  }

  @Override
  protected void getPutForTable(PersistentContent instance, ExecutorService service, Put put) {
    WriteableContent content = instance.getMutableContent();
    putSelfData(content, put);
    putFields(content, put);
  }

  @Override
  protected void getDeleteForTable(PersistentContent instance, ExecutorService service, Delete put) {
    //Should delete all row at once
  }

  @Override
  public PersistentContent rowsToObject(Result startRow, ExecutorService executorService) {
    PersistableContent content = SmartContentSPI.getInstance().getPersistableDomainFactory().createPersistableContent();
    try {
      content.setContentId(getInfoProvider().getIdFromRowId(startRow.getRow()));
    }
    catch (Exception ex) {
      logger.error("Could not parse content Id", ex);
      throw new RuntimeException(ex);
    }
    final byte[] parentId = startRow.getValue(FAMILY_SELF, CELL_PARENT_ID);
    try {
      if (parentId != null) {
        content.setParentId(getInfoProvider().getIdFromRowId(parentId));
      }
    }
    catch (Exception ex) {
      logger.error("Could not parse parent content Id", ex);
      throw new RuntimeException(ex);
    }
    content.setCreationDate(Utils.toDate(startRow.getValue(FAMILY_SELF, CELL_CREATED)));
    content.setLastModifiedDate(Utils.toDate(startRow.getValue(FAMILY_SELF, CELL_LAST_MODIFIED)));
    ContentType type = null;
    try {
      ContentTypeId typeId =
                    contentTypeSchemaProvider.getIdFromRowId(startRow.getValue(FAMILY_SELF, CELL_CONTENT_TYPE_ID));
      type = SmartContentAPI.getInstance().getContentTypeLoader().loadContentType(typeId);
      if (type != null) {
        content.setContentDefinition(type);
      }
      else {
        logger.error(new StringBuilder("Could not retrieve content type id ").append(typeId).append(
            " for content ").append(content.getContentId()).toString());
        throw new RuntimeException("Could not find content type with id " + typeId);
      }
      byte[] status = startRow.getValue(FAMILY_SELF, CELL_STATUS);
      MutableContentStatus contentStatus = SmartContentAPI.getInstance().getContentTypeLoader().
          createMutableContentStatus();
      contentStatus.setName(Bytes.toString(status));
      contentStatus.setContentTypeID(typeId);
      contentStatus.setId(0);
      content.setStatus(contentStatus);
    }
    catch (RuntimeException ex) {
      logger.error("Could not parse content type id", ex);
      throw ex;
    }
    catch (Exception ex) {
      logger.error("Could not parse content type id", ex);
      throw new RuntimeException(ex);
    }
    Map<byte[], byte[]> fieldTypeMap = startRow.getFamilyMap(FAMILY_FIELD_TYPE);
    for (Entry<byte[], byte[]> entry : fieldTypeMap.entrySet()) {
      String fieldName = Bytes.toString(entry.getKey());
      String typeName = Bytes.toString(entry.getValue());
      FieldValueType valueType = FieldValueType.valueOf(typeName);
      final FieldDef fieldDef;
      if (type != null) {
        fieldDef = type.getFieldDefs().get(fieldName);
      }
      else {
        fieldDef = null;
      }
      if (fieldDef == null) {
        logger.warn(new StringBuilder("Ignoring field with name ").append(fieldName).append(" as not definition found").
            toString());
        continue;
      }
      final String value;
      switch (valueType) {
        case COLLECTION:
          value = Bytes.toString(startRow.getValue(FAMILY_COLLECTION, entry.getKey()));
          break;
        case OTHER:
          value = Bytes.toString(startRow.getValue(FAMILY_OTHER, entry.getKey()));
          break;
        case STRING:
          value = Bytes.toString(startRow.getValue(FAMILY_STRING, entry.getKey()));
          break;
        default:
          value = Bytes.toString(startRow.getValue(FAMILY_SIMPLE_FIELDS, entry.getKey()));
          break;
      }
      MutableField field = SmartContentAPI.getInstance().getContentLoader().createMutableField(fieldDef);
      field.setValue(SmartContentAPI.getInstance().getContentLoader().getValueFor(value, fieldDef));
      content.setField(field);
    }
    PersistentContent persistentContent = new PersistentContent();
    persistentContent.setMutableContent(content);
    return persistentContent;
  }

  protected void putSelfData(WriteableContent content, Put put) {
    try {
      put.add(FAMILY_SELF, CELL_CONTENT_TYPE_ID,
              contentTypeSchemaProvider.getRowIdFromId(content.getContentDefinition().getContentTypeID()));
    }
    catch (IOException ex) {
      logger.error("Could put content type ID", ex);
      throw new RuntimeException(ex);
    }
    put.add(FAMILY_SELF, CELL_CREATED, Utils.toBytes(content.getCreationDate()));
    put.add(FAMILY_SELF, CELL_LAST_MODIFIED, Utils.toBytes(content.getLastModifiedDate()));
    if (content.getStatus() != null) {
      put.add(FAMILY_SELF, CELL_STATUS, Bytes.toBytes(content.getStatus().getName()));
    }
    if (content.getParentId() != null) {
      try {
        put.add(FAMILY_SELF, CELL_PARENT_ID, getInfoProvider().getRowIdFromId(content.getParentId()));
      }
      catch (IOException ex) {
        logger.error("Could put parent ID", ex);
        throw new RuntimeException(ex);
      }
    }
  }

  private void putFields(WriteableContent content, Put put) {
    for (Field field : content.getFields().values()) {
      switch (field.getValue().getDataType()) {
        case COLLECTION:
          putField(field, put, FAMILY_COLLECTION);
          break;
        case STRING:
          putField(field, put, FAMILY_STRING);
          break;
        case OTHER:
          putField(field, put, FAMILY_OTHER);
          break;
        default:
          putField(field, put, FAMILY_SIMPLE_FIELDS);
      }
    }
  }

  private void putField(Field field, Put put, byte[] family) {
    final byte[] toBytes = Bytes.toBytes(field.getName());
    final String name = field.getValue().getDataType().name();
    put.add(FAMILY_FIELD_TYPE, toBytes, Bytes.toBytes(name.toString()));
    put.add(family, toBytes, Bytes.toBytes(field.getValue().toString()));
  }
}
