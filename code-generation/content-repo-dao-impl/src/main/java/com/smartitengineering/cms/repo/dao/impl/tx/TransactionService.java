package com.smartitengineering.cms.repo.dao.impl.tx;

/**
 *
 * @author imyousuf
 */
public interface TransactionService {

  public String getNextTransactionId();

  public void commit(String txId);

  public void rollback(String txId);
}
