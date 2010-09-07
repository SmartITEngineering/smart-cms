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
package com.smartitengineering.cms.api.type;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * Defines a specific type of content identified by {@link ContentTypeID}.
 * This is the generalized form of content definition.
 * @author imyousuf
 * @since 0.1
 */
public interface ContentType {

  /**
   * Retrieve the unique ID of the content type to be used for relating
   * this content type to other objects.
   * @return the id representation of the content type
   */
  public ContentTypeId getContentTypeID();

  /**
   * Retrieve the statuses available for the workflow of contents of
   * this type. The collection returned could be unmodifiable.
   * @return {@link Collection} of statuses of this content type
   */
  public Map<String, ContentStatus> getStatuses();

  /**
   * Retrieve the defined fields for this content type. The collection
   * returned could be unmodifiable.
   * @return defined fields
   */
  public Map<String, FieldDef> getFields();

  public ContentTypeId getParent();

  public String getDisplayName();

  public Map<String, RepresentationDef> getRepresentations();

  public Date getCreationDate();

  public Date getLastModifiedDate();
}
