/*
 *
 * This is a simple Content Management System (CMS)
 * Copyright (C) 2009  Imran M Yousuf (imyousuf@smartitengineering.com)
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
package com.smartitengineering.cms.spi.lock;

import com.smartitengineering.cms.api.factory.write.Lock;
import com.smartitengineering.cms.spi.SmartContentSPI;
import com.smartitengineering.util.bean.BeanFactoryRegistrar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class for managing instances of locks being used by concrete objects to
 * attain their own lock. The idea is all instances which are similar or IOW
 * equal should share the same {@link Lock} accross every instance and thread.
 * That they their mutual exclusive operations such as CRUD operations can be
 * performed in a synchronous manner. All objects intending to avail this lock
 * facility should implement {@link Key}. Use {@link LockManager#SPI_CONTEXT} for
 * bean factory key to be used in {@link BeanFactoryRegistrar}
 * @author imyousuf
 * @since 0.1
 */
public final class LockManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(LockManager.class);
  /**
   * The lock handler implementation to be used to receive lock implementations.
   * Use "lockHandler" as bean name to be injected here.
   */
  

  private LockManager() {
  }

  /**
   * Registers a {@link Key} and returns its respective {@link Lock}. If the
   * key was already present then it will return the same lock instance as for
   * other instances of the key or else it will create a lock for the key and
   * return it.
   * @param key Key to register lock against
   * @return Lock for the key.
   * @see {@link LockHandler#register(com.smartitengineering.cms.content.lock.Key)}
   */
  public static synchronized Lock register(Key key) {
    final LockHandler handler = SmartContentSPI.getInstance().getLockHandler();
    if (handler == null) {
      LOGGER.warn("No lock handler found LockManager");
      return null;
    }
    return handler.register(key);
  }

  /**
   * Unregister a key from the registrar. It does not necessarily mean that
   * the key/lock will be removed from the registrar. They will be removed if
   * and only if register and unregister invocation is equal.
   * @param key Key to unregister
   * @see {@link LockHandler#unregister(com.smartitengineering.cms.content.lock.Key)}
   */
  public static synchronized void unregister(Key key) {
    final LockHandler handler = SmartContentSPI.getInstance().getLockHandler();
    if (handler != null) {
      handler.unregister(key);
    }
    else {
      LOGGER.warn("No lock handler found LockManager");
    }
  }
}
