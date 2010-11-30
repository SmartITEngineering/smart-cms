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
package com.smartitengineering.cms.spi.impl.type.validator;

import com.smartitengineering.cms.api.exception.InvalidReferenceException;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.cms.api.common.MediaType;
import com.smartitengineering.cms.api.factory.type.WritableContentType;
import com.smartitengineering.cms.api.type.FieldDef;
import com.smartitengineering.cms.api.type.MutableContentType;
import com.smartitengineering.cms.api.type.MutableFieldDef;
import com.smartitengineering.cms.spi.SmartContentSPI;
import com.smartitengineering.cms.spi.type.ContentTypeDefinitionParser;
import com.smartitengineering.cms.spi.type.PersistableContentType;
import com.smartitengineering.cms.type.xml.XMLParserIntrospector;
import com.smartitengineering.cms.type.xml.XmlConstants;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author kaisar
 */
public class XMLContentTypeDefinitionParser implements ContentTypeDefinitionParser {

  private Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public Collection<WritableContentType> parseStream(WorkspaceId workspaceId, InputStream inputStream) throws
      InvalidReferenceException {
    ContentTypeDefinitionValidator parser = new ContentTypeDefinitionValidator(workspaceId, inputStream, new XMLParserIntrospector() {

      @Override
      public MutableContentType createMutableContentType() {
        return SmartContentSPI.getInstance().getPersistableDomainFactory().createPersistableContentType();
      }

      @Override
      public void processMutableContentType(MutableContentType type, Element element) {
        if(type.getPrimaryFieldDef() != null) {
          FieldDef fieldDef = type.getPrimaryFieldDef();
          if(fieldDef instanceof MutableFieldDef) {
            MutableFieldDef mutableFieldDef = (MutableFieldDef) fieldDef;
            mutableFieldDef.setRequired(true);
          }
        }
        if (type instanceof PersistableContentType) {
          PersistableContentType contentType = (PersistableContentType) type;
          contentType.setRepresentations(Collections.singletonMap(MediaType.APPLICATION_XML, createRootNodeAndAddChild(
              element.copy()).toXML()));
        }
      }
    });
    Collection<MutableContentType> parse = new ArrayList<MutableContentType>();

    parse = parser.getIfValid();

    final List<WritableContentType> list = new ArrayList<WritableContentType>(parse.size());
    for (MutableContentType type : parse) {
      list.add((WritableContentType) type);
    }
    return list;
  }

  @Override
  public Collection<MediaType> getSupportedTypes() {
    return Collections.singletonList(MediaType.APPLICATION_XML);
  }

  protected Element createRootNodeAndAddChild(Node childNode) {
    Element root = new Element(XmlConstants.CONTENT_TYPES, XmlConstants.NAMESPACE);
    Attribute attr = new Attribute("xsi:schemaLocation", XmlConstants.XSI_NAMESPACE, new StringBuilder(
        XmlConstants.XSI_NAMESPACE).append(' ').append(
        SmartContentSPI.getInstance().getSchemaLocationForContentTypeXml()).toString());
    root.addAttribute(attr);
    root.appendChild(childNode);
    if (logger.isDebugEnabled()) {
      logger.debug(root.toXML());
    }
    return root;
  }
}
