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
package com.smartitengineering.cms.spi.impl.lock;

import com.smartitengineering.cms.spi.lock.*;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Server side implementation of locks
 * @author imyousuf
 */
public class LockHandlerImpl
				implements LockHandler {

		private Map<String, LockProvider> locks;

		{
				locks = new Hashtable<String, LockProvider>();
		}

		public synchronized Lock register(Key key) {
				if (!locks.containsKey(key.getKeyStringRep())) {
						locks.put(new String(key.getKeyStringRep()), new LockProvider(
										new ReentrantLock()));
				}
				return locks.get(key.getKeyStringRep()).get();
		}

		public synchronized void unregister(Key key) {
				if (locks.containsKey(key.getKeyStringRep())) {
						LockProvider provider = locks.get(key.getKeyStringRep());
						if (provider != null) {
								provider.decreateCount();
								if (provider.getRegisterCount() < 1) {
										locks.remove(key.getKeyStringRep());
								}
						}
				}
		}

		private static class LockProvider {

				private int registerCount = 0;
				private ReentrantLock lock;

				public LockProvider(ReentrantLock lock) {
						this.lock = lock;
				}

				public Lock get() {
						registerCount += 1;
						return lock;
				}

				public void decreateCount() {
						if (lock.isHeldByCurrentThread()) {
								lock.unlock();
						}
						registerCount -= 1;
				}

				public int getRegisterCount() {
						return registerCount;
				}
		}
}
