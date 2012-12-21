package com.smartitengineering.cms.repo.dao.impl.tx;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author imyousuf
 */
public class TransactionInMemoryCacheTest {

  private Injector injector;

  @Before
  public void setup() {
    injector = Guice.createInjector(new DemoDomainMasterModule());
  }

  @Test
  public void testInitialization() {
    TransactionInMemoryCache memCache = injector.getInstance(TransactionInMemoryCache.class);
    Assert.assertNotNull(memCache);
    TransactionFactory factory = injector.getInstance(TransactionFactory.class);
    Assert.assertNotNull(factory);
  }

  /**
   * Here the test would actually test how it should act. First create 2 transactions for the same object type and id.
   * Put it in transactional memory and then verify isolated and non-isolated retrieval
   */
  @Test
  public void testCacheMemory() {
    final TransactionInMemoryCache memCache = injector.getInstance(TransactionInMemoryCache.class);
    final TransactionFactory factory = injector.getInstance(TransactionFactory.class);
    DemoDomain d0 = new DemoDomain();
    DemoDomain d1 = new DemoDomain();
    DemoDomain d2 = new DemoDomain();
    d0.setTestValue(0);
    d0.setId("1");
    d1.setTestValue(1);
    d1.setId("1");
    d2.setTestValue(2);
    d2.setId("1");
    TransactionStoreKey k1 = factory.createTrasactionStoreKey();
    k1.setTransactionId("1");
    k1.setObjectId(d0.getId());
    k1.setObjectType(d0.getClass());
    k1.setOpTimestamp(System.currentTimeMillis() - 10);
    TransactionStoreValue v1 = factory.createTransactionStoreValue();
    v1.setCurrentState(d1);
    v1.setOpSequence(1);
    v1.setOpState(OpState.SAVE);
    v1.setOriginalState(d0);
    memCache.storeTransactionValue(k1, v1);
    TransactionStoreKey k2 = factory.createTrasactionStoreKey();
    k2.setTransactionId("2");
    k2.setObjectId(d0.getId());
    k2.setObjectType(d0.getClass());
    k2.setOpTimestamp(System.currentTimeMillis());
    TransactionStoreValue v2 = factory.createTransactionStoreValue();
    v2.setCurrentState(d2);
    v2.setOpSequence(3);
    v2.setOpState(OpState.UPDATE);
    v2.setOriginalState(d0);
    memCache.storeTransactionValue(k2, v2);
    Pair<TransactionStoreKey, TransactionStoreValue> pair = memCache.getValueForIsolatedTransaction(k1);
    Assert.assertSame(k1, pair.getKey());
    Assert.assertSame(v1, pair.getValue());
    pair = memCache.getValueForIsolatedTransaction(k2);
    Assert.assertSame(k2, pair.getKey());
    Assert.assertSame(v2, pair.getValue());
    pair = memCache.getValueForNonIsolatedTransacton(k1);
    Assert.assertSame(k2, pair.getKey());
    Assert.assertSame(v2, pair.getValue());
    pair = memCache.getValueForNonIsolatedTransacton(k2);
    Assert.assertSame(k2, pair.getKey());
    Assert.assertSame(v2, pair.getValue());
  }
}
