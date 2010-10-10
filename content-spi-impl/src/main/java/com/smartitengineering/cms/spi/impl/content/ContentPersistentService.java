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
import com.smartitengineering.cms.api.factory.content.WriteableContent;
import com.smartitengineering.cms.spi.content.PersistentContentReader;
import com.smartitengineering.cms.spi.persistence.PersistentService;
import com.smartitengineering.dao.common.CommonReadDao;
import com.smartitengineering.dao.common.CommonWriteDao;
import com.smartitengineering.util.bean.adapter.GenericAdapter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
    commonWriteDao.save(adapter.convert(bean));
  }

  @Override
  public void update(WriteableContent bean) throws Exception {
    commonWriteDao.update(adapter.convert(bean));
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
}
