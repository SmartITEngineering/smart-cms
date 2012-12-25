package com.smartitengineering.cms.repo.dao.impl.tx;

import com.smartitengineering.cms.repo.dao.impl.AbstractRepositoryDomain;

/**
 *
 * @author imyousuf
 */
public interface TransactionService {

  public String getNextTransactionId();

  public void commit(String txId);

  public void rollback(String txId);

  public <T extends AbstractRepositoryDomain> void save(TransactionElement<T> element);

  public <T extends AbstractRepositoryDomain> void update(TransactionElement<T> element);

  public <T extends AbstractRepositoryDomain> void delete(TransactionElement<T> element);
}
