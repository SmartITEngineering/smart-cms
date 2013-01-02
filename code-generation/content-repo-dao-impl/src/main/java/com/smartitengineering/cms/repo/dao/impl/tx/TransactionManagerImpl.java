package com.smartitengineering.cms.repo.dao.impl.tx;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.smartitengineering.cms.repo.dao.tx.Transaction;
import com.smartitengineering.cms.repo.dao.tx.TransactionCompletionEvent;
import com.smartitengineering.cms.repo.dao.tx.TransactionCompletionListener;
import com.smartitengineering.cms.repo.dao.tx.TransactionManager;
import java.util.ArrayDeque;
import java.util.Deque;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
@Singleton
class TransactionManagerImpl implements TransactionManager, TransactionCompletionListener {

  private final ThreadLocal<Deque<Transaction>> transactions = new ThreadLocal<Deque<Transaction>>();
  private final TransactionFactory factory;
  private final TransactionInMemoryCache memCache;
  private static final Logger LOGGER = LoggerFactory.getLogger(TransactionManagerImpl.class);

  @Inject
  public TransactionManagerImpl(TransactionFactory factory, TransactionInMemoryCache memCache) {
    this.factory = factory;
    this.memCache = memCache;
  }

  public Transaction beginTransaction() {
    return beginTransaction(true);
  }

  public Transaction beginTransaction(boolean isolatedTransaction) {
    Deque<Transaction> stack = transactions.get();
    if (stack == null) {
      stack = new ArrayDeque<Transaction>();
      transactions.set(stack);
    }
    final Transaction tx = this.factory.createTransaction(isolatedTransaction);
    tx.addTransactionCompletionListener(this);
    stack.push(tx);
    return getCurrentTransaction();
  }

  public Transaction getCurrentTransaction() {
    Deque<Transaction> stack = transactions.get();
    if (stack != null) {
      return stack.peek();
    }
    else {
      return null;
    }
  }

  public void transactionComplete(TransactionCompletionEvent event) {
    Deque<Transaction> stack = transactions.get();
    if (stack != null && !stack.isEmpty()) {
      final Transaction peek = stack.peek();
      if (peek.equals(event.getTransaction())) {
        stack.pop();
      }
      else {
        if (LOGGER.isWarnEnabled()) {
          LOGGER.warn("Removing a transaction (" + event.getTransaction().getId() + ") that is not the 'peek'");
        }
        stack.remove(event.getTransaction());
      }
    }
    memCache.removeTransactionReferences(event.getTransaction().getId());
  }
}
