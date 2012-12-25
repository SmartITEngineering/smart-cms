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
import com.smartitengineering.dao.common.queryparam.QueryParameter;
import com.smartitengineering.dao.common.queryparam.QueryParameterFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author imyousuf
 */
public class CommonTxDaoTest {

  private Mockery mockery;
  private CommonReadDao<DemoDomain, String> readDao;
  private CommonWriteDao<DemoDomain> writeDao;
  private TransactionService txService;
  private TransactionManager txManager;
  private TransactionInMemoryCache memCache;
  private Injector injector;
  private Services services;

  @Before
  public void setup() {
    mockery = new Mockery();
    readDao = mockery.mock(CommonReadDao.class);
    writeDao = mockery.mock(CommonWriteDao.class);
    txManager = mockery.mock(TransactionManager.class);
    txService = mockery.mock(TransactionService.class);
    memCache = mockery.mock(TransactionInMemoryCache.class);
    injector = getInjector();
    services = injector.getInstance(Services.class);
  }

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

  private Injector getInjector() {
    return Guice.createInjector(new AbstractModule() {

      @Override
      protected void configure() {
        bind(TransactionService.class).toInstance(txService);
        bind(TransactionManager.class).toInstance(txManager);
        bind(TransactionInMemoryCache.class).toInstance(memCache);
        bind(new TypeLiteral<Class<DemoDomain>>() {
        }).toInstance(DemoDomain.class);
        bind(new TypeLiteral<CommonWriteDao<DemoDomain>>() {
        }).annotatedWith(Transactionable.class).toInstance(writeDao);
        bind(new TypeLiteral<CommonReadDao<DemoDomain, String>>() {
        }).annotatedWith(Transactionable.class).toInstance(readDao);
        final TypeLiteral<CommonDao<DemoDomain, String>> cmnDaoTypeLiteral =
                                                         new TypeLiteral<CommonDao<DemoDomain, String>>() {
        };
        bind(new TypeLiteral<CommonWriteDao<DemoDomain>>() {
        }).to(cmnDaoTypeLiteral);
        bind(new TypeLiteral<CommonReadDao<DemoDomain, String>>() {
        }).to(cmnDaoTypeLiteral);
        bind(cmnDaoTypeLiteral).to(new TypeLiteral<CommonTxDao<DemoDomain>>() {
        });
        bind(Services.class);
      }
    });
  }

  @Test
  public void testInitialization() {
    Assert.assertNotNull(this.services);
    Assert.assertNotNull(this.services.cmnDao);
    Assert.assertNotNull(this.services.readDao);
    Assert.assertNotNull(this.services.writeDao);
  }

  @Test
  public void testGetAllDelegation() {
    final Set<DemoDomain> all = new HashSet<DemoDomain>();
    mockery.checking(new Expectations() {

      {
        exactly(1).of(readDao).getAll();
        will(returnValue(all));
      }
    });
    Assert.assertSame(all, this.services.readDao.getAll());
    mockery.assertIsSatisfied();
  }

  private static List<QueryParameter> getTestParams() {
    List<QueryParameter> params = new ArrayList<QueryParameter>();
    params.add(QueryParameterFactory.getEqualPropertyParam("id", "1"));
    params.add(QueryParameterFactory.getEqualPropertyParam("id", "2"));
    params.add(QueryParameterFactory.getEqualPropertyParam("id", "3"));
    return params;
  }
  private static final List<QueryParameter> PARAM_LIST = getTestParams();
  private static final QueryParameter[] PARAM_ARRAY = PARAM_LIST.toArray(new QueryParameter[PARAM_LIST.size()]);

  @Test
  public void testGetListDelegation() {
    final List<DemoDomain> result = new ArrayList<DemoDomain>();
    mockery.checking(new Expectations() {

      {
        exactly(1).of(readDao).getList(with(equal(PARAM_LIST)));
        will(returnValue(result));
      }
    });
    Assert.assertSame(result, this.services.readDao.getList(PARAM_LIST));
    mockery.assertIsSatisfied();
  }

  @Test
  public void testGetOtherDelegation() {
    mockery.checking(new Expectations() {

      {
        exactly(1).of(readDao).getOther(with(equal(PARAM_LIST)));
        will(returnValue(PARAM_ARRAY));
      }
    });
    Assert.assertSame(PARAM_ARRAY, this.services.readDao.<QueryParameter[]>getOther(PARAM_LIST));
    mockery.assertIsSatisfied();
  }

