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

import java.util.Map;

/**
 *
 * @author imyousuf
 */
public class FieldDefImpl implements FieldDef {

  private String name, type, displayName;
  private boolean required;
  private Map<String, String> parameters;
  private Map<String, String> parameterizedDisplayNames;

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public boolean isRequired() {
    return required;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setRequired(boolean required) {
    this.required = required;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Map<String, String> getParameters() {
    return parameters;
  }

  public void setParameters(Map<String, String> parameters) {
    this.parameters = parameters;
  }

  public Map<String, String> getParameterizedDisplayNames() {
    return parameterizedDisplayNames;
  }

  public void setParameterizedDisplayNames(Map<String, String> parameterizedDisplayNames) {
    this.parameterizedDisplayNames = parameterizedDisplayNames;
  }
}
