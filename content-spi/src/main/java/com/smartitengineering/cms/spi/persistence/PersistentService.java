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
package com.smartitengineering.cms.spi.persistence;

import com.smartitengineering.cms.api.common.PersistentWriter;

/**
 * A service API for performing the C, U, D of <tt>CRUD</tt> operations. This
 * will basically abstract the invocation to persistent storage.
 * @author imyousuf
 * @since 0.1
 */
public interface PersistentService<T extends PersistentWriter> {

		/**
		 * Save the supplied bean as a new bean.
		 * @param bean The new bean
		 * @throws Exception If any error during creation
		 */
		public void create(T bean)
						throws Exception;

		/**
		 * Update the supplied bean and will be assumed that it was already created.
		 * @param bean Bean to be updated
		 * @throws Exception If any error during creation
		 */
		public void update(T bean)
						throws Exception;

		/**
		 * Delete the supplied bean from the persistent storage.
		 * @param bean Bean to be deleted
		 * @throws Exception If any error during creation
		 */
		public void delete(T bean)
						throws Exception;
}
