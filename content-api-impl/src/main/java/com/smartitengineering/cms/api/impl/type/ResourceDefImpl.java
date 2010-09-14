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
package com.smartitengineering.cms.api.impl.type;

import com.smartitengineering.cms.api.type.MutuableResourceDef;
import com.smartitengineering.cms.api.type.ResourceDef;
import com.smartitengineering.cms.api.type.ResourceUri;
import com.smartitengineering.cms.api.type.TemplateType;
import java.util.Date;

/**
 *
 * @author kaisar
 */
public class ResourceDefImpl implements MutuableResourceDef, ResourceDef {

  private TemplateType templateType;
  private String mimeType;
  private String name;
  private ResourceUri resourceUri;
  private Date creationDate;
  private Date lastModifiedDate;

  @Override
  public void setTemplateType(TemplateType templateType) {
    this.templateType = templateType;
  }

  @Override
  public void setMIMEType(String mimeType) {
    this.mimeType = mimeType;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public void setResourceUri(ResourceUri resourceUri) {
    this.resourceUri = resourceUri;
  }

  @Override
  public TemplateType getTemplateType() {
    return this.templateType;
  }

  @Override
  public String getMIMEType() {
    return this.mimeType;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public ResourceUri getResourceUri() {
    return this.resourceUri;
  }

  @Override
  public Date getCreationDate() {
    return this.creationDate;
  }

  @Override
  public Date getLastModifiedDate() {
    return this.lastModifiedDate;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public void setLastModifiedDate(Date lastModifiedDate) {
    this.lastModifiedDate = lastModifiedDate;
  }
}
