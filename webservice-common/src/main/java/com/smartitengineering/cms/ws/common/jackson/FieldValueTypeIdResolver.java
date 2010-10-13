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
package com.smartitengineering.cms.ws.common.jackson;

import com.smartitengineering.cms.ws.common.domains.CollectionFieldValue;
import com.smartitengineering.cms.ws.common.domains.FieldValue;
import com.smartitengineering.cms.ws.common.domains.OtherFieldValue;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonTypeInfo.Id;
import org.codehaus.jackson.map.jsontype.TypeIdResolver;
import org.codehaus.jackson.map.type.SimpleType;
import org.codehaus.jackson.type.JavaType;

/**
 *
 * @author imyousuf
 */
public class FieldValueTypeIdResolver implements TypeIdResolver {

  @Override
  public void init(JavaType baseType) {
  }

  @Override
  public String idFromValue(Object value) {
    if (value instanceof FieldValue) {
      FieldValue fieldValue = (FieldValue) value;
      return fieldValue.getType();
    }
    else {
      throw new IllegalArgumentException("Only supports FieldValue!");
    }
  }

  @Override
  public JavaType typeFromId(String id) {
    if (StringUtils.equalsIgnoreCase(id, "collection")) {
      return SimpleType.construct(CollectionFieldValue.class);
    }
    else if (StringUtils.equalsIgnoreCase(id, "string") || StringUtils.equalsIgnoreCase(id, "other")) {
      return SimpleType.construct(OtherFieldValue.class);
    }
    else {
      return SimpleType.construct(FieldValue.class);
    }
  }

  @Override
  public Id getMechanism() {
    return Id.CUSTOM;
  }
}
