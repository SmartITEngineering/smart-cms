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
  public static final String CONTENT_TYPE_DISPLAY_NAME = "displayName_STRING_i";
  @Inject
  private SchemaInfoProvider<PersistentContentType, ContentTypeId> contentTypeScehmaProvider;
  private final transient Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  protected MultivalueMap<String, Object> newTInstance() {
    return new MultivalueMapImpl<String, Object>();
  }

  @Override
  protected void mergeFromF2T(final ContentType contentType,
                              final MultivalueMap<String, Object> toBean) {
    toBean.addValue(SolrFieldNames.TYPE, CONTENT_TYPE);
    final ContentTypeId id = contentType.getContentTypeID();
    toBean.addValue(SolrFieldNames.ID, id.toString());
    toBean.addValue(SolrFieldNames.WORKSPACEID, id.getWorkspace().toString());
    toBean.addValue(SolrFieldNames.CREATIONDATE, contentType.getCreationDate());
    toBean.addValue(SolrFieldNames.LASTMODIFIEDDATE, contentType.getLastModifiedDate());
    if (org.apache.commons.lang.StringUtils.isNotBlank(contentType.getDisplayName())) {
      toBean.addValue(CONTENT_TYPE_DISPLAY_NAME, contentType.getDisplayName());
    }
    toBean.addValue(SolrFieldNames.STATUS, "published");
    toBean.addValue(SolrFieldNames.PRIVATE, false);
    ContentTypeId parent = contentType.getParent();
    if (logger.isInfoEnabled()) {
      logger.info("Parent " + parent);
    }
    if (parent != null) {
      toBean.addValue(SolrFieldNames.CONTENTTYPEID, parent.toString());
      do {
        toBean.addValue(SolrFieldNames.INSTANCE_OF, parent.toString());
        ContentType parentType = parent.getContentType();
        if (parentType != null) {
          parent = parentType.getParent();
        }
        else {
          parent = null;
        }
      }
      while (parent != null);
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
