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
package com.smartitengineering.cms.content.api;

/**
 * Editable version of {@link TextualContentType}.
 * @author imyousuf
 * @since 0.1
 */
public interface MutableTextualContentType
				extends TextualContentType,
								MutableContentType {

		/**
		 * Sets the content type id of the parent content type. It may be to indicate
		 * that there is no parent. If content type id is invalid there could be
		 * error during write operation.
		 * @param contentTypeID ID of the parent content type
		 */
		public void setParentContentTypeID(ContentTypeID contentTypeID);
}
