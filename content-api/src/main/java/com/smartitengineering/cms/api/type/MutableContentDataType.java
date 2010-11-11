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
package com.smartitengineering.cms.api.type;

/**
 * The editable version of {@link ContentDataType}
 * @author imyousuf
 * @since 0.1
 */
public interface MutableContentDataType
    extends ContentDataType {

  /**
   * Sets the limiting definition of the content type, i.e. what type of
   * content may be related using this {@link DataType}. If null that means
   * any content type is allowed.
   * @param typeDef The type name to restrict on, if null no restriction.
   */
  public void setTypeDef(ContentTypeId typeDef);

  public void setBiBidirectionalFieldName(String fieldName);

  public void setAvailableForSearch(boolean availableForSearch);
}
