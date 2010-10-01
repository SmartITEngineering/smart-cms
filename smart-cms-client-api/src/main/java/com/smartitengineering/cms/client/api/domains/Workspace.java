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
package com.smartitengineering.cms.client.api.domains;

import java.util.Date;
import java.util.List;

/**
 *
 * @author imyousuf
 */
public class Workspace {

  private WorkspaceId id;
  private Date creationDate;
  private List<WorkspaceId> friendlies;

  public Date getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public List<WorkspaceId> getFriendlies() {
    return friendlies;
  }

  public void setFriendlies(List<WorkspaceId> friendlies) {
    this.friendlies = friendlies;
  }

  public WorkspaceId getId() {
    return id;
  }

  public void setId(WorkspaceId id) {
    this.id = id;
  }
}
