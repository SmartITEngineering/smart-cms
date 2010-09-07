/*
 *
 * This is a simple Content Management System (CMS)
 * Copyright (C) 2009  Imran M Yousuf (imyousuf@smartitengineering.com)
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
package com.smartitengineering.cms.api.content.impl;

import com.smartitengineering.cms.api.content.Filter;
import com.smartitengineering.cms.api.type.ContentStatus;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.dao.common.queryparam.QueryParameter;
import java.util.Date;

/**
 *
 * @author kaisar
 */
public class FilterImpl implements Filter {

  private ContentTypeId[] types;
  private QueryParameter<Date> modifiedDateParameter;
  private QueryParameter<Date> creationDateParameter;
  private QueryParameter<String>[] parameters;
  private ContentStatus[] status;

  public void addContentTypeToFilter(ContentTypeId... types) {
    this.types = types;
  }

  public void removeContentTypeFromFilter(ContentTypeId... types) {
    this.types = types;
  }

  public void setCreationDateFilter(QueryParameter<Date> creationDateParam) {
    this.creationDateParameter = creationDateParam;
  }

  public void setLastModifiedDateFilter(QueryParameter<Date> modifiedDateParam) {
    this.modifiedDateParameter = modifiedDateParam;
  }

  public void addFieldFilter(QueryParameter... parameters) {
    this.parameters = parameters;
  }

  public void removeFieldFilter(QueryParameter... parameters) {
    this.parameters = parameters;
  }

  public void addStatusFilter(ContentStatus... status) {
    this.status = status;
  }

  public void removeStatusFilter(ContentStatus... status) {
    this.status = status;
  }
}
