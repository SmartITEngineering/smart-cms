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
package com.smartitengineering.cms.content.spi.type;

import java.io.File;
import java.net.URL;
import junit.framework.TestCase;

/**
 *
 * @author imyousuf
 */
public class XMLSchemaBasedTypeValidatorTest
				extends TestCase {

		public XMLSchemaBasedTypeValidatorTest(String testName) {
				super(testName);
		}

		@Override
		protected void setUp()
						throws Exception {
				super.setUp();
		}

		/**
		 * Test of isValid method, of class XMLSchemaBasedTypeValidator.
		 */
		public void testIsValid()
						throws Exception {
				TypeValidator validator = new XMLSchemaBasedTypeValidator();
				URL def1 = getClass().getClassLoader().getResource(
								"content-type-def-1.xml");
				File file = new File(def1.toURI());
				assertTrue(validator.isValid(file));
				URL shopping = getClass().getClassLoader().getResource(
								"content-type-def-shopping.xml");
				file = new File(shopping.toURI());
				assertTrue(validator.isValid(file));
				assertFalse(validator.isValid((File)null));
		}
}
