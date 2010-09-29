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

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.smartitengineering.cms.api.common.TemplateType;
import com.smartitengineering.cms.api.workspace.RepresentationTemplate;
import com.smartitengineering.cms.api.workspace.VariationTemplate;
import com.smartitengineering.cms.api.workspace.Workspace;
import com.smartitengineering.cms.api.workspace.WorkspaceAPI;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.cms.spi.SmartContentSPI;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author imyousuf
 */
public class WorkspaceAPIImpl implements WorkspaceAPI {

  private String globalNamespace;

  @Inject
  public void setGlobalNamespace(@Named("globalNamespace") String globalNamespace) {
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
  public WorkspaceId createWorkspace(String globalNamespace, String name) {
    return createWorkspace(createWorkspaceId(globalNamespace, name));
  }

  @Override
  public WorkspaceId createWorkspace(WorkspaceId workspaceId) {
    SmartContentSPI.getInstance().getWorkspaceService().create(workspaceId);
    return workspaceId;
  }

  @Override
  public WorkspaceId createWorkspaceId(String name) {
    return createWorkspaceId(null, name);
  }

  @Override
  public WorkspaceId createWorkspaceId(final String namespace, String name) {
    final WorkspaceIdImpl workspaceIdImpl = new WorkspaceIdImpl();
    workspaceIdImpl.setGlobalNamespace(StringUtils.isBlank(namespace) ? getGlobalNamespace() : namespace);
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
    return SmartContentSPI.getInstance().getWorkspaceService().load(workspaceId);
  }

  @Override
  public Collection<Workspace> getWorkspaces() {
    return SmartContentSPI.getInstance().getWorkspaceService().getWorkspaces();
  }

  @Override
  public RepresentationTemplate putRepresentationTemplate(WorkspaceId to, String name, TemplateType templateType,
                                                          InputStream stream)
      throws IOException {
    return putRepresentationTemplate(to, name, templateType, IOUtils.toByteArray(stream));
  }

  @Override
  public RepresentationTemplate putRepresentationTemplate(WorkspaceId to, String name, TemplateType templateType,
                                                          byte[] data) {
    return SmartContentSPI.getInstance().getWorkspaceService().putRepresentationTemplate(to, name, templateType, data);
  }

  @Override
  public VariationTemplate putVariationTemplate(WorkspaceId to, String name, TemplateType templateType,
                                                InputStream stream) throws
      IOException {
    return putVariationTemplate(to, name, templateType, IOUtils.toByteArray(stream));
  }

  @Override
  public VariationTemplate putVariationTemplate(WorkspaceId to, String name, TemplateType templateType, byte[] data) {
    return SmartContentSPI.getInstance().getWorkspaceService().putVariationTemplate(to, name, templateType, data);
  }

  @Override
  public void delete(RepresentationTemplate template) {
    SmartContentSPI.getInstance().getWorkspaceService().deleteRepresentation(template);
  }

  @Override
  public void delete(VariationTemplate template) {
    SmartContentSPI.getInstance().getWorkspaceService().deleteVariation(template);
  }

  @Override
  public Collection<WorkspaceId> getFriendlies(WorkspaceId workspaceId) {
    return SmartContentSPI.getInstance().getWorkspaceService().getFriendlies(workspaceId);
  }

  @Override
  public void addFriend(WorkspaceId to, WorkspaceId workspaceId) {
    SmartContentSPI.getInstance().getWorkspaceService().addFriend(to, workspaceId);
  }

  @Override
  public void removeFriend(WorkspaceId from, WorkspaceId workspaceId) {
    SmartContentSPI.getInstance().getWorkspaceService().removeFriend(from, workspaceId);
  }
}
