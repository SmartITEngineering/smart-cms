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
package com.smartitengineering.cms.api.type;

/**
 * Represents a single status defines in a {@link ContentType}.
 * @author imyousuf
 * since 0.1
 */
public interface ContentStatus {

		/**
		 * Retrieve the unique identifier integer for this status.
		 * @return unique positive identifier
		 */
		public int getId();

		/**
		 * Retrieve the identifier for the associated content type.
		 * @return associated content type
		 */
		public ContentTypeID getContentType();

		/**
		 * Retrive the name of the status. It has to be a non-empty string.
		 * @return name of the status
		 */
		public String getName();
}
