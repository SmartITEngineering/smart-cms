package com.smartitengineering.cms.repo.dao.impl.tx;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.smartitengineering.cms.repo.dao.tx.Transaction;
import com.smartitengineering.cms.repo.dao.tx.TransactionCompletionEvent;
import com.smartitengineering.cms.repo.dao.tx.TransactionCompletionListener;
import com.smartitengineering.cms.repo.dao.tx.TransactionManager;
import junit.framework.Assert;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author imyousuf
 */
public class TransactionManagerImplTest {

  private Mockery mockery;
  private TransactionFactory factory;
  private TransactionInMemoryCache memCache;

  @Before
  public void setup() {
    mockery = new Mockery();
    factory = mockery.mock(TransactionFactory.class);
    memCache = mockery.mock(TransactionInMemoryCache.class);
  }

  @Test
  public void testNonExistingTransaction() {
    Injector injector = initializeInjector();
    final TransactionManager manager = injector.getInstance(TransactionManager.class);
    mockery.checking(new Expectations() {

      {
      }
    });
    Assert.assertNull(manager.getCurrentTransaction());
    mockery.assertIsSatisfied();
  }

  @Test
  public void testBeginTransaction() {
    Injector injector = initializeInjector();
    final TransactionManager manager = injector.getInstance(TransactionManager.class);
    mockery.checking(new Expectations() {

      {
        exactly(1).of(factory).createTransaction();
        final Transaction txMock = mockery.mock(Transaction.class);
        will(returnValue(txMock));
        exactly(1).of(txMock).addTransactionCompletionListener((TransactionCompletionListener) manager);
      }
    });
    manager.beginTransaction();
    mockery.assertIsSatisfied();
  }

  @Test
  public void testGetCurrentTransactionForSingleTxOnAThread() {
    Injector injector = initializeInjector();
    final TransactionManager manager = injector.getInstance(TransactionManager.class);
    mockery.checking(new Expectations() {

      {
        exactly(1).of(factory).createTransaction();
        final Transaction txMock = mockery.mock(Transaction.class);
        will(returnValue(txMock));
        exactly(1).of(txMock).addTransactionCompletionListener((TransactionCompletionListener) manager);
      }
    });
    Transaction tx = manager.beginTransaction();
    Assert.assertSame(tx, manager.getCurrentTransaction());
    mockery.assertIsSatisfied();
  }

  @Test
  public void testGetCurrentTransactionForMultiTxOnAThread() {
    Injector injector = initializeInjector();
    final TransactionManager manager = injector.getInstance(TransactionManager.class);
    mockery.checking(new Expectations() {

      {
        exactly(1).of(factory).createTransaction();
        final Transaction txMock1 = mockery.mock(Transaction.class, "Tx1");
        will(returnValue(txMock1));
        exactly(1).of(txMock1).addTransactionCompletionListener((TransactionCompletionListener) manager);
        exactly(1).of(factory).createTransaction();
        final Transaction txMock2 = mockery.mock(Transaction.class, "Tx2");
        will(returnValue(txMock2));
        exactly(1).of(txMock2).addTransactionCompletionListener((TransactionCompletionListener) manager);
      }
    });
    Transaction tx1 = manager.beginTransaction();
    Assert.assertSame(tx1, manager.getCurrentTransaction());
    Transaction tx2 = manager.beginTransaction();
    Assert.assertSame(tx2, manager.getCurrentTransaction());
    mockery.assertIsSatisfied();
  }

