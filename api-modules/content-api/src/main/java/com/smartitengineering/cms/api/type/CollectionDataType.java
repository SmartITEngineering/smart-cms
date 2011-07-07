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
 * Defines the datatype for representing collection
 * @author imyousuf
 * @since 0.1
 */
public interface CollectionDataType
    extends DataType {

  /**
   * Retrieve the definition of the collection's items. Since currently
   * collection only supports homogeneous types so it will return only a
   * definite existing content type definition.
   * @return Definition of collection's items.
   */
  public DataType getItemDataType();

  /**
   * Retrieve the maximum size of the collection. If it is a non positive
   * number, it will indicate that the upper bound of the collection
   * is infinity.
   * @return the maximum size
   */
  public int getMaxSize();

  public int getMinSize();
}
