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
package com.smartitengineering.cms.spi.impl.workspace.search;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.smartitengineering.cms.api.event.Event;
import com.smartitengineering.cms.api.event.Event.Type;
import com.smartitengineering.cms.api.event.EventListener;
import com.smartitengineering.cms.api.workspace.Sequence;
import com.smartitengineering.common.dao.search.CommonFreeTextPersistentDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
@Singleton
public class SequenceEventListener implements EventListener<Sequence> {

  @Inject
  private CommonFreeTextPersistentDao<Sequence> persistentDao;
  protected final transient Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public boolean accepts(Event<Sequence> event) {
    return event.getEventSourceType().equals(Type.SEQUENCE);
  }

  @Override
  public void notify(Event<Sequence> event) {
    final Sequence source = event.getSource();
    if (source == null) {
      logger.warn("Sequence event ignored as source is null!");
      return;
    }
    switch (event.getEventType()) {
      case DELETE:
        persistentDao.delete(source);
        break;
      case CREATE:
        persistentDao.delete(source);
        persistentDao.save(source);
        break;
      case UPDATE:
        break;
    }
  }
}
