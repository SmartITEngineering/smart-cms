package com.smartitengineering.cms.repo.dao.impl.tx;

/**
 *
 * @author imyousuf
 */
public interface TransactionFactory {

  TransactionStoreKey createTrasactionStoreKey();

  TransactionStoreValue createTransactionStoreValue();
}
