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
import com.smartitengineering.common.dao.search.CommonFreeTextPersistentTxDao;
import com.smartitengineering.events.async.api.EventConsumer;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.StringReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
@Singleton
public class ContentEventConsumerImpl implements EventConsumer {

  @Inject
  private CommonFreeTextPersistentTxDao<Content> persistentDao;
  private final transient Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public void consume(String eventContentType, String eventMessage) {
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new StringReader(eventMessage));
      EventType type = EventType.valueOf(reader.readLine());
      final StringBuilder idStr = new StringBuilder("");
      String line;
      do {
        line = reader.readLine();
        if (StringUtils.isNotBlank(line)) {
          idStr.append(line).append('\n');
        }
      }
      while (StringUtils.isNotBlank(line));
      final ContentId contentId = (ContentId) new ObjectInputStream(new ByteArrayInputStream(Base64.decodeBase64(idStr.
          toString()))).readObject();
      Content content = SmartContentAPI.getInstance().getContentLoader().loadContent(contentId);
      switch (type) {
        case CREATE:
          persistentDao.save(content);
          break;
        case UPDATE:
          persistentDao.update(content);
          break;
        case DELETE:
          if (content == null) {
            content = (Content) Proxy.newProxyInstance(Content.class.getClassLoader(), new Class[]{Content.class},
                                                       new InvocationHandler() {

              @Override
              public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getName().equals("getContentId")) {
                  return contentId;
                }
                return null;
              }
            });
          }
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

  @Override
  public void startConsumption() {
  }

  @Override
  public void endConsumption(boolean prematureEnd) {
    try {
      if (prematureEnd) {
        persistentDao.rollback();
      }
      else {
        persistentDao.commit();
      }
    }
    catch (Exception ex) {
      logger.error("Could not commit/rollback for prematureEnd (" + prematureEnd + ")", ex);
    }
  }
}
