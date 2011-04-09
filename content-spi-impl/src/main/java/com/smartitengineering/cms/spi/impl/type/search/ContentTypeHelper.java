/*
 *
 * This is a simple Content Management System (CMS)
 * Copyright (C) 2011  Imran M Yousuf (imyousuf@smartitengineering.com)
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
package com.smartitengineering.cms.spi.impl.type.search;

import com.google.inject.Inject;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.spi.impl.events.SolrFieldNames;
import com.smartitengineering.cms.spi.impl.type.PersistentContentType;
import com.smartitengineering.dao.impl.hbase.spi.SchemaInfoProvider;
import com.smartitengineering.dao.solr.MultivalueMap;
import com.smartitengineering.dao.solr.impl.MultivalueMapImpl;
import com.smartitengineering.util.bean.adapter.AbstractAdapterHelper;
import org.apache.commons.codec.binary.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class ContentTypeHelper extends AbstractAdapterHelper<ContentType, MultivalueMap<String, Object>> {

  protected static final String CONTENT_TYPE = "contentType";
  @Inject
  private SchemaInfoProvider<PersistentContentType, ContentTypeId> contentTypeScehmaProvider;
  private final transient Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  protected MultivalueMap<String, Object> newTInstance() {
    return new MultivalueMapImpl<String, Object>();
  }

  @Override
  protected void mergeFromF2T(ContentType fromBean,
                              MultivalueMap<String, Object> toBean) {
    toBean.addValue(SolrFieldNames.TYPE, CONTENT_TYPE);
    final ContentTypeId id = fromBean.getContentTypeID();
    toBean.addValue(SolrFieldNames.ID, id.toString());
    toBean.addValue(SolrFieldNames.WORKSPACEID, id.getWorkspace().toString());
    final ContentType contentType = fromBean;
    toBean.addValue(SolrFieldNames.CREATIONDATE, contentType.getCreationDate());
    toBean.addValue(SolrFieldNames.LASTMODIFIEDDATE, contentType.getLastModifiedDate());
    toBean.addValue(SolrFieldNames.STATUS, "published");
    final String typeIdString = id.toString();
    toBean.addValue(SolrFieldNames.CONTENTTYPEID, typeIdString);
    toBean.addValue(SolrFieldNames.PRIVATE, false);
    toBean.addValue(SolrFieldNames.INSTANCE_OF, typeIdString);
    //Content is a instance of all it parent types
    ContentTypeId parent = contentType.getParent();
    while (parent != null) {
      toBean.addValue(SolrFieldNames.INSTANCE_OF, parent.toString());
      final ContentType partentType = SmartContentAPI.getInstance().getContentTypeLoader().loadContentType(parent);
      parent = partentType.getParent();
    }
  }

  @Override
  protected ContentType convertFromT2F(MultivalueMap<String, Object> toBean) {
    try {
      byte[] contentId = StringUtils.getBytesUtf8(toBean.getFirst(SolrFieldNames.ID).toString());
      ContentTypeId id = contentTypeScehmaProvider.getIdFromRowId(contentId);
      return SmartContentAPI.getInstance().getContentTypeLoader().loadContentType(id);
    }
    catch (Exception ex) {
      logger.error("Error converting to content type, returning null!", ex);
    }
    return null;
  }
}
