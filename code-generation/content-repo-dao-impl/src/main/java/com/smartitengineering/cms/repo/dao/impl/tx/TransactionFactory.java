package com.smartitengineering.cms.repo.dao.impl.tx;

import com.smartitengineering.cms.repo.dao.tx.Transaction;

/**
 *
 * @author imyousuf
 */
public interface TransactionFactory {

  TransactionStoreKey createTransactionStoreKey();

  TransactionStoreValue createTransactionStoreValue();

  Transaction createTransaction(boolean isolatedTransaction);
}
