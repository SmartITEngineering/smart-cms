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
package com.smartitengineering.cms.api.impl.workspace;

import com.smartitengineering.cms.api.workspace.RepresentationTemplate;
import com.smartitengineering.cms.api.workspace.VariationTemplate;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.spi.SmartContentSPI;
import com.smartitengineering.cms.spi.workspace.PersistableWorkspace;
import java.util.Collection;
import java.util.Date;

/**
 *
 * @author imyousuf
 */
public class WorkspaceImpl implements PersistableWorkspace {

  private WorkspaceId id;
  private Date creationDate;

  @Override
  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  @Override
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
    return SmartContentSPI.getInstance().getWorkspaceService().getContentDefintions(getId());
  }

  @Override
  public Collection<WorkspaceId> getFriendlies() {
    return SmartContentSPI.getInstance().getWorkspaceService().getFriendlies(getId());
  }

  @Override
  public RepresentationTemplate getRepresentation(String name) {
    return SmartContentSPI.getInstance().getWorkspaceService().getRepresentationTemplate(id, name);
  }

  @Override
  public VariationTemplate getVariations(String name) {
    return SmartContentSPI.getInstance().getWorkspaceService().getVariationTemplate(id, name);
  }
}
