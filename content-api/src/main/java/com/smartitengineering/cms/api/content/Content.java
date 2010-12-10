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
package com.smartitengineering.cms.api.content;

import com.smartitengineering.cms.api.common.ReferrableResource;
import com.smartitengineering.cms.api.type.ContentStatus;
import com.smartitengineering.cms.api.type.ContentType;
import java.util.Date;
import java.util.Map;

/**
 *
 * @author imyousuf
 */
public interface Content extends ReferrableResource {

  public ContentId getContentId();

  public ContentId getParentId();

  public Content getParent();

  public ContentType getContentDefinition();

  public Map<String, Field> getFields();

  public Map<String, Field> getOwnFields();

  public Field getField(String fieldName);

  public ContentStatus getStatus();

  public Representation getRepresentation(String repName);

  public Date getCreationDate();

  public Date getLastModifiedDate();

  public String getEntityTagValue();
}
