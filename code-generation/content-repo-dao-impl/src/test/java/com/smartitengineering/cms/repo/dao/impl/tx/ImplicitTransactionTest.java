package com.smartitengineering.cms.repo.dao.impl.tx;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.smartitengineering.cms.repo.dao.tx.TransactionException;
import com.smartitengineering.cms.repo.dao.tx.TransactionManager;
import com.smartitengineering.cms.repo.dao.tx.Transactional;
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
public class ImplicitTransactionTest {

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

    @Transactional
    public void testCommit(DemoDomain d1) {
      writeDao.save(d1);
    }

    @Transactional
    public void testRollback(DemoDomain d1) {
      writeDao.save(d1);
      throw new NullPointerException();
    }

    @Transactional
    public void testHardRollback(DemoDomain d1, DemoDomain d2, DemoDomain d3) {
      System.out.println("----------------------START---------------------------");
      writeDao.save(d1);
      System.out.println("1");
      writeDao.delete(d2);
      System.out.println("2");
      writeDao.update(d3);
      System.out.println("3");
      System.out.println("----------------------DONE---------------------------");
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
  public void testImplicitCommit() {
    final DemoDomain d1 = new DemoDomain();
    mockery.checking(new Expectations() {

      {
        exactly(1).of(writeDao).save(d1);
      }
    });
    services.testCommit(d1);
    mockery.assertIsSatisfied();
  }

  @Test(expected = NullPointerException.class)
  public void testImplicitRollback() {
    final DemoDomain d1 = new DemoDomain();
    services.testRollback(d1);
  }

  @Test(expected = TransactionException.class)
  public void testImplicitHardRollback() {
    final DemoDomain d1 = new DemoDomain();
    d1.setId("1");
    final DemoDomain d2 = new DemoDomain();
    d2.setId("2");
    final DemoDomain d3 = new DemoDomain();
    d3.setId("3");
    mockery.checking(new Expectations() {

      {
        exactly(1).of(readDao).getById("2");
        will(returnValue(d2));
        exactly(1).of(readDao).getById("3");
        will(returnValue(d3));
        exactly(1).of(writeDao).save(d1);
        exactly(1).of(writeDao).delete(d2);
        exactly(1).of(writeDao).update(d3);
        will(throwException(new TransactionException()));
        exactly(1).of(writeDao).save(d2);
        exactly(1).of(writeDao).delete(d1);
      }
    });
    try {
      services.testHardRollback(d1, d2, d3);
    }
    catch(RuntimeException ex) {
      throw ex;
    }
    finally {
      mockery.assertIsSatisfied();
    }
  }
}
