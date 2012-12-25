package com.smartitengineering.cms.repo.dao.tx;

/**
 * API for manually working with transactions
 * @author imyousuf
 */
public interface TransactionManager {

  /**
   * Start a new transaction.
   * @return A new transaction
   */
  public Transaction beginTransaction();

  /**
   * Start a transaction that is isolated from rest of the transactions of the system
   * @param isolatedTransaction Whether this transaction is isolated or not
   * @return A new transaction
   */
  public Transaction beginTransaction(boolean isolatedTransaction);

  /**
   * Retrieves the current transaction in "effect". In effect means in this context a non-completed transaction
   * @return Currently effective transaction or null
   */
  public Transaction getCurrentTransaction();
}
