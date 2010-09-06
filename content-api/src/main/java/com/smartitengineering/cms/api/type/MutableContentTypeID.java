/*
 *
 * This is a simple Content Management System (CMS)
 * Copyright (C) 2009  Imran M Yousuf (imyousuf@smartitengineering.com)
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

import com.smartitengineering.cms.api.WorkspaceId;

/**
 * A {@link ContentTypeID} that can be edited.
 * @author imyousuf
 * @since 0.1
 */
public interface MutableContentTypeID
    extends ContentTypeID {

  /**
   * Sets the new namespace of the content type. If null it will be treated as
   * blank.
   * @param newNamespace The new namespace for the content type
   */
  public void setNamespace(String newNamespace);

  /**
   * Sets the new name for this content type. Blank string is not accepted as
   * name of the content type.
   * @param newContentTypeName The new name of the content type
   * @throws IllegalArgumentException If the new name is empty or null
   */
  public void setName(String newContentTypeName)
      throws IllegalArgumentException;

  public void setWorkspace(WorkspaceId workspaceId);
}
