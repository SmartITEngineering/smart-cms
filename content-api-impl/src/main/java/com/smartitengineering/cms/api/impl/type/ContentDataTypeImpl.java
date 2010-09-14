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

import com.smartitengineering.cms.api.type.ContentDataType;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.api.type.FieldValueType;
import com.smartitengineering.cms.api.type.MutableContentDataType;

/**
 *
 * @author kaisar
 */
public class ContentDataTypeImpl implements MutableContentDataType,ContentDataType {

  private ContentTypeId contentTypeId;
  private String bidirectionalFieldName;

  @Override
  public ContentTypeId getTypeDef() {
    return this.contentTypeId;
  }

  @Override
  public String getBidirectionalFieldName() {
    return this.bidirectionalFieldName;
  }

  @Override
  public void setTypeDef(ContentTypeId typeDef) {
    this.contentTypeId = typeDef;
  }

  @Override
  public void setBiBidirectionalFieldName(String fieldName) {
    this.bidirectionalFieldName = fieldName;
  }

  @Override
  public FieldValueType getType() {
    return FieldValueType.CONTENT;
  }
}
