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

import com.smartitengineering.cms.api.type.MutableResourceUri;
import com.smartitengineering.cms.api.type.ResourceUri;

/**
 *
 * @author kaisar
 */
public class ResourceUriImpl implements MutableResourceUri {

  private Type type;
  private String value;

  @Override
  public void setType(Type type) {
    this.type = type;
  }

  @Override
  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public Type getType() {
    return this.type;
  }

  @Override
  public String getValue() {
    return this.value;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (ResourceUri.class.isAssignableFrom(obj.getClass())) {
      return false;
    }
    final ResourceUri other = (ResourceUri) obj;
    if (this.type != other.getType()) {
      return false;
    }
    if ((this.value == null) ? (other.getValue() != null) : !this.value.equals(other.getValue())) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 89 * hash + (this.type != null ? this.type.hashCode() : 0);
    hash = 89 * hash + (this.value != null ? this.value.hashCode() : 0);
    return hash;
  }
}
