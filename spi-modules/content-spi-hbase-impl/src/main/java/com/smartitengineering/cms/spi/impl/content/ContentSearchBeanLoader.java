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
package com.smartitengineering.cms.spi.impl.content;

import com.google.inject.Inject;
import com.smartitengineering.cms.api.content.Content;
import com.smartitengineering.cms.api.content.ContentId;
import com.smartitengineering.cms.spi.impl.SearchBeanLoader;
import com.smartitengineering.dao.common.CommonReadDao;
import com.smartitengineering.dao.common.queryparam.QueryParameter;
import com.smartitengineering.dao.impl.hbase.spi.SchemaInfoProvider;
import edu.emory.mathcs.backport.java.util.Collections;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author imyousuf
 */
public class ContentSearchBeanLoader implements SearchBeanLoader<Content, ContentId> {

  @Inject
  private CommonReadDao<PersistentContent, ContentId> readDao;
  @Inject
  private SchemaInfoProvider<PersistentContent, ContentId> schemaInfoProvider;

  public Content loadById(ContentId id) {
    final PersistentContent byId = readDao.getById(id);
    if (byId == null) {
      return null;
    }
    return byId.getMutableContent();
  }

  public ContentId getFromByteArray(byte[] byteArray) {
    try {
      return schemaInfoProvider.getIdFromRowId(byteArray);
    }
    catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public List<Content> getQueryResult(List<QueryParameter> params) {
    List<PersistentContent> contents = readDao.getList(params);
    if (contents == null || contents.isEmpty()) {
      return Collections.emptyList();
    }
    List<Content> mainContents = new ArrayList<Content>(contents.size());
    for (PersistentContent pc : contents) {
      mainContents.add(pc.getMutableContent());
    }
    return mainContents;
  }

  public byte[] getByteArrayFromId(ContentId id) {
    try {
      return schemaInfoProvider.getRowIdFromId(id);
    }
    catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }
}
