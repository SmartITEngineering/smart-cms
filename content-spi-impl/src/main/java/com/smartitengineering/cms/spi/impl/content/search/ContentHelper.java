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
import com.smartitengineering.cms.api.content.ContentId;
import com.smartitengineering.cms.api.content.Field;
import com.smartitengineering.cms.api.content.FieldValue;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.factory.content.WriteableContent;
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.type.ContentTypeId;
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
    toBean.addValue("type", "content");
    final ContentId id = fromBean.getId();
    toBean.addValue("id", id.toString());
    toBean.addValue("workspaceId", id.getWorkspaceId().toString());
    final WriteableContent mutableContent = fromBean.getMutableContent();
    toBean.addValue("creationDate", mutableContent.getCreationDate());
    toBean.addValue("lastModifiedDate", mutableContent.getLastModifiedDate());
    toBean.addValue("status", mutableContent.getStatus().getName());
    final String typeIdString = mutableContent.getContentDefinition().getContentTypeID().toString();
    toBean.addValue("contentTypeId", typeIdString);
    toBean.addValue("instanceOf", typeIdString);
    //Content is a instance of all it parent types
    ContentType contentType = mutableContent.getContentDefinition();
    ContentTypeId parent = contentType.getParent();
    while (parent != null) {
      toBean.addValue("instanceOf", parent.toString());
      final ContentType partentType = SmartContentAPI.getInstance().getContentTypeLoader().loadContentType(parent);
      parent = partentType.getParent();
    }
    Map<String, Field> fields = mutableContent.getFields();
    for (Entry<String, Field> entry : fields.entrySet()) {
      FieldDef def = entry.getValue().getFieldDef();
      Field field = entry.getValue();
      String searchFieldName = SmartContentSPI.getInstance().getSearchFieldNameGenerator().getSearchFieldName(def);
      if (org.apache.commons.lang.StringUtils.isNotBlank(searchFieldName)) {
        addFieldValue(toBean, searchFieldName, field);
      }
    }
  }

  protected void addFieldValue(MultivalueMap<String, Object> toBean, String indexFieldName, Field field) {
    final Object value = field.getValue().getValue();
    addSimpleValue(field.getValue(), toBean, indexFieldName, value);
  }

  protected void addSimpleValue(final FieldValue def,
                                MultivalueMap<String, Object> toBean, String indexFieldName, final Object value) {
    final FieldValueType valueDef = def.getDataType();
    switch (valueDef) {
      case COLLECTION:
        CollectionFieldValue fieldValue = (CollectionFieldValue) def;
        Collection<FieldValue> values = fieldValue.getValue();
        for (FieldValue val : values) {
          addSimpleValue(val, toBean, indexFieldName, val.getValue());
        }
        break;
      case CONTENT:
        toBean.addValue(indexFieldName, value.toString());
        break;
      default:
        toBean.addValue(indexFieldName, value);
        break;
    }
  }

  @Override
  protected PersistentContent convertFromT2F(MultivalueMap<String, Object> toBean) {
    try {
      byte[] contentId = StringUtils.getBytesUtf8(toBean.getFirst("id").toString());
      ContentId id = contentScehmaProvider.getIdFromRowId(contentId);
      return readDao.getById(id);
    }
    catch (Exception ex) {
      logger.error("Error converting to content, returning null!", ex);
    }
    return null;
  }
}