  @Test
  public void testGetOtherListDelegation() {
    mockery.checking(new Expectations() {

      {
        exactly(1).of(readDao).getOtherList(with(equal(PARAM_LIST)));
        will(returnValue(Arrays.<QueryParameter[]>asList(PARAM_ARRAY, PARAM_ARRAY)));
      }
    });
    Assert.assertEquals(Arrays.<QueryParameter[]>asList(PARAM_ARRAY, PARAM_ARRAY),
                        this.services.readDao.<QueryParameter[]>getOtherList(
        PARAM_LIST));
    mockery.assertIsSatisfied();
  }

  @Test
  public void testGetListVarArgsDelegation() {
    final List<DemoDomain> result = new ArrayList<DemoDomain>();
    mockery.checking(new Expectations() {

      {
        exactly(1).of(readDao).getList(with(equal(PARAM_ARRAY)));
        will(returnValue(result));
      }
    });
    Assert.assertSame(result, this.services.readDao.getList(PARAM_ARRAY));
    mockery.assertIsSatisfied();
  }

  @Test
  public void testGetOtherVarArgsDelegation() {
    mockery.checking(new Expectations() {

      {
        exactly(1).of(readDao).getOther(with(equal(PARAM_ARRAY)));
        will(returnValue(PARAM_LIST));
      }
    });
    Assert.assertSame(PARAM_LIST, this.services.readDao.<List<QueryParameter>>getOther(PARAM_ARRAY));
    mockery.assertIsSatisfied();
  }

  @Test
  public void testGetOtherListVarArgsListDelegation() {
    mockery.checking(new Expectations() {

      {
        exactly(1).of(readDao).getOtherList(with(equal(PARAM_ARRAY)));
        will(returnValue(Arrays.<List<QueryParameter>>asList(PARAM_LIST, PARAM_LIST)));
      }
    });
    Assert.assertEquals(Arrays.<List<QueryParameter>>asList(PARAM_LIST, PARAM_LIST),
                        this.services.readDao.<List<QueryParameter>>getOtherList(PARAM_ARRAY));
    mockery.assertIsSatisfied();
  }

  private static List<DemoDomain> getWriteTestData() {
    final DemoDomain demoDomain1 = new DemoDomain();
    final DemoDomain demoDomain2 = new DemoDomain();
    final DemoDomain demoDomain3 = new DemoDomain();
    demoDomain1.setId("1");
    demoDomain2.setId("2");
    demoDomain3.setId("3");
    demoDomain1.setTestValue(3);
    demoDomain2.setTestValue(2);
    demoDomain3.setTestValue(1);
    return Arrays.asList(demoDomain1, demoDomain2, demoDomain3);
  }
  private static final List<DemoDomain> TEST_DOMAINS = getWriteTestData();

  private TransactionElement<DemoDomain> getElement(DemoDomain domain) {
    TransactionElement<DemoDomain> dto = new TransactionElement<DemoDomain>();
    dto.setDto(domain);
    dto.setObjectType(DemoDomain.class);
    dto.setReadDao(this.readDao);
    dto.setTxId("1");
    dto.setWriteDao(this.writeDao);
    return dto;
  }

  @Test
  public void testSaveWithNull() {
    this.services.writeDao.save((DemoDomain[]) null);
    mockery.assertIsSatisfied();
  }

  @Test
  public void testSaveWithoutTransaction() {
    final DemoDomain dto = getWriteTestData().get(0);
    mockery.checking(new Expectations() {

      {
        exactly(1).of(txManager).getCurrentTransaction();
        will(returnValue(null));
        exactly(1).of(writeDao).save(with(new BaseMatcher<DemoDomain[]>() {

          public boolean matches(Object item) {
            DemoDomain[] args = (DemoDomain[]) item;
            DemoDomain[] expecteds = new DemoDomain[]{dto};
            return Arrays.equals(expecteds, args);
          }

          public void describeTo(Description description) {
          }
        }));
      }
    });
    this.services.writeDao.save(dto);
    mockery.assertIsSatisfied();
  }

  @Test
  public void testSave() {
    final DemoDomain dto = getWriteTestData().get(0);
    mockery.checking(new Expectations() {

      {
        exactly(2).of(txManager).getCurrentTransaction();
        Transaction tx = mockery.mock(Transaction.class);
        will(returnValue(tx));
        exactly(1).of(tx).getId();
        will(returnValue("1"));
        exactly(1).of(txService).save(with(equal(getElement(dto))));
      }
    });
    this.services.writeDao.save(dto);
    mockery.assertIsSatisfied();
  }

