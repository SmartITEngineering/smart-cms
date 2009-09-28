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
package com.smartitengineering.cms.content.lock;

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A class for managing instances of locks being used by concrete objects to
 * attain their own lock. The idea is all instances which are similar or IOW
 * equal should share the same {@link Lock} accross every instance and thread.
 * That they their mutual exclusive operations such as CRUD operations can be
 * performed in a synchronous manner. All objects intending to avail this lock
 * facility should implement {@link Key}.
 * @author imyousuf
 * @since 0.1
 */
public class LockManager {

		private static Map<String, LockProvider> locks;

		static {
				locks = new Hashtable<String, LockProvider>();
		}

		/**
		 * Registers a {@link Key} and returns its respective {@link Lock}. If the
		 * key was already present then it will return the same lock instance as for
		 * other instances of the key or else it will create a lock for the key and
		 * return it.
		 * @param key Key to register lock against
		 * @return Lock for the key.
		 */
		public static synchronized Lock register(Key key) {
				if (!locks.containsKey(key.getKeyStringRep())) {
						locks.put(new String(key.getKeyStringRep()), new LockProvider(
										new ReentrantLock()));
				}
				return locks.get(key.getKeyStringRep()).get();
		}

		/**
		 * Unregister a key from the registrar. It does not necessarily mean that
		 * the key/lock will be removed from the registrar. They will be removed if
		 * and only if register and unregister invocation is equal.
		 * @param key Key to unregister
		 */
		public static synchronized void unregister(Key key) {
				if(locks.containsKey(key.getKeyStringRep())) {
						LockProvider provider = locks.get(key.getKeyStringRep());
						if(provider != null) {
								provider.decreateCount();
								if(provider.getRegisterCount() < 1) {
										locks.remove(key.getKeyStringRep());
								}
						}
				}
		}

		private static class LockProvider {

				private int registerCount = 0;
				private Lock lock;

				public LockProvider(Lock lock) {
						this.lock = lock;
				}

				public Lock get() {
						registerCount += 1;
						return lock;
				}

				public void decreateCount() {
						registerCount -= 1;
				}

				public int getRegisterCount() {
						return registerCount;
				}
		}
}
