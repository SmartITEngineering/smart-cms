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
package com.smartitengineering.cms.spi.impl.content.search;

import com.google.inject.Inject;
import com.smartitengineering.cms.api.content.CollectionFieldValue;
import com.smartitengineering.cms.api.content.Content;
import com.smartitengineering.cms.api.content.ContentId;
import com.smartitengineering.cms.api.content.Field;
import com.smartitengineering.cms.api.content.FieldValue;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.factory.content.WriteableContent;
import com.smartitengineering.cms.api.type.CollectionDataType;
import com.smartitengineering.cms.api.type.ContentDataType;
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.api.type.DataType;
import com.smartitengineering.cms.api.type.FieldDef;
import com.smartitengineering.cms.api.type.FieldValueType;
import com.smartitengineering.cms.spi.SmartContentSPI;
import com.smartitengineering.cms.spi.impl.content.PersistentContent;
import com.smartitengineering.dao.common.CommonReadDao;
import com.smartitengineering.dao.impl.hbase.spi.SchemaInfoProvider;
import com.smartitengineering.dao.solr.MultivalueMap;
import com.smartitengineering.dao.solr.impl.MultivalueMapImpl;
import com.smartitengineering.util.bean.adapter.AbstractAdapterHelper;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.codec.binary.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class ContentHelper extends AbstractAdapterHelper<PersistentContent, MultivalueMap<String, Object>> {

  public static final String CONTENT = "content";
  public static final String CONTENTTYPEID = "contentTypeId";
  public static final String CREATIONDATE = "creationDate";
  public static final String ID = "id";
  public static final String INSTANCE_OF = "instanceOf";
  public static final String LASTMODIFIEDDATE = "lastModifiedDate";
  public static final String STATUS = "status";
  public static final String TYPE = "type";
  public static final String WORKSPACEID = "workspaceId";
  @Inject
  private SchemaInfoProvider<PersistentContent, ContentId> contentScehmaProvider;
  @Inject
  private CommonReadDao<PersistentContent, ContentId> readDao;
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  protected MultivalueMap<String, Object> newTInstance() {
    return new MultivalueMapImpl<String, Object>();
  }

  @Override
  protected void mergeFromF2T(PersistentContent fromBean,
                              MultivalueMap<String, Object> toBean) {
    toBean.addValue(TYPE, CONTENT);
    final ContentId id = fromBean.getId();
    toBean.addValue(ID, id.toString());
    toBean.addValue(WORKSPACEID, id.getWorkspaceId().toString());
    final WriteableContent mutableContent = fromBean.getMutableContent();
    toBean.addValue(CREATIONDATE, mutableContent.getCreationDate());
    toBean.addValue(LASTMODIFIEDDATE, mutableContent.getLastModifiedDate());
    toBean.addValue(STATUS, mutableContent.getStatus().getName());
    final String typeIdString = mutableContent.getContentDefinition().getContentTypeID().toString();
    toBean.addValue(CONTENTTYPEID, typeIdString);
    toBean.addValue(INSTANCE_OF, typeIdString);
    //Content is a instance of all it parent types
    ContentType contentType = mutableContent.getContentDefinition();
    ContentTypeId parent = contentType.getParent();
    while (parent != null) {
      toBean.addValue(INSTANCE_OF, parent.toString());
      final ContentType partentType = SmartContentAPI.getInstance().getContentTypeLoader().loadContentType(parent);
      parent = partentType.getParent();
    }
    indexFields(mutableContent, toBean, "");
  }

  protected void indexFields(final Content mutableContent, MultivalueMap<String, Object> toBean, String prefix) {
    Map<String, Field> fields = mutableContent.getFields();
    for (Entry<String, Field> entry : fields.entrySet()) {
      FieldDef def = entry.getValue().getFieldDef();
      if (def.getSearchDefinition() == null) {
        continue;
      }
      Field field = entry.getValue();
      StringBuilder builder = new StringBuilder();
      if (org.apache.commons.lang.StringUtils.isNotBlank(prefix)) {
        builder.append(prefix).append('_');
      }
      String defName = SmartContentSPI.getInstance().getSearchFieldNameGenerator().getSearchFieldName(def);
      if (org.apache.commons.lang.StringUtils.isBlank(defName)) {
        continue;
      }
      String searchFieldName = builder.append(defName).toString();
      if (org.apache.commons.lang.StringUtils.isNotBlank(searchFieldName)) {
        addFieldValue(toBean, searchFieldName, field, prefix);
      }
    }
  }

  protected void addFieldValue(MultivalueMap<String, Object> toBean, String indexFieldName, Field field, String prefix) {
    final Object value = field.getValue().getValue();
    StringBuilder builder = new StringBuilder();
    if (org.apache.commons.lang.StringUtils.isNotBlank(prefix)) {
      builder.append(prefix).append('_');
    }
    final String name = builder.append(field.getName()).toString();
    addSimpleValue(field.getValue(), field.getFieldDef().getValueDef(), toBean, name, indexFieldName, value);

  }

  protected void addSimpleValue(final FieldValue def, DataType fieldDataType, MultivalueMap<String, Object> toBean,
                                String fieldName, String indexFieldName, final Object value) {
    final FieldValueType valueDef = def.getDataType();
    switch (valueDef) {
      case COLLECTION:
        CollectionFieldValue fieldValue = (CollectionFieldValue) def;
        Collection<FieldValue> values = fieldValue.getValue();
        for (FieldValue val : values) {
          addSimpleValue(val, ((CollectionDataType) fieldDataType).getItemDataType(), toBean, fieldName, indexFieldName, val.
              getValue());
        }
        break;
      case CONTENT:
        toBean.addValue(indexFieldName, value.toString());
        final ContentDataType contentDataType = (ContentDataType) fieldDataType;
        if (contentDataType.isAvaialbleForSearch()) {
          Content content = SmartContentAPI.getInstance().getContentLoader().loadContent((ContentId) value);
          if (content != null) {
            indexFields(content, toBean, fieldName);
          }
        }
        break;
      default:
        toBean.addValue(indexFieldName, value);
        break;
    }
  }

  @Override
  protected PersistentContent convertFromT2F(MultivalueMap<String, Object> toBean) {
    try {
      byte[] contentId = StringUtils.getBytesUtf8(toBean.getFirst(ID).toString());
      ContentId id = contentScehmaProvider.getIdFromRowId(contentId);
      return readDao.getById(id);
    }
    catch (Exception ex) {
      logger.error("Error converting to content, returning null!", ex);
    }
    return null;
  }
}
