/*
 *
 * This is a simple Content Management System (CMS)
 * Copyright (C) 2009  Imran M Yousuf (imyousuf@smartitengineering.com)
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
package com.smartitengineering.cms.api.content.impl;

import com.smartitengineering.cms.api.content.Field;
import com.smartitengineering.cms.api.content.FieldValue;
import com.smartitengineering.cms.api.content.MutableField;
import com.smartitengineering.cms.api.content.Variation;

/**
 *
 * @author kaisar
 */
public class FieldImpl implements Field, MutableField {

  private String fieldName;
  private String varName;
  private Variation variation;
  private FieldValue fieldValue;

  public void setValue(FieldValue fieldValue) {
    this.fieldValue = fieldValue;
  }

  public void setName(String name) {
    this.fieldName = name;
  }

  public String getName() {
    return this.fieldName;
  }

  public Variation getVariation(String varName) {
    this.varName = varName;
    return this.variation;
  }

  public FieldValue getValue() {
    return this.fieldValue;
  }
}
