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
package com.smartitengineering.cms.spi.impl.content;

import com.smartitengineering.cms.api.content.ContentId;
import com.smartitengineering.cms.api.factory.content.WriteableContent;
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.type.FieldDef;
import com.smartitengineering.domain.AbstractGenericPersistentDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class PersistentContent extends AbstractGenericPersistentDTO<PersistentContent, ContentId, Long> {

  private WriteableContent mutableContent;
  protected final transient Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public boolean isValid() {
    if (logger.isDebugEnabled()) {
      logger.debug("Mutable content: " + mutableContent);
      if (mutableContent != null) {
        logger.debug("Mutable content ID: " + mutableContent.getContentId());
        logger.debug("Mutable content Definition: " + mutableContent.getContentDefinition());
        if (mutableContent.getContentDefinition() != null) {
          logger.debug("Mutable required fields present: " + isMandatoryFieldsPresent());
        }
      }
    }
    return mutableContent != null && mutableContent.getContentId() != null && mutableContent.getContentDefinition()
        != null && isMandatoryFieldsPresent();
  }

  protected boolean isMandatoryFieldsPresent() {
    ContentType type = getMutableContent().getContentDefinition();
    boolean valid = true;
    for (FieldDef def : type.getFieldDefs().values()) {
      if (logger.isDebugEnabled()) {
        logger.debug(def.getName() + " is required: " + def.isRequired());
        logger.debug(def.getName() + ": " + getMutableContent().getField(def.getName()));
      }
      if (def.isRequired() && getMutableContent().getField(def.getName()) == null) {
        valid = valid && false;
      }
    }
    return valid;
  }

  public WriteableContent getMutableContent() {
    return mutableContent;
  }

  public void setMutableContent(WriteableContent mutableContent) {
    this.mutableContent = mutableContent;
  }

  @Override
  public ContentId getId() {
    if (getMutableContent() == null) {
      return null;
    }
    return getMutableContent().getContentId();
  }

  @Override
  @Deprecated
  public void setId(ContentId id) {
    throw new UnsupportedOperationException("Do not use this operation!");
  }
}
