package com.smartitengineering.cms.repo.dao.impl.tx;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 *
 * @author imyousuf
 */
public class TransactionImplModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(TransactionInMemoryCache.class).to(TransactionInMemoryCacheImpl.class).in(Singleton.class);
    install(new FactoryModuleBuilder().implement(TransactionStoreKey.class, TransactionStoreKeyImpl.class).implement(
        TransactionStoreValue.class, TransactionStoreValueImpl.class).build(TransactionFactory.class));
  }
}
