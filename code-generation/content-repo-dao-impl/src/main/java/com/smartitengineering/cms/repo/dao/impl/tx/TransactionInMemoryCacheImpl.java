package com.smartitengineering.cms.repo.dao.impl.tx;

import com.google.inject.Singleton;
import edu.emory.mathcs.backport.java.util.Arrays;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
@Singleton
class TransactionInMemoryCacheImpl implements TransactionInMemoryCache {

  private final ConcurrentMap<MultiKey, Pair<TransactionStoreKey, TransactionStoreValue>> isolatedTxCache;
  private final ConcurrentMap<MultiKey, Deque<Pair<TransactionStoreKey, TransactionStoreValue>>> globalCache;
  private final Semaphore semaphore;
  private static final Logger LOGGER = LoggerFactory.getLogger(TransactionInMemoryCacheImpl.class);

  public TransactionInMemoryCacheImpl() {
    isolatedTxCache = new ConcurrentHashMap<MultiKey, Pair<TransactionStoreKey, TransactionStoreValue>>();
    globalCache = new ConcurrentHashMap<MultiKey, Deque<Pair<TransactionStoreKey, TransactionStoreValue>>>();
    semaphore = new Semaphore(1);
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
    final Deque<Pair<TransactionStoreKey, TransactionStoreValue>> stack = globalCache.get(gKey);
    if (stack != null) {
      return stack.peek();
    }
    else {
      return null;
    }
  }

  public void storeTransactionValue(TransactionStoreKey key, TransactionStoreValue val) {
    Pair<TransactionStoreKey, TransactionStoreValue> pairVal = new Pair<TransactionStoreKey, TransactionStoreValue>(key,
                                                                                                                    val);
    MultiKey iKey = new MultiKey(new Object[]{key.getTransactionId(), key.getObjectType().getName(), key.getObjectId()});
    isolatedTxCache.put(iKey, pairVal);
    MultiKey gKey = new MultiKey(new Object[]{key.getObjectType().getName(), key.getObjectId()});
    ArrayDeque<Pair<TransactionStoreKey, TransactionStoreValue>> newStack =
                                                                 new ArrayDeque<Pair<TransactionStoreKey, TransactionStoreValue>>();
    Deque<Pair<TransactionStoreKey, TransactionStoreValue>> stack = globalCache.putIfAbsent(gKey, newStack);
    if (stack == null) {
      stack = newStack;
    }
    stack.push(pairVal);
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

  public void removeTransactionReferences(String txId) {
    final Iterator<Entry<MultiKey, Pair<TransactionStoreKey, TransactionStoreValue>>> iterator =
                                                                                      isolatedTxCache.entrySet().
        iterator();
    while (iterator.hasNext()) {
      Entry<MultiKey, Pair<TransactionStoreKey, TransactionStoreValue>> entry = iterator.next();
      MultiKey tKey = entry.getKey();
      if (txId.equals(tKey.getKey(0))) {
        MultiKey gKey = new MultiKey(Arrays.copyOfRange(tKey.getKeys(), 1, 3));
        final Deque<Pair<TransactionStoreKey, TransactionStoreValue>> mainStack = globalCache.get(gKey);
        mainStack.remove(entry.getValue());
        iterator.remove();
        try {
          semaphore.acquire();
          try {
            if (mainStack.isEmpty()) {
              globalCache.remove(gKey);
            }
          }
          finally {
            semaphore.release();
          }
        }
        catch (Exception ex) {
          LOGGER.error("Error acquiring lock for global cache cleanup", ex);
        }
      }
    }
  }
}
