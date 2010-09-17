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

import com.smartitengineering.cms.api.type.CollectionDataType;
import com.smartitengineering.cms.api.type.DataType;
import com.smartitengineering.cms.api.type.FieldValueType;
import com.smartitengineering.cms.api.type.MutableCollectionDataType;

/**
 *
 * @author kaisar
 */
public class CollectionDataTypeImpl implements MutableCollectionDataType {

  private DataType dataType;
  private int maxSize;
  private int minSize;

  @Override
  public void setItemDataType(DataType newDataType) throws IllegalArgumentException {
    this.dataType = newDataType;
  }

  @Override
  public void setMaxSize(int maxSize) {
    this.maxSize = maxSize;
  }

  @Override
  public void setMinSeize(int minSize) {
    this.minSize = minSize;
  }

  @Override
  public DataType getItemDataType() {

    return this.dataType;

  }

  @Override
  public int getMaxSize() {
    return this.maxSize;
  }

  @Override
  public int getMinSize() {
    return this.minSize;
  }

  @Override
  public FieldValueType getType() {
    return FieldValueType.COLLECTION;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (!CollectionDataType.class.isAssignableFrom(obj.getClass())) {
      return false;
    }
    final CollectionDataType other = (CollectionDataType) obj;
    if (this.dataType != other.getItemDataType() && (this.dataType == null || !this.dataType.equals(other.getItemDataType()))) {
      return false;
    }
    if (this.maxSize != other.getMaxSize()) {
      return false;
    }
    if (this.minSize != other.getMinSize()) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 89 * hash + (this.dataType != null ? this.dataType.hashCode() : 0);
    hash = 89 * hash + this.maxSize;
    hash = 89 * hash + this.minSize;
    return hash;
  }
}