  @Test
  public void testSaves() {
    final List<DemoDomain> dtos = getWriteTestData();
    mockery.checking(new Expectations() {

      {
        exactly(dtos.size() + 1).of(txManager).getCurrentTransaction();
        Transaction tx = mockery.mock(Transaction.class);
        will(returnValue(tx));
        exactly(dtos.size()).of(tx).getId();
        will(returnValue("1"));
        for (DemoDomain dto : dtos) {
          exactly(1).of(txService).save(with(equal(getElement(dto))));
        }
      }
    });
    this.services.writeDao.save(dtos.toArray(new DemoDomain[dtos.size()]));
    mockery.assertIsSatisfied();
  }

  @Test
  public void testUpdateWithNull() {
    this.services.writeDao.update((DemoDomain[]) null);
    mockery.assertIsSatisfied();
  }

  @Test
  public void testUpdateWithoutTransaction() {
    final DemoDomain dto = getWriteTestData().get(0);
    mockery.checking(new Expectations() {

      {
        exactly(1).of(txManager).getCurrentTransaction();
        will(returnValue(null));
        exactly(1).of(writeDao).update(with(new BaseMatcher<DemoDomain[]>() {

          public boolean matches(Object item) {
            DemoDomain[] args = (DemoDomain[]) item;
            DemoDomain[] expecteds = new DemoDomain[]{dto};
            return Arrays.equals(expecteds, args);
          }

          public void describeTo(Description description) {
          }
        }));
      }
    });
    this.services.writeDao.update(dto);
    mockery.assertIsSatisfied();
  }

  @Test
  public void testUpdate() {
    final DemoDomain dto = getWriteTestData().get(0);
    mockery.checking(new Expectations() {

      {
        exactly(2).of(txManager).getCurrentTransaction();
        Transaction tx = mockery.mock(Transaction.class);
        will(returnValue(tx));
        exactly(1).of(tx).getId();
        will(returnValue("1"));
        exactly(1).of(txService).update(with(equal(getElement(dto))));
      }
    });
    this.services.writeDao.update(dto);
    mockery.assertIsSatisfied();
  }

  @Test
  public void testUpdates() {
    final List<DemoDomain> dtos = getWriteTestData();
    mockery.checking(new Expectations() {

      {
        exactly(dtos.size() + 1).of(txManager).getCurrentTransaction();
        Transaction tx = mockery.mock(Transaction.class);
        will(returnValue(tx));
        exactly(dtos.size()).of(tx).getId();
        will(returnValue("1"));
        for (DemoDomain dto : dtos) {
          exactly(1).of(txService).update(with(equal(getElement(dto))));
        }
      }
    });
    this.services.writeDao.update(dtos.toArray(new DemoDomain[dtos.size()]));
    mockery.assertIsSatisfied();
  }

  @Test
  public void testDeleteWithNull() {
    this.services.writeDao.delete((DemoDomain[]) null);
    mockery.assertIsSatisfied();
  }

  @Test
  public void testDeleteWithoutTransaction() {
    final DemoDomain dto = getWriteTestData().get(0);
    mockery.checking(new Expectations() {

      {
        exactly(1).of(txManager).getCurrentTransaction();
        will(returnValue(null));
        exactly(1).of(writeDao).delete(with(new BaseMatcher<DemoDomain[]>() {

          public boolean matches(Object item) {
            DemoDomain[] args = (DemoDomain[]) item;
            DemoDomain[] expecteds = new DemoDomain[]{dto};
            return Arrays.equals(expecteds, args);
          }

          public void describeTo(Description description) {
          }
        }));
      }
    });
    this.services.writeDao.delete(dto);
    mockery.assertIsSatisfied();
  }

  @Test
  public void testDelete() {
    final DemoDomain dto = getWriteTestData().get(0);
    mockery.checking(new Expectations() {

      {
        exactly(2).of(txManager).getCurrentTransaction();
        Transaction tx = mockery.mock(Transaction.class);
        will(returnValue(tx));
        exactly(1).of(tx).getId();
        will(returnValue("1"));
        exactly(1).of(txService).delete(with(equal(getElement(dto))));
      }
    });
    this.services.writeDao.delete(dto);
    mockery.assertIsSatisfied();
  }