  @Test
  public void testGetCurrentTransactionWithTransactionCompletionInProperOrder() {
    Injector injector = initializeInjector();
    final TransactionManager manager = injector.getInstance(TransactionManager.class);
    final TransactionCompletionListener listener = (TransactionCompletionListener) manager;
    mockery.checking(new Expectations() {

      {
        exactly(1).of(factory).createTransaction();
        final Transaction txMock1 = mockery.mock(Transaction.class, "Tx1");
        will(returnValue(txMock1));
        exactly(1).of(txMock1).addTransactionCompletionListener(listener);
        exactly(1).of(txMock1).getId();
        will(returnValue("1"));
        exactly(1).of(memCache).removeTransactionReferences("1");
        exactly(1).of(factory).createTransaction();
        final Transaction txMock2 = mockery.mock(Transaction.class, "Tx2");
        will(returnValue(txMock2));
        exactly(1).of(txMock2).getId();
        will(returnValue("2"));
        exactly(1).of(txMock2).addTransactionCompletionListener(listener);
        exactly(1).of(memCache).removeTransactionReferences("2");
      }
    });
    Transaction tx1 = manager.beginTransaction();
    Assert.assertSame(tx1, manager.getCurrentTransaction());
    Transaction tx2 = manager.beginTransaction();
    Assert.assertSame(tx2, manager.getCurrentTransaction());
    TransactionCompletionEvent event2 = new TransactionCompletionEvent(TransactionCompletionEvent.CompletionEvent.COMMIT,
                                                                       true, tx2);
    listener.transactionComplete(event2);
    Assert.assertSame(tx1, manager.getCurrentTransaction());
    TransactionCompletionEvent event1 = new TransactionCompletionEvent(TransactionCompletionEvent.CompletionEvent.COMMIT,
                                                                       true, tx1);
    listener.transactionComplete(event1);
    Assert.assertNull(manager.getCurrentTransaction());
    mockery.assertIsSatisfied();
  }

  @Test
  public void testGetCurrentTransactionWithTransactionCompletionInInproperOrder() {
    Injector injector = initializeInjector();
    final TransactionManager manager = injector.getInstance(TransactionManager.class);
    final TransactionCompletionListener listener = (TransactionCompletionListener) manager;
    mockery.checking(new Expectations() {

      {
        exactly(1).of(factory).createTransaction();
        final Transaction txMock1 = mockery.mock(Transaction.class, "Tx1");
        will(returnValue(txMock1));
        exactly(2).of(txMock1).getId();
        will(returnValue("1"));
        exactly(1).of(txMock1).addTransactionCompletionListener(listener);
        exactly(1).of(memCache).removeTransactionReferences("1");
        exactly(1).of(factory).createTransaction();
        final Transaction txMock2 = mockery.mock(Transaction.class, "Tx2");
        will(returnValue(txMock2));
        exactly(1).of(txMock2).getId();
        will(returnValue("2"));
        exactly(1).of(txMock2).addTransactionCompletionListener(listener);
        exactly(1).of(memCache).removeTransactionReferences("2");
      }
    });
    Transaction tx1 = manager.beginTransaction();
    Assert.assertSame(tx1, manager.getCurrentTransaction());
    Transaction tx2 = manager.beginTransaction();
    Assert.assertSame(tx2, manager.getCurrentTransaction());
    TransactionCompletionEvent event1 = new TransactionCompletionEvent(TransactionCompletionEvent.CompletionEvent.COMMIT,
                                                                       true, tx1);
    listener.transactionComplete(event1);
    Assert.assertSame(tx2, manager.getCurrentTransaction());
    TransactionCompletionEvent event2 = new TransactionCompletionEvent(TransactionCompletionEvent.CompletionEvent.COMMIT,
                                                                       true, tx2);
    listener.transactionComplete(event2);
    Assert.assertNull(manager.getCurrentTransaction());
    mockery.assertIsSatisfied();
  }

  private Injector initializeInjector() {
    final Injector injector = Guice.createInjector(new AbstractModule() {

      @Override
      protected void configure() {
        bind(TransactionManager.class).to(TransactionManagerImpl.class);
        bind(TransactionFactory.class).toInstance(factory);
        bind(TransactionInMemoryCache.class).toInstance(memCache);
      }
    });
    return injector;
  }
}
