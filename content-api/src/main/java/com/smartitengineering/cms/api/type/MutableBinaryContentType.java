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
 * Editable version of {@link BinaryContentType}
 * @author imyousuf
 * @since 0.1
 */
public interface MutableBinaryContentType
				extends BinaryContentType,
								MutableContentType {

		/**
		 * Sets the new mime type for this binary content type.
		 * @param newMimeType New mime type
		 * @throws IllegalArgumentException If new mime type is blank string
		 */
		public void setMimeType(String newMimeType)
						throws IllegalArgumentException;
}
