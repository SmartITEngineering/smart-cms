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
package com.smartitengineering.cms.ws.common.domains;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 *
 * @author imyousuf
 */
@JsonDeserialize(as = ContentImpl.class)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_DEFAULT)
public interface Content {

  String getContentId();

  String getReindexUri();

  String getSelfUri();

  Date getCreationDate();

  Date getLastModifiedDate();

  String getContentTypeUri();

  String getParentContentUri();

  String getStatus();

  Collection<Field> getFields();

  Map<String, Field> getFieldsMap();

  Map<String, String> getRepresentations();

  Map<String, String> getRepresentationsByName();

  boolean isPrivateContent();
}
