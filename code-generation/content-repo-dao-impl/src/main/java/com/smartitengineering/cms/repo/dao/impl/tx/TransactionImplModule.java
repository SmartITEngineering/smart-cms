package com.smartitengineering.cms.repo.dao.impl.tx;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.smartitengineering.cms.repo.dao.tx.Transaction;
import com.smartitengineering.cms.repo.dao.tx.TransactionManager;

/**
 *
 * @author imyousuf
 */
public class TransactionImplModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(TransactionInMemoryCache.class).to(TransactionInMemoryCacheImpl.class).in(Singleton.class);
    bind(TransactionService.class).to(TransactionServiceImpl.class).in(Singleton.class);
    bind(TransactionManager.class).to(TransactionManagerImpl.class).in(Singleton.class);
    install(new FactoryModuleBuilder().implement(TransactionStoreKey.class, TransactionStoreKeyImpl.class).implement(
        TransactionStoreValue.class, TransactionStoreValueImpl.class).implement(Transaction.class, TransactionImpl.class).
        build(TransactionFactory.class));
  }
}
