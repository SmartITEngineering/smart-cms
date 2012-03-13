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

import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.type.ContentDataType;
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.type.FieldDef;
import com.smartitengineering.cms.api.type.FieldValueType;
import com.smartitengineering.cms.api.type.MutableCompositeDataType;
import com.smartitengineering.cms.api.type.MutableFieldDef;
import java.io.Serializable;
import java.util.ArrayList;
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
  private EmbeddedContentDataType embeddedContentDataType;
  protected transient final Logger logger = LoggerFactory.getLogger(getClass());

  public void setEmbeddedContentDataType(EmbeddedContentDataType contentDataType) {
    this.embeddedContentDataType = contentDataType;
  }

  public Collection<FieldDef> getOwnMutableComposition() {
    return ownedCompositeFields;
  }

  public ContentDataType getEmbeddedContentType() {
    if (embeddedContentDataType == null) {
      return null;
    }
    return embeddedContentDataType.getContentDataType();
  }

  public Collection<FieldDef> getOwnComposition() {
    return Collections.unmodifiableSet(ownedCompositeFields);
  }

  public Collection<FieldDef> getComposition() {
    Set<FieldDef> compositeFields = new LinkedHashSet<FieldDef>();
    if (logger.isDebugEnabled()) {
      logger.debug("Embedded content type id " + getEmbeddedContentType());
    }
    if (getEmbeddedContentType() != null && getEmbeddedContentType().getTypeDef() != null) {
      ContentType type = getEmbeddedContentType().getTypeDef().getContentType();
      if (logger.isDebugEnabled()) {
        logger.debug("Embedded content type " + type);
      }
      if (type != null) {
        final Map<String, FieldDef> fieldDefs = type.getFieldDefs();
        if (logger.isDebugEnabled()) {
          logger.debug("Embedded fields " + fieldDefs);
        }
        if (fieldDefs != null && !fieldDefs.isEmpty()) {
          compositeFields.addAll(getEmbeddedFieldDefs(embeddedContentDataType, fieldDefs.values()));
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

  public EmbeddedContentDataType getEmbeddedContentDataType() {
    return embeddedContentDataType;
  }

  private Collection<? extends FieldDef> getEmbeddedFieldDefs(EmbeddedContentDataType embeddedContentType,
                                                              Collection<FieldDef> values) {
    if (embeddedContentType == null || embeddedContentType.getFieldEmbeddedIn() == null) {
      logger.debug("Could not find valid embedded content data type!");
      return values;
    }
    logger.debug("Cloning field defs with parent container");
    final FieldDef fieldEmbeddedIn = embeddedContentType.getFieldEmbeddedIn();
    ArrayList<FieldDef> defs = new ArrayList<FieldDef>(values.size());
    for (FieldDef def : values) {
      MutableFieldDef fieldDef = SmartContentAPI.getInstance().getContentTypeLoader().createMutableFieldDef(
          fieldEmbeddedIn);
      fieldDef.setCustomValidators(def.getCustomValidators());
      fieldDef.setDisplayName(def.getDisplayName());
      fieldDef.setFieldStandaloneUpdateAble(def.isFieldStandaloneUpdateAble());
      fieldDef.setName(def.getName());
      fieldDef.setParameterizedDisplayNames(def.getParameterizedDisplayNames());
      fieldDef.setParameters(def.getParameters());
      fieldDef.setRequired(def.isRequired());
      fieldDef.setSearchDefinition(def.getSearchDefinition());
      fieldDef.setValueDef(def.getValueDef());
      fieldDef.setVariations(def.getVariations().values());
      defs.add(fieldDef);
    }
    return defs;
  }

  public static class EmbeddedContentDataTypeImpl implements EmbeddedContentDataType, Serializable {

    private FieldDef fieldEmbeddedIn;
    private ContentDataType contentDataType;

    public EmbeddedContentDataTypeImpl(FieldDef fieldEmbeddedIn, ContentDataType contentDataType) {
      this.fieldEmbeddedIn = fieldEmbeddedIn;
      this.contentDataType = contentDataType;
    }

    public ContentDataType getContentDataType() {
      return contentDataType;
    }

    public FieldDef getFieldEmbeddedIn() {
      return fieldEmbeddedIn;
    }
  }
}
