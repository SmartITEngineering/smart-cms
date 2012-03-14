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
package com.smartitengineering.cms.api.impl.content;

import com.smartitengineering.cms.api.content.CollectionFieldValue;
import com.smartitengineering.cms.api.content.CompositeFieldValue;
import com.smartitengineering.cms.api.content.Field;
import com.smartitengineering.cms.api.content.FieldValue;
import com.smartitengineering.cms.api.content.MutableCompositeFieldValue;
import com.smartitengineering.cms.api.type.FieldValueType;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import org.codehaus.jackson.JsonGenerator.Feature;
import org.codehaus.jackson.impl.WriterBasedGenerator;
import org.codehaus.jackson.io.IOContext;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.util.BufferRecycler;

/**
 *
 * @author imyousuf
 */
public class CompositeFieldValueImpl extends FieldValueImpl<Collection<Field>> implements
    MutableCompositeFieldValue {

  public CompositeFieldValueImpl() {
    setFieldValueType(FieldValueType.COMPOSITE);
  }

  @Override
  protected String getValueAsString() {
    ObjectNode objectNode = getAsJsonNode();
    if (objectNode != null) {
      final StringWriter stringWriter = new StringWriter();
      String toString;
      try {
        new WriterBasedGenerator(new IOContext(new BufferRecycler(), new Object(), true), Feature.collectDefaults(),
                                 CollectionFieldValueImpl.MAPPER, stringWriter).writeTree(objectNode);
        toString = stringWriter.toString();
        if (getLogger().isDebugEnabled()) {
          getLogger().debug("Returning composite field value " + toString);
        }
        return toString;
      }
      catch (Exception ex) {
        getLogger().warn("Could not serialize JSON node", ex);
      }
    }
    return getValue() != null ? Arrays.toString(getValue().toArray()) : super.getValueAsString();
  }

  public ObjectNode getAsJsonNode() {
    Collection<Field> fields = getValue();
    if (fields != null) {
      JsonNodeFactory factory = CollectionFieldValueImpl.MAPPER.getNodeFactory();
      ObjectNode objectNode = new ObjectNode(factory);
      for (Field field : fields) {
        final FieldValue value = field.getValue();
        if (value != null) {
          switch (field.getFieldDef().getValueDef().getType()) {
            case COLLECTION:
              objectNode.put(field.getName(), (ArrayNode) ((CollectionFieldValue) value).getAsJsonNode());
              break;
            case COMPOSITE:
              objectNode.put(field.getName(), (ObjectNode) ((CompositeFieldValue) value).getAsJsonNode());
              break;
            default: {
              final String toString = value.toString();
              if (getLogger().isDebugEnabled()) {
                getLogger().debug("Adding default type field value for " + field.getName() + ", value " + toString);
              }
              objectNode.put(field.getName(), toString);
            }
          }
        }
      }
      return objectNode;
    }
    return null;
  }

  public Map<String, Field> getValueAsMap() {
    Collection<Field> fields = getValue();
    LinkedHashMap<String, Field> fieldMap = new LinkedHashMap<String, Field>();
    if (fields != null && !fields.isEmpty()) {
      for (Field field : fields) {
        fieldMap.put(field.getName(), field);
      }
    }
    else {
      getLogger().warn("EMPTY COMPOSITE FIELD!");
    }
    return fieldMap;
  }

  public Map<String, Field> getFields() {
    return getValueAsMap();
  }

  public Field getField(String fieldName) {
    return getValueAsMap().get(fieldName);
  }

  @Override
  public void setValue(Collection<Field> newV) {
    Collection<Field> fields = new LinkedHashSet<Field>();
    if (newV != null) {
      fields.addAll(newV);
    }
    super.setValue(fields);
  }

  public void setField(Field field) {
    if (getValue() == null) {
      setValue(null);
    }
    getValue().add(field);
  }

  public void removeField(String fieldName) {
    final Field field = getFields().get(fieldName);
    if (field != null) {
      getValue().remove(field);
    }
  }
}
