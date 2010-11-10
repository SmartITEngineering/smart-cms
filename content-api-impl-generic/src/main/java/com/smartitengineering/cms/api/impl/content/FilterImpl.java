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
package com.smartitengineering.cms.api.impl.content;

import com.smartitengineering.cms.api.content.Filter;
import com.smartitengineering.cms.api.type.ContentStatus;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.dao.common.queryparam.QueryParameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author kaisar
 */
public class FilterImpl implements Filter {

  private final Set<ContentTypeId> types = new HashSet<ContentTypeId>();
  private QueryParameter<Date> modifiedDateParameter;
  private QueryParameter<Date> creationDateParameter;
  private final List<QueryParameter> parameters = new ArrayList<QueryParameter>();
  private final Set<ContentStatus> statuses = new HashSet<ContentStatus>();
  private boolean disjunction;
  private int startFrom = 0;
  private int maxContents = 10;
  private WorkspaceId workspaceId;

  @Override
  public void addContentTypeToFilter(ContentTypeId... types) {
    if (types != null) {
      this.types.addAll(Arrays.asList(types));
    }
  }

  @Override
  public void removeContentTypeFromFilter(ContentTypeId... types) {
    if (types != null) {
      this.types.removeAll(Arrays.asList(types));
    }
  }

  @Override
  public void setCreationDateFilter(QueryParameter<Date> creationDateParam) {
    this.creationDateParameter = creationDateParam;
  }

  @Override
  public void setLastModifiedDateFilter(QueryParameter<Date> modifiedDateParam) {
    this.modifiedDateParameter = modifiedDateParam;
  }

  @Override
  public void addFieldFilter(QueryParameter... parameters) {
    if (parameters != null) {
      this.parameters.addAll(Arrays.asList(parameters));
    }
  }

  @Override
  public void removeFieldFilter(QueryParameter... parameters) {
    if (parameters != null) {
      this.parameters.removeAll(Arrays.asList(parameters));
    }
  }

  @Override
  public void addStatusFilter(ContentStatus... status) {
    if (status != null) {
      this.statuses.addAll(Arrays.asList(status));
    }
  }

  @Override
  public void removeStatusFilter(ContentStatus... status) {
    if (status != null) {
      this.statuses.removeAll(Arrays.asList(status));
    }
  }

  @Override
  public Set<ContentTypeId> getContentTypeFilters() {
    return Collections.unmodifiableSet(types);
  }

  @Override
  public QueryParameter<Date> getCreationDateFilter() {
    return creationDateParameter;
  }

  @Override
  public QueryParameter<Date> getLastModifiedDateFilter() {
    return modifiedDateParameter;
  }

  @Override
  public Collection<QueryParameter> getFieldFilters() {
    return Collections.unmodifiableCollection(parameters);
  }

  @Override
  public Set<ContentStatus> getStatusFilters() {
    return Collections.unmodifiableSet(statuses);
  }

  @Override
  public boolean isDisjunction() {
    return disjunction;
  }

  @Override
  public void setDisjunction(boolean disjunction) {
    this.disjunction = disjunction;
  }

  @Override
  public void setWorkspaceId(WorkspaceId workspaceId) {
    this.workspaceId = workspaceId;
  }

  @Override
  public WorkspaceId getWorkspaceId() {
    return workspaceId;
  }

  @Override
  public int getMaxContents() {
    return maxContents;
  }

  @Override
  public int getStartFrom() {
    return startFrom;
  }

  @Override
  public void setMaxContents(int maxContents) {
    this.maxContents = maxContents;
  }

  @Override
  public void setStartFrom(int startFrom) {
    this.startFrom = startFrom;
  }
}
