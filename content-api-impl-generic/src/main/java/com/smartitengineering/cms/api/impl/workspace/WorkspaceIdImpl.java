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
package com.smartitengineering.cms.api.impl.workspace;

import com.smartitengineering.cms.api.impl.Utils;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.cms.api.impl.type.ContentTypeIdImpl;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author imyousuf
 */
public class WorkspaceIdImpl implements WorkspaceId {

  private String globalNamespace;
  private String name;

  public void setGlobalNamespace(String globalNamespace) {
    if (StringUtils.isBlank(globalNamespace) || StringUtils.containsAny(globalNamespace, new char[]{':'})) {
      throw new IllegalArgumentException(String.format(ContentTypeIdImpl.STANDARD_ERROR_MSG, "Global Namespace"));
    }
    this.globalNamespace = globalNamespace;
  }

  public void setName(String name) {
    if (StringUtils.isBlank(name) || StringUtils.containsAny(name, new char[]{':'})) {
      throw new IllegalArgumentException(String.format(ContentTypeIdImpl.STANDARD_ERROR_MSG, "Workspace name"));
    }
    this.name = name;
  }

  @Override
  public String getGlobalNamespace() {
    return globalNamespace;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void writeExternal(DataOutput out) throws IOException {
    out.write(org.apache.commons.codec.binary.StringUtils.getBytesUtf8(toString()));
  }

  @Override
  public void readExternal(DataInput in) throws IOException, ClassNotFoundException {
    String idString = Utils.readStringInUTF8(in);
    if (StringUtils.isBlank(idString)) {
      throw new IOException("No content!");
    }
    String[] params = idString.split(":");
    if (params == null || params.length != 2) {
      throw new IOException("Object should have been in the format globalNamespace:name!");
    }
    setGlobalNamespace(params[0]);
    setName(params[1]);
  }

  @Override
  public String toString() {
    return new StringBuilder(getGlobalNamespace()).append(':').append(getName()).toString();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      System.out.println("1");
      return false;
    }
    if (!WorkspaceId.class.isAssignableFrom(obj.getClass())) {
      return false;
    }
    final WorkspaceId other = (WorkspaceId) obj;
    if ((this.globalNamespace == null) ? (other.getGlobalNamespace() != null) : !this.globalNamespace.equals(other.
        getGlobalNamespace())) {
      return false;
    }
    if ((this.name == null) ? (other.getName() != null) : !this.name.equals(other.getName())) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + (this.globalNamespace != null ? this.globalNamespace.hashCode() : 0);
    hash = 31 * hash + (this.name != null ? this.name.hashCode() : 0);
    return hash;
  }

  @Override
  public int compareTo(WorkspaceId o) {
    if (o == null) {
      return 1;
    }
    if (equals(o)) {
      return 0;
    }
    return toString().compareTo(o.toString());
  }
}
