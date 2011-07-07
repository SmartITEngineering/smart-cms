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
 * The editable {@link ContentStatus}
 * @author imyousuf
 * @since 0.1
 */
public interface MutableContentStatus
    extends ContentStatus {

  /**
   * Sets the id of the status. so that it could be directly retrieved from a
   * content type.
   * @param id The new id of the status.
   */
  public void setId(int id);

  /**
   * Sets the name of the status. It has to be non-blank
   * @param newName The new name of the status
   * @throws IllegalArgumentException If newName is blank
   */
  public void setName(String newName)
      throws IllegalArgumentException;

  /**
   * Set the content type id of the status. This could be used to change the
   * content type of a status
   * @param typeId Content Type's identifier for this status
   * @throws IllegalArgumentException If typeId is null
   */
  public void setContentTypeID(ContentTypeId typeId)
      throws IllegalArgumentException;
}
