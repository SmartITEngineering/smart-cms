package com.smartitengineering.cms.repo.dao.impl;

import com.smartitengineering.cms.api.factory.write.Lock;
import com.smartitengineering.domain.AbstractGenericPersistentDTO;
import com.smartitengineering.domain.PersistentDTO;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author imyousuf
 */
public abstract class AbstractRepositoryDomain<T extends PersistentDTO>
    extends AbstractGenericPersistentDTO<T, String, Long> implements Lock {

  protected Date creationDate, lastModificationDate;
  protected String status;
  protected String entityValue;
  protected String workspaceId;
  protected Lock lock;

  protected AbstractRepositoryDomain() {
  }

  public Lock getLock() {
    return lock;
  }

  protected void setLock(Lock lock) {
    this.lock = lock;
  }

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

  public boolean isLockOwned() {
    return lock != null ? lock.isLockOwned() : true;
  }

  public void lock() {
    if (lock != null) {
      lock.lock();
    }
  }

  public boolean tryLock() {
    return lock != null ? lock.tryLock() : true;
  }

  public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
    return lock != null ? lock.tryLock(time, unit) : false;
  }

  public void unlock() {
    if (lock != null) {
      lock.unlock();
    }
  }
}
