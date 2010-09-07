/*
 *
 * This is a simple Content Management System (CMS)
 * Copyright (C) 2009  Imran M Yousuf (imyousuf@smartitengineering.com)
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
package com.smartitengineering.cms.api.content.impl;

import com.smartitengineering.cms.api.content.Content;
import com.smartitengineering.cms.api.content.ContentId;
import com.smartitengineering.cms.api.content.Field;
import com.smartitengineering.cms.api.content.Representation;
import com.smartitengineering.cms.api.type.ContentStatus;
import com.smartitengineering.cms.api.type.ContentType;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author kaisar
 */
public class ContentImpl implements Content{

  private ContentId contentId;
  private ContentId parentId;
  private Content parentContent;
  private ContentType contentDef;
  private Field field;
  private ContentStatus contentStatus;
  private Representation representation;
  private Date creationDate;
  private Date lastModifiedDate;
  private String fieldName;
  private String repName;
  private Map map= new HashMap();

  public void setParentId(ContentId contentId) {
    this.parentId = contentId;
  }

  public void setContentDefinition(ContentType contentType) {
    this.contentDef = contentType;
  }

  public void setField(String fieldName, Field field) {
    this.fieldName = fieldName;
    this.field = field;
  }

//  public void removeField(String fieldName);
  public void setStatus(ContentStatus contentStatus) {
    this.contentStatus = contentStatus;
  }

  public ContentId getContentId() {
    return this.contentId;
  }

  public ContentId getParentId() {
    return this.parentId;
  }

  public Content getParent() {
    return this.parentContent;
  }

  public ContentType getContentDefinition() {
    return this.contentDef;
  }

  public Map<String, Field> getFields(){
    return this.map;
  }
  public Field getField(String fieldName) {
    this.fieldName = fieldName;
    return this.field;
  }

  public ContentStatus getStatus() {
    return this.contentStatus;
  }

  public Representation getRepresentation(String repName) {
    this.repName = repName;
    return this.representation;
  }

  public Date getCreationDate() {
    return this.creationDate;
  }

  public Date getLastModifiedDate() {
    return this.lastModifiedDate;
  }
}
