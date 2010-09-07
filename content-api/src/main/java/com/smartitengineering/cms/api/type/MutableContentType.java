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
package com.smartitengineering.cms.api.type;

import com.smartitengineering.cms.api.common.PersistentWriter;
import java.util.Collection;

/**
 * A mutable version of {@link ContentType}. It should be used attained using a
 * factory.
 * @author imyousuf
 * @since 0.1
 */
public interface MutableContentType
				extends PersistentWriter,
								ContentType {

		/**
		 * Set the content type identifier for the content type.
		 * @param contentTypeID The content type identifier
		 * @throws IllegalArgumentException If contentTypeID is null
		 */
		public void setContentTypeID(ContentTypeId contentTypeID)
						throws IllegalArgumentException;

		/**
		 * Retrieve the statuses available for the workflow of contents of
		 * this type in a mutable {@link Collection}
		 * @return Mutable collection fo statuses, could be empty if no status
		 */
		public Collection<ContentStatus> getMutableStatuses();

		/**
		 * Retrieve the defined fields for this content type in a mutable
		 * {@link Collection}
		 * @return Mutable collection of fields, could be empty if not field
		 */
		public Collection<FieldDef> getMutableFields();
}
