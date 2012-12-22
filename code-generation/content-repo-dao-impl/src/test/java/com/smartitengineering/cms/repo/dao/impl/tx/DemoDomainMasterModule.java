package com.smartitengineering.cms.repo.dao.impl.tx;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

/**
 *
 * @author imyousuf
 */
public class DemoDomainMasterModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(boolean.class).annotatedWith(Names.named("nonIsolatedLookupEnabled")).toInstance(Boolean.TRUE);
    install(new TransactionImplModule());
  }
}
