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
package com.smartitengineering.cms.spi.impl.type;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.factory.type.WritableContentType;
import com.smartitengineering.cms.api.type.ContentCoProcessorDef;
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.cms.spi.SmartContentSPI;
import com.smartitengineering.cms.spi.persistence.PersistentService;
import com.smartitengineering.cms.spi.type.PersistableContentType;
import com.smartitengineering.cms.spi.type.PersistentContentTypeReader;
import com.smartitengineering.dao.common.CommonReadDao;
import com.smartitengineering.dao.common.CommonWriteDao;
import com.smartitengineering.dao.common.queryparam.MatchMode;
import com.smartitengineering.dao.common.queryparam.QueryParameter;
import com.smartitengineering.dao.common.queryparam.QueryParameterFactory;
import com.smartitengineering.util.bean.adapter.GenericAdapter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 *
 * @author imyousuf
 */
@Singleton
public class ContentTypePersistentService implements PersistentService<WritableContentType>, PersistentContentTypeReader {

  @Inject
  private GenericAdapter<WritableContentType, PersistentContentType> adapter;
  @Inject
  private CommonReadDao<PersistentContentType, ContentTypeId> commonReadDao;
  @Inject
  private CommonWriteDao<PersistentContentType> commonWriteDao;

  public GenericAdapter<WritableContentType, PersistentContentType> getAdapter() {
    return adapter;
  }

  public CommonReadDao<PersistentContentType, ContentTypeId> getCommonReadDao() {
    return commonReadDao;
  }

  public CommonWriteDao<PersistentContentType> getCommonWriteDao() {
    return commonWriteDao;
  }

  @Override
  public void create(WritableContentType bean) throws Exception {
    PersistableContentType contentType = getPersistableContentType(bean);
    final Date date = new Date();
    contentType.setCreationDate(date);
    contentType.setLastModifiedDate(date);
    contentType.setEntityTagValue(SmartContentAPI.getInstance().getContentTypeLoader().getEntityTagValueForContentType(
        contentType));
    commonWriteDao.save(getAdapter().convert(contentType));
  }

  @Override
  public void update(WritableContentType bean) throws Exception {
    PersistableContentType contentType = getPersistableContentType(bean);
    contentType.setLastModifiedDate(new Date());
    contentType.setEntityTagValue(SmartContentAPI.getInstance().getContentTypeLoader().getEntityTagValueForContentType(
        contentType));
    commonWriteDao.update(getAdapter().convert(contentType));
  }

  @Override
  public void delete(WritableContentType bean) throws Exception {
    commonWriteDao.delete(getAdapter().convert(bean));
  }

  @Override
  public Collection<? extends ContentType> readContentTypeFromPersistentStorage(ContentTypeId... contentTypeId) {
    final Set<PersistentContentType> byIds = commonReadDao.getByIds(Arrays.asList(contentTypeId));
    return getAdapter().convertInversely(byIds.toArray(new PersistentContentType[byIds.size()]));
  }

  @Override
  public Collection<? extends ContentType> getByWorkspace(WorkspaceId workspaceId) {
    QueryParameter parameter = QueryParameterFactory.getStringLikePropertyParam("id", new StringBuilder(workspaceId.
        toString()).append(':').toString(), MatchMode.START);
    final List<PersistentContentType> list = commonReadDao.getList(parameter);
    if (list == null || list.isEmpty()) {
      return Collections.emptyList();
    }
    return getAdapter().convertInversely(list.toArray(new PersistentContentType[list.size()]));
  }

  private PersistableContentType getPersistableContentType(WritableContentType bean) {
    PersistableContentType contentType = SmartContentSPI.getInstance().getPersistableDomainFactory().
        createPersistableContentType();
    contentType.setPrimaryFieldName(bean.getPrimaryFieldName());
    contentType.setContentTypeID(bean.getContentTypeID());
    contentType.setCreationDate(bean.getCreationDate());
    contentType.setDisplayName(bean.getDisplayName());
    contentType.setEntityTagValue(bean.getEntityTagValue());
    contentType.setLastModifiedDate(bean.getLastModifiedDate());
    contentType.setParent(bean.getParent());
    contentType.setRepresentations(bean.getRepresentations());
    contentType.getMutableFieldDefs().addAll(bean.getMutableFieldDefs());
    contentType.getMutableRepresentationDefs().addAll(bean.getMutableRepresentationDefs());
    contentType.getMutableStatuses().addAll(bean.getMutableStatuses());
    contentType.setParameterizedDisplayNames(bean.getParameterizedDisplayNames());
    contentType.setDefinitionType(bean.getSelfDefinitionType());
    for (Collection<ContentCoProcessorDef> defs : bean.getContentCoProcessorDefs().values()) {
      for (ContentCoProcessorDef def : defs) {
        contentType.addContentCoProcessorDef(def);
      }
    }
    return contentType;
  }
}
