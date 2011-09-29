/*
 *
 * This is a simple Content Management System (CMS)
 * Copyright (C) 2011  Imran M Yousuf (imyousuf@smartitengineering.com)
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
package com.smartitengineering.cms.api.impl.workspace;

import com.smartitengineering.cms.api.workspace.WorkspaceId;
import java.io.Serializable;

/**
 *
 * @author imyousuf
 */
public class WorkspaceResourceCacheKey implements Serializable {

  private final WorkspaceId workspaceId;
  private final WorkspaceResourceType type;
  private final String localName;

  public WorkspaceResourceCacheKey(WorkspaceId workspaceId, WorkspaceResourceType type, String localName) {
    this.workspaceId = workspaceId;
    this.type = type;
    this.localName = localName;
  }

  public String getLocalName() {
    return localName;
  }

  public WorkspaceResourceType getType() {
    return type;
  }

  public WorkspaceId getWorkspaceId() {
    return workspaceId;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final WorkspaceResourceCacheKey other = (WorkspaceResourceCacheKey) obj;
    if (this.workspaceId != other.workspaceId &&
        (this.workspaceId == null || !this.workspaceId.equals(other.workspaceId))) {
      return false;
    }
    if (this.type != other.type) {
      return false;
    }
    if ((this.localName == null) ? (other.localName != null) : !this.localName.equals(other.localName)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 97 * hash + (this.workspaceId != null ? this.workspaceId.hashCode() : 0);
    hash = 97 * hash + (this.type != null ? this.type.hashCode() : 0);
    hash = 97 * hash + (this.localName != null ? this.localName.hashCode() : 0);
    return hash;
  }

  @Override
  public String toString() {
    return new StringBuilder().append(workspaceId.toString()).append(':').append(type).append(':').append(localName).
        toString();
  }

  public static enum WorkspaceResourceType {

    REPRESENTATION_GEN, VARIATION_GEN, VALIDATION_SCR, CONTENT_CO_PROCESSOR_GEN;
  }
}
