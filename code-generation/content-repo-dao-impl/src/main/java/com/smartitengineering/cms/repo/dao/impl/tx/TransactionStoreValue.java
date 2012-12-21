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
}
