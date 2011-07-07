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
import java.util.List;
import java.util.Map;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 *
 * @author imyousuf
 */
public class CompositeFieldDefImpl extends FieldDefImpl implements CompositeFieldDef {

  private final Map<String, FieldDef> composedFields = new LinkedHashMap<String, FieldDef>();

  public Map<String, FieldDef> getComposedFields() {
    return composedFields;
  }

  @JsonIgnore
  public void setComposedFields(List<FieldDef> composedFields) {
    this.composedFields.clear();
    if (composedFields != null && !composedFields.isEmpty()) {
      for (FieldDef field : composedFields) {
        this.composedFields.put(field.getName(), field);
      }
    }
  }

  @JsonIgnore
  public Collection<FieldDef> getComposedFieldDefs() {
    return composedFields.values();
  }

  public void setComposedFields(Map<String, FieldDef> defs) {
    composedFields.clear();
    if (defs != null && !defs.isEmpty()) {
      composedFields.putAll(defs);
    }
  }
}
