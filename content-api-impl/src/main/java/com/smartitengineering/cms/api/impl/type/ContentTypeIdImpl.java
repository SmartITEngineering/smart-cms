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
import com.smartitengineering.cms.api.impl.Utils;
import com.smartitengineering.cms.api.impl.WorkspaceIdImpl;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.api.type.MutableContentTypeId;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author kaisar
 */
public class ContentTypeIdImpl implements MutableContentTypeId {

  public static final String STANDARD_ERROR_MSG = "%s can not be blank or contain ':'";
  private String newNamespace;
  private String newContentTypeName;
  private WorkspaceId workspaceId;

  @Override
  public void setNamespace(String newNamespace) {
    if (StringUtils.isBlank(newNamespace) || StringUtils.containsAny(newNamespace, new char[]{':'})) {
      throw new IllegalArgumentException(String.format(STANDARD_ERROR_MSG, "Namespace"));
    }
    this.newNamespace = newNamespace;
  }

  @Override
  public void setName(String newContentTypeName) throws IllegalArgumentException {
    if (StringUtils.isBlank(newContentTypeName) || StringUtils.containsAny(newContentTypeName, new char[]{':'})) {
      throw new IllegalArgumentException(String.format(STANDARD_ERROR_MSG, "Content Type Name"));
    }
    this.newContentTypeName = newContentTypeName;
  }

  @Override
  public void setWorkspace(WorkspaceId workspaceId) {
    if (workspaceId == null) {
      throw new IllegalArgumentException(String.format(STANDARD_ERROR_MSG, "Workspace Id"));
    }
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
    if (!ContentTypeId.class.isAssignableFrom(obj.getClass())) {
      return false;
    }
    final ContentTypeId other = (ContentTypeId) obj;
    if ((this.newNamespace == null) ? (other.getNamespace() != null) : !this.newNamespace.equals(other.getNamespace())) {
      return false;
    }
    if ((this.newContentTypeName == null) ? (other.getName() != null) : !this.newContentTypeName.equals(other.getName())) {
      return false;
    }
    if (this.workspaceId != other.getWorkspace() &&
        (this.workspaceId == null || !this.workspaceId.equals(other.getWorkspace()))) {
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

  @Override
  public String toString() {
    return new StringBuilder().append(workspaceId).append(':').
        append(newNamespace).append(':').append(newContentTypeName).toString();
  }

  @Override
  public void writeExternal(DataOutput output) throws IOException {
    output.write(toString().getBytes("UTF-8"));
  }

  @Override
  public void readExternal(DataInput input) throws IOException, ClassNotFoundException {
    String idString = Utils.readStringInUTF8(input);
    if (StringUtils.isBlank(idString)) {
      throw new IOException("No content!");
    }
    String[] params = idString.split(":");
    if (params == null || params.length != 4) {
      throw new IOException(
          "Object should have been in the format globalNamespace:workspace-name:type-namespace:type-name!");
    }
    WorkspaceIdImpl workspaceIdImpl = new WorkspaceIdImpl();
    workspaceIdImpl.setGlobalNamespace(params[0]);
    workspaceIdImpl.setName(params[1]);
    setWorkspace(workspaceIdImpl);
    setNamespace(params[2]);
    setName(params[3]);

  }

  @Override
  public int compareTo(ContentTypeId o) {
    if (o == null) {
      return 1;
    }
    if (equals(o)) {
      return 0;
    }
    return toString().compareTo(o.toString());
  }
}
