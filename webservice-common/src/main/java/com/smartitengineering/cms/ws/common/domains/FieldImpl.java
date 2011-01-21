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
package com.smartitengineering.cms.ws.common.domains;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author imyousuf
 */
public class FieldImpl implements Field {

  private String name, fieldUri, fieldRawContentUri;
  private FieldValue value;
  private Map<String, String> variations = new LinkedHashMap<String, String>();
  private Map<String, String> variationsByNames = new LinkedHashMap<String, String>();

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getFieldUri() {
    return fieldUri;
  }

  @Override
  public FieldValue getValue() {
    return value;
  }

  @Override
  public String getFieldRawContentUri() {
    return fieldRawContentUri;
  }

  @Override
  public Map<String, String> getVariations() {
    return variations;
  }

  @Override
  public Map<String, String> getVariationsByNames() {
    return variationsByNames;
  }

  public void setFieldUri(String fieldUri) {
    this.fieldUri = fieldUri;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setValue(FieldValue value) {
    this.value = value;
  }

  public void setFieldRawContentUri(String fieldRawContentUri) {
    this.fieldRawContentUri = fieldRawContentUri;
  }
}
