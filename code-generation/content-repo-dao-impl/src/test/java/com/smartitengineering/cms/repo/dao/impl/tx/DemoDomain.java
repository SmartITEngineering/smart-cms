package com.smartitengineering.cms.repo.dao.impl.tx;

import com.smartitengineering.cms.repo.dao.impl.AbstractRepositoryDomain;

/**
 *
 * @author imyousuf
 */
public class DemoDomain extends AbstractRepositoryDomain<DemoDomain> {

  private int testValue;

  public int getTestValue() {
    return testValue;
  }

  public void setTestValue(int testValue) {
    this.testValue = testValue;
  }
}
