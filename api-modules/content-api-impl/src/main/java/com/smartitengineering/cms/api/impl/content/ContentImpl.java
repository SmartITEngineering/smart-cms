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

import com.smartitengineering.cms.api.content.FieldValue;
import com.smartitengineering.cms.api.type.ContentType.ContentProcessingPhase;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.cms.api.content.Content;
import com.smartitengineering.cms.api.content.ContentId;
import com.smartitengineering.cms.api.content.Field;
import com.smartitengineering.cms.api.content.Representation;
import com.smartitengineering.cms.api.content.template.ContentCoProcessor;
import com.smartitengineering.cms.api.event.Event;
import com.smartitengineering.cms.api.event.Event.EventType;
import com.smartitengineering.cms.api.event.Event.Type;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.factory.content.WriteableContent;
import com.smartitengineering.cms.api.impl.AbstractPersistableDomain;
import com.smartitengineering.cms.api.type.ContentCoProcessorDef;
import com.smartitengineering.cms.api.type.ContentStatus;
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.type.FieldDef;
import com.smartitengineering.cms.api.workspace.Workspace;
import com.smartitengineering.cms.spi.SmartContentSPI;
import com.smartitengineering.cms.spi.content.PersistableContent;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.ObjectUtils;

/**
 *
 * @author kaisar
 */
public class ContentImpl extends AbstractPersistableDomain<WriteableContent, ContentId> implements PersistableContent {

  private ContentId contentId;
  private ContentId parentId;
  private ContentType contentDef;
  private ContentStatus contentStatus;
  private Date creationDate;
  private Date lastModifiedDate;
  private Map<String, Field> map;
  private Map<String, Field> cachedFieldMap;
  private String entityTagValue;
  private boolean privateContent;
  private boolean supressChecking;

  public ContentImpl() {
    super(LOCK_KEY_PREFIX);
  }

  public boolean isSupressChecking() {
    return supressChecking;
  }

  public void setSupressChecking(boolean supressChecking) {
    this.supressChecking = supressChecking;
  }

  @Override
  public void put() throws IOException {
    final boolean attainedLockLocally;
    if (isPersisted() && !isLockOwned()) {
      lock();
      attainedLockLocally = true;
    }
    else {
      attainedLockLocally = false;
    }
    try {
      triggerContentCoProcessors(ContentType.ContentProcessingPhase.WRITE);
      if (!isValid()) {
        getLogger().info("Content not in valid state!");
        //First get contents indexed before attempting to use this validity!
        //Uncomment the following line once indexing is ensured in testing
        throw new IOException("Content is not in valid state!");
      }
      super.put();
    }
    finally {
      if (attainedLockLocally) {
        unlock();
      }
    }
  }

