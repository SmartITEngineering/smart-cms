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
package com.smartitengineering.cms.spi.impl.content.search;

import com.smartitengineering.cms.api.type.CollectionDataType;
import com.smartitengineering.cms.api.type.FieldDef;
import com.smartitengineering.cms.api.type.FieldValueType;
import com.smartitengineering.cms.api.type.SearchDef;
import com.smartitengineering.cms.spi.type.SearchFieldNameGenerator;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class SearchFieldNameGeneratorImpl implements SearchFieldNameGenerator {

  protected final transient Logger logger = LoggerFactory.getLogger(getClass());
  public static final String PROP_CMS_SEARCH_FIELD_TYPE_NAME = "CMS_SEARCH_FIELD_TYPE_NAME";

  public String getFieldName(FieldDef def) {
    if (def == null) {
      return "";
    }
    String parentName = getFieldName(def.getParentContainer());
    StringBuilder builder = new StringBuilder();
    if (StringUtils.isNotBlank(parentName)) {
      builder.append(parentName).append('.');
    }
    builder.append(def.getName());
    logger.info("Search field name for " + def.getDisplayName() + " is " + builder.toString());
    return builder.toString();
  }

  @Override
  public String getSearchFieldName(FieldDef def) {
    final SearchDef searchDefinition = def.getSearchDefinition();
    if (searchDefinition != null && (searchDefinition.isIndexed() || searchDefinition.isStored())) {
      StringBuilder indexFieldName = new StringBuilder(getFieldName(def));
      final FieldValueType type = def.getValueDef().getType();
      final FieldValueType mainType;
      final String multi;
      if (type.equals(FieldValueType.COLLECTION)) {
        mainType = ((CollectionDataType) def.getValueDef()).getItemDataType().getType();
        multi = "m";
      }
      else {
        boolean found = false;
        FieldDef fd = def;
        while (!found && fd.getParentContainer() != null) {
          fd = fd.getParentContainer();
          if (fd.getValueDef() != null && fd.getValueDef().getType() != null && fd.getValueDef().getType().equals(
              FieldValueType.COLLECTION)) {
            found = true;
          }
        }
        mainType = type;
        if (found) {
          multi = "m";
        }
        else {
          multi = "";
        }
      }
      final String typeSuffix;
      final String getSuffixProp = def.getParameters().get(PROP_CMS_SEARCH_FIELD_TYPE_NAME);
      if (StringUtils.isNotBlank(getSuffixProp)) {
        typeSuffix = getSuffixProp;
      }
      else {
        typeSuffix = mainType.name();
      }
      indexFieldName.append('_').append(typeSuffix).append('_').append(multi);
      if (searchDefinition.isIndexed()) {
        indexFieldName.append('i');
      }
      if (searchDefinition.isStored()) {
        indexFieldName.append('s');
      }
      return indexFieldName.toString();
    }
    else {
      return null;
    }
  }
}
