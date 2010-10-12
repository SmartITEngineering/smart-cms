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

import com.smartitengineering.cms.api.content.MutableFieldValue;
import com.smartitengineering.cms.api.type.FieldValueType;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author kaisar
 */
public class FieldValueImpl<V> implements MutableFieldValue<V> {

  private FieldValueType fieldValueType;
  private V value;
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public void setValue(V newV) {
    this.value = newV;
  }

  @Override
  public FieldValueType getDataType() {
    return this.fieldValueType;
  }

  @Override
  public V getValue() {
    return value;
  }

  protected String getValueAsString() {
    if (getValue() == null) {
      return null;
    }
    return getValue().toString();
  }

  @Override
  public final String toString() {
    return StringUtils.defaultIfEmpty(getValueAsString(), "NULL");
  }
}
