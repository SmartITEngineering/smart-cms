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
package com.smartitengineering.cms.api.impl;

import com.google.inject.Inject;
import com.smartitengineering.cms.api.Workspace;
import com.smartitengineering.cms.api.WorkspaceAPI;
import com.smartitengineering.cms.api.WorkspaceId;
import com.smartitengineering.cms.spi.SmartSPI;
import java.util.Collection;

/**
 *
 * @author imyousuf
 */
public class WorkspaceAPIImpl implements WorkspaceAPI {

  private String globalNamespace;

  @Inject
  public void setGlobalNamespace(String globalNamespace) {
    this.globalNamespace = globalNamespace;
  }

  @Override
  public String getGlobalNamespace() {
    return globalNamespace;
  }

  @Override
  public WorkspaceId createWorkspace(String name) {
    WorkspaceId workspaceIdImpl = createWorkspaceId(name);
    return createWorkspace(workspaceIdImpl);
  }

  @Override
  public WorkspaceId createWorkspace(WorkspaceId workspaceId) {
    SmartSPI.getInstance().getWorkspaceService().create(workspaceId);
    return workspaceId;
  }

  protected WorkspaceId createWorkspaceId(String name) {
    final WorkspaceIdImpl workspaceIdImpl = new WorkspaceIdImpl();
    workspaceIdImpl.setGlobalNamespace(getGlobalNamespace());
    workspaceIdImpl.setName(name);
    return workspaceIdImpl;
  }

  @Override
  public WorkspaceId getWorkspaceIdIfExists(String name) {
    final WorkspaceId createdWorkspaceId = createWorkspaceId(name);
    return getWorkspaceIdIfExists(createdWorkspaceId);
  }

  @Override
  public WorkspaceId getWorkspaceIdIfExists(WorkspaceId workspaceId) {
    Workspace workspace = getWorkspace(workspaceId);
    if (workspace != null) {
      return workspaceId;
    }
    return null;
  }

  @Override
  public Workspace getWorkspace(WorkspaceId workspaceId) {
    return SmartSPI.getInstance().getWorkspaceService().load(workspaceId);
  }

  @Override
  public Collection<Workspace> getWorkspaces() {
    return SmartSPI.getInstance().getWorkspaceService().getWorkspaces();
  }
}
