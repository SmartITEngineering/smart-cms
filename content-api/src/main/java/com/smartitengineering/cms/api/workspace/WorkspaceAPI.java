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
package com.smartitengineering.cms.api.workspace;

import com.smartitengineering.cms.api.common.TemplateType;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

/**
 *
 * @author imyousuf
 */
public interface WorkspaceAPI {

  String getGlobalNamespace();

  WorkspaceId createWorkspaceId(String globalNamespace, String name);

  WorkspaceId createWorkspaceId(String name);

  WorkspaceId createWorkspace(String name);

  WorkspaceId createWorkspace(String globalNamespace, String name);

  WorkspaceId createWorkspace(WorkspaceId workspaceId);

  RepresentationTemplate putRepresentationTemplate(WorkspaceId to, String name, TemplateType templateType,
                                                   InputStream stream) throws
      IOException;

  RepresentationTemplate putRepresentationTemplate(WorkspaceId to, String name, TemplateType templateType, byte[] data);

  RepresentationTemplate getRepresentationTemplate(WorkspaceId id, String name);

  void delete(RepresentationTemplate template);

  void delete(VariationTemplate template);

  VariationTemplate putVariationTemplate(WorkspaceId to, String name, TemplateType templateType, InputStream stream)
      throws IOException;

  VariationTemplate putVariationTemplate(WorkspaceId to, String name, TemplateType templateType, byte[] data);

  VariationTemplate getVariationTemplate(WorkspaceId id, String name);

  WorkspaceId getWorkspaceIdIfExists(String name);

  WorkspaceId getWorkspaceIdIfExists(WorkspaceId workspaceId);

  Workspace getWorkspace(WorkspaceId workspaceId);

  Collection<Workspace> getWorkspaces();

  public Collection<WorkspaceId> getFriendlies(WorkspaceId workspaceId);

  public void addFriend(WorkspaceId to, WorkspaceId... workspaceIds);

  public void removeFriend(WorkspaceId from, WorkspaceId workspaceId);

  public void removeAllFriendlies(WorkspaceId workspaceId);

  public void removeAllRepresentationTemplates(WorkspaceId workspaceId);

  public void removeAllVariationTemplates(WorkspaceId workspaceId);

  enum ResourceSortCriteria {

    BY_NAME,
    BY_DATE,
  }

  public Collection<String> getRepresentationNames(WorkspaceId id);

  public Collection<String> getVariationNames(WorkspaceId id);

  public Collection<String> getRepresentationNames(WorkspaceId id, String startPoint, int count);

  public Collection<String> getVariationNames(WorkspaceId id, String startPoint, int count);

  public Collection<String> getRepresentationNames(WorkspaceId id, ResourceSortCriteria criteria);

  public Collection<String> getVariationNames(WorkspaceId id, ResourceSortCriteria criteria);

  public Collection<String> getRepresentationNames(WorkspaceId id, ResourceSortCriteria criteria, String startPoint, int count);

  public Collection<String> getVariationNames(WorkspaceId id, ResourceSortCriteria criteria, String startPoint, int count);
}
