/*
 *
 * This is a simple Content Management System (CMS)
 * Copyright (C) 2011  Imran M Yousuf (imyousuf@smartitengineering.com)
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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 *
 * @author imyousuf
 */
public class CompositeFieldValueImpl extends FieldValueImpl implements CompositeFieldValue {

  private final Map<String, Field> values = new LinkedHashMap<String, Field>();

  public Map<String, Field> getValues() {
    return values;
  }

  public void setValues(Map<String, Field> values) {
    this.values.clear();
    if (values != null && !values.isEmpty()) {
      this.values.putAll(values);
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

  @JsonIgnore
  public void setValuesAsCollection(Collection<Field> values) {
    this.values.clear();
    if (values != null && !values.isEmpty()) {
      for (Field field : values) {
        this.values.put(field.getName(), field);
      }
    }
  }
}
