package com.smartitengineering.cms.repo.dao.impl.tx;

import com.google.inject.AbstractModule;

/**
 *
 * @author imyousuf
 */
public class DemoDomainMasterModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new TransactionImplModule());
  }
}
