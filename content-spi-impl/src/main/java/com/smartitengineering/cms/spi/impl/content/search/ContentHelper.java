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
import com.smartitengineering.cms.api.content.CompositeFieldValue;
import com.smartitengineering.cms.api.content.Content;
import com.smartitengineering.cms.api.content.ContentId;
import com.smartitengineering.cms.api.content.Field;
import com.smartitengineering.cms.api.content.FieldValue;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.type.CollectionDataType;
import com.smartitengineering.cms.api.type.ContentDataType;
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.api.type.DataType;
import com.smartitengineering.cms.api.type.FieldDef;
import com.smartitengineering.cms.api.type.FieldValueType;
import com.smartitengineering.cms.spi.SmartContentSPI;
import com.smartitengineering.cms.spi.impl.content.PersistentContent;
import com.smartitengineering.cms.spi.impl.events.SolrFieldNames;
import com.smartitengineering.dao.impl.hbase.spi.SchemaInfoProvider;
import com.smartitengineering.dao.solr.MultivalueMap;
import com.smartitengineering.dao.solr.impl.MultivalueMapImpl;
import com.smartitengineering.util.bean.adapter.AbstractAdapterHelper;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.codec.binary.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class ContentHelper extends AbstractAdapterHelper<Content, MultivalueMap<String, Object>> {

  protected static final String CONTENT = "content";
  @Inject
  private SchemaInfoProvider<PersistentContent, ContentId> contentScehmaProvider;
  private final transient Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  protected MultivalueMap<String, Object> newTInstance() {
    return new MultivalueMapImpl<String, Object>();
  }

  @Override
  protected void mergeFromF2T(Content fromBean,
                              MultivalueMap<String, Object> toBean) {
    toBean.addValue(SolrFieldNames.TYPE, CONTENT);
    final ContentId id = fromBean.getContentId();
    toBean.addValue(SolrFieldNames.ID, id.toString());
    toBean.addValue(SolrFieldNames.WORKSPACEID, id.getWorkspaceId().toString());
    final Content mutableContent = fromBean;
    toBean.addValue(SolrFieldNames.CREATIONDATE, mutableContent.getCreationDate());
    toBean.addValue(SolrFieldNames.LASTMODIFIEDDATE, mutableContent.getLastModifiedDate());
    toBean.addValue(SolrFieldNames.STATUS, mutableContent.getStatus().getName());
    final String typeIdString = mutableContent.getContentDefinition().getContentTypeID().toString();
    toBean.addValue(SolrFieldNames.CONTENTTYPEID, typeIdString);
    toBean.addValue(SolrFieldNames.PRIVATE, mutableContent.isPrivate());
    toBean.addValue(SolrFieldNames.INSTANCE_OF, typeIdString);
    //Content is a instance of all it parent types
    ContentType contentType = mutableContent.getContentDefinition();
    ContentTypeId parent = contentType.getParent();
    while (parent != null) {
      toBean.addValue(SolrFieldNames.INSTANCE_OF, parent.toString());
      final ContentType partentType = SmartContentAPI.getInstance().getContentTypeLoader().loadContentType(parent);
      parent = partentType.getParent();
    }
    Set<ContentId> indexedContents = new HashSet<ContentId>();
    indexFields(mutableContent, toBean, "", indexedContents, '_');
  }

  protected void indexFields(final Content mutableContent, MultivalueMap<String, Object> toBean, final String prefix,
                             Set<ContentId> indexedContents, final char separator) {
    if (logger.isInfoEnabled()) {
      logger.info("Indexing contents with prefix " + prefix + " and separator " + separator);
    }
    if (indexedContents.contains(mutableContent.getContentId())) {
      return;
    }
    indexedContents.add(mutableContent.getContentId());
    Map<String, Field> fields = mutableContent.getFields();
    indexFields(fields, prefix, separator, toBean, indexedContents);
  }

  protected void indexFields(Map<String, Field> fields, final String prefix, final char separator,
                             MultivalueMap<String, Object> toBean, Set<ContentId> indexedContents) {
    if (logger.isInfoEnabled()) {
      logger.info("Indexing fields" + fields + " with prefix " + prefix + " and separator " + separator);
    }
    for (Entry<String, Field> entry : fields.entrySet()) {
      FieldDef def = entry.getValue().getFieldDef();
      if (logger.isDebugEnabled()) {
        logger.debug("Field Def " + def.getName() + " available for search: " + def.getSearchDefinition());
      }
      if (def.getSearchDefinition() == null) {
        continue;
      }
      Field field = entry.getValue();
      StringBuilder builder = new StringBuilder();
      if (org.apache.commons.lang.StringUtils.isNotBlank(prefix)) {
        builder.append(prefix).append(separator);
      }
      String defName = SmartContentSPI.getInstance().getSearchFieldNameGenerator().
          getSearchFieldName(def, org.apache.commons.lang.StringUtils.isNotBlank(prefix));
      if (org.apache.commons.lang.StringUtils.isBlank(defName)) {
        continue;
      }
      String searchFieldName = builder.append(defName).toString();
      if (logger.isDebugEnabled()) {
        logger.debug("Search field name " + searchFieldName);
      }
      if (org.apache.commons.lang.StringUtils.isNotBlank(searchFieldName)) {
        addFieldValue(toBean, searchFieldName, field, prefix, indexedContents, '_');
      }
    }
  }

  protected void addFieldValue(MultivalueMap<String, Object> toBean, String indexFieldName, Field field,
                               final String prefix, Set<ContentId> indexedContents, final char separator) {
    if (logger.isInfoEnabled()) {
      logger.info("Indexing field value " + field.getName() + " with prefix " + prefix + " and separator " + separator);
    }
    final Object value = field.getValue().getValue();
    addSimpleValue(field.getValue(), field.getFieldDef().getValueDef(), toBean, field.getName(), indexFieldName, value,
                   prefix, '_', indexedContents);

  }

  protected void addSimpleValue(final FieldValue def, DataType fieldDataType, MultivalueMap<String, Object> toBean,
                                String fieldName, String indexFieldName, final Object value, final String prefix,
                                final char separator, Set<ContentId> indexedContents) {
    if (logger.isInfoEnabled()) {
      logger.info("Indexing simple value " + fieldDataType.getType() + " with prefix " + prefix + " and separator " +
          separator);
    }
    final FieldValueType valueDef = def.getDataType();
    switch (valueDef) {
      case COMPOSITE: {
        StringBuilder builder = new StringBuilder();
        if (org.apache.commons.lang.StringUtils.isNotBlank(prefix)) {
          builder.append(prefix).append('.');
        }
        builder.append(fieldName);
        CompositeFieldValue compositeFieldValue = (CompositeFieldValue) def;
        indexFields(compositeFieldValue.getValueAsMap(), builder.toString(), '.', toBean, indexedContents);
        break;
      }
      case COLLECTION:
        CollectionFieldValue fieldValue = (CollectionFieldValue) def;
        Collection<FieldValue> values = fieldValue.getValue();
        for (FieldValue val : values) {
          addSimpleValue(val, ((CollectionDataType) fieldDataType).getItemDataType(), toBean, fieldName, indexFieldName,
                         val.getValue(), prefix, '_', indexedContents);
        }
        break;
      case CONTENT:
        toBean.addValue(indexFieldName, value.toString());
        final ContentDataType contentDataType = (ContentDataType) fieldDataType;
        if (logger.isDebugEnabled()) {
          logger.debug("Nested Content " + contentDataType.getTypeDef().toString() + " available for search: " +
              contentDataType.isAvaialbleForSearch());
        }
        if (contentDataType.isAvaialbleForSearch()) {
          Content content = SmartContentAPI.getInstance().getContentLoader().loadContent((ContentId) value);
          if (content != null) {
            indexFields(content, toBean, fieldName, indexedContents, '_');
          }
        }
        break;
      default:
        toBean.addValue(indexFieldName, value);
        break;
    }
  }

  @Override
  protected Content convertFromT2F(MultivalueMap<String, Object> toBean) {
    try {
      byte[] contentId = StringUtils.getBytesUtf8(toBean.getFirst(SolrFieldNames.ID).toString());
      ContentId id = contentScehmaProvider.getIdFromRowId(contentId);
      return SmartContentAPI.getInstance().getContentLoader().loadContent(id);
    }
    catch (Exception ex) {
      logger.error("Error converting to content, returning null!", ex);
    }
    return null;
  }
}
