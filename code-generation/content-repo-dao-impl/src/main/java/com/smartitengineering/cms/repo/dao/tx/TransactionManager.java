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
   * Retrieves the current transaction in "effect". In effect means in this context a non-completed transaction
   * @return Currently effective transaction or null
   */
  public Transaction getCurrentTransaction();
}
