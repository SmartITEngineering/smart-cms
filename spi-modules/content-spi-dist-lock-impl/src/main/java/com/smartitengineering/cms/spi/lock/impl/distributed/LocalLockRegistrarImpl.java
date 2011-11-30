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
  private final Map<Key, LockDetails> lockMap = new HashMap<Key, LockDetails>();
  private final Timer timer = new Timer();
  protected final transient Logger logger = LoggerFactory.getLogger(getClass());
  @Inject(optional = true)
  @Named("localLockTimeout")
  private int localLockTimeout = 2 * 60 * 1000;

  @Inject
  public void initTimeoutChecking() {
    timer.schedule(new TimerTask() {

      @Override
      public void run() {
        lock.lock();
        List<Entry<Key, LockDetails>> removables = new ArrayList<Entry<Key, LockDetails>>();
        try {
          long currentTime = System.currentTimeMillis();

          final Set<Entry<Key, LockDetails>> entrySet = lockMap.entrySet();
          for (Entry<Key, LockDetails> entry : entrySet) {
            if (currentTime >= entry.getValue().getTimeoutTime()) {
              removables.add(new SimpleEntry<Key, LockDetails>(entry.getKey(), lockMap.get(entry.getKey())));
            }
          }
        }
        catch (Exception ex) {
          logger.warn(ex.getMessage(), ex);
        }
        finally {
          lock.unlock();
        }
        for (Entry<Key, LockDetails> removable : removables) {
          unlock(removable.getKey(), removable.getValue().getLockId());
          removable.getValue().getListener().lockTimedOut(removable.getKey());
        }
      }
    }, localLockTimeout, localLockTimeout);
  }

  public String lock(Key key, LockTimeoutListener listener, long waitTime) {
    long availableWaitTime = waitTime;
    while (true) {
      lock.lock();
      try {
        if (!lockMap.containsKey(key)) {
          final String id = String.valueOf(longFactory.incrementAndGet());
          LockDetails details = new LockDetails((System.currentTimeMillis() + localLockTimeout), id, listener);
          lockMap.put(key, details);
        }
      }
      catch (Exception ex) {
        logger.warn(ex.getMessage(), ex);
      }
      finally {
        lock.unlock();
      }
      if (availableWaitTime > 0) {
        return null;
      }
      else {
        long startTime = 0;
        try {
          synchronized (this) {
            startTime = System.currentTimeMillis();
            wait(waitTime);
          }
        }
        catch (Exception ex) {
          logger.warn(ex.getMessage(), ex);
        }
        availableWaitTime = availableWaitTime - (System.currentTimeMillis() - startTime);
      }
    }
  }

  public boolean unlock(Key key, String lockId) {
    if (StringUtils.isBlank(lockId)) {
      return false;
    }
    lock.lock();
    try {
      if (lockMap.containsKey(key)) {
        if (lockId.equals(lockMap.get(key).getLockId())) {
          lockMap.remove(key);
          synchronized (this) {
            notifyAll();
          }
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

  private static class LockDetails {

    private final long timeoutTime;
    private final String lockId;
    private final LockTimeoutListener listener;

    public LockDetails(long timeoutTime, String lockId, LockTimeoutListener listener) {
      this.timeoutTime = timeoutTime;
      this.lockId = lockId;
      this.listener = listener;
    }

    public LockTimeoutListener getListener() {
      return listener;
    }

    public String getLockId() {
      return lockId;
    }

    public long getTimeoutTime() {
      return timeoutTime;
    }
  }
}
