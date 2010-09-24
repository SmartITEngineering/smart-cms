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

import com.smartitengineering.cms.api.type.MutableValidatorDef;
import com.smartitengineering.cms.api.type.ResourceUri;
import com.smartitengineering.cms.api.type.ValidatorDef;
import com.smartitengineering.cms.api.type.ValidatorType;

/**
 *
 * @author kaisar
 */
public class ValidatorDefImpl implements MutableValidatorDef {

  private ValidatorType validatorType;
  private ResourceUri uri;

  @Override
  public void seType(ValidatorType validatorType) {
    this.validatorType = validatorType;
  }

  @Override
  public void setUri(ResourceUri uri) {
    this.uri = uri;
  }

  @Override
  public ValidatorType geType() {
    return this.validatorType;
  }

  @Override
  public ResourceUri getUri() {
    return this.uri;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (ValidatorDef.class.isAssignableFrom(obj.getClass())) {
      return false;
    }
    final ValidatorDef other = (ValidatorDef) obj;
    if (this.validatorType != other.geType()) {
      return false;
    }
    if (this.uri != other.getUri() && (this.uri == null || !this.uri.equals(other.getUri()))) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 19 * hash + (this.validatorType != null ? this.validatorType.hashCode() : 0);
    hash = 19 * hash + (this.uri != null ? this.uri.hashCode() : 0);
    return hash;
  }

  @Override
  public String toString() {
    return "ValidatorDefImpl{" + "validatorType=" + validatorType + "; uri=" + uri + '}';
  }
}
