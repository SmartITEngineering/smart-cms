package com.smartitengineering.cms.repo.dao.impl.tx;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.smartitengineering.dao.common.CommonDao;
import com.smartitengineering.dao.common.CommonReadDao;
import com.smartitengineering.dao.common.CommonWriteDao;

/**
 *
 * @author imyousuf
 */
public class DemoDomainMasterModule extends AbstractModule {

  private final Module primaryDaoModule;

  public DemoDomainMasterModule(Module primaryDaoModule) {
    this.primaryDaoModule = primaryDaoModule;
  }

  @Override
  protected void configure() {
    if (primaryDaoModule != null) {
      install(primaryDaoModule);
      final TypeLiteral<CommonDao<DemoDomain, String>> cmnDaoTypeLiteral =
                                                       new TypeLiteral<CommonDao<DemoDomain, String>>() {
      };
      bind(new TypeLiteral<CommonWriteDao<DemoDomain>>() {
      }).to(cmnDaoTypeLiteral);
      bind(new TypeLiteral<CommonReadDao<DemoDomain, String>>() {
      }).to(cmnDaoTypeLiteral);
      bind(cmnDaoTypeLiteral).to(new TypeLiteral<CommonTxDao<DemoDomain>>() {
      });
    }
    bind(boolean.class).annotatedWith(Names.named("nonIsolatedLookupEnabled")).toInstance(Boolean.TRUE);
    install(new TransactionImplModule());
  }
}
