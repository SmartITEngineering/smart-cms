package com.smartitengineering.cms.repo.dao.impl.tx;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.smartitengineering.cms.repo.dao.impl.AbstractRepositoryDomain;
import com.smartitengineering.cms.repo.dao.tx.TransactionException;
import com.smartitengineering.dao.common.CommonReadDao;
import com.smartitengineering.dao.common.CommonWriteDao;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;
import junit.framework.Assert;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class TransactionServiceImplTest {

  private Injector injector;
  private Mockery mockery;
  private TransactionInMemoryCache mockMemCache;
  private TransactionFactory mockFactory;
  private static final Logger LOGGER = LoggerFactory.getLogger(TransactionServiceImplTest.class);

  @Before
  public void setup() {
    mockery = new Mockery();
    mockFactory = mockery.mock(TransactionFactory.class);
    mockMemCache = mockery.mock(TransactionInMemoryCache.class);
    injector = Guice.createInjector(new AbstractModule() {

      @Override
      protected void configure() {
        bind(TransactionFactory.class).toInstance(mockFactory);
        bind(TransactionInMemoryCache.class).toInstance(mockMemCache);
        bind(TransactionService.class).to(TransactionServiceImpl.class);
      }
    });
  }

  @Test
  public void testAtomicLongIncrementForMaxValue() {
    Assert.assertEquals(Long.MIN_VALUE, new AtomicLong(Long.MAX_VALUE).incrementAndGet());
  }

  @Test
  public void testConcurrentIdGeneration() {
    final TransactionService service = injector.getInstance(TransactionService.class);
    final Set<String> txIds = new ConcurrentSkipListSet<String>();
    final int threadCount = 10;
    final int loopCount = 100000;
    List<Thread> threads = new ArrayList<Thread>();
    long start = System.currentTimeMillis();
    for (int i = 0; i < threadCount; ++i) {
      Thread thread = new Thread(new Runnable() {

        public void run() {
          for (int j = 0; j < loopCount; ++j) {
            txIds.add(service.getNextTransactionId());
          }
        }
      });
      thread.start();
      threads.add(thread);
    }
    for (Thread thread : threads) {
      try {
        thread.join();
      }
      catch (Exception ex) {
        LOGGER.error("Error waiting for a thread", ex);
      }
    }
    long end = System.currentTimeMillis();
    Assert.assertEquals((threadCount * loopCount), txIds.size());
    System.out.println("Time taken to generate " + (threadCount * loopCount) + " ids is " + (end - start));
  }

  @Test(expected = IllegalStateException.class)
  public void testTransitionFromSaveToSave() {
    final TransactionService service = injector.getInstance(TransactionService.class);
    final TransactionServiceImpl serviceImpl = (TransactionServiceImpl) service;
    serviceImpl.getActualStateAfterTransition(OpState.SAVE, OpState.SAVE);
  }

  @Test
  public void testTransitionFromSaveToUpdate() {
    final TransactionService service = injector.getInstance(TransactionService.class);
    final TransactionServiceImpl serviceImpl = (TransactionServiceImpl) service;
    Assert.assertEquals(OpState.SAVE, serviceImpl.getActualStateAfterTransition(OpState.SAVE, OpState.UPDATE));
  }

  @Test
  public void testTransitionFromSaveToDelete() {
    final TransactionService service = injector.getInstance(TransactionService.class);
    final TransactionServiceImpl serviceImpl = (TransactionServiceImpl) service;
    Assert.assertNull(serviceImpl.getActualStateAfterTransition(OpState.SAVE, OpState.DELETE));
  }

  @Test(expected = IllegalStateException.class)
  public void testTransitionFromSaveToNull() {
    final TransactionService service = injector.getInstance(TransactionService.class);
    final TransactionServiceImpl serviceImpl = (TransactionServiceImpl) service;
    serviceImpl.getActualStateAfterTransition(OpState.SAVE, null);
  }

  @Test(expected = IllegalStateException.class)
  public void testTransitionFromNullToUpdate() {
    final TransactionService service = injector.getInstance(TransactionService.class);
    final TransactionServiceImpl serviceImpl = (TransactionServiceImpl) service;
    serviceImpl.getActualStateAfterTransition(null, OpState.UPDATE);
  }

  @Test
  public void testTransitionFromNullToSave() {
    final TransactionService service = injector.getInstance(TransactionService.class);
    final TransactionServiceImpl serviceImpl = (TransactionServiceImpl) service;
    Assert.assertEquals(OpState.SAVE, serviceImpl.getActualStateAfterTransition(null, OpState.SAVE));
  }

  @Test
  public void testTransitionFromNullToNull() {
    final TransactionService service = injector.getInstance(TransactionService.class);
    final TransactionServiceImpl serviceImpl = (TransactionServiceImpl) service;
    Assert.assertNull(serviceImpl.getActualStateAfterTransition(null, null));
  }

  @Test(expected = IllegalStateException.class)
  public void testTransitionFromNullToDelete() {
    final TransactionService service = injector.getInstance(TransactionService.class);
    final TransactionServiceImpl serviceImpl = (TransactionServiceImpl) service;
    serviceImpl.getActualStateAfterTransition(null, OpState.DELETE);
  }

  @Test(expected = IllegalStateException.class)
  public void testTransitionFromUpdateToSave() {
    final TransactionService service = injector.getInstance(TransactionService.class);
    final TransactionServiceImpl serviceImpl = (TransactionServiceImpl) service;
    serviceImpl.getActualStateAfterTransition(OpState.UPDATE, OpState.SAVE);
  }

  @Test
  public void testTransitionFromUpdateToUpdate() {
    final TransactionService service = injector.getInstance(TransactionService.class);
    final TransactionServiceImpl serviceImpl = (TransactionServiceImpl) service;
    Assert.assertEquals(OpState.UPDATE, serviceImpl.getActualStateAfterTransition(OpState.UPDATE, OpState.UPDATE));
  }

  @Test
  public void testTransitionFromUpdateToDelete() {
    final TransactionService service = injector.getInstance(TransactionService.class);
    final TransactionServiceImpl serviceImpl = (TransactionServiceImpl) service;
    Assert.assertEquals(OpState.DELETE, serviceImpl.getActualStateAfterTransition(OpState.UPDATE, OpState.DELETE));
  }

  @Test(expected = IllegalStateException.class)
  public void testTransitionFromUpdateToNull() {
    final TransactionService service = injector.getInstance(TransactionService.class);
    final TransactionServiceImpl serviceImpl = (TransactionServiceImpl) service;
    serviceImpl.getActualStateAfterTransition(OpState.UPDATE, null);
  }

  public void testTransitionFromDeleteToSave() {
    final TransactionService service = injector.getInstance(TransactionService.class);
    final TransactionServiceImpl serviceImpl = (TransactionServiceImpl) service;
    Assert.assertEquals(OpState.SAVE, serviceImpl.getActualStateAfterTransition(OpState.DELETE, OpState.SAVE));
  }

  @Test(expected = IllegalStateException.class)
  public void testTransitionFromDeleteToUpdate() {
    final TransactionService service = injector.getInstance(TransactionService.class);
    final TransactionServiceImpl serviceImpl = (TransactionServiceImpl) service;
    serviceImpl.getActualStateAfterTransition(OpState.DELETE, OpState.UPDATE);
  }

  @Test(expected = IllegalStateException.class)
  public void testTransitionFromDeleteToDelete() {
    final TransactionService service = injector.getInstance(TransactionService.class);
    final TransactionServiceImpl serviceImpl = (TransactionServiceImpl) service;
    serviceImpl.getActualStateAfterTransition(OpState.DELETE, OpState.DELETE);
  }

  @Test(expected = IllegalStateException.class)
  public void testTransitionFromDeleteToNull() {
    final TransactionService service = injector.getInstance(TransactionService.class);
    final TransactionServiceImpl serviceImpl = (TransactionServiceImpl) service;
    serviceImpl.getActualStateAfterTransition(OpState.DELETE, null);
  }

  private TransactionElement<DemoDomain> getTransactionElement(CommonReadDao<DemoDomain, String> readDao,
                                                               CommonWriteDao<DemoDomain> writeDao) {
    TransactionElement<DemoDomain> demoElement = new TransactionElement<DemoDomain>();
    DemoDomain dto = new DemoDomain();
    dto.setId("1");
    demoElement.setDto(dto);
    demoElement.setObjectType(DemoDomain.class);
    demoElement.setTxId("1");
    demoElement.setReadDao(readDao);
    demoElement.setWriteDao(writeDao);
    return demoElement;
  }

  @Test
  public void testPopulateTransactionCacheWithNonExistingValueForSave() {
    final TransactionService service = injector.getInstance(TransactionService.class);
    final TransactionServiceImpl serviceImpl = (TransactionServiceImpl) service;
    CommonReadDao<DemoDomain, String> readDao = mockery.mock(CommonReadDao.class);
    CommonWriteDao<DemoDomain> writeDao = mockery.mock(CommonWriteDao.class);
    final TransactionElement<DemoDomain> element = getTransactionElement(readDao, writeDao);
    mockery.checking(new Expectations() {

      {
        Sequence seq = mockery.sequence("serial");
        exactly(1).of(mockFactory).createTransactionStoreKey();
        TransactionStoreKey key = mockery.mock(TransactionStoreKey.class);
        will(returnValue(key));
        inSequence(seq);
        exactly(1).of(key).setOpTimestamp(with(any(Long.class)));
        inSequence(seq);
        exactly(1).of(key).setTransactionId("1");
        inSequence(seq);
        exactly(1).of(key).setObjectType(DemoDomain.class);
        inSequence(seq);
        exactly(1).of(key).setObjectId("1");
        inSequence(seq);
        exactly(1).of(mockMemCache).getValueForIsolatedTransaction(with(same(key)));
        will(returnValue(null));
        inSequence(seq);
        exactly(1).of(mockFactory).createTransactionStoreValue();
        TransactionStoreValue val = mockery.mock(TransactionStoreValue.class);
        will(returnValue(val));
        inSequence(seq);
        exactly(1).of(val).setCurrentState(with(same(element.getDto())));
        inSequence(seq);
        exactly(1).of(val).setOpSequence(with(equal(0)));
        inSequence(seq);
        exactly(1).of(val).setOpState(with(equal(OpState.SAVE)));
        inSequence(seq);
        exactly(1).of(mockMemCache).storeTransactionValue(with(same(key)), with(same(val)));
        inSequence(seq);
      }
    });
    serviceImpl.populateCache(element, OpState.SAVE);
    mockery.assertIsSatisfied();
  }

  @Test
  public void testPopulateTransactionCacheWithNonExistingValueForNotSave() {
    final TransactionService service = injector.getInstance(TransactionService.class);
    final TransactionServiceImpl serviceImpl = (TransactionServiceImpl) service;
    final CommonReadDao<DemoDomain, String> readDao = mockery.mock(CommonReadDao.class);
    final CommonWriteDao<DemoDomain> writeDao = mockery.mock(CommonWriteDao.class);
    final TransactionElement<DemoDomain> element = getTransactionElement(readDao, writeDao);
    mockery.checking(new Expectations() {

      {
        Sequence seq = mockery.sequence("serial");
        exactly(1).of(mockFactory).createTransactionStoreKey();
        TransactionStoreKey key = mockery.mock(TransactionStoreKey.class);
        will(returnValue(key));
        inSequence(seq);
        exactly(1).of(key).setOpTimestamp(with(any(Long.class)));
        inSequence(seq);
        exactly(1).of(key).setTransactionId("1");
        inSequence(seq);
        exactly(1).of(key).setObjectType(DemoDomain.class);
        inSequence(seq);
        exactly(1).of(key).setObjectId("1");
        inSequence(seq);
        exactly(1).of(mockMemCache).getValueForIsolatedTransaction(with(same(key)));
        will(returnValue(null));
        inSequence(seq);
        exactly(1).of(mockFactory).createTransactionStoreValue();
        TransactionStoreValue val = mockery.mock(TransactionStoreValue.class);
        will(returnValue(val));
        inSequence(seq);
        exactly(1).of(val).setCurrentState(with(same(element.getDto())));
        inSequence(seq);
        exactly(1).of(val).setOpSequence(with(equal(0)));
        inSequence(seq);
        exactly(1).of(val).setOpState(with(equal(OpState.UPDATE)));
        inSequence(seq);
        exactly(1).of(key).getObjectId();
        will(returnValue("1"));
        DemoDomain orgDemo = new DemoDomain();
        exactly(1).of(readDao).getById("1");
        will(returnValue(orgDemo));
        inSequence(seq);
        exactly(1).of(val).setOriginalState(with(same(orgDemo)));
        inSequence(seq);
        exactly(1).of(mockMemCache).storeTransactionValue(with(same(key)), with(same(val)));
        inSequence(seq);
      }
    });
    serviceImpl.populateCache(element, OpState.UPDATE);
    mockery.assertIsSatisfied();

  }

  @Test
  public void testPopulateTransactionCacheWithExistingValue() {
    final TransactionService service = injector.getInstance(TransactionService.class);
    final TransactionServiceImpl serviceImpl = (TransactionServiceImpl) service;
    CommonReadDao<DemoDomain, String> readDao = mockery.mock(CommonReadDao.class);
    CommonWriteDao<DemoDomain> writeDao = mockery.mock(CommonWriteDao.class);
    final TransactionElement<DemoDomain> element = getTransactionElement(readDao, writeDao);
    mockery.checking(new Expectations() {

      {
        Sequence seq = mockery.sequence("serial");
        exactly(1).of(mockFactory).createTransactionStoreKey();
        TransactionStoreKey key = mockery.mock(TransactionStoreKey.class);
        will(returnValue(key));
        inSequence(seq);
        exactly(1).of(key).setOpTimestamp(with(any(Long.class)));
        inSequence(seq);
        exactly(1).of(key).setTransactionId("1");
        inSequence(seq);
        exactly(1).of(key).setObjectType(DemoDomain.class);
        inSequence(seq);
        exactly(1).of(key).setObjectId("1");
        inSequence(seq);
        TransactionStoreValue val = mockery.mock(TransactionStoreValue.class);
        Pair<TransactionStoreKey, TransactionStoreValue> pair = new Pair<TransactionStoreKey, TransactionStoreValue>(
            key, val);
        exactly(1).of(mockMemCache).getValueForIsolatedTransaction(with(same(key)));
        will(returnValue(pair));
        inSequence(seq);
        exactly(1).of(val).setCurrentState(with(same(element.getDto())));
        inSequence(seq);
        exactly(1).of(val).getOpState();
        will(returnValue(OpState.SAVE));
        inSequence(seq);
        exactly(1).of(val).setOpState(with(equal(OpState.SAVE)));
        inSequence(seq);
      }
    });
    serviceImpl.populateCache(element, OpState.UPDATE);
    mockery.assertIsSatisfied();
  }

  @Test
  public void testUpdate() {
    final TransactionService service = injector.getInstance(TransactionService.class);
    final CommonReadDao<DemoDomain, String> readDao = mockery.mock(CommonReadDao.class);
    final CommonWriteDao<DemoDomain> writeDao = mockery.mock(CommonWriteDao.class);
    final TransactionElement<DemoDomain> element = getTransactionElement(readDao, writeDao);
    mockery.checking(new Expectations() {

      {
        exactly(1).of(mockFactory).createTransactionStoreKey();
        TransactionStoreKey key = mockery.mock(TransactionStoreKey.class);
        will(returnValue(key));
        exactly(1).of(key).setOpTimestamp(with(any(Long.class)));
        exactly(1).of(key).setTransactionId("1");
        exactly(1).of(key).setObjectType(DemoDomain.class);
        exactly(1).of(key).setObjectId("1");
        TransactionStoreValue val = mockery.mock(TransactionStoreValue.class);
        Pair<TransactionStoreKey, TransactionStoreValue> pair = new Pair<TransactionStoreKey, TransactionStoreValue>(
            key, val);
        exactly(1).of(mockMemCache).getValueForIsolatedTransaction(with(same(key)));
        will(returnValue(pair));
        exactly(1).of(val).setCurrentState(with(same(element.getDto())));
        exactly(1).of(val).getOpState();
        will(returnValue(OpState.SAVE));
        exactly(1).of(val).setOpState(with(equal(OpState.SAVE)));
      }
    });
    service.update(element);
    mockery.assertIsSatisfied();
  }

  @Test
  public void testDelete() {
    final TransactionService service = injector.getInstance(TransactionService.class);
    final CommonReadDao<DemoDomain, String> readDao = mockery.mock(CommonReadDao.class);
    final CommonWriteDao<DemoDomain> writeDao = mockery.mock(CommonWriteDao.class);
    final TransactionElement<DemoDomain> element = getTransactionElement(readDao, writeDao);
    mockery.checking(new Expectations() {

      {
        exactly(1).of(mockFactory).createTransactionStoreKey();
        TransactionStoreKey key = mockery.mock(TransactionStoreKey.class);
        will(returnValue(key));
        exactly(1).of(key).setOpTimestamp(with(any(Long.class)));
        exactly(1).of(key).setTransactionId("1");
        exactly(1).of(key).setObjectType(DemoDomain.class);
        exactly(1).of(key).setObjectId("1");
        TransactionStoreValue val = mockery.mock(TransactionStoreValue.class);
        Pair<TransactionStoreKey, TransactionStoreValue> pair = new Pair<TransactionStoreKey, TransactionStoreValue>(
            key, val);
        exactly(1).of(mockMemCache).getValueForIsolatedTransaction(with(same(key)));
        will(returnValue(pair));
        exactly(1).of(val).setCurrentState(with(same(element.getDto())));
        exactly(1).of(val).getOpState();
        will(returnValue(OpState.UPDATE));
        exactly(1).of(val).setOpState(with(equal(OpState.DELETE)));
      }
    });
    service.delete(element);
    mockery.assertIsSatisfied();
  }

  @Test
  public void testSaveWithId() {
    final TransactionService service = injector.getInstance(TransactionService.class);
    final CommonReadDao<DemoDomain, String> readDao = mockery.mock(CommonReadDao.class);
    final CommonWriteDao<DemoDomain> writeDao = mockery.mock(CommonWriteDao.class);
    final TransactionElement<DemoDomain> element = getTransactionElement(readDao, writeDao);
    mockery.checking(new Expectations() {

      {
        exactly(1).of(mockFactory).createTransactionStoreKey();
        TransactionStoreKey key = mockery.mock(TransactionStoreKey.class);
        will(returnValue(key));
        exactly(1).of(key).setOpTimestamp(with(any(Long.class)));
        exactly(1).of(key).setTransactionId("1");
        exactly(1).of(key).setObjectType(DemoDomain.class);
        exactly(1).of(key).setObjectId("1");
        TransactionStoreValue val = mockery.mock(TransactionStoreValue.class);
        Pair<TransactionStoreKey, TransactionStoreValue> pair = new Pair<TransactionStoreKey, TransactionStoreValue>(
            key, val);
        exactly(1).of(mockMemCache).getValueForIsolatedTransaction(with(same(key)));
        will(returnValue(pair));
        exactly(1).of(val).setCurrentState(with(same(element.getDto())));
        exactly(1).of(val).getOpState();
        will(returnValue(null));
        exactly(1).of(val).setOpState(with(equal(OpState.SAVE)));
      }
    });
    service.save(element);
    mockery.assertIsSatisfied();
  }

  @Test
  public void testSaveWithoutId() {
    final TransactionService service = injector.getInstance(TransactionService.class);
    final CommonReadDao<DemoDomain, String> readDao = mockery.mock(CommonReadDao.class);
    final CommonWriteDao<DemoDomain> writeDao = mockery.mock(CommonWriteDao.class);
    final TransactionElement<DemoDomain> element = getTransactionElement(readDao, writeDao);
    element.getDto().setId(null);
    mockery.checking(new Expectations() {

      {
        exactly(1).of(mockFactory).createTransactionStoreKey();
        TransactionStoreKey key = mockery.mock(TransactionStoreKey.class);
        will(returnValue(key));
        exactly(1).of(key).setOpTimestamp(with(any(Long.class)));
        exactly(1).of(key).setTransactionId("1");
        exactly(1).of(key).setObjectType(DemoDomain.class);
        exactly(1).of(key).setObjectId(with(aNonNull(String.class)));
        TransactionStoreValue val = mockery.mock(TransactionStoreValue.class);
        Pair<TransactionStoreKey, TransactionStoreValue> pair = new Pair<TransactionStoreKey, TransactionStoreValue>(
            key, val);
        exactly(1).of(mockMemCache).getValueForIsolatedTransaction(with(same(key)));
        will(returnValue(pair));
        exactly(1).of(val).setCurrentState(with(same(element.getDto())));
        exactly(1).of(val).getOpState();
        will(returnValue(null));
        exactly(1).of(val).setOpState(with(equal(OpState.SAVE)));
      }
    });
    service.save(element);
    mockery.assertIsSatisfied();
  }

  @Test
  public void testHardRollbackWithEmptyDeque() {
    final TransactionService service = injector.getInstance(TransactionService.class);
    final TransactionServiceImpl serviceImpl = (TransactionServiceImpl) service;
    serviceImpl.rollback(new LinkedList<Pair<TransactionStoreKey, TransactionStoreValue>>());
    serviceImpl.rollback((Deque<Pair<TransactionStoreKey, TransactionStoreValue>>) null);
  }

  @Test
  public void testHardRollback() {
    final CommonReadDao<DemoDomain, String> readDao = mockery.mock(CommonReadDao.class);
    final CommonWriteDao<DemoDomain> writeDao = mockery.mock(CommonWriteDao.class);
    final TransactionService service = injector.getInstance(TransactionService.class);
    final TransactionServiceImpl serviceImpl = (TransactionServiceImpl) service;
    ConcurrentMap<String, Pair<CommonWriteDao<? extends AbstractRepositoryDomain>, CommonReadDao<? extends AbstractRepositoryDomain, String>>> daoCache;
    try {
      final Field daoCacheField =
                  TransactionServiceImpl.class.getDeclaredField("daoCache");
      daoCacheField.setAccessible(true);
      daoCache =
      (ConcurrentMap<String, Pair<CommonWriteDao<? extends AbstractRepositoryDomain>, CommonReadDao<? extends AbstractRepositoryDomain, String>>>) daoCacheField.
          get(serviceImpl);
      daoCache.put(DemoDomain.class.getName(),
                   new Pair<CommonWriteDao<? extends AbstractRepositoryDomain>, CommonReadDao<? extends AbstractRepositoryDomain, String>>(
          writeDao, readDao));
    }
    catch (Exception ex) {
      throw new IllegalStateException(ex);
    }
    final TransactionStoreKey k1 = mockery.mock(TransactionStoreKey.class, "k1"), k2 = mockery.mock(
        TransactionStoreKey.class, "k2");
    final TransactionStoreValue v1 = mockery.mock(TransactionStoreValue.class, "v1"), v2 = mockery.mock(
        TransactionStoreValue.class, "v2");
    final TransactionStoreKey k3 = mockery.mock(TransactionStoreKey.class, "k3"), k4 = mockery.mock(
        TransactionStoreKey.class, "k4");
    final TransactionStoreValue v3 = mockery.mock(TransactionStoreValue.class, "v3"), v4 = mockery.mock(
        TransactionStoreValue.class, "v4");
    final Deque<Pair<TransactionStoreKey, TransactionStoreValue>> linkedList =
                                                                  new LinkedList<Pair<TransactionStoreKey, TransactionStoreValue>>();
    linkedList.push(new Pair<TransactionStoreKey, TransactionStoreValue>(k1, v1));
    linkedList.push(new Pair<TransactionStoreKey, TransactionStoreValue>(k2, v2));
    linkedList.push(new Pair<TransactionStoreKey, TransactionStoreValue>(k3, v3));
    linkedList.push(new Pair<TransactionStoreKey, TransactionStoreValue>(k4, v4));
    mockery.checking(new Expectations() {

      {
        DemoDomain d1 = new DemoDomain();
        Sequence seq = mockery.sequence("hardRollback");
        exactly(1).of(k4).getObjectType();
        will(returnValue(DemoDomain.class));
        inSequence(seq);
        exactly(1).of(v4).getOpState();
        will(returnValue(OpState.SAVE));
        inSequence(seq);
        exactly(1).of(v4).getCurrentState();
        will(returnValue(d1));
        inSequence(seq);
        exactly(1).of(writeDao).delete(d1);
        inSequence(seq);

        exactly(1).of(k3).getObjectType();
        will(returnValue(DemoDomain.class));
        inSequence(seq);
        exactly(1).of(v3).getOpState();
        will(returnValue(OpState.UPDATE));
        inSequence(seq);
        exactly(1).of(v3).getCurrentState();
        will(returnValue(d1));
        inSequence(seq);
        exactly(1).of(writeDao).update(d1);
        inSequence(seq);

        exactly(1).of(k2).getObjectType();
        will(returnValue(DemoDomain.class));
        inSequence(seq);
        exactly(1).of(v2).getOpState();
        will(returnValue(OpState.DELETE));
        inSequence(seq);
        exactly(1).of(v2).getCurrentState();
        will(returnValue(d1));
        inSequence(seq);
        exactly(1).of(writeDao).save(d1);
        inSequence(seq);

        exactly(1).of(k1).getObjectType();
        will(returnValue(DemoDomain.class));
        inSequence(seq);
        exactly(1).of(v1).getOpState();
        will(returnValue(null));
        inSequence(seq);
      }
    });
    serviceImpl.rollback(linkedList);
    mockery.assertIsSatisfied();
  }

  @Test
  public void testCommitWithBlankTxId() {
    final TransactionService service = injector.getInstance(TransactionService.class);
    service.commit(null);
    service.commit("");
    service.commit("    ");
  }

  @Test
  public void testCommitWithNullOrEmptyTransactionOps() {
    final TransactionService service = injector.getInstance(TransactionService.class);
    mockery.checking(new Expectations() {

      {
        exactly(1).of(mockMemCache).getTransactionParticipants("1");
        will(returnValue(null));
        exactly(1).of(mockMemCache).getTransactionParticipants("1");
        will(returnValue(new ArrayList<Pair<TransactionStoreKey, TransactionStoreValue>>()));
      }
    });
    service.commit("1");
    service.commit("1");
    mockery.assertIsSatisfied();
  }

  @Test
  public void testCommit() {
    final CommonReadDao<DemoDomain, String> readDao = mockery.mock(CommonReadDao.class);
    final CommonWriteDao<DemoDomain> writeDao = mockery.mock(CommonWriteDao.class);
    final TransactionService service = injector.getInstance(TransactionService.class);
    final TransactionServiceImpl serviceImpl = (TransactionServiceImpl) service;
    ConcurrentMap<String, Pair<CommonWriteDao<? extends AbstractRepositoryDomain>, CommonReadDao<? extends AbstractRepositoryDomain, String>>> daoCache;
    try {
      final Field daoCacheField =
                  TransactionServiceImpl.class.getDeclaredField("daoCache");
      daoCacheField.setAccessible(true);
      daoCache =
      (ConcurrentMap<String, Pair<CommonWriteDao<? extends AbstractRepositoryDomain>, CommonReadDao<? extends AbstractRepositoryDomain, String>>>) daoCacheField.
          get(serviceImpl);
      daoCache.put(DemoDomain.class.getName(),
                   new Pair<CommonWriteDao<? extends AbstractRepositoryDomain>, CommonReadDao<? extends AbstractRepositoryDomain, String>>(
          writeDao, readDao));
    }
    catch (Exception ex) {
      throw new IllegalStateException(ex);
    }
    final TransactionStoreKey k1 = mockery.mock(TransactionStoreKey.class, "k1"), k2 = mockery.mock(
        TransactionStoreKey.class, "k2");
    final TransactionStoreValue v1 = mockery.mock(TransactionStoreValue.class, "v1"), v2 = mockery.mock(
        TransactionStoreValue.class, "v2");
    final TransactionStoreKey k3 = mockery.mock(TransactionStoreKey.class, "k3"), k4 = mockery.mock(
        TransactionStoreKey.class, "k4");
    final TransactionStoreValue v3 = mockery.mock(TransactionStoreValue.class, "v3"), v4 = mockery.mock(
        TransactionStoreValue.class, "v4");
    final List<Pair<TransactionStoreKey, TransactionStoreValue>> opsList =
                                                                 new ArrayList<Pair<TransactionStoreKey, TransactionStoreValue>>();
    opsList.add(new Pair<TransactionStoreKey, TransactionStoreValue>(k1, v1));
    opsList.add(new Pair<TransactionStoreKey, TransactionStoreValue>(k2, v2));
    opsList.add(new Pair<TransactionStoreKey, TransactionStoreValue>(k3, v3));
    opsList.add(new Pair<TransactionStoreKey, TransactionStoreValue>(k4, v4));
    mockery.checking(new Expectations() {

      {
        DemoDomain d1 = new DemoDomain();
        Sequence seq = mockery.sequence("commit");

        exactly(1).of(mockMemCache).getTransactionParticipants("1");
        will(returnValue(opsList));
        inSequence(seq);

        atLeast(1).of(v1).getOpSequence();
        will(returnValue(3));
        atLeast(1).of(v2).getOpSequence();
        will(returnValue(2));
        atLeast(1).of(v3).getOpSequence();
        will(returnValue(1));
        atLeast(1).of(v4).getOpSequence();
        will(returnValue(0));

        exactly(1).of(k4).getObjectType();
        will(returnValue(DemoDomain.class));
        inSequence(seq);
        exactly(1).of(v4).getOpState();
        will(returnValue(OpState.SAVE));
        inSequence(seq);
        exactly(1).of(v4).getCurrentState();
        will(returnValue(d1));
        inSequence(seq);
        exactly(1).of(writeDao).save(d1);
        inSequence(seq);

        exactly(1).of(k3).getObjectType();
        will(returnValue(DemoDomain.class));
        inSequence(seq);
        exactly(1).of(v3).getOpState();
        will(returnValue(OpState.UPDATE));
        inSequence(seq);
        exactly(1).of(v3).getCurrentState();
        will(returnValue(d1));
        inSequence(seq);
        exactly(1).of(writeDao).update(d1);
        inSequence(seq);

        exactly(1).of(k2).getObjectType();
        will(returnValue(DemoDomain.class));
        inSequence(seq);
        exactly(1).of(v2).getOpState();
        will(returnValue(OpState.DELETE));
        inSequence(seq);
        exactly(1).of(v2).getCurrentState();
        will(returnValue(d1));
        inSequence(seq);
        exactly(1).of(writeDao).delete(d1);
        inSequence(seq);

        exactly(1).of(k1).getObjectType();
        will(returnValue(DemoDomain.class));
        inSequence(seq);
        exactly(1).of(v1).getOpState();
        will(returnValue(null));
        inSequence(seq);
      }
    });
    serviceImpl.commit("1");
    mockery.assertIsSatisfied();
  }

  @Test(expected = TransactionException.class)
  public void testCommitWithHardRollback() {
    final CommonReadDao<DemoDomain, String> readDao = mockery.mock(CommonReadDao.class);
    final CommonWriteDao<DemoDomain> writeDao = mockery.mock(CommonWriteDao.class);
    final TransactionService service = injector.getInstance(TransactionService.class);
    final TransactionServiceImpl serviceImpl = (TransactionServiceImpl) service;
    ConcurrentMap<String, Pair<CommonWriteDao<? extends AbstractRepositoryDomain>, CommonReadDao<? extends AbstractRepositoryDomain, String>>> daoCache;
    try {
      final Field daoCacheField =
                  TransactionServiceImpl.class.getDeclaredField("daoCache");
      daoCacheField.setAccessible(true);
      daoCache =
      (ConcurrentMap<String, Pair<CommonWriteDao<? extends AbstractRepositoryDomain>, CommonReadDao<? extends AbstractRepositoryDomain, String>>>) daoCacheField.
          get(serviceImpl);
      daoCache.put(DemoDomain.class.getName(),
                   new Pair<CommonWriteDao<? extends AbstractRepositoryDomain>, CommonReadDao<? extends AbstractRepositoryDomain, String>>(
          writeDao, readDao));
    }
    catch (Exception ex) {
      throw new IllegalStateException(ex);
    }
    final TransactionStoreKey k1 = mockery.mock(TransactionStoreKey.class, "k1"), k2 = mockery.mock(
        TransactionStoreKey.class, "k2");
    final TransactionStoreValue v1 = mockery.mock(TransactionStoreValue.class, "v1"), v2 = mockery.mock(
        TransactionStoreValue.class, "v2");
    final TransactionStoreKey k3 = mockery.mock(TransactionStoreKey.class, "k3"), k4 = mockery.mock(
        TransactionStoreKey.class, "k4");
    final TransactionStoreValue v3 = mockery.mock(TransactionStoreValue.class, "v3"), v4 = mockery.mock(
        TransactionStoreValue.class, "v4");
    final List<Pair<TransactionStoreKey, TransactionStoreValue>> opsList =
                                                                 new ArrayList<Pair<TransactionStoreKey, TransactionStoreValue>>();
    opsList.add(new Pair<TransactionStoreKey, TransactionStoreValue>(k1, v1));
    opsList.add(new Pair<TransactionStoreKey, TransactionStoreValue>(k2, v2));
    opsList.add(new Pair<TransactionStoreKey, TransactionStoreValue>(k3, v3));
    opsList.add(new Pair<TransactionStoreKey, TransactionStoreValue>(k4, v4));
    mockery.checking(new Expectations() {

      {
        DemoDomain d1 = new DemoDomain();
        Sequence seq = mockery.sequence("commitWithHardRollback");

        exactly(1).of(mockMemCache).getTransactionParticipants("1");
        will(returnValue(opsList));
        inSequence(seq);

        atLeast(1).of(v1).getOpSequence();
        will(returnValue(2));
        atLeast(1).of(v2).getOpSequence();
        will(returnValue(3));
        atLeast(1).of(v3).getOpSequence();
        will(returnValue(1));
        atLeast(1).of(v4).getOpSequence();
        will(returnValue(0));

        exactly(1).of(k4).getObjectType();
        will(returnValue(DemoDomain.class));
        inSequence(seq);
        exactly(1).of(v4).getOpState();
        will(returnValue(OpState.SAVE));
        inSequence(seq);
        exactly(1).of(v4).getCurrentState();
        will(returnValue(d1));
        inSequence(seq);
        exactly(1).of(writeDao).save(d1);
        inSequence(seq);

        exactly(1).of(k3).getObjectType();
        will(returnValue(DemoDomain.class));
        inSequence(seq);
        exactly(1).of(v3).getOpState();
        will(returnValue(OpState.UPDATE));
        inSequence(seq);
        exactly(1).of(v3).getCurrentState();
        will(returnValue(d1));
        inSequence(seq);
        exactly(1).of(writeDao).update(d1);
        inSequence(seq);

        exactly(1).of(k1).getObjectType();
        will(returnValue(DemoDomain.class));
        inSequence(seq);
        exactly(1).of(v1).getOpState();
        will(returnValue(null));
        inSequence(seq);

        exactly(1).of(k2).getObjectType();
        will(returnValue(DemoDomain.class));
        inSequence(seq);
        exactly(1).of(v2).getOpState();
        will(returnValue(OpState.DELETE));
        inSequence(seq);
        exactly(1).of(v2).getCurrentState();
        will(returnValue(d1));
        inSequence(seq);
        exactly(1).of(writeDao).delete(d1);
        will(throwException(new NullPointerException()));
        inSequence(seq);

        exactly(1).of(k1).getObjectType();
        will(returnValue(DemoDomain.class));
        inSequence(seq);
        exactly(1).of(v1).getOpState();
        will(returnValue(null));
        inSequence(seq);

        exactly(1).of(k3).getObjectType();
        will(returnValue(DemoDomain.class));
        inSequence(seq);
        exactly(1).of(v3).getOpState();
        will(returnValue(OpState.UPDATE));
        inSequence(seq);
        exactly(1).of(v3).getCurrentState();
        will(returnValue(d1));
        inSequence(seq);
        exactly(1).of(writeDao).update(d1);
        inSequence(seq);

        exactly(1).of(k4).getObjectType();
        will(returnValue(DemoDomain.class));
        inSequence(seq);
        exactly(1).of(v4).getOpState();
        will(returnValue(OpState.SAVE));
        inSequence(seq);
        exactly(1).of(v4).getCurrentState();
        will(returnValue(d1));
        inSequence(seq);
        exactly(1).of(writeDao).delete(d1);
        inSequence(seq);
      }
    });
    serviceImpl.commit("1");
    mockery.assertIsSatisfied();
  }
}
