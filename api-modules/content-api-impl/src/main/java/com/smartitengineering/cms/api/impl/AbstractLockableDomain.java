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
package com.smartitengineering.cms.api.impl;

import com.smartitengineering.cms.api.factory.write.Lock;
import com.smartitengineering.cms.spi.lock.Key;
import com.smartitengineering.cms.spi.lock.LockManager;
import com.smartitengineering.cms.spi.util.LockUtil;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Integrates lock implementation for domain objects needing locking support. It
 * uses the infrastructure provided by {@link LockManager}
 * @author imyousuf
 * @since 0.1
 */
public abstract class AbstractLockableDomain<K>
    implements Lock {

  private transient Logger logger = LoggerFactory.getLogger(getClass());
  private transient Lock lock;
  protected final String lockPrefix;

  protected Logger getLogger() {
    if (logger == null) {
      logger = Utils.getLogger(getClass());
    }
    return logger;
  }

  /**
   * Gets the lock for the concrete class invoking this constructor
   */
  protected AbstractLockableDomain(String lockPrefix) {
    this.lockPrefix = lockPrefix;
  }

  public Lock getLock() {
    if (lock == null) {
      initLock();
    }
    return lock;
  }

  private synchronized void initLock() {
    if (lock == null) {
      Key key = LockUtil.getCommonLockKey(lockPrefix, getKeySpecimen());
      lock = LockManager.register(key);
    }
  }

  public void setLock(Lock lock) {
    this.lock = lock;
  }

  @Override
  public boolean isLockOwned() {
    return getLock().isLockOwned();
  }

  @Override
  public void lock() {
    getLock().lock();
  }

  @Override
  public boolean tryLock() {
    boolean locked = getLock().tryLock();
    return locked;
  }

  @Override
  public boolean tryLock(long time,
                         TimeUnit unit)
      throws InterruptedException {
    boolean locked = getLock().tryLock(time, unit);
    return locked;
  }

  @Override
  public void unlock() {
    getLock().unlock();
  }

  protected abstract K getKeySpecimen();
}
