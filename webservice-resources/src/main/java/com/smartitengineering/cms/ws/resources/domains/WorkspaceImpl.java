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
package com.smartitengineering.cms.ws.resources.domains;

import java.util.Date;

/**
 *
 * @author imyousuf
 */
class WorkspaceImpl implements Workspace {

  private final com.smartitengineering.cms.api.workspace.Workspace workspace;
  private final WorkspaceId id;

  public WorkspaceImpl(com.smartitengineering.cms.api.workspace.Workspace workspace) {
    this.workspace = workspace;
    this.id = new WorkspaceIdImpl(workspace.getId());
  }

  @Override
  public Date getCreationDate() {
    return workspace.getCreationDate();
  }

  @Override
  public WorkspaceId getId() {
    return id;
  }

  static class WorkspaceIdImpl implements WorkspaceId {

    private final com.smartitengineering.cms.api.workspace.WorkspaceId id;

    public WorkspaceIdImpl(com.smartitengineering.cms.api.workspace.WorkspaceId id) {
      this.id = id;
    }

    @Override
    public String getGlobalNamespace() {
      return id.getGlobalNamespace();
    }

    @Override
    public String getName() {
      return id.getName();
    }
  }
}
