/*
 *
 * This is a simple Content Management System (CMS)
 * Copyright (C) 2010 Imran M Yousuf (imyousuf@smartitengineering.com)
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
package com.smartitengineering.cms.spi.impl;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.smartitengineering.cms.api.factory.write.Lock;
import com.smartitengineering.cms.spi.lock.Key;
import com.smartitengineering.cms.spi.lock.LockHandler;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author imyousuf
 */
public class DefaultLockHandler implements LockHandler {

  @Inject
  @Named("waitTime")
  private Long waitTime;
  @Inject
  @Named("unit")
  private TimeUnit waitUnit;

  @Override
  public Lock register(Key key) {
    return new LockImpl();
  }

  @Override
  public void unregister(Key key) {
    return;
  }

  class LockImpl implements Lock {

    @Override
    public boolean isLockOwned() {
      return true;
    }

    @Override
    public void lock() {
    }

    @Override
    public boolean tryLock() {
      try {
        return tryLock(waitTime, waitUnit);
      }
      catch (InterruptedException ex) {
        return false;
      }
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
      return true;
    }

    @Override
    public void unlock() {
    }
  }
}
