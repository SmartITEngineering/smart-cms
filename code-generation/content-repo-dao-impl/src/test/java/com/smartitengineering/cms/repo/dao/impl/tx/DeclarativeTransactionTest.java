package com.smartitengineering.cms.repo.dao.impl.tx;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.smartitengineering.cms.repo.dao.tx.Transaction;
import com.smartitengineering.cms.repo.dao.tx.TransactionManager;
import com.smartitengineering.dao.common.CommonDao;
import com.smartitengineering.dao.common.CommonReadDao;
import com.smartitengineering.dao.common.CommonWriteDao;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author imyousuf
 */
public class DeclarativeTransactionTest {

  private static class Services {

    private final CommonDao<DemoDomain, String> cmnDao;
    private final CommonReadDao<DemoDomain, String> readDao;
    private final CommonWriteDao<DemoDomain> writeDao;

    @Inject
    public Services(CommonDao<DemoDomain, String> cmnDao,
                    CommonReadDao<DemoDomain, String> readDao,
                    CommonWriteDao<DemoDomain> writeDao) {
      this.cmnDao = cmnDao;
      this.readDao = readDao;
      this.writeDao = writeDao;
    }
  }
  private Mockery mockery;
  private CommonReadDao<DemoDomain, String> readDao;
  private CommonWriteDao<DemoDomain> writeDao;
  private Injector injector;
  private Services services;
  private TransactionManager manager;

  @Before
  public void setup() {
    mockery = new Mockery();
    readDao = mockery.mock(CommonReadDao.class);
    writeDao = mockery.mock(CommonWriteDao.class);
    injector = Guice.createInjector(new DemoDomainMasterModule(new AbstractModule() {

      @Override
      protected void configure() {
        bind(new TypeLiteral<Class<DemoDomain>>() {
        }).toInstance(DemoDomain.class);
        bind(new TypeLiteral<CommonWriteDao<DemoDomain>>() {
        }).annotatedWith(Transactionable.class).toInstance(writeDao);
        bind(new TypeLiteral<CommonReadDao<DemoDomain, String>>() {
        }).annotatedWith(Transactionable.class).toInstance(readDao);
        bind(Services.class);
      }
    }));
    services = injector.getInstance(Services.class);
    manager = injector.getInstance(TransactionManager.class);
  }

  @Test
  public void testDecalarativeCommit() {
    Transaction tx = manager.beginTransaction();
    try {
      final DemoDomain d1 = new DemoDomain();
      services.writeDao.save(d1);
      mockery.checking(new Expectations() {

        {
          exactly(1).of(writeDao).save(d1);
        }
      });
      tx.commit();
      mockery.assertIsSatisfied();
    }
    catch (Exception ex) {
      tx.rollback();
    }
  }

  @Test
  public void testDecalarativeRollback() {
    Transaction tx = manager.beginTransaction();
    try {
      final DemoDomain d1 = new DemoDomain();
      services.writeDao.save(d1);
      throw new NullPointerException();
    }
    catch (Exception ex) {
      tx.rollback();
    }
  }
}
