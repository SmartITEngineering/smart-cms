package com.smartitengineering.cms.repo.dao.tx;

/**
 * The listener is an Observer API used to notify the completion of a transaction.
 * @author imyousuf
 */
public interface TransactionCompletionListener {

  /**
   * Notify the observer that the observation point, i.e. transaction completion, has been attained
   * @param event The event representing the completion
   */
  public void transactionComplete(TransactionCompletionEvent event);
}
