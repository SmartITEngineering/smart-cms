/*
 *
 * This is a simple Content Management System (CMS)
 * Copyright (C) 2012  Imran M Yousuf (imyousuf@smartitengineering.com)
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
package com.smartitengineering.cms.spi.impl.cache;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.smartitengineering.cms.api.content.ContentId;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.factory.content.ContentLoader;
import com.smartitengineering.cms.spi.impl.content.PersistentContent;
import com.smartitengineering.dao.common.CommonReadDao;
import com.smartitengineering.dao.common.queryparam.QueryParameter;
import java.util.List;
import java.util.Set;

/**
 *
 * @author imyousuf
 */
public class CacheableContentReadDao implements CommonReadDao<PersistentContent, ContentId> {

  @Inject
  @Named("cacheableDao")
  private CommonReadDao<PersistentContent, ContentId> cacheableReadDao;

  public PersistentContent getSingle(List<QueryParameter> query) {
    return cacheableReadDao.getSingle(query);
  }

  public <OtherTemplate> List<OtherTemplate> getOtherList(List<QueryParameter> query) {
    return cacheableReadDao.getOtherList(query);
  }

  public <OtherTemplate> OtherTemplate getOther(List<QueryParameter> query) {
    return cacheableReadDao.getOther(query);
  }

  public List<PersistentContent> getList(List<QueryParameter> query) {
    return cacheableReadDao.getList(query);
  }

  public PersistentContent getSingle(QueryParameter... query) {
    return cacheableReadDao.getSingle(query);
  }

  public <OtherTemplate> List<OtherTemplate> getOtherList(QueryParameter... query) {
    return cacheableReadDao.getOtherList(query);
  }

  public <OtherTemplate> OtherTemplate getOther(QueryParameter... query) {
    return cacheableReadDao.getOther(query);
  }

  public List<PersistentContent> getList(QueryParameter... query) {
    return cacheableReadDao.getList(query);
  }

  public Set<PersistentContent> getByIds(List<ContentId> ids) {
    final Set<PersistentContent> byIds = cacheableReadDao.getByIds(ids);
    final ContentLoader contentLoader = SmartContentAPI.getInstance().getContentLoader();
    if (byIds != null) {
      for (PersistentContent content : byIds) {
        if (content != null) {
          content.setMutableContent(contentLoader.getWritableContent(content.getMutableContent().clone()));
        }
      }
    }
    return byIds;
  }

  public PersistentContent getById(ContentId id) {
    final ContentLoader contentLoader = SmartContentAPI.getInstance().getContentLoader();
    PersistentContent content = cacheableReadDao.getById(id);
    if (content != null) {
      content.setMutableContent(contentLoader.getWritableContent(content.getMutableContent().clone()));
    }
    return content;
  }

  public Set<PersistentContent> getAll() {
    return cacheableReadDao.getAll();
  }
}
