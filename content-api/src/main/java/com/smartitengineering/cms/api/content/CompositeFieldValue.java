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
package com.smartitengineering.cms.api.content;

import java.util.Collection;
import java.util.Map;

/**
 *
 * @author imyousuf
 */
public interface CompositeFieldValue extends FieldValue<Collection<Field>> {

  Object getAsJsonNode();

  Map<String, Field> getValueAsMap();

  /**
   * Same as {@link CompositeFieldValue#getValueAsMap() }, is there to keep coherence with {@link Content#getFields() }
   * @return Fields value composed of
   */
  Map<String, Field> getFields();

  /**
   * Same as {@link CompositeFieldValue#getValueAsMap() valueMap}.get(String fieldName), is there to keep coherence with
   * {@link Content#getField(java.lang.String)  }
   * @param fieldName Name of the composed field
   * @return  Return the composed specified by field name
   */
  Field getField(String fieldName);
}