  @Test
  public void testDeletes() {
    final List<DemoDomain> dtos = getWriteTestData();
    mockery.checking(new Expectations() {

      {
        exactly(dtos.size() + 1).of(txManager).getCurrentTransaction();
        Transaction tx = mockery.mock(Transaction.class);
        will(returnValue(tx));
        exactly(dtos.size()).of(tx).getId();
        will(returnValue("1"));
        for (DemoDomain dto : dtos) {
          exactly(1).of(txService).delete(with(equal(getElement(dto))));
        }
      }
    });
    this.services.writeDao.delete(dtos.toArray(new DemoDomain[dtos.size()]));
    mockery.assertIsSatisfied();
  }

  @Test
  public void testGetSingleWithoutTransactionDelegation() {
    mockery.checking(new Expectations() {

      {
        exactly(2).of(txManager).getCurrentTransaction();
        will(returnValue(null));
        exactly(2).of(readDao).getSingle(with(equal(PARAM_LIST)));
        will(returnValue(TEST_DOMAINS.get(0)));
      }
    });
    Assert.assertSame(TEST_DOMAINS.get(0), this.services.readDao.getSingle(PARAM_LIST));
    Assert.assertSame(TEST_DOMAINS.get(0), this.services.readDao.getSingle(PARAM_ARRAY));
    mockery.assertIsSatisfied();
  }

  @Test
  public void testGetSingleWithTransactionButNullReturnValue() {
    mockery.checking(new Expectations() {

      {
        exactly(2).of(txManager).getCurrentTransaction();
        Transaction tx = mockery.mock(Transaction.class);
        will(returnValue(tx));
        exactly(2).of(readDao).getSingle(with(equal(PARAM_LIST)));
        will(returnValue(null));
      }
    });
    Assert.assertNull(this.services.readDao.getSingle(PARAM_LIST));
    Assert.assertNull(this.services.readDao.getSingle(PARAM_ARRAY));
    mockery.assertIsSatisfied();
  }

  @Test
  public void testGetByIdWithTransactionIsolationFromCache() {
    mockery.checking(new Expectations() {

      {
        exactly(2).of(txManager).getCurrentTransaction();
        Transaction tx = mockery.mock(Transaction.class);
        will(returnValue(tx));
        exactly(1).of(tx).isIsolatedTransaction();
        will(returnValue(true));
        exactly(1).of(tx).getId();
        will(returnValue("1"));
        exactly(1).of(memCache).getValueForIsolatedTransaction("1", DemoDomain.class.getName(), TEST_DOMAINS.get(0).
            getId());
        TransactionStoreKey key = mockery.mock(TransactionStoreKey.class);
        TransactionStoreValue val = mockery.mock(TransactionStoreValue.class);
        Pair<TransactionStoreKey, TransactionStoreValue> pair = new Pair<TransactionStoreKey, TransactionStoreValue>(
            key, val);
        will(returnValue(pair));
        exactly(1).of(val).getCurrentState();
        will(returnValue(TEST_DOMAINS.get(0)));
      }
    });
    Assert.assertSame(TEST_DOMAINS.get(0), this.services.readDao.getById("1"));
    mockery.assertIsSatisfied();
  }

  @Test
  public void testGetByIdWithTransactionIsolationNotFromCache() {
    mockery.checking(new Expectations() {

      {
        exactly(2).of(txManager).getCurrentTransaction();
        Transaction tx = mockery.mock(Transaction.class);
        will(returnValue(tx));
        exactly(1).of(tx).isIsolatedTransaction();
        will(returnValue(true));
        exactly(1).of(tx).getId();
        will(returnValue("1"));
        final String id = TEST_DOMAINS.get(0).getId();
        exactly(1).of(memCache).getValueForIsolatedTransaction("1", DemoDomain.class.getName(), id);
        will(returnValue(null));
        exactly(1).of(readDao).getById(id);
        will(returnValue(TEST_DOMAINS.get(0)));
      }
    });
    Assert.assertSame(TEST_DOMAINS.get(0), this.services.readDao.getById("1"));
    mockery.assertIsSatisfied();
  }

