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
package com.smartitengineering.cms.api.impl;

import com.smartitengineering.cms.api.Workspace;
import com.smartitengineering.cms.api.WorkspaceId;
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.type.RepresentationDef;
import com.smartitengineering.cms.api.type.VariationDef;
import com.smartitengineering.cms.spi.SmartSPI;
import java.util.Collection;
import java.util.Date;

/**
 *
 * @author imyousuf
 */
public class WorkspaceImpl implements Workspace {

  private WorkspaceId id;
  private Date creationDate;

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public void setId(WorkspaceId id) {
    this.id = id;
  }

  @Override
  public WorkspaceId getId() {
    return id;
  }

  @Override
  public Date getCreationDate() {
    return creationDate;
  }

  @Override
  public Collection<ContentType> getContentDefintions() {
    return SmartSPI.getInstance().getWorkspaceService().getContentDefintions(getId());
  }

  @Override
  public Collection<WorkspaceId> getFriendlies() {
    return SmartSPI.getInstance().getWorkspaceService().getFriendlies(getId());
  }

  @Override
  public Collection<RepresentationDef> getRepresentations() {
    return SmartSPI.getInstance().getWorkspaceService().getRepresentations(getId());
  }

  @Override
  public Collection<VariationDef> getVariations() {
    return SmartSPI.getInstance().getWorkspaceService().getVariations(getId());
  }
}
