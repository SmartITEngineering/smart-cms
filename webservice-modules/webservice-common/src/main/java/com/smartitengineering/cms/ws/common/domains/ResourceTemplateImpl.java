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
package com.smartitengineering.cms.ws.common.domains;

import java.util.Date;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 *
 * @author imyousuf
 */
public class ResourceTemplateImpl implements ResourceTemplate {

  private WorkspaceId workspaceId;
  private String name, templateType;
  private byte[] template;
  private Date createdDate, lastModifiedDate;

  @Override
  public WorkspaceId getWorkspaceId() {
    return workspaceId;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getTemplateType() {
    return templateType;
  }

  @Override
  public byte[] getTemplate() {
    return template;
  }

  @Override
  @JsonIgnore
  public String getTemplateString() {
    return new String(template);
  }

  @Override
  public Date getCreatedDate() {
    return createdDate;
  }

  @Override
  public Date getLastModifiedDate() {
    return lastModifiedDate;
  }

  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }

  public void setLastModifiedDate(Date lastModifiedDate) {
    this.lastModifiedDate = lastModifiedDate;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setTemplate(byte[] template) {
    this.template = template;
  }

  public void setTemplateType(String templateType) {
    this.templateType = templateType;
  }

  public void setWorkspaceId(WorkspaceId workspaceId) {
    this.workspaceId = workspaceId;
  }
}
