package com.smartitengineering.cms.repo.dao.impl.tx;

import com.smartitengineering.cms.repo.dao.impl.AbstractRepositoryDomain;

/**
 *
 * @author imyousuf
 */
class TransactionStoreValueImpl implements TransactionStoreValue {

  private int opSequence;
  private OpState opState;
  private AbstractRepositoryDomain originalState;
  private AbstractRepositoryDomain currentState;

  public AbstractRepositoryDomain getCurrentState() {
    return currentState;
  }

  public void setCurrentState(AbstractRepositoryDomain currentState) {
    this.currentState = currentState;
  }

  public int getOpSequence() {
    return opSequence;
  }

  public void setOpSequence(int opSequence) {
    this.opSequence = opSequence;
  }

  public OpState getOpState() {
    return opState;
  }

  public void setOpState(OpState opState) {
    this.opState = opState;
  }

  public AbstractRepositoryDomain getOriginalState() {
    return originalState;
  }

  public void setOriginalState(AbstractRepositoryDomain originalState) {
    this.originalState = originalState;
  }
}
