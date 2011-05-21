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
import com.smartitengineering.cms.api.content.MutableCompositeFieldValue;
import com.smartitengineering.cms.api.type.FieldValueType;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
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
        return toString;
      }
      catch (Exception ex) {
        logger.warn("Could not serialize JSON node", ex);
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
        switch (field.getFieldDef().getValueDef().getType()) {
          case COLLECTION:
            objectNode.put(field.getName(), (ArrayNode) ((CollectionFieldValue) field.getValue()).getAsJsonNode());
            break;
          case COMPOSITE:
            objectNode.put(field.getName(), (ObjectNode) ((CompositeFieldValue) field.getValue()).getAsJsonNode());
            break;
          default:
            objectNode.put(field.getName(), field.toString());
        }
      }
      return objectNode;
    }
    return null;
  }
}
