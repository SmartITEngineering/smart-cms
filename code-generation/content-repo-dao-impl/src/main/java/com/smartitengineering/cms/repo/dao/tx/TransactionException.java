package com.smartitengineering.cms.repo.dao.tx;

/**
 * A runtime exception that would represent any unexpected exception at the time of commit or rollback. It can also be
 * thrown if the transaction has been closed and client API has re-requested commit or rollback.
 * @author imyousuf
 */
public class TransactionException extends RuntimeException {

  public TransactionException(Throwable cause) {
    super(cause);
  }

  public TransactionException(String message, Throwable cause) {
    super(message, cause);
  }

  public TransactionException(String message) {
    super(message);
  }

  public TransactionException() {
  }
}
