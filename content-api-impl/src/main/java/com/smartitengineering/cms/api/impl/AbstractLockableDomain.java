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
import java.util.concurrent.TimeUnit;

/**
 * Integrates lock implementation for domain objects needing locking support. It
 * uses the infrastructure provided by {@link LockManager}
 * @author imyousuf
 * @since 0.1
 */
public abstract class AbstractLockableDomain
    implements Lock,
               Key {

  protected Lock lock;

  /**
   * Gets the lock for the concrete class invoking this constructor
   */
  protected AbstractLockableDomain() {
    lock = LockManager.register(this);
  }

  @Override
  protected void finalize()
      throws Throwable {
    LockManager.unregister(this);
    super.finalize();
  }

  @Override
  public boolean isLockOwned() {
    return lock.isLockOwned();
  }

  @Override
  public void lock() {
    lock.lock();
  }

  @Override
  public boolean tryLock() {
    boolean locked = lock.tryLock();
    return locked;
  }

  @Override
  public boolean tryLock(long time,
                         TimeUnit unit)
      throws InterruptedException {
    boolean locked = lock.tryLock(time, unit);
    return locked;
  }

  @Override
  public void unlock() {
    lock.unlock();
  }
}
