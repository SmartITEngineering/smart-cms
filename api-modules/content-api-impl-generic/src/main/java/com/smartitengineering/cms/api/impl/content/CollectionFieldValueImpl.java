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
package com.smartitengineering.cms.api.impl.content;

import com.smartitengineering.cms.api.content.CompositeFieldValue;
import com.smartitengineering.cms.api.content.FieldValue;
import com.smartitengineering.cms.api.content.MutableCollectionFieldValue;
import com.smartitengineering.cms.api.type.FieldValueType;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import org.codehaus.jackson.JsonGenerator.Feature;
import org.codehaus.jackson.impl.WriterBasedGenerator;
import org.codehaus.jackson.io.IOContext;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.util.BufferRecycler;

/**
 *
 * @author kaisar
 */
public class CollectionFieldValueImpl extends FieldValueImpl<Collection<FieldValue>> implements
    MutableCollectionFieldValue {

  static final ObjectMapper MAPPER = new ObjectMapper();

  public CollectionFieldValueImpl() {
    setFieldValueType(FieldValueType.COLLECTION);
  }

  @Override
  protected String getValueAsString() {

    ArrayNode arrayNode = getAsJsonNode();
    if (arrayNode != null) {
      final StringWriter stringWriter = new StringWriter();
      String toString;
      try {
        new WriterBasedGenerator(new IOContext(new BufferRecycler(), new Object(), true), Feature.collectDefaults(),
                                 MAPPER, stringWriter).writeTree(arrayNode);
        toString = stringWriter.toString();
        return toString;
      }
      catch (Exception ex) {
        logger.warn("Could not serialize JSON node", ex);
      }
    }
    return getValue() != null ? Arrays.toString(getValue().toArray()) : super.getValueAsString();
  }

  public ArrayNode getAsJsonNode() {
    Collection<FieldValue> fieldValues = getValue();
    if (fieldValues != null) {
      JsonNodeFactory factory = MAPPER.getNodeFactory();
      ArrayNode arrayNode = new ArrayNode(factory);
      for (FieldValue fieldValue : fieldValues) {
        if (fieldValue.getDataType().equals(FieldValueType.COMPOSITE)) {
          arrayNode.add((ObjectNode)((CompositeFieldValue) fieldValue).getAsJsonNode());
        }
        else {
          arrayNode.add(fieldValue.toString());
        }
      }
      return arrayNode;
    }
    return null;
  }
}
