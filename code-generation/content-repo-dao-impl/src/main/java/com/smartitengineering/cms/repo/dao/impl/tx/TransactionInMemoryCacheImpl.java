package com.smartitengineering.cms.repo.dao.impl.tx;

import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.collections.keyvalue.MultiKey;

/**
 *
 * @author imyousuf
 */
@Singleton
class TransactionInMemoryCacheImpl implements TransactionInMemoryCache {

  private final ConcurrentMap<MultiKey, Pair<TransactionStoreKey, TransactionStoreValue>> isolatedTxCache;
  private final ConcurrentMap<MultiKey, Pair<TransactionStoreKey, TransactionStoreValue>> globalCache;

  public TransactionInMemoryCacheImpl() {
    isolatedTxCache = new ConcurrentHashMap<MultiKey, Pair<TransactionStoreKey, TransactionStoreValue>>();
    globalCache = new ConcurrentHashMap<MultiKey, Pair<TransactionStoreKey, TransactionStoreValue>>();
  }

  public Pair<TransactionStoreKey, TransactionStoreValue> getValueForIsolatedTransaction(TransactionStoreKey key) {
    return getValueForIsolatedTransaction(key.getTransactionId(), key.getObjectType().getName(), key.getObjectId());
  }

  public Pair<TransactionStoreKey, TransactionStoreValue> getValueForNonIsolatedTransacton(TransactionStoreKey key) {
    return getValueForNonIsolatedTransacton(key.getObjectType().getName(), key.getObjectId());
  }

  public Pair<TransactionStoreKey, TransactionStoreValue> getValueForIsolatedTransaction(String txId, String objectType,
                                                                                         String objectId) {
    MultiKey isolatedTxKey = new MultiKey(new Object[]{txId, objectType, objectId});
    return isolatedTxCache.get(isolatedTxKey);
  }

  public Pair<TransactionStoreKey, TransactionStoreValue> getValueForNonIsolatedTransacton(String objectType,
                                                                                           String objectId) {
    MultiKey gKey = new MultiKey(new Object[]{objectType, objectId});
    return globalCache.get(gKey);
  }

  public void storeTransactionValue(TransactionStoreKey key, TransactionStoreValue val) {
    Pair<TransactionStoreKey, TransactionStoreValue> pairVal = new Pair<TransactionStoreKey, TransactionStoreValue>(key,
                                                                                                                    val);
    MultiKey iKey = new MultiKey(new Object[]{key.getTransactionId(), key.getObjectType().getName(), key.getObjectId()});
    isolatedTxCache.put(iKey, pairVal);
    MultiKey gKey = new MultiKey(new Object[]{key.getObjectType().getName(), key.getObjectId()});
    globalCache.put(gKey, pairVal);
  }

  public List<Pair<TransactionStoreKey, TransactionStoreValue>> getTransactionParticipants(String txId) {
    List<Pair<TransactionStoreKey, TransactionStoreValue>> pairs =
                                                           new ArrayList<Pair<TransactionStoreKey, TransactionStoreValue>>();
    for (MultiKey tKey : isolatedTxCache.keySet()) {
      if (txId.equals(tKey.getKey(0))) {
        pairs.add(isolatedTxCache.get(tKey));
      }
    }
    return pairs;
  }
}
