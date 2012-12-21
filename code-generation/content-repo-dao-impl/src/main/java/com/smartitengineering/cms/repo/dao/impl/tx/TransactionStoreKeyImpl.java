package com.smartitengineering.cms.repo.dao.impl.tx;

import com.smartitengineering.cms.repo.dao.impl.AbstractRepositoryDomain;

/**
 *
 * @author imyousuf
 */
class TransactionStoreKeyImpl implements TransactionStoreKey {

  private String transactionId;
  private Class<? extends AbstractRepositoryDomain> objectType;
  private String objectId;
  private long opTimestamp;

  public String getObjectId() {
    return objectId;
  }

  public void setObjectId(String objectId) {
    this.objectId = objectId;
  }

  public <T extends AbstractRepositoryDomain> Class<T> getObjectType() {
    return (Class<T>) objectType;
  }

  public <T extends AbstractRepositoryDomain> void setObjectType(Class<T> objectType) {
    this.objectType = objectType;
  }

  public long getOpTimestamp() {
    return opTimestamp;
  }

  public void setOpTimestamp(long opTimestamp) {
    this.opTimestamp = opTimestamp;
  }

  public String getTransactionId() {
    return transactionId;
  }

  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }
}
