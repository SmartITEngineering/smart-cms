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

import com.smartitengineering.cms.api.common.TemplateType;
import com.smartitengineering.cms.api.workspace.ResourceTemplate;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.cms.spi.workspace.PersistableResourceTemplate;
import java.nio.ByteBuffer;
import java.util.Date;

/**
 *
 * @author imyousuf
 */
public class ResourceTemplateImpl implements PersistableResourceTemplate {

  public static final byte[] EMPTY_ARRAY = new byte[0];
  private String name;
  private TemplateType templateType;
  private ByteBuffer buffer;
  private Date createdDate;
  private Date lastModifiedDate;
  private WorkspaceId workspaceId;

  @Override
  public void setWorkspaceId(WorkspaceId workspaceId) {
    this.workspaceId = workspaceId;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public void setTemplateType(TemplateType templateType) {
    this.templateType = templateType;
  }

  @Override
  public void setTemplate(byte[] data) {
    this.buffer = ByteBuffer.wrap(data);
  }

  @Override
  public void setCreatedDate(Date creationDate) {
    this.createdDate = creationDate;
  }

  @Override
  public void setLastModifiedDate(Date lastModifiedDate) {
    this.lastModifiedDate = lastModifiedDate;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public TemplateType getTemplateType() {
    return templateType;
  }

  @Override
  public byte[] getTemplate() {
    if (buffer == null) {
      return EMPTY_ARRAY;
    }
    return buffer.array();
  }

  @Override
  public Date getCreatedDate() {
    return createdDate;
  }

  @Override
  public Date getLastModifiedDate() {
    return lastModifiedDate;
  }

  @Override
  public WorkspaceId getWorkspaceId() {
    return workspaceId;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (ResourceTemplate.class.isAssignableFrom(obj.getClass())) {
      return false;
    }
    final ResourceTemplate other = (ResourceTemplate) obj;
    if ((this.name == null) ? (other.getName() != null) : !this.name.equals(other.getName())) {
      return false;
    }
    if (this.workspaceId != other.getWorkspaceId() && (this.workspaceId == null || !this.workspaceId.equals(other.
        getWorkspaceId()))) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 29 * hash + (this.name != null ? this.name.hashCode() : 0);
    hash = 29 * hash + (this.workspaceId != null ? this.workspaceId.hashCode() : 0);
    return hash;
  }
}
