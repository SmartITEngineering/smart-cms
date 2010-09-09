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
import com.smartitengineering.cms.api.type.MutableFieldDef;
import com.smartitengineering.cms.api.type.SearchDef;
import com.smartitengineering.cms.api.type.ValidatorDef;
import com.smartitengineering.cms.api.type.VariationDef;
import com.smartitengineering.cms.spi.SmartSPI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 *
 * @author kaisar
 */
public class FieldDefImpl implements MutableFieldDef {

  private String newFieldName;
  private boolean required = false;
  private DataType dataType;
  private Collection<VariationDef> variationDefs = new ArrayList<VariationDef>();
  private ValidatorDef validatorDef;
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
  public void setVariations(Collection<VariationDef> variationDefs) {
    if (variationDefs != null) {
      this.variationDefs.addAll(variationDefs);
    }
    else {
      variationDefs.clear();
    }
  }

  @Override
  public void getCustomValidator(ValidatorDef validatorDef) {
    this.validatorDef = validatorDef;
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
  public Collection<VariationDef> getVariations() {
    return Collections.unmodifiableCollection(variationDefs);
  }

  @Override
  public ValidatorDef getCustomValidator() {
    return this.validatorDef;
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
  public String getSearchFieldName() {
    return SmartSPI.getInstance().getSearchFieldNameGenerator().getSearchFieldName(this);
  }
}
