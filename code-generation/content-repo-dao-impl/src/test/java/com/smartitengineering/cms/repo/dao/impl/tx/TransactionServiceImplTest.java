package com.smartitengineering.cms.repo.dao.impl.tx;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.smartitengineering.dao.common.CommonReadDao;
import com.smartitengineering.dao.common.CommonWriteDao;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
}
