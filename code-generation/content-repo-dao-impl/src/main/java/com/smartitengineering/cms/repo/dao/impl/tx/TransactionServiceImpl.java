package com.smartitengineering.cms.repo.dao.impl.tx;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.smartitengineering.cms.repo.dao.tx.TransactionManager;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author imyousuf
 */
@Singleton
public class TransactionServiceImpl implements TransactionService {

  private final AtomicLong uniqueIdGen = new AtomicLong(Long.MIN_VALUE);
  private final TransactionManager manager;

  @Inject
  public TransactionServiceImpl(TransactionManager manager) {
    this.manager = manager;
  }

  public String getNextTransactionId() {
    return String.valueOf(uniqueIdGen.incrementAndGet());
  }

  public void commit(String txId) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void rollback(String txId) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
