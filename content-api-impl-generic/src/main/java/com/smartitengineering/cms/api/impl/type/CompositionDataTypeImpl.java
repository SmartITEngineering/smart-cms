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
package com.smartitengineering.cms.api.impl.type;

import com.smartitengineering.cms.api.type.ContentDataType;
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.type.FieldDef;
import com.smartitengineering.cms.api.type.FieldValueType;
import com.smartitengineering.cms.api.type.MutableCompositeDataType;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class CompositionDataTypeImpl implements MutableCompositeDataType {

  private final Set<FieldDef> ownedCompositeFields = new LinkedHashSet<FieldDef>();
  private ContentDataType embeddedContentType;
  protected transient final Logger logger = LoggerFactory.getLogger(getClass());

  public void setEmbeddedContentType(ContentDataType contentDataType) {
    this.embeddedContentType = contentDataType;
  }

  public Collection<FieldDef> getOwnMutableComposition() {
    return ownedCompositeFields;
  }

  public ContentDataType getEmbeddedContentType() {
    return embeddedContentType;
  }

  public Collection<FieldDef> getOwnComposition() {
    return Collections.unmodifiableSet(ownedCompositeFields);
  }

  public Collection<FieldDef> getComposition() {
    Set<FieldDef> compositeFields = new LinkedHashSet<FieldDef>();
    if (logger.isDebugEnabled()) {
      logger.debug("Embedded content type id " + embeddedContentType);
    }
    if (embeddedContentType != null && embeddedContentType.getTypeDef() != null) {
      ContentType type = embeddedContentType.getTypeDef().getContentType();
      if (logger.isDebugEnabled()) {
        logger.debug("Embedded content type " + type);
      }
      if (type != null) {
        final Map<String, FieldDef> fieldDefs = type.getFieldDefs();
        if (logger.isDebugEnabled()) {
          logger.debug("Embedded fields " + fieldDefs);
        }
        if (fieldDefs != null && !fieldDefs.isEmpty()) {
          compositeFields.addAll(fieldDefs.values());
        }
      }
    }
    compositeFields.addAll(ownedCompositeFields);
    return compositeFields;
  }

  public FieldValueType getType() {
    return FieldValueType.COMPOSITE;
  }

  public Map<String, FieldDef> getComposedFieldDefs() {
    Collection<FieldDef> allFields = getComposition();
    if (allFields == null || allFields.isEmpty()) {
      return Collections.emptyMap();
    }
    LinkedHashMap<String, FieldDef> fields = new LinkedHashMap<String, FieldDef>(allFields.size());
    for (FieldDef def : allFields) {
      fields.put(def.getName(), def);
    }
    return Collections.unmodifiableMap(fields);
  }
}
