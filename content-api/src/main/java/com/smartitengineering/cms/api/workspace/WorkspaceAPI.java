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

  RepresentationTemplate putRepresentationTemplate(String name, TemplateType templateType, InputStream stream) throws
      IOException;

  RepresentationTemplate putRepresentationTemplate(String name, TemplateType templateType, byte[] data);

  void delete(RepresentationTemplate template);

  void delete(VariationTemplate template);

  VariationTemplate putVariationTemplate(String name, TemplateType templateType, InputStream stream) throws IOException;

  VariationTemplate putVariationTemplate(String name, TemplateType templateType, byte[] data);

  WorkspaceId getWorkspaceIdIfExists(String name);

  WorkspaceId getWorkspaceIdIfExists(WorkspaceId workspaceId);

  Workspace getWorkspace(WorkspaceId workspaceId);

  Collection<Workspace> getWorkspaces();
}
