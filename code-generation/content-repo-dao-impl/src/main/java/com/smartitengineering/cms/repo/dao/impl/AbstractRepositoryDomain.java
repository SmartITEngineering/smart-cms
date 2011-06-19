package com.smartitengineering.cms.repo.dao.impl;

import com.smartitengineering.domain.AbstractGenericPersistentDTO;
import com.smartitengineering.domain.PersistentDTO;
import java.util.Date;

/**
 *
 * @author imyousuf
 */
public abstract class AbstractRepositoryDomain<T extends PersistentDTO>
    extends AbstractGenericPersistentDTO<T, String, Long> {

  protected Date creationDate, lastModificationDate;
  protected String status;
  protected String entityValue;
  protected String workspaceId;

  public String getWorkspaceId() {
    return workspaceId;
  }

  public void setWorkspaceId(String workspaceId) {
    this.workspaceId = workspaceId;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public String getEntityValue() {
    return entityValue;
  }

  public void setEntityValue(String entityValue) {
    this.entityValue = entityValue;
  }

  public Date getLastModificationDate() {
    return lastModificationDate;
  }

  public void setLastModificationDate(Date lastModificationDate) {
    this.lastModificationDate = lastModificationDate;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public boolean isValid() {
    return true;
  }
}
