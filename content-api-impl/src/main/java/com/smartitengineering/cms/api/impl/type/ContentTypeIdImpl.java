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

import com.smartitengineering.cms.api.WorkspaceId;
import com.smartitengineering.cms.api.type.MutableContentTypeId;

/**
 *
 * @author kaisar
 */
public class ContentTypeIdImpl implements MutableContentTypeId {

  private String newNamespace;
  private String newContentTypeName;
  private WorkspaceId workspaceId;

  @Override
  public void setNamespace(String newNamespace) {
    this.newNamespace = newNamespace;
  }

  @Override
  public void setName(String newContentTypeName) throws IllegalArgumentException {
    this.newContentTypeName = newContentTypeName;
  }

  @Override
  public void setWorkspace(WorkspaceId workspaceId) {
    this.workspaceId = workspaceId;
  }

  @Override
  public WorkspaceId getWorkspace() {
    return this.workspaceId;
  }

  @Override
  public String getName() {
    return this.newContentTypeName;
  }

  @Override
  public String getNamespace() {
    return this.newNamespace;
  }
}
