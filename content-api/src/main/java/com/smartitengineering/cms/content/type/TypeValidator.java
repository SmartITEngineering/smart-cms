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
package com.smartitengineering.cms.content.type;

import java.io.File;
import org.w3c.dom.Document;

/**
 * An API for validating content type
 * @author imyousuf
 * @since 0.1
 */
public interface TypeValidator {

		/**
		 * Validate the specified content type definition.
		 * @param contentTypeDef File to validate
		 * @return True if valid else false
		 * @throws java.lang.Exception If any error in validating
		 */
		public boolean isValid(File contentTypeDef)
						throws Exception;

		/**
		 * Validate the specified content type definition.
		 * @param document XML {@link Document} to validate
		 * @return True if valid else false
		 * @throws Exception If any error in validating
		 */
		public boolean isValid(Document document)
						throws Exception;
}
