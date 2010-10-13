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

import com.smartitengineering.cms.api.content.CollectionFieldValue;
import com.smartitengineering.cms.api.content.ContentFieldValue;
import com.smartitengineering.cms.api.content.Field;
import com.smartitengineering.cms.api.content.FieldValue;
import com.smartitengineering.cms.api.type.CollectionDataType;
import com.smartitengineering.cms.api.type.ContentStatus;
import com.smartitengineering.cms.api.type.DataType;
import com.smartitengineering.cms.api.type.OtherDataType;
import com.smartitengineering.cms.ws.common.domains.CollectionFieldValueImpl;
import com.smartitengineering.cms.ws.common.domains.Content;
import com.smartitengineering.cms.ws.common.domains.ContentImpl;
import com.smartitengineering.cms.ws.common.domains.FieldImpl;
import com.smartitengineering.cms.ws.common.domains.FieldValueImpl;
import com.smartitengineering.cms.ws.common.domains.OtherFieldValueImpl;
import com.smartitengineering.cms.ws.common.domains.ResourceTemplate;
import com.smartitengineering.cms.ws.common.domains.ResourceTemplateImpl;
import com.smartitengineering.cms.ws.common.domains.WorkspaceImpl;
import com.smartitengineering.cms.ws.common.domains.Workspace;
import com.smartitengineering.cms.ws.resources.content.ContentResource;
import com.smartitengineering.cms.ws.resources.type.ContentTypeResource;
import java.util.Collection;
import java.util.Map;

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

  public static Content getContent(com.smartitengineering.cms.api.content.Content content) {
    ContentImpl contentImpl = new ContentImpl();
    contentImpl.setContentTypeUri(ContentTypeResource.getContentTypeRelativeURI(content.getContentDefinition().
        getContentTypeID()).toASCIIString());
    if (content.getParentId() != null) {
      contentImpl.setParentContentUri(ContentResource.getContentUri(content.getParentId()).toASCIIString());
    }
    contentImpl.setCreationDate(content.getCreationDate());
    contentImpl.setLastModifiedDate(content.getLastModifiedDate());
    final ContentStatus status = content.getStatus();
    if (status != null) {
      contentImpl.setStatus(status.getName());
    }
    Map<String, Field> fields = content.getFields();
    String contentUri = ContentResource.getContentUri(content.getContentId()).toASCIIString();
    for (Field field : fields.values()) {
      FieldImpl fieldImpl = new FieldImpl();
      fieldImpl.setName(field.getName());
      fieldImpl.setFieldUri(new StringBuilder(contentUri).append('/').append(field.getName()).toString());
      final FieldValueImpl value;
      final FieldValue contentFieldValue = field.getValue();
      final DataType valueDef = field.getFieldDef().getValueDef();
      value = getFieldvalue(valueDef, contentFieldValue);
      fieldImpl.setValue(value);
    }
    return contentImpl;
  }

  private static FieldValueImpl getFieldvalue(final DataType valueDef, final FieldValue contentFieldValue) {
    final FieldValueImpl value;
    switch (valueDef.getType()) {
      case CONTENT: {
        FieldValueImpl valueImpl = new FieldValueImpl();
        valueImpl.setValue(ContentResource.getContentUri(((ContentFieldValue) contentFieldValue).getValue()).
            toASCIIString());
        value = valueImpl;
        break;
      }
      case COLLECTION: {
        CollectionFieldValueImpl valueImpl =
                                 new CollectionFieldValueImpl();
        Collection<FieldValue> contentValues =
                               ((CollectionFieldValue) contentFieldValue).getValue();
        final DataType itemDataType = ((CollectionDataType) valueDef).getItemDataType();
        for (FieldValue contentValue : contentValues) {
          valueImpl.getValues().add(getFieldvalue(itemDataType, contentValue));
        }
        value = valueImpl;
        break;
      }
      case OTHER:
      case STRING: {
        OtherFieldValueImpl valueImpl = new OtherFieldValueImpl();
        valueImpl.setValue(contentFieldValue.toString());
        valueImpl.setMimeType(((OtherDataType) valueDef).getMIMEType());
        value = valueImpl;
        break;
      }
      default: {
        FieldValueImpl valueImpl = new FieldValueImpl();
        valueImpl.setValue(contentFieldValue.toString());
        value = valueImpl;
      }
    }
    value.setType(contentFieldValue.getDataType().name());
    return value;
  }
}
