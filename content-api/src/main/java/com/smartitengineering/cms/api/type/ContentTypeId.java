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

import com.smartitengineering.cms.api.WorkspaceId;
import com.smartitengineering.dao.impl.hbase.spi.Externalizable;
import java.io.Serializable;

/**
 * Represents the unique identifier for a {@link ContentType}, specified by its
 * namespace, similar to that of package of java, and its name.
 * @author imyousuf
 * @since 0.1
 */
public interface ContentTypeId extends Externalizable, Comparable<ContentTypeId> {

  public WorkspaceId getWorkspace();

  /**
   * Retrieve the name of the {@link ContentType}
   * @return a non-empty string
   */
  public String getName();

  /**
   * Retrieve the namespace of the {@link ContentType}
   * @return a non-null string, but may be empty
   */
  public String getNamespace();

  /**
   * Override the toString so that it could be used to compare to ids of this instance. It should represent the state
   * of the Id.
   * @return String representation, i.e. state, of the id
   */
  @Override
  public String toString();
}
