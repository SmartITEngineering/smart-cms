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

import com.smartitengineering.cms.ws.common.domains.CollectionFieldDef;
import com.smartitengineering.cms.ws.common.domains.CompositeFieldDef;
import com.smartitengineering.cms.ws.common.domains.ContentFieldDef;
import com.smartitengineering.cms.ws.common.domains.EnumFieldDef;
import com.smartitengineering.cms.ws.common.domains.FieldDef;
import com.smartitengineering.cms.ws.common.domains.OtherFieldDef;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonTypeInfo.Id;
import org.codehaus.jackson.map.jsontype.TypeIdResolver;
import org.codehaus.jackson.map.type.SimpleType;
import org.codehaus.jackson.type.JavaType;

/**
 *
 * @author imyousuf
 */
public class FieldDefTypeIdResolver implements TypeIdResolver {

  @Override
  public void init(JavaType baseType) {
  }

  @Override
  public String idFromValue(Object value) {
    if (value instanceof FieldDef) {
      FieldDef fieldValue = (FieldDef) value;
      return fieldValue.getType();
    }
    else {
      throw new IllegalArgumentException("Only supports FieldDef!");
    }
  }

  @Override
  public JavaType typeFromId(String id) {
    if (StringUtils.equalsIgnoreCase(id, "collection")) {
      return SimpleType.construct(CollectionFieldDef.class);
    }
    else if (StringUtils.equalsIgnoreCase(id, "content")) {
      return SimpleType.construct(ContentFieldDef.class);
    }
    else if (StringUtils.equalsIgnoreCase(id, "composite")) {
      return SimpleType.construct(CompositeFieldDef.class);
    }
    else if (StringUtils.equalsIgnoreCase(id, "enum")) {
      return SimpleType.construct(EnumFieldDef.class);
    }
    else if (StringUtils.equalsIgnoreCase(id, "string") || StringUtils.equalsIgnoreCase(id, "other")) {
      return SimpleType.construct(OtherFieldDef.class);
    }
    else {
      return SimpleType.construct(FieldDef.class);
    }
  }

  @Override
  public Id getMechanism() {
    return Id.CUSTOM;
  }
}
