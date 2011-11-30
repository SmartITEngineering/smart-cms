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

import com.smartitengineering.cms.api.factory.write.Lock;
import com.smartitengineering.cms.spi.lock.Key;
import java.util.concurrent.TimeUnit;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

/**
 *
 * @author imyousuf
 */
public class ZKLock implements Lock, Watcher {

  protected final ZKConfig config;
  protected final Key key;

  protected ZKLock(ZKConfig config, Key key) {
    this.config = config;
    this.key = key;
  }

  public boolean isLockOwned() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void lock() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean tryLock() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void unlock() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void process(WatchedEvent event) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  protected String getNode() {
    return new StringBuilder(config.getRootNode()).append('/').append(key.getKeyStringRep()).toString();
  }
}
