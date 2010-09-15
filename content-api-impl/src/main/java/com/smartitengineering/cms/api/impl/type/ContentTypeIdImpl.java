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

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ContentTypeIdImpl other = (ContentTypeIdImpl) obj;
    if ((this.newNamespace == null) ? (other.newNamespace != null) : !this.newNamespace.equals(other.newNamespace)) {
      return false;
    }
    if ((this.newContentTypeName == null) ? (other.newContentTypeName != null)
        : !this.newContentTypeName.equals(other.newContentTypeName)) {
      return false;
    }
    if (this.workspaceId != other.workspaceId &&
        (this.workspaceId == null || !this.workspaceId.equals(other.workspaceId))) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 43 * hash + (this.newNamespace != null ? this.newNamespace.hashCode() : 0);
    hash = 43 * hash + (this.newContentTypeName != null ? this.newContentTypeName.hashCode() : 0);
    hash = 43 * hash + (this.workspaceId != null ? this.workspaceId.hashCode() : 0);
    return hash;
  }
}
