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

import com.smartitengineering.cms.ws.common.domains.ResourceTemplate;
import com.smartitengineering.cms.ws.common.domains.ResourceTemplateImpl;
import com.smartitengineering.cms.ws.common.domains.WorkspaceImpl;
import com.smartitengineering.cms.ws.common.domains.Workspace;

/**
 *
 * @author imyousuf
 */
public final class Factory {

  private Factory() {
  }

  public static Workspace getWorkspace(com.smartitengineering.cms.api.workspace.Workspace workspace) {
    return new WorkspaceImpl(new WorkspaceImpl.WorkspaceIdImpl(workspace.getId().getGlobalNamespace(), workspace.getId().
        getName()), workspace.getCreationDate());
  }

  public static ResourceTemplate getResourceTemplate(com.smartitengineering.cms.api.workspace.ResourceTemplate t) {
    ResourceTemplateImpl template = new ResourceTemplateImpl();
    template.setCreatedDate(t.getCreatedDate());
    template.setLastModifiedDate(t.getLastModifiedDate());
    template.setName(t.getName());
    template.setTemplate(t.getTemplate());
    template.setTemplateType(t.getTemplateType().name());
    template.setWorkspaceId(new WorkspaceImpl.WorkspaceIdImpl(t.getWorkspaceId().getGlobalNamespace(), t.getWorkspaceId().
        getName()));
    return template;
  }
}
