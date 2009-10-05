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
package com.smartitengineering.cms.content.api.impl;

import com.smartitengineering.cms.content.spi.lock.Key;
import com.smartitengineering.cms.content.spi.lock.LockManager;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

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
		private boolean lockAttained;

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

		public void lock() {
				lock.lock();
				synchronized(this) {
						setLockAttained(true);
				}
		}

		public void lockInterruptibly()
						throws InterruptedException {
				lock.lockInterruptibly();
		}

		public boolean tryLock() {
				setLockAttained(lock.tryLock());
				return isLockAttained();
		}

		public boolean tryLock(long time,
													 TimeUnit unit)
						throws InterruptedException {
				setLockAttained(lock.tryLock(time, unit));
				return isLockAttained();
		}

		public void unlock() {
				lock.unlock();
				synchronized(this) {
						setLockAttained(false);
				}
		}

		public Condition newCondition() {
				return lock.newCondition();
		}

		protected boolean isLockAttained() {
				return lockAttained;
		}

		private void setLockAttained(boolean lockAttained) {
				this.lockAttained = lockAttained;
		}
}
