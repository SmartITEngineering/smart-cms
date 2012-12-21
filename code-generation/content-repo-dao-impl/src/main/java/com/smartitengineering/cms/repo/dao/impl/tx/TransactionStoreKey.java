package com.smartitengineering.cms.repo.dao.impl.tx;

import com.smartitengineering.cms.repo.dao.impl.AbstractRepositoryDomain;
import java.io.Serializable;

/**
 *
 * @author imyousuf
 */
public interface TransactionStoreKey extends Serializable {

  public String getTransactionId();

  public String getObjectId();

  public long getOpTimestamp();

  public <T extends AbstractRepositoryDomain> Class<T> getObjectType();

  public void setOpTimestamp(long opTimestamp);

  public void setTransactionId(String txId);

  public void setObjectId(String objectId);

  public <T extends AbstractRepositoryDomain> void setObjectType(Class<T> type);
}
