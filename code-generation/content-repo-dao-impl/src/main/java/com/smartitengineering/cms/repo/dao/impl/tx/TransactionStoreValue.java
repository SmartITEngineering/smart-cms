package com.smartitengineering.cms.repo.dao.impl.tx;

import com.smartitengineering.cms.repo.dao.impl.AbstractRepositoryDomain;
import java.io.Serializable;

/**
 *
 * @author imyousuf
 */
public interface TransactionStoreValue extends Serializable {

  public int getOpSequence();

  public OpState getOpState();

  public <T extends AbstractRepositoryDomain> T getOriginalState();

  public <T extends AbstractRepositoryDomain> T getCurrentState();

  public void setOpSequence(int opSeq);

  public void setOpState(OpState opState);

  public <T extends AbstractRepositoryDomain> void setOriginalState(T orgState);

  public <T extends AbstractRepositoryDomain> void setCurrentState(T currentState);
}
