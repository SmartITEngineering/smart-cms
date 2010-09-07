/*
 *
 * This is a simple Content Management System (CMS)
 * Copyright (C) 2010  Imran M Yousuf (imyousuf@smartitengineering.com)
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
package com.smartitengineering.cms.api.common;

import java.util.concurrent.TimeUnit;

/**
 * Represents the lock of the CMS API. The lock basically extends the
 * {@link java.util.concurrent.locks.Lock} by adding capabilities of detecting
 * whether current thread is the owner or not.
 * @author imyousuf
 * @since 0.1
 */
public interface Lock {

  public boolean isLockOwned();

  public void lock();

  public boolean tryLock();

  public boolean tryLock(long time, TimeUnit unit) throws InterruptedException;

  public void unlock();
}
