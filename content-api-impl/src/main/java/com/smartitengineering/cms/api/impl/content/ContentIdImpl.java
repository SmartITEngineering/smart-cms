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
package com.smartitengineering.cms.api.impl.content;

import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.cms.api.content.ContentId;
import com.smartitengineering.cms.api.impl.Utils;
import com.smartitengineering.cms.api.impl.workspace.WorkspaceIdImpl;
import com.smartitengineering.cms.api.impl.type.ContentTypeIdImpl;
import com.smartitengineering.cms.spi.SmartContentSPI;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author kaisar
 */
public class ContentIdImpl implements ContentId {

  private WorkspaceId workspaceId;
  private byte[] id;

  public void setWorkspaceId(WorkspaceId workspaceId) {
    if (workspaceId == null) {
      throw new IllegalArgumentException(String.format(ContentTypeIdImpl.STANDARD_ERROR_MSG, "Workspace Id"));
    }
    this.workspaceId = workspaceId;
  }

  public void setId(byte[] id) {
    if (id == null || id.length <= 0) {
      throw new IllegalArgumentException(String.format(ContentTypeIdImpl.STANDARD_ERROR_MSG, "Content Id"));
    }
    this.id = id;
  }

  @Override
  public WorkspaceId getWorkspaceId() {
    return this.workspaceId;
  }

  @Override
  public byte[] getId() {
    return this.id;
  }

  @Override
  public String toString() {
    return new StringBuilder().append(workspaceId).append(SmartContentSPI.getInstance().getContentIdProcessor().
        getIdAsString(id)).toString();
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
    setWorkspaceId(workspaceIdImpl);
    setId(SmartContentSPI.getInstance().getContentIdProcessor().getStringAsId(params[2]));
  }

  @Override
  public int compareTo(ContentId o) {
    if (o == null) {
      return 1;
    }
    if (equals(o)) {
      return 0;
    }
    return toString().compareTo(o.toString());
  }
}
