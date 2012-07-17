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
import com.smartitengineering.dao.impl.hbase.spi.ExecutorService;
import com.smartitengineering.dao.impl.hbase.spi.impl.AbstractObjectRowConverter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

/**
 *
 * @author imyousuf
 */
public class FieldsObjectConverter extends AbstractObjectRowConverter<PersistentContentFields, ContentId> {

  public final static byte[] FAMILY_SELF = Bytes.toBytes("self");

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
    final Map<byte[], byte[]> fieldMap = new LinkedHashMap<byte[], byte[]>(startRow.getFamilyMap(FAMILY_SELF));
    for (Entry<byte[], byte[]> entry : fieldMap.entrySet()) {
      final byte[] key = entry.getKey();
      String fieldName = Bytes.toString(key);
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
      value = Bytes.toString(fieldMap.get(key));
      MutableField field = SmartContentAPI.getInstance().getContentLoader().createMutableField(content.getId(),
                                                                                               fieldDef);
      try {
        field.setValue(SmartContentAPI.getInstance().getContentLoader().getValueFor(value, fieldDef.getValueDef()));
        content.addField(field);
      }
      catch (Exception ex) {
        if (logger.isWarnEnabled()) {
          logger.warn("Could not fetch field value for field " + fieldName +
              ". This field will not be populated in the content.");
        }
      }
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
        if (logger.isWarnEnabled()) {
          logger.warn("Null value for field " + field.getName());
        }
        continue;
      }
      putField(field, put);
    }
  }

  private void putField(Field field, Put put) {
    final byte[] toBytes = Bytes.toBytes(field.getName());
    final String fieldValueAsString = field.getValue().toString();
    if (StringUtils.isNotBlank(fieldValueAsString)) {
      put.add(FAMILY_SELF, toBytes, Bytes.toBytes(fieldValueAsString));
    }
    else {
      if (logger.isInfoEnabled()) {
        logger.info("The following field is not persisted because it contains empty value - " + field.getName());
      }
    }
  }
}
