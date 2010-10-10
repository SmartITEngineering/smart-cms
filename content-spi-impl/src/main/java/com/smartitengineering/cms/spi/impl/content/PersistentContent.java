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

/**
 *
 * @author imyousuf
 */
public class PersistentContent extends AbstractGenericPersistentDTO<PersistentContent, ContentId, Long> {

  private WriteableContent mutableContent;

  @Override
  public boolean isValid() {
    return mutableContent != null && mutableContent.getContentId() != null && mutableContent.getContentDefinition() !=
        null && isMandatoryFieldsPresent();
  }

  protected boolean isMandatoryFieldsPresent() {
    ContentType type = getMutableContent().getContentDefinition();
    for (FieldDef def : type.getFieldDefs().values()) {
      if (def.isRequired() && getMutableContent().getField(def.getName()) == null) {
        return false;
      }
    }
    return true;
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
