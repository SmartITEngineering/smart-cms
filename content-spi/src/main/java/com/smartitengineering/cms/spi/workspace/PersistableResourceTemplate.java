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
package com.smartitengineering.cms.spi.workspace;

import com.smartitengineering.cms.api.common.TemplateType;
import com.smartitengineering.cms.api.workspace.ResourceTemplate;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import java.util.Date;

/**
 *
 * @author imyousuf
 */
public interface PersistableResourceTemplate extends ResourceTemplate {

  void setWorkspaceId(WorkspaceId workspaceId);

  void setName(String name);

  void setTemplateType(TemplateType templateType);

  void setTemplate(byte[] data);

  void setCreatedDate(Date creationDate);

  void setLastModifiedDate(Date lastModifiedDate);

  void setEntityTagValue(String entityTagValue);
}
