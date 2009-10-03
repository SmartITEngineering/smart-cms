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
package com.smartitengineering.cms.content.spi.impl.type;

import com.smartitengineering.cms.content.spi.type.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Implementation for validating XML content type definition based on Schema
 * @author imyousuf
 * @since 0.1
 */
public class XMLSchemaBasedTypeValidator
				implements TypeValidator {

		public static final String CONTENT_TYPE_SCHEMA_URI =
															 "http://www.smartitengineering.com/smart-cms/content/content-type-schema";
		public static final String XSD_LOCATION =
															 "com/smartitengineering/cms/content/content-type-schema.xsd";

		public boolean isValid(File contentTypeDef)
						throws Exception {
				if (contentTypeDef == null) {
						return false;
				}
				Document document = getDocumentForSource(contentTypeDef);
				return isValid(document);
		}

		public boolean isValid(Document document)
						throws Exception {
				// create a SchemaFactory capable of understanding WXS schemas
				SchemaFactory factory = SchemaFactory.newInstance(
								XMLConstants.W3C_XML_SCHEMA_NS_URI);
				final InputStream xsdStream = getClass().getClassLoader().
								getResourceAsStream(XSD_LOCATION);
				// load a WXS schema, represented by a Schema instance
				Source schemaFile = new StreamSource(xsdStream);
				schemaFile.setSystemId(CONTENT_TYPE_SCHEMA_URI);
				Schema schema = factory.newSchema(schemaFile);
				// create a Validator instance, which can be used to validate an instance document
				Validator validator = schema.newValidator();
				// validate the DOM tree
				try {
						validator.validate(new DOMSource(document));
						return true;
				}
				catch (SAXException e) {
						e.printStackTrace();
						return false;
				}
		}

		private Document getDocumentForSource(File contentTypeDef)
						throws SAXException,
									 ParserConfigurationException,
									 IOException {
				final DocumentBuilderFactory docBuilderFactor =
																		 DocumentBuilderFactory.newInstance();
				docBuilderFactor.setNamespaceAware(true);
				DocumentBuilder parser = docBuilderFactor.newDocumentBuilder();
				parser.setErrorHandler(new ErrorHandler() {

						public void warning(SAXParseException exception)
										throws SAXException {
								throw exception;
						}

						public void error(SAXParseException exception)
										throws SAXException {
								throw exception;
						}

						public void fatalError(SAXParseException exception)
										throws SAXException {
								throw exception;
						}
				});
				Document document = parser.parse(contentTypeDef);
				return document;
		}
}
