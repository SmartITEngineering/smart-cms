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

import com.smartitengineering.cms.ws.common.jackson.FieldDefTypeIdResolver;
import java.util.Map;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonTypeIdResolver;

/**
 *
 * @author imyousuf
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "@valueType")
@JsonTypeIdResolver(FieldDefTypeIdResolver.class)
@JsonDeserialize(as = FieldDefImpl.class)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public interface FieldDef {

  String getName();

  String getType();

  boolean isRequired();

  Map<String, String> getParameters();

  public Map<String, String> getParameterizedDisplayNames();
}
