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

import com.smartitengineering.cms.api.WorkspaceId;
import com.smartitengineering.cms.api.impl.AbstractPersistableDomain;
import com.smartitengineering.cms.api.type.ContentStatus;
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.api.type.FieldDef;
import com.smartitengineering.cms.api.type.MutableContentType;
import com.smartitengineering.cms.api.type.RepresentationDef;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author kaisar
 */
public class ContentTypeImpl extends AbstractPersistableDomain<MutableContentType> implements MutableContentType,
                                                                                              ContentType {

  public ContentTypeImpl() {
    super(MutableContentType.class);
  }
  private ContentTypeId contentTypeId;
  private final Set<ContentStatus> contentStatus = new LinkedHashSet<ContentStatus>();
  private final Set<FieldDef> fieldDefs = new LinkedHashSet<FieldDef>();
  private final Set<RepresentationDef> representationDefs = new LinkedHashSet<RepresentationDef>();
  private ContentTypeId parentTypeId;
  private String displayName;
  private Date creationDate;
  private Date lastModifiedDate;
  private boolean fromPersistentStorage;

  @Override
  public void setContentTypeID(ContentTypeId contentTypeID) throws IllegalArgumentException {
    if (contentTypeID != null) {
      this.contentTypeId = contentTypeID;
    }
  }

  @Override
  public Collection<ContentStatus> getMutableStatuses() {
    return this.contentStatus;
  }

  @Override
  public Collection<FieldDef> getMutableFields() {
    return this.fieldDefs;
  }

  @Override
  public ContentTypeId getContentTypeID() {
    return this.contentTypeId;
  }

  @Override
  public Map<String, ContentStatus> getStatuses() {
    Map<String, ContentStatus> statusMap = new LinkedHashMap<String, ContentStatus>(contentStatus.size());
    for (ContentStatus status : contentStatus) {
      statusMap.put(status.getName(), status);
    }
    return Collections.unmodifiableMap(statusMap);
  }

  @Override
  public Map<String, FieldDef> getFields() {
    Map<String, FieldDef> fieldDefMap = new LinkedHashMap<String, FieldDef>(fieldDefs.size());
    for (FieldDef fieldDef : fieldDefs) {
      fieldDefMap.put(fieldDef.getName(), fieldDef);
    }
    return Collections.unmodifiableMap(fieldDefMap);
  }

  @Override
  public Map<String, RepresentationDef> getRepresentations() {
    Map<String, RepresentationDef> representationDefMap = new LinkedHashMap<String, RepresentationDef>(representationDefs.
        size());
    for (RepresentationDef representationDef : representationDefs) {
      representationDefMap.put(representationDef.getName(), representationDef);
    }
    return Collections.unmodifiableMap(representationDefMap);
  }

  @Override
  public ContentTypeId getParent() {
    return this.parentTypeId;
  }

  @Override
  public String getDisplayName() {
    return this.displayName;
  }

  @Override
  public Date getCreationDate() {
    return this.creationDate;

  }

  @Override
  public Date getLastModifiedDate() {
    return this.lastModifiedDate;

  }

  public boolean isFromPersistentStorage() {
    return fromPersistentStorage;
  }

  public void setFromPersistentStorage(boolean fromPersistentStorage) {
    this.fromPersistentStorage = fromPersistentStorage;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public void setLastModifiedDate(Date lastModifiedDate) {
    this.lastModifiedDate = lastModifiedDate;
  }

  @Override
  public boolean isPersisted() {
    return isFromPersistentStorage();
  }

  @Override
  public String getKeyStringRep() {
    StringBuilder keyString = new StringBuilder();
    if (contentTypeId != null) {
      WorkspaceId workspaceId = contentTypeId.getWorkspace();
      if (workspaceId != null) {
        keyString.append(workspaceId.getGlobalNamespace()).append(':').append(workspaceId.getName()).append(':');
      }
      keyString.append(contentTypeId.getNamespace()).append(':').append(contentTypeId.getName());
    }
    return keyString.toString();
  }

  @Override
  public Collection<RepresentationDef> getMutableRepresentationDefs() {
    return representationDefs;
  }

  @Override
  public void setParent(ContentTypeId parentId) {
    this.parentTypeId = parentId;
  }

  @Override
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (!ContentType.class.isAssignableFrom(obj.getClass())) {
      return false;
    }
    final ContentType other = (ContentType) obj;
    if (this.contentTypeId != other.getContentTypeID() &&
        (this.contentTypeId == null || !this.contentTypeId.equals(other.getContentTypeID()))) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 61 * hash + (this.contentTypeId != null ? this.contentTypeId.hashCode() : 0);
    return hash;
  }

}
