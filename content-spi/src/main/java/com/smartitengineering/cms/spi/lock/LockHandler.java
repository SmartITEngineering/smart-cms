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
package com.smartitengineering.cms.spi.lock;

import java.util.concurrent.locks.ReentrantLock;

/**
 * The SPI used the implementation to achieve locks.
 * @author imyousuf
 */
public interface LockHandler {

		/**
		 * Registers a {@link Key} and returns its respective 
     * {@link ReentrantLock lock}. If the key was already present then it will
     * return the same lock instance as for other instances of the key or else
     * it will create a lock for the key and return it.
		 * @param key Key to register lock against
		 * @return Lock for the key.
		 */
		public ReentrantLock register(Key key);

		/**
		 * Unregister a key from the registrar. It does not necessarily mean that
		 * the key/lock will be removed from the registrar. They will be removed if
		 * and only if register and unregister invocation is equal.
		 * @param key Key to unregister
		 */
		public void unregister(Key key);
}
