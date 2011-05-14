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

import com.smartitengineering.cms.api.type.DataType;
import com.smartitengineering.cms.api.type.FieldDef;
import com.smartitengineering.cms.api.type.MutableFieldDef;
import com.smartitengineering.cms.api.type.SearchDef;
import com.smartitengineering.cms.api.type.ValidatorDef;
import com.smartitengineering.cms.api.type.VariationDef;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author kaisar
 */
public class FieldDefImpl implements MutableFieldDef {

  private String newFieldName;
  private String displayName;
  private boolean required = false;
  private DataType dataType;
  private Collection<VariationDef> variationDefs = new LinkedHashSet<VariationDef>();
  private final List<ValidatorDef> validatorDefs = new ArrayList<ValidatorDef>();
  private SearchDef searchDef;
  private boolean standaloneUpdateAble = false;

  @Override
  public void setName(String newFieldName) throws IllegalArgumentException {
    this.newFieldName = newFieldName;
  }

  @Override
  public void setRequired(boolean required) {
    this.required = required;
  }

  @Override
  public void setValueDef(DataType dataType) {
    this.dataType = dataType;
  }

  @Override
  public void setVariations(Collection<? extends VariationDef> variationDefs) {
    if (variationDefs != null) {
      this.variationDefs.clear();
      this.variationDefs.addAll(variationDefs);
    }
    else {
      variationDefs.clear();
    }
  }

  @Override
  public void setCustomValidators(Collection<? extends ValidatorDef> validatorDefs) {
    this.validatorDefs.clear();
    if (validatorDefs != null && !validatorDefs.isEmpty()) {
      this.validatorDefs.addAll(validatorDefs);
    }
  }

  @Override
  public void setSearchDefinition(SearchDef searchDef) {
    this.searchDef = searchDef;
  }

  @Override
  public void setFieldStandaloneUpdateAble(boolean standaloneUpdateAble) {
    this.standaloneUpdateAble = standaloneUpdateAble;
  }

  @Override
  public String getName() {
    return this.newFieldName;
  }

  @Override
  public DataType getValueDef() {
    return this.dataType;
  }

  @Override
  public boolean isRequired() {
    return this.required;
  }

  @Override
  public Map<String, VariationDef> getVariations() {
    Map<String, VariationDef> defs = new LinkedHashMap<String, VariationDef>(variationDefs.size());
    for (VariationDef def : variationDefs) {
      defs.put(def.getName(), def);
    }
    return Collections.unmodifiableMap(defs);
  }

  @Override
  public Collection<ValidatorDef> getCustomValidators() {
    return Collections.unmodifiableList(this.validatorDefs);
  }

  @Override
  public SearchDef getSearchDefinition() {
    return this.searchDef;
  }

  @Override
  public boolean isFieldStandaloneUpdateAble() {
    return this.standaloneUpdateAble;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (FieldDef.class.isAssignableFrom(obj.getClass())) {
      return false;
    }
    final FieldDef other = (FieldDef) obj;
    if ((this.newFieldName == null) ? (other.getName() != null) : !this.newFieldName.equals(other.getName())) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 23 * hash + (this.newFieldName != null ? this.newFieldName.hashCode() : 0);
    return hash;
  }

  @Override
  public String toString() {
    return "FieldDefImpl{" + "newFieldName=" + newFieldName + "; required=" + required + "; dataType=" + dataType +
        "; variationDefs=" + variationDefs + "; validatorDef=" + validatorDefs + "; searchDef=" + searchDef +
        "; standaloneUpdateAble=" + standaloneUpdateAble + '}';
  }

  @Override
  public VariationDef getVariationDefForMimeType(String mimeType) {
    TreeMap<String, VariationDef> map = new TreeMap<String, VariationDef>();
    for (VariationDef def : variationDefs) {
      if (def.getMIMEType().equals(mimeType)) {
        map.put(def.getName(), def);
      }
    }
    if (map.isEmpty()) {
      return null;
    }
    else {
      return map.firstEntry().getValue();
    }
  }

  public void setDisplayName(String newDisplayNameForField) {
    this.displayName = newDisplayNameForField;
  }

  public String getDisplayName() {
    return StringUtils.isNotBlank(displayName) ? displayName : getName();
  }
}
