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

import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.impl.Utils;
import com.smartitengineering.cms.api.workspace.Sequence;
import com.smartitengineering.cms.api.workspace.SequenceId;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class SequenceIdImpl implements SequenceId {

  private WorkspaceId workspaceId;
  private String name;
  private final transient Logger logger = LoggerFactory.getLogger(getClass());

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public WorkspaceId getWorkspaceId() {
    return workspaceId;
  }

  public void setWorkspaceId(WorkspaceId workspaceId) {
    this.workspaceId = workspaceId;
  }

  @Override
  public String toString() {
    return new StringBuilder().append(workspaceId).append(':').append(name).toString();
  }

  @Override
  public void writeExternal(DataOutput output) throws IOException {
    output.write(org.apache.commons.codec.binary.StringUtils.getBytesUtf8(toString()));
  }

  @Override
  public void readExternal(DataInput input) throws IOException, ClassNotFoundException {
    String idString = Utils.readStringInUTF8(input);
    if (logger.isDebugEnabled()) {
      logger.debug("Trying to parse sequence id: " + idString);
    }
    if (StringUtils.isBlank(idString)) {
      throw new IOException("No Sequence!");
    }
    String[] params = idString.split(":");
    if (logger.isDebugEnabled()) {
      logger.debug("Params " + Arrays.toString(params));
    }
    if (params == null || params.length != 3) {
      throw new IOException(
          "Object should have been in the format globalNamespace:workspace-name:type-name!");
    }
    WorkspaceId workspaceIdImpl =
                SmartContentAPI.getInstance().getWorkspaceApi().createWorkspaceId(params[0], params[1]);
    setWorkspaceId(workspaceIdImpl);
    setName(params[2]);
  }

  public int compareTo(SequenceId o) {
    if (o == null) {
      return 1;
    }
    if (equals(o)) {
      return 0;
    }
    return toString().compareTo(o.toString());
  }

  public Sequence getSequence() {
    return SmartContentAPI.getInstance().getWorkspaceApi().getSequence(workspaceId, name);
  }
}