  @Test
  public void testGetByIdWithTransactionNonIsolationFromCache() {
    mockery.checking(new Expectations() {

      {
        exactly(2).of(txManager).getCurrentTransaction();
        Transaction tx = mockery.mock(Transaction.class);
        will(returnValue(tx));
        exactly(1).of(tx).isIsolatedTransaction();
        will(returnValue(false));
        final String id = TEST_DOMAINS.get(0).getId();
        exactly(1).of(memCache).getValueForNonIsolatedTransaction(DemoDomain.class.getName(), TEST_DOMAINS.get(0).
            getId());
        TransactionStoreKey key = mockery.mock(TransactionStoreKey.class);
        TransactionStoreValue val = mockery.mock(TransactionStoreValue.class);
        Pair<TransactionStoreKey, TransactionStoreValue> pair = new Pair<TransactionStoreKey, TransactionStoreValue>(
            key, val);
        will(returnValue(pair));
        exactly(1).of(val).getCurrentState();
        will(returnValue(TEST_DOMAINS.get(0)));
      }
    });
    Assert.assertSame(TEST_DOMAINS.get(0), this.services.readDao.getById("1"));
    mockery.assertIsSatisfied();
  }

  @Test
  public void testGetByIdWithTransactionNonIsolationNotFromCache() {
    mockery.checking(new Expectations() {

      {
        exactly(2).of(txManager).getCurrentTransaction();
        Transaction tx = mockery.mock(Transaction.class);
        will(returnValue(tx));
        exactly(1).of(tx).isIsolatedTransaction();
        will(returnValue(false));
        final String id = TEST_DOMAINS.get(0).getId();
        exactly(1).of(memCache).getValueForNonIsolatedTransaction(DemoDomain.class.getName(), id);
        will(returnValue(null));
        exactly(1).of(readDao).getById(id);
        will(returnValue(TEST_DOMAINS.get(0)));
      }
    });
    Assert.assertSame(TEST_DOMAINS.get(0), this.services.readDao.getById("1"));
    mockery.assertIsSatisfied();
  }

  @Test
  public void testGetSingleWithTransactionIsolation() {
    mockery.checking(new Expectations() {

      {
        exactly(6).of(txManager).getCurrentTransaction();
        Transaction tx = mockery.mock(Transaction.class);
        will(returnValue(tx));
        exactly(2).of(tx).isIsolatedTransaction();
        will(returnValue(true));
        exactly(2).of(tx).getId();
        will(returnValue("1"));
        exactly(2).of(memCache).getValueForIsolatedTransaction("1", DemoDomain.class.getName(), TEST_DOMAINS.get(0).
            getId());
        TransactionStoreKey key = mockery.mock(TransactionStoreKey.class);
        TransactionStoreValue val = mockery.mock(TransactionStoreValue.class);
        Pair<TransactionStoreKey, TransactionStoreValue> pair = new Pair<TransactionStoreKey, TransactionStoreValue>(
            key, val);
        will(returnValue(pair));
        exactly(2).of(val).getCurrentState();
        will(returnValue(TEST_DOMAINS.get(0)));
        exactly(2).of(readDao).getSingle(with(equal(PARAM_LIST)));
        will(returnValue(TEST_DOMAINS.get(0)));
      }
    });
    Assert.assertSame(TEST_DOMAINS.get(0), this.services.readDao.getSingle(PARAM_LIST));
    Assert.assertSame(TEST_DOMAINS.get(0), this.services.readDao.getSingle(PARAM_ARRAY));
    mockery.assertIsSatisfied();
  }

  @Test
  public void testGetByIdsWithTransactionIsolation() {
    mockery.checking(new Expectations() {

      {
        exactly(6).of(txManager).getCurrentTransaction();
        Transaction tx = mockery.mock(Transaction.class);
        will(returnValue(tx));
        exactly(3).of(tx).isIsolatedTransaction();
        will(returnValue(false));
        for (DemoDomain domain : TEST_DOMAINS) {
          exactly(1).of(memCache).getValueForNonIsolatedTransaction(DemoDomain.class.getName(), domain.getId());
          TransactionStoreKey key = mockery.mock(TransactionStoreKey.class, "tsk-" + domain.getId());
          TransactionStoreValue val = mockery.mock(TransactionStoreValue.class, "tsv-" + domain.getId());
          Pair<TransactionStoreKey, TransactionStoreValue> pair = new Pair<TransactionStoreKey, TransactionStoreValue>(
              key, val);
          will(returnValue(pair));
          exactly(1).of(val).getCurrentState();
          will(returnValue(domain));
        }
      }
    });
    Set<DemoDomain> domains = this.services.readDao.getByIds(Arrays.asList("1", "2", "3"));
    Assert.assertEquals(TEST_DOMAINS, new ArrayList(domains));
    mockery.assertIsSatisfied();
  }
}
