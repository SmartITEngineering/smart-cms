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
package com.smartitengineering.cms.api.impl.content;

import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.cms.api.content.Content;
import com.smartitengineering.cms.api.content.ContentId;
import com.smartitengineering.cms.api.content.Field;
import com.smartitengineering.cms.api.content.Representation;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.factory.content.WriteableContent;
import com.smartitengineering.cms.api.impl.AbstractPersistableDomain;
import com.smartitengineering.cms.api.type.ContentStatus;
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.type.FieldDef;
import com.smartitengineering.cms.spi.SmartContentSPI;
import com.smartitengineering.cms.spi.content.PersistableContent;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.ObjectUtils;

/**
 *
 * @author kaisar
 */
public class ContentImpl extends AbstractPersistableDomain<WriteableContent> implements PersistableContent {

  private ContentId contentId;
  private ContentId parentId;
  private ContentType contentDef;
  private ContentStatus contentStatus;
  private Date creationDate;
  private Date lastModifiedDate;
  private Map<String, Field> map = new HashMap<String, Field>();
  private Map<String, Field> cachedFieldMap;

  @Override
  public void setParentId(ContentId contentId) {
    if (contentDef == null) {
      throw new IllegalArgumentException("Content Type Definition must be set before setting parent content ID");
    }
    if (contentId != null && SmartContentAPI.getInstance().getContentLoader().loadContent(contentId) == null) {
      throw new IllegalArgumentException("Parent must exist for it to be set!");
    }
    this.parentId = contentId;
  }

  @Override
  public void setContentDefinition(ContentType contentType) {
    this.contentDef = contentType;
  }

  @Override
  public void setField(Field field) {
    map.put(field.getName(), field);
    cachedFieldMap = null;
  }

  @Override
  public void setStatus(ContentStatus contentStatus) {
    this.contentStatus = contentStatus;
  }

  @Override
  public ContentId getContentId() {
    return this.contentId;
  }

  @Override
  public ContentId getParentId() {
    return this.parentId;
  }

  @Override
  public Content getParent() {
    return SmartContentAPI.getInstance().getContentLoader().loadContent(parentId);
  }

  @Override
  public ContentType getContentDefinition() {
    return this.contentDef;
  }

  @Override
  public Map<String, Field> getFields() {
    final Content parent = getParent();
    final ContentType def = getContentDefinition();
    if (cachedFieldMap == null) {
      Map<String, Field> fields = new HashMap<String, Field>(this.map);
      if (parent != null && def != null) {
        Map<String, Field> parentFields = parent.getFields();
        for (String fieldName : parentFields.keySet()) {
          FieldDef myDef = def.getFieldDefs().get(fieldName);
          FieldDef thatDef = parent.getContentDefinition().getFieldDefs().get(fieldName);
          if (myDef != null && thatDef != null && ObjectUtils.equals(myDef, thatDef) && myDef.getValueDef().getType().
              equals(thatDef.getValueDef().getType()) && !fields.containsKey(fieldName)) {
            fields.put(fieldName, parent.getField(fieldName));
          }
        }
      }
      cachedFieldMap = fields;
    }
    return Collections.unmodifiableMap(cachedFieldMap);
  }

  @Override
  public Map<String, Field> getOwnFields() {
    return Collections.unmodifiableMap(map);
  }

  @Override
  public Field getField(String fieldName) {
    return getFields().get(fieldName);
  }

  @Override
  public ContentStatus getStatus() {
    return this.contentStatus;
  }

  @Override
  public Representation getRepresentation(String repName) {
    return SmartContentSPI.getInstance().getRepresentationProvider().getRepresentation(repName, getContentDefinition(),
                                                                                       this);
  }

  @Override
  public Date getCreationDate() {
    return this.creationDate;
  }

  @Override
  public Date getLastModifiedDate() {
    return this.lastModifiedDate;
  }

  @Override
  public boolean isPersisted() {
    return creationDate != null;
  }

  @Override
  public String getKeyStringRep() {
    StringBuilder builder = new StringBuilder();
    if (isPersisted()) {
      WorkspaceId workspaceId = contentId.getWorkspaceId();
      builder.append(workspaceId.getGlobalNamespace()).append(':').append(workspaceId.getName()).append(':').
          append(new String(contentId.getId()));
    }
    return builder.toString();
  }

  @Override
  public void removeField(String fieldName) {
    map.remove(fieldName);
  }

  @Override
  public void setContentId(ContentId contentId) {
    this.contentId = contentId;
  }

  @Override
  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  @Override
  public void setLastModifiedDate(Date lastModifiedDate) {
    this.lastModifiedDate = lastModifiedDate;
  }

  @Override
  protected void create() throws IOException {
    if (contentId == null && contentDef != null && contentDef.getContentTypeID() != null) {
      createContentId(contentDef.getContentTypeID().getWorkspace());
    }
    else if (contentId == null && (contentDef == null || contentDef.getContentTypeID() == null)) {
      throw new IOException("Content ID and Content Type Definition is not set!");
    }
    super.create();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (!Content.class.isAssignableFrom(obj.getClass())) {
      return false;
    }
    final Content other = (Content) obj;
    if (this.contentId != other.getContentId() && (this.contentId == null ||
                                                   !this.contentId.equals(other.getContentId()))) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 29 * hash + (this.contentId != null ? this.contentId.hashCode() : 0);
    return hash;
  }

  @Override
  public void createContentId(WorkspaceId workspace) {
    if (workspace == null) {
      throw new IllegalArgumentException("Workspace ID can not be null!");
    }
    setContentId(SmartContentAPI.getInstance().getContentLoader().generateContentId(workspace));
  }
}
