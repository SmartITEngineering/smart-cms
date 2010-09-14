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
package com.smartitengineering.cms.api.impl.type;

import com.smartitengineering.cms.api.type.ContentStatus;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.api.type.MutableContentStatus;

/**
 *
 * @author kaisar
 */
public class ContentStatusImpl implements MutableContentStatus,ContentStatus {

  private int id;
  private ContentTypeId contentTypeId;
  private String name;

  @Override
  public int getId() {
    return this.id;

  }

  @Override
  public ContentTypeId getContentType() {
    return this.contentTypeId;

  }

  @Override
  public String getName() {
    return this.name;

  }

  @Override
  public void setId(int id) {
    this.id = id;
  }

  @Override
  public void setName(String newName) throws IllegalArgumentException {
    this.name = newName;
  }

  @Override
  public void setContentTypeID(ContentTypeId typeId) throws IllegalArgumentException {
    this.contentTypeId = typeId;
  }
}
