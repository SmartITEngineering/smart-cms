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
package com.smartitengineering.cms.spi.impl.content.search;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.smartitengineering.cms.api.content.Content;
import com.smartitengineering.cms.api.content.ContentId;
import com.smartitengineering.cms.api.event.Event.EventType;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.common.dao.search.CommonFreeTextPersistentDao;
import com.smartitengineering.events.async.api.EventConsumer;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.StringReader;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
@Singleton
public class ContentEventConsumerImpl implements EventConsumer {

  @Inject
  private CommonFreeTextPersistentDao<Content> persistentDao;
  private final transient Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public void consume(String eventContentType, String eventMessage) {
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new StringReader(eventMessage));
      EventType type = EventType.valueOf(reader.readLine());
      ContentId contentId = (ContentId) new ObjectInputStream(new ByteArrayInputStream(Base64.decodeBase64(reader.
          readLine()))).readObject();
      Content content = SmartContentAPI.getInstance().getContentLoader().loadContent(contentId);
      switch (type) {
        case CREATE:
          persistentDao.save(content);
          break;
        case UPDATE:
          persistentDao.update(content);
          break;
        case DELETE:
          persistentDao.delete(content);
          break;
      }
    }
    catch (Exception ex) {
      logger.warn("Could not persist content ID!", ex);
      throw new RuntimeException(ex);
    }
    finally {
      try {
        reader.close();
      }
      catch (Exception ex) {
        logger.warn("Could not close reader!", ex);
      }
    }
  }
}
