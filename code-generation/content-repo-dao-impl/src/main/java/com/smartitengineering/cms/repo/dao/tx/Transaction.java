package com.smartitengineering.cms.repo.dao.tx;

/**
 * API for interacting with the transaction. Once a transaction is started there is basically only 2 operations that can
 * be performed either a commit or a rollback; those 2 primary operations are exposed by the API. In addition the state
 * of transaction is exposed too. Transaction by nature is idempotent.
 * @author imyousuf
 */
public interface Transaction {

  /**
   * Retrieve the transaction id
   * @return ID of the transaction
   */
  public String getId();

  /**
   * Commit (confirm/affirm) the activities performed during this transaction's life.
   * @throws TransactionException If underlying storage throws any exception or if invoked after once the transaction
   *                              has been completed. In case of underlying storage throwing exception an rollback would
   *                              be attempted.
   */
  public void commit() throws TransactionException;

  /**
   * Rollback (reject/negate) the activities performed during this transaction's life.
   * @throws TransactionException If underlying storage throws any exception or if invoked after once the transaction
   *                              has been completed.
   */
  public void rollback() throws TransactionException;

  /**
   * Whether the transaction has once been completed or not
   * @return True if either rollback or commit has been called at least once.
   */
  public boolean isCompleted();

  /**
   * Add a completion listener to be notified when a transaction has completed if life-span
   * @param completionListener The listener to notify
   */
  public void addTransactionCompletionListener(TransactionCompletionListener completionListener);

  /**
   * Remove a listener if it had been added earlier
   * @param completionListener The listener to remove
   */
  public void removeTransactionCompletionListener(TransactionCompletionListener completionListener);
}
