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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author imyousuf
 */
public class CollectionFieldValueImpl extends FieldValueImpl implements CollectionFieldValue {

  private List<FieldValue> values = new ArrayList<FieldValue>();

  @Override
  public Collection<FieldValue> getValues() {
    return values;
  }

  public void setValues(Collection<FieldValue> values) {
    this.values.clear();
    if (values != null) {
      this.values.addAll(values);
    }
  }

  @Override
  public String getValue() {
    return "";
  }

  @Override
  public void setValue(String value) {
    throw new UnsupportedOperationException("Should not be called from here!");
  }
}
