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
package com.smartitengineering.cms.api.impl.workspace;

import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.workspace.SequenceId;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.cms.spi.SmartContentSPI;
import com.smartitengineering.cms.spi.workspace.PersistableSequence;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author imyousuf
 */
public class SequenceImpl implements PersistableSequence {

  private final AtomicLong currentValue;
  private String name;
  private WorkspaceId workspace;

  public SequenceImpl() {
    currentValue = new AtomicLong();
  }

  public void setCurrentValue(long currentValue) {
    this.currentValue.set(currentValue);
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setWorkspace(WorkspaceId workspaceId) {
    this.workspace = workspaceId;
  }

  public long getCurrentValue() {
    return currentValue.longValue();
  }

  public String getName() {
    return name;
  }

  public WorkspaceId getWorkspace() {
    return workspace;
  }

  public long change(long delta) {
    final long newValue = SmartContentSPI.getInstance().getWorkspaceService().modifySequenceValue(this, delta);
    setCurrentValue(newValue);
    return newValue;
  }

  public long increment() {
    return change(1);
  }

  public long decrement() {
    return change(-1);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final SequenceImpl other = (SequenceImpl) obj;
    if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
      return false;
    }
    if (this.workspace != other.workspace && (this.workspace == null || !this.workspace.equals(other.workspace))) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 67 * hash + (this.name != null ? this.name.hashCode() : 0);
    hash = 67 * hash + (this.workspace != null ? this.workspace.hashCode() : 0);
    return hash;
  }

  public SequenceId getSequenceId() {
    return SmartContentAPI.getInstance().getWorkspaceApi().createSequenceId(workspace, name);
  }
}
