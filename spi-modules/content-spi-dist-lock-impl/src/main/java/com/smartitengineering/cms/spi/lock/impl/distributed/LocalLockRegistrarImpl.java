/*
 *
 * This is a simple Content Management System (CMS)
 * Copyright (C) 2011 Imran M Yousuf (imyousuf@smartitengineering.com)
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
package com.smartitengineering.cms.spi.lock.impl.distributed;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.smartitengineering.cms.spi.lock.Key;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
@Singleton
public class LocalLockRegistrarImpl implements LocalLockRegistrar {

  private final AtomicLong longFactory = new AtomicLong(0);
  private final ReentrantLock lock = new ReentrantLock();
  private final Map<Key, String> lockMap = new HashMap<Key, String>();
  private final Map<Key, Long> timeoutMap = new HashMap<Key, Long>();
  private final Timer timer = new Timer();
  protected final transient Logger logger = LoggerFactory.getLogger(getClass());
  @Inject
  @Named("localLockTimeout")
  private int localLockTimeout;

  @Inject
  public void initTimeoutChecking() {
    timer.schedule(new TimerTask() {

      @Override
      public void run() {
        lock.lock();
        List<Entry<Key, String>> removables = new ArrayList<Entry<Key, String>>();
        try {
          long currentTime = System.currentTimeMillis();

          final Set<Entry<Key, Long>> entrySet = timeoutMap.entrySet();
          for (Entry<Key, Long> entry : entrySet) {
            if (currentTime >= entry.getValue().longValue()) {
              removables.add(new SimpleEntry<Key, String>(entry.getKey(), lockMap.get(entry.getKey())));
            }
          }
        }
        catch (Exception ex) {
          logger.warn(ex.getMessage(), ex);
        }
        finally {
          lock.unlock();
        }
        for (Entry<Key, String> removable : removables) {
          unlock(removable.getKey(), removable.getValue());
        }
      }
    }, localLockTimeout, localLockTimeout);
  }

  public String lock(Key key) {
    lock.lock();
    try {
      if (!lockMap.containsKey(key)) {
        final String id = String.valueOf(longFactory.incrementAndGet());
        lockMap.put(key, id);
        timeoutMap.put(key, (System.currentTimeMillis() + localLockTimeout));
      }
    }
    catch (Exception ex) {
      logger.warn(ex.getMessage(), ex);
    }
    finally {
      lock.unlock();
    }
    return null;
  }

  public boolean unlock(Key key, String lockId) {
    if (StringUtils.isBlank(lockId)) {
      return false;
    }
    lock.lock();
    try {
      if (lockMap.containsKey(key)) {
        if (lockId.equals(lockMap.get(key))) {
          lockMap.remove(key);
          timeoutMap.remove(key);
          return true;
        }
      }
    }
    catch (Exception ex) {
      logger.warn(ex.getMessage(), ex);
    }
    finally {
      lock.unlock();
    }
    return false;
  }
}
