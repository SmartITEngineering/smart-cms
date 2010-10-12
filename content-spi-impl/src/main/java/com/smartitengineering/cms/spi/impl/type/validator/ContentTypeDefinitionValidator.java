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
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.type.CollectionDataType;
import com.smartitengineering.cms.api.type.ContentDataType;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.api.type.FieldDef;
import com.smartitengineering.cms.api.type.FieldValueType;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import com.smartitengineering.cms.api.type.MutableContentType;
import com.smartitengineering.cms.spi.SmartContentSPI;
import com.smartitengineering.cms.spi.type.PersistableContentType;
import com.smartitengineering.cms.type.xml.XMLParserIntrospector;
import com.smartitengineering.cms.type.xml.XmlParser;
import java.util.Iterator;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author kaisar
 */
public class ContentTypeDefinitionValidator {

  private Collection<MutableContentType> contentTypes = new ArrayList<MutableContentType>();
  private Logger logger = LoggerFactory.getLogger(getClass());

  public ContentTypeDefinitionValidator(WorkspaceId workspaceID, InputStream inputStream,
                                        XMLParserIntrospector introspector) {
    if (workspaceID == null || inputStream == null) {
      this.contentTypes = null;
    }
    else {
      this.contentTypes = new XmlParser(workspaceID, inputStream, introspector).parse();
    }
  }

  public Collection<MutableContentType> getIfValid() throws InvalidReferenceException {
    if (validate()) {
      return this.contentTypes;
    }
    else {
      throw new InvalidReferenceException("Is not a valid XML");
    }
  }

  protected boolean validate() {

    logger.info(":::::::::::::::::VALIDATING THE COLLECTION OF CONTENT TYPE AFTER PARSING:::::::::::::::::");

    boolean isValid = true;
    if (this.contentTypes == null) {
      if (logger.isDebugEnabled()) {
        logger.error("No ContentType in Parsed XML");
      }
      isValid = false;
    }
    else {
      for (MutableContentType contentType : this.contentTypes) {
        if (contentType.getParent() != null) {
          final ContentTypeId parent = contentType.getParent();
          isValid = isValid && validateContentTypeIdExistence(parent);
        }
        else {
          Collection<FieldDef> fieldDefs = new ArrayList<FieldDef>();
          fieldDefs = contentType.getMutableFieldDefs();
          if (fieldDefs == null) {
            if (logger.isDebugEnabled()) {
              logger.error("No Field Definitions in " + contentType.getDisplayName());
            }
            isValid = false;
          }
          Iterator<FieldDef> fieldIterator = fieldDefs.iterator();
          while (fieldIterator.hasNext()) {
            FieldDef fieldDef = fieldIterator.next();
            if (fieldDef == null) {
              if (logger.isDebugEnabled()) {
                logger.error("Field Def is empty");
              }
              isValid = false;
            }
            else {
              final FieldValueType type = fieldDef.getValueDef().getType();
              if (type == null) {
                if (logger.isDebugEnabled()) {
                  logger.error("Not a valid Value Def. ");
                }
                isValid = false;
              }
              else {
                if (type.equals(FieldValueType.COLLECTION)) {
                  CollectionDataType dataType = (CollectionDataType) fieldDef.getValueDef();
                  if (dataType == null) {
                    if (logger.isDebugEnabled()) {
                      logger.error("Collection value is empty");
                    }
                    isValid = false;
                  }
                  else if (dataType.getMinSize() > dataType.getMaxSize()) {
                    if (logger.isDebugEnabled()) {
                      logger.error("MinSize = ( " + dataType.getMinSize() + " ) can not be grater than MaxSize = ( " + dataType.
                          getMaxSize() + " )");
                    }
                    isValid = false;
                  }
                  else if (dataType.getItemDataType() == null || dataType.getItemDataType().getType() == null) {
                    if (logger.isDebugEnabled()) {
                      logger.error("Collection's Item type is empty");
                    }
                    isValid = false;
                  }
                  else if (dataType.getItemDataType().getType().equals(FieldValueType.CONTENT)) {
                    ContentDataType contentDataType = (ContentDataType) dataType.getItemDataType();
                    isValid = isValid && validateContent(contentDataType);
                  }
                }
                else if (type.equals(FieldValueType.CONTENT)) {
                  ContentDataType dataType = (ContentDataType) fieldDef.getValueDef();
                  isValid = isValid && validateContent(dataType);
                }
              }
            }
          }
        }
      }
    }
    return isValid;
  }

  private boolean validateContent(ContentDataType contentDataType) throws NullPointerException {
    boolean isValid = true;
    if (contentDataType.getTypeDef() == null) {
      if (logger.isDebugEnabled()) {
        logger.error("Content def inside the collection def can not be empty");
      }
      isValid = false;
    }
    else {
      ContentTypeId typeId = contentDataType.getTypeDef();
      isValid = isValid && validateContentTypeIdExistence(typeId);
    }
    return isValid;
  }

  private boolean validateContentTypeIdExistence(ContentTypeId typeId) {
    boolean isValid = true;
    final PersistableContentType dummyType = SmartContentSPI.getInstance().getPersistableDomainFactory().
        createPersistableContentType();
    dummyType.setContentTypeID(typeId);
    if (StringUtils.isBlank(typeId.getName()) || StringUtils.isBlank(typeId.getNamespace())) {
      if (logger.isDebugEnabled()) {
        logger.error("No Parent or Parent is not correct");
      }
      isValid = false;
    }
    else if (SmartContentAPI.getInstance().getContentTypeLoader().loadContentType(typeId) == null && !this.contentTypes.
        contains(dummyType)) {
      if (logger.isDebugEnabled()) {
        logger.error("Content Type ID does not exist " + typeId);
      }
      isValid = false;
    }
    return isValid;
  }
}
