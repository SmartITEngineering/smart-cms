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

import com.smartitengineering.cms.api.type.FieldValueType;
import com.smartitengineering.cms.api.type.MutableStringDataType;
import com.smartitengineering.cms.api.type.StringDataType;

/**
 *
 * @author kaisar
 */
public class StringDataTypeImpl implements MutableStringDataType {

  private String encoding;
  private String mimeType;

  @Override
  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  @Override
  public String getEncoding() {
    return this.encoding;
  }

  @Override
  public void setMIMEType(String mimeType) {
    this.mimeType = mimeType;
  }

  @Override
  public String getMIMEType() {
    return this.mimeType;
  }

  @Override
  public FieldValueType getType() {
    return FieldValueType.STRING;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (StringDataType.class.isAssignableFrom(obj.getClass())) {
      return false;
    }
    final StringDataType other = (StringDataType) obj;
    if ((this.encoding == null) ? (other.getEncoding() != null) : !this.encoding.equals(other.getEncoding())) {
      return false;
    }
    if ((this.mimeType == null) ? (other.getMIMEType() != null) : !this.mimeType.equals(other.getMIMEType())) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 23 * hash + (this.encoding != null ? this.encoding.hashCode() : 0);
    hash = 23 * hash + (this.mimeType != null ? this.mimeType.hashCode() : 0);
    return hash;
  }
}
