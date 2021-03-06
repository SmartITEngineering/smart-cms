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

import com.smartitengineering.cms.api.content.ContentId;
import com.smartitengineering.cms.api.content.Field;
import com.smartitengineering.cms.api.content.FieldValue;
import com.smartitengineering.cms.api.content.MutableField;
import com.smartitengineering.cms.api.content.Variation;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.type.FieldDef;
import com.smartitengineering.cms.spi.SmartContentSPI;
import java.net.URI;

/**
 *
 * @author kaisar
 */
public class FieldImpl implements MutableField {

  private String fieldName;
  private FieldValue fieldValue;
  private FieldDef fieldDef;
  private ContentId content;

  @Override
  public void setValue(FieldValue fieldValue) {
    this.fieldValue = fieldValue;
  }

  @Override
  public void setName(String name) {
    this.fieldName = name;
  }

  @Override
  public String getName() {
    return this.fieldName;
  }

  @Override
  public FieldValue getValue() {
    return this.fieldValue;
  }

  @Override
  public FieldDef getFieldDef() {
    return fieldDef;
  }

  public void setFieldDef(FieldDef fieldDef) {
    this.fieldDef = fieldDef;
  }

  public void setContent(ContentId content) {
    this.content = content;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (!Field.class.isAssignableFrom(obj.getClass())) {
      return false;
    }
    final Field other = (Field) obj;
    if ((this.fieldName == null) ? (other.getName() != null) : !this.fieldName.equals(other.getName())) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 97 * hash + (this.fieldName != null ? this.fieldName.hashCode() : 0);
    return hash;
  }

  @Override
  public ContentId getContent() {
    return content;
  }

  @Override
  public URI getRawFieldContentUri() {
    return SmartContentSPI.getInstance().getUriProvider().getRawFieldContentUri(content, fieldDef);
  }

  @Override
  public String getEncodedRawFieldContentUriAsString() {
    URI uri = getRawFieldContentUri();
    return uri == null ? null : uri.toASCIIString();
  }

  @Override
  public URI getAbsoluteRawFieldContentUri() {
    return SmartContentSPI.getInstance().getUriProvider().getAbsRawFieldContentUri(content, fieldDef);
  }

  @Override
  public String getEncodedAbsoluteRawFieldContentUriAsString() {
    URI uri = getAbsoluteRawFieldContentUri();
    return uri == null ? null : uri.toASCIIString();
  }

  @Override
  public Variation getVariation(String varName) {
    return SmartContentAPI.getInstance().getContentLoader().getVariation(content.getContent(), this, varName);
  }

  @Override
  public URI getUri() {
    if (content == null || fieldDef == null) {
      return null;
    }
    return SmartContentSPI.getInstance().getUriProvider().getFieldUri(content, fieldDef);
  }

  @Override
  public String getEncodedUriString() {
    URI uri = getUri();
    return uri == null ? null : uri.toASCIIString();
  }
}
