package com.smartitengineering.cms.repo.dao.tx;

/**
 * An event representing transaction completion
 * @author imyousuf
 */
public class TransactionCompletionEvent {

  /**
   * Represents the completion event of a transaction
   */
  public static enum CompletionEvent {

    COMMIT, ROLLBACK
  }
  private final CompletionEvent completionEvent;
  private final boolean completedSuccessfully;
  private final Transaction transaction;

  /**
   * Initializes a read-only event with the completion event, success status of the operation and the transaction
   * completed
   * @param completionEvent The event of the transaction - i.e. commit or rollback
   * @param completedSuccessfully Whether the event was completed without any exception
   * @param tx The transaction
   */
  public TransactionCompletionEvent(CompletionEvent completionEvent, boolean completedSuccessfully, Transaction tx) {
    this.completionEvent = completionEvent;
    this.completedSuccessfully = completedSuccessfully;
    this.transaction = tx;
  }

  /**
   * Retrieve the transaction completed
   * @return The transaction
   */
  public Transaction getTransaction() {
    return transaction;
  }

  /**
   * Retrieve whether the transaction was completed successfully or not
   * @return True if completed successfully or false if any exception was encountered.
   */
  public boolean isCompletedSuccessfully() {
    return completedSuccessfully;
  }

  /**
   * The event that completed the transaction
   * @return The completion event
   */
  public CompletionEvent getCompletionEvent() {
    return completionEvent;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final TransactionCompletionEvent other = (TransactionCompletionEvent) obj;
    if (this.completionEvent != other.completionEvent) {
      return false;
    }
    if (this.completedSuccessfully != other.completedSuccessfully) {
      return false;
    }
    if (this.transaction != other.transaction &&
        (this.transaction == null || !this.transaction.equals(other.transaction))) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 29 * hash + (this.completionEvent != null ? this.completionEvent.hashCode() : 0);
    hash = 29 * hash + (this.completedSuccessfully ? 1 : 0);
    hash = 29 * hash + (this.transaction != null ? this.transaction.hashCode() : 0);
    return hash;
  }
}