  protected void triggerContentCoProcessors(final ContentProcessingPhase phase) {
    //Trigger content co processors
    if (contentDef != null && contentId != null) {
      Collection<ContentCoProcessorDef> defs = contentDef.getContentCoProcessorDefs().get(
          phase);
      if (defs != null && !defs.isEmpty()) {
        List<ContentCoProcessorDef> list = new ArrayList<ContentCoProcessorDef>(defs);
        Collections.sort(list, new Comparator<ContentCoProcessorDef>() {

          public int compare(ContentCoProcessorDef o1, ContentCoProcessorDef o2) {
            return o1.getPriority() - o2.getPriority();
          }
        });
        for (ContentCoProcessorDef def : list) {
          ContentCoProcessor processor = SmartContentAPI.getInstance().getWorkspaceApi().getContentCoProcessor(contentId.
              getWorkspaceId(), def.getResourceUri().getValue());
          if (processor != null) {
            processor.processContent(this, def.getParameters());
          }
        }
      }
    }
  }

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
    getMap().put(field.getName(), field);
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
    if (parentId != null) {
      return SmartContentAPI.getInstance().getContentLoader().loadContent(parentId);
    }
    else {
      return null;
    }
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
      Map<String, Field> fields = new LinkedHashMap<String, Field>();
      if (parent != null && def != null) {
        Map<String, Field> parentFields = parent.getFields();
        for (String fieldName : parentFields.keySet()) {
          FieldDef myDef = def.getOwnFieldDefs().get(fieldName);
          FieldDef thatDef = parent.getContentDefinition().getOwnFieldDefs().get(fieldName);
          if (myDef != null && thatDef != null && ObjectUtils.equals(myDef, thatDef) && myDef.getValueDef().getType().
              equals(thatDef.getValueDef().getType())) {
            fields.put(fieldName, parent.getField(fieldName));
          }
        }
      }
      fields.putAll(getMap());
      cachedFieldMap = fields;
    }
    return Collections.unmodifiableMap(cachedFieldMap);
  }

  public Map<String, Field> getMap() {
    if (map == null) {
      map = new LinkedHashMap<String, Field>(SmartContentSPI.getInstance().getContentReader().getFieldsForContent(
          contentId));
      triggerContentCoProcessors(ContentType.ContentProcessingPhase.READ);
    }
    return map;
  }

  @Override
  public Map<String, Field> getOwnFields() {
    return Collections.unmodifiableMap(getMap());
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
  public ContentId getKeySpecimen() {
    return contentId;
  }

  @Override
  public void removeField(String fieldName) {
    getMap().remove(fieldName);
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
    if (contentId != null) {
      if (contentId.getWorkspaceId() == null && contentId.getId() == null) {
        throw new IOException("Workspace Id and Id is null though Content Id is not null");
      }
      Workspace workspace = SmartContentAPI.getInstance().getWorkspaceApi().getWorkspace(contentId.getWorkspaceId());
      if (workspace == null) {
        throw new IOException("Non existance workspace Id " + contentId.getWorkspaceId());
      }
      else {
        throw new IOException("Invalid workspace null");
      }
    }
    if (contentId == null && contentDef != null && contentDef.getContentTypeID() != null) {
      createContentId(contentDef.getContentTypeID().getWorkspace());
    }
    else if (contentId == null && (contentDef == null || contentDef.getContentTypeID() == null)) {
      throw new IOException("Content ID and Content Type Definition is not set!");
    }
    super.create();
    Event<Content> contentEvent = SmartContentAPI.getInstance().getEventRegistrar().<Content>createEvent(
        EventType.CREATE, Type.CONTENT, this);
    SmartContentAPI.getInstance().getEventRegistrar().notifyEvent(contentEvent);
  }

  @Override
  public void delete() throws IOException {
    super.delete();
    Event<Content> contentEvent = SmartContentAPI.getInstance().getEventRegistrar().<Content>createEvent(
        EventType.DELETE, Type.CONTENT, this);
    SmartContentAPI.getInstance().getEventRegistrar().notifyEvent(contentEvent);
  }

  @Override
  protected void update() throws IOException {
    super.update();
    Event<Content> contentEvent = SmartContentAPI.getInstance().getEventRegistrar().<Content>createEvent(
        EventType.UPDATE, Type.CONTENT, this);
    SmartContentAPI.getInstance().getEventRegistrar().notifyEvent(contentEvent);
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

  @Override
  public void setEntityTagValue(String eTagValue) {
    this.entityTagValue = eTagValue;
  }

  @Override
  public String getEntityTagValue() {
    return this.entityTagValue;
  }

  @Override
  public boolean isValid() {
    boolean validContent = supressChecking || SmartContentAPI.getInstance().getContentLoader().isValidContent(this);
    if (getLogger().isDebugEnabled()) {
      getLogger().debug("!!! Checking Content " + validContent);
    }
    return validContent;
  }

  @Override
  public URI getUri() {
    return SmartContentSPI.getInstance().getUriProvider().getContentUri(contentId);
  }

  @Override
  public String getEncodedUriString() {
    if (getUri() == null) {
      return null;
    }
    else {
      return getUri().toASCIIString();
    }
  }

  @Override
  public void setPrivate(boolean privateContent) {
    this.privateContent = privateContent;
  }

  @Override
  public boolean isPrivate() {
    return this.privateContent;
  }

  @Override
  public ContentImpl clone() {
    ContentImpl contentImpl = new ContentImpl();
    contentImpl.contentDef = contentDef;
    contentImpl.contentId = contentId;
    contentImpl.parentId = parentId;
    contentImpl.privateContent = privateContent;
    contentImpl.contentStatus = contentStatus;
    contentImpl.nextPerformToWaitForLock = nextPerformToWaitForLock;
    contentImpl.supressChecking = supressChecking;
    contentImpl.entityTagValue = entityTagValue;
    if (creationDate != null) {
      contentImpl.creationDate = new Date(creationDate.getTime());
    }
    if (lastModifiedDate != null) {
      contentImpl.lastModifiedDate = new Date(lastModifiedDate.getTime());
    }
    contentImpl.map = map;
    if (map != null) {
      contentImpl.map = new LinkedHashMap<String, Field>();
      cloneFields(map, contentImpl.map);
    }
    if (cachedFieldMap != null) {
      contentImpl.cachedFieldMap = new LinkedHashMap<String, Field>();
      cloneFields(cachedFieldMap, contentImpl.cachedFieldMap);
    }
    return contentImpl;
  }

  protected void cloneFields(Map<String, Field> fieldMap, Map<String, Field> destMap) {
    for (Map.Entry<String, Field> oneField : fieldMap.entrySet()) {
      FieldImpl fieldImpl = new FieldImpl();
      final Field value = oneField.getValue();
      fieldImpl.setContent(value.getContent());
      fieldImpl.setFieldDef(value.getFieldDef());
      fieldImpl.setName(value.getName());
      final FieldValue fieldVal = value.getValue();
      fieldImpl.setValue(fieldVal);
      destMap.put(oneField.getKey(), fieldImpl);
    }
  }
}
