package com.smartitengineering.cms.repo.dao.impl.tx;

import java.util.List;

/**
 * A SPI for interacting with transactional memory
 * @author imyousuf
 */
public interface TransactionInMemoryCache {

  public Pair<TransactionStoreKey, TransactionStoreValue> getValueForIsolatedTransaction(TransactionStoreKey key);

  public Pair<TransactionStoreKey, TransactionStoreValue> getValueForNonIsolatedTransacton(TransactionStoreKey key);

  public Pair<TransactionStoreKey, TransactionStoreValue> getValueForIsolatedTransaction(String txId, String objectType,
                                                                                         String objectId);

  public Pair<TransactionStoreKey, TransactionStoreValue> getValueForNonIsolatedTransacton(String objectType,
                                                                                           String objectId);

  public List<Pair<TransactionStoreKey, TransactionStoreValue>> getTransactionParticipants(String txId);

  public void storeTransactionValue(TransactionStoreKey key, TransactionStoreValue val);
}
