/*
 *
 * This is a simple Content Management System (CMS)
 * Copyright (C) 2011  Imran M Yousuf (imyousuf@smartitengineering.com)
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

import com.smartitengineering.cms.api.content.Content;
import com.smartitengineering.cms.api.content.ContentId;
import com.smartitengineering.cms.api.content.Field;
import com.smartitengineering.cms.api.content.MutableField;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.type.FieldDef;
import com.smartitengineering.cms.api.type.FieldValueType;
import com.smartitengineering.dao.impl.hbase.spi.ExecutorService;
import com.smartitengineering.dao.impl.hbase.spi.impl.AbstractObjectRowConverter;
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
public class FieldsObjectConverter extends AbstractObjectRowConverter<PersistentContentFields, ContentId> {

  public final static byte[] FAMILY_SIMPLE_FIELDS = Bytes.toBytes("simpleFields");
  public final static byte[] FAMILY_COLLECTION = Bytes.toBytes("collections");
  public final static byte[] FAMILY_STRING = Bytes.toBytes("strings");
  public final static byte[] FAMILY_OTHER = Bytes.toBytes("others");
  public final static byte[] FAMILY_FIELD_TYPE = Bytes.toBytes("fieldType");

  @Override
  protected String[] getTablesToAttainLock() {
    return new String[]{getInfoProvider().getMainTableName()};
  }

  @Override
  protected void getPutForTable(PersistentContentFields instance, ExecutorService service, Put put) {
    putFields(instance.getFields(), put);
  }

  @Override
  protected void getDeleteForTable(PersistentContentFields instance, ExecutorService service, Delete put) {
    //Should delete all row at once
  }

  @Override
  public PersistentContentFields rowsToObject(Result startRow, ExecutorService executorService) {
    PersistentContentFields content = new PersistentContentFields();
    try {
      content.setId(getInfoProvider().getIdFromRowId(startRow.getRow()));
    }
    catch (Exception ex) {
      logger.error("Could not parse content Id", ex);
      throw new RuntimeException(ex);
    }
    final Content mainContent = SmartContentAPI.getInstance().getContentLoader().loadContent(content.getId());
    if (mainContent == null) {
      return null;
    }
    final ContentType type = mainContent.getContentDefinition();
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
      MutableField field = SmartContentAPI.getInstance().getContentLoader().createMutableField(content.getId(),
                                                                                               fieldDef);
      field.setValue(SmartContentAPI.getInstance().getContentLoader().getValueFor(value, fieldDef.getValueDef()));
      content.addField(field);
    }
    return content;
  }

  private void putFields(Map<String, Field> fields, Put put) {
    for (Field field : fields.values()) {
      if (field == null) {
        logger.warn("Null field in content's own field");
        continue;
      }
      if (field.getValue() == null) {
        logger.warn("Null value for field " + field.getName());
        continue;
      }
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
    final String fieldType = field.getValue().getDataType().name();
    put.add(FAMILY_FIELD_TYPE, toBytes, Bytes.toBytes(fieldType.toString()));
    put.add(family, toBytes, Bytes.toBytes(field.getValue().toString()));
  }
}
