/*
 *
 * This is a simple ContentType Management System (CMS)
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
import com.google.inject.Singleton;
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.event.Event;
import com.smartitengineering.cms.api.event.Event.Type;
import com.smartitengineering.cms.api.event.EventListener;
import com.smartitengineering.common.dao.search.CommonFreeTextPersistentDao;

/**
 *
 * @author imyousuf
 */
@Singleton
public class ContentTypeEventListener implements EventListener<ContentType> {

  @Inject
  private CommonFreeTextPersistentDao<ContentType> persistentDao;

  @Override
  public boolean accepts(Event<ContentType> event) {
    return event.getEventSourceType().equals(Type.CONTENT_TYPE);
  }

  @Override
  public void notify(Event<ContentType> event) {
    final ContentType source = event.getSource();
    if (source == null) {
      return;
    }
    switch (event.getEventType()) {
      case CREATE:
        persistentDao.save(source);
        break;
      case UPDATE:
        persistentDao.update(source);
        break;
      case DELETE:
        persistentDao.delete(source);
        break;
    }
  }
}
