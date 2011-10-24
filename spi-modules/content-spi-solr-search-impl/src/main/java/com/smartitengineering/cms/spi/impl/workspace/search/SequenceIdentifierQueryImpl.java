/*
 *
 * This is a simple Content Management System (CMS)
 * Copyright (C) 2011  Imran M Yousuf (imyousuf@smartitengineering.com)
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
package com.smartitengineering.cms.spi.impl.workspace.search;

import com.smartitengineering.cms.api.workspace.Sequence;
import com.smartitengineering.cms.spi.impl.events.SolrFieldNames;
import com.smartitengineering.common.dao.search.solr.spi.ObjectIdentifierQuery;

/**
 *
 * @author imyousuf
 */
public class SequenceIdentifierQueryImpl implements ObjectIdentifierQuery<Sequence> {

  private static final String TYPE_ID = new StringBuilder(SolrFieldNames.TYPE).append(": \"").append(
      SequenceHelper.SEQUENCE).append("\" AND id: \"").toString();

  @Override
  public String getQuery(Sequence object) {
    return new StringBuilder(TYPE_ID).append(object.getSequenceId().toString()).append('"').toString();
  }
}
