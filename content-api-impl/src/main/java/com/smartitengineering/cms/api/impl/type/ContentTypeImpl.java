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

import com.smartitengineering.cms.api.type.ContentStatus;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.api.type.FieldDef;
import com.smartitengineering.cms.api.type.MutableContentType;
import com.smartitengineering.cms.api.type.RepresentationDef;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author kaisar
 */
public class ContentTypeImpl implements MutableContentType {

  private ContentTypeId contentTypeId;
  private final List<ContentStatus> contentStatus = new ArrayList<ContentStatus>();
  private Collection<FieldDef> fieldDef = new ArrayList<FieldDef>();
  private ContentTypeId contentParent;
  private String displayName;
  private final Map<String, ContentStatus> statusMap = new HashMap<String, ContentStatus>();
  private final Map<String, RepresentationDef> representationDefMap = new HashMap<String, RepresentationDef>();
  private final Map<String, FieldDef> fieldDefMap = new HashMap<String, FieldDef>();
  private Date creationDate;
  private Date lastModifiedDate;
  private boolean lock;

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
    return this.fieldDef;
  }

  @Override
  public ContentTypeId getContentTypeID() {
    return this.contentTypeId;
  }

  @Override
  public Map<String, ContentStatus> getStatuses() {
    return Collections.unmodifiableMap(this.statusMap);
  }

  @Override
  public Map<String, FieldDef> getFields() {
    return Collections.unmodifiableMap(this.fieldDefMap);

  }

  @Override
  public ContentTypeId getParent() {
    return this.contentParent;
  }

  @Override
  public String getDisplayName() {
    return this.displayName;
  }

  @Override
  public Map<String, RepresentationDef> getRepresentations() {
    return Collections.unmodifiableMap(this.representationDefMap);
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
  public void create() throws IOException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void update() throws IOException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void delete() throws IOException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean isLockOwned() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void lock() {
    this.lock = true;
  }

  @Override
  public boolean tryLock() {
    return this.lock;
  }

  @Override
  public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void unlock() {
    this.lock = false;
  }
}
