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
package com.smartitengineering.cms.spi.impl.content;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.smartitengineering.cms.api.content.Content;
import com.smartitengineering.cms.api.content.ContentId;
import com.smartitengineering.cms.api.content.Field;
import com.smartitengineering.cms.api.factory.content.WriteableContent;
import com.smartitengineering.cms.spi.SmartContentSPI;
import com.smartitengineering.cms.spi.content.PersistableContent;
import com.smartitengineering.cms.spi.content.PersistentContentReader;
import com.smartitengineering.cms.spi.persistence.PersistentService;
import com.smartitengineering.dao.common.CommonReadDao;
import com.smartitengineering.dao.common.CommonWriteDao;
import com.smartitengineering.util.bean.adapter.GenericAdapter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

/**
 *
 * @author imyousuf
 */
@Singleton
public class ContentPersistentService implements PersistentService<WriteableContent>, PersistentContentReader {

  @Inject
  private GenericAdapter<WriteableContent, PersistentContent> adapter;
  @Inject
  private CommonReadDao<PersistentContent, ContentId> commonReadDao;
  @Inject
  private CommonWriteDao<PersistentContent> commonWriteDao;

  public GenericAdapter<WriteableContent, PersistentContent> getAdapter() {
    return adapter;
  }

  public CommonReadDao<PersistentContent, ContentId> getCommonReadDao() {
    return commonReadDao;
  }

  public CommonWriteDao<PersistentContent> getCommonWriteDao() {
    return commonWriteDao;
  }

  @Override
  public void create(WriteableContent bean) throws Exception {
    final PersistableContent content;
    if (bean instanceof PersistableContent) {
      content = (PersistableContent) bean;
    }
    else {
      content = SmartContentSPI.getInstance().getPersistableDomainFactory().createPersistableContent();
      copy(bean, content);
    }
    Date date = new Date();
    content.setCreationDate(date);
    content.setLastModifiedDate(date);
    commonWriteDao.save(adapter.convert(content));
  }

  @Override
  public void update(WriteableContent bean) throws Exception {
    final PersistableContent content;
    if (bean instanceof PersistableContent) {
      content = (PersistableContent) bean;
    }
    else {
      content = SmartContentSPI.getInstance().getPersistableDomainFactory().createPersistableContent();
      copy(bean, content);
    }
    content.setLastModifiedDate(new Date());
    commonWriteDao.update(adapter.convert(content));
  }

  @Override
  public void delete(WriteableContent bean) throws Exception {
    commonWriteDao.delete(adapter.convert(bean));
  }

  @Override
  public Collection<Content> readContentsFromPersistentStorage(ContentId... ids) {
    final Set<PersistentContent> byIds = commonReadDao.getByIds(Arrays.asList(ids));
    return Collections.<Content>unmodifiableCollection(adapter.convertInversely(byIds.toArray(new PersistentContent[byIds.
        size()])));
  }

  protected void copy(WriteableContent from, PersistableContent to) {
    to.setContentDefinition(from.getContentDefinition());
    to.setContentId(from.getContentId());
    to.setCreationDate(from.getCreationDate());
    to.setLastModifiedDate(from.getLastModifiedDate());
    to.setParentId(from.getParentId());
    to.setStatus(from.getStatus());
    for (Field field : from.getFields().values()) {
      to.setField(field);
    }
  }
}
