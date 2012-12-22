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

  @Before
  public void setup() {
    mockery = new Mockery();
  }

  @Test
  public void testNonExistingTransaction() {
    final TransactionFactory factory = mockery.mock(TransactionFactory.class);
    Injector injector = initializeInjector(factory);
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
    final TransactionFactory factory = mockery.mock(TransactionFactory.class);
    Injector injector = initializeInjector(factory);
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
    final TransactionFactory factory = mockery.mock(TransactionFactory.class);
    Injector injector = initializeInjector(factory);
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
    final TransactionFactory factory = mockery.mock(TransactionFactory.class);
    Injector injector = initializeInjector(factory);
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
    final TransactionFactory factory = mockery.mock(TransactionFactory.class);
    Injector injector = initializeInjector(factory);
    final TransactionManager manager = injector.getInstance(TransactionManager.class);
    final TransactionCompletionListener listener = (TransactionCompletionListener) manager;
    mockery.checking(new Expectations() {

      {
        exactly(1).of(factory).createTransaction();
        final Transaction txMock1 = mockery.mock(Transaction.class, "Tx1");
        will(returnValue(txMock1));
        exactly(1).of(txMock1).addTransactionCompletionListener(listener);
        exactly(1).of(factory).createTransaction();
        final Transaction txMock2 = mockery.mock(Transaction.class, "Tx2");
        will(returnValue(txMock2));
        exactly(1).of(txMock2).addTransactionCompletionListener(listener);
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
    final TransactionFactory factory = mockery.mock(TransactionFactory.class);
    Injector injector = initializeInjector(factory);
    final TransactionManager manager = injector.getInstance(TransactionManager.class);
    final TransactionCompletionListener listener = (TransactionCompletionListener) manager;
    mockery.checking(new Expectations() {

      {
        exactly(1).of(factory).createTransaction();
        final Transaction txMock1 = mockery.mock(Transaction.class, "Tx1");
        will(returnValue(txMock1));
        exactly(1).of(txMock1).getId();
        will(returnValue("1"));
        exactly(1).of(txMock1).addTransactionCompletionListener(listener);
        exactly(1).of(factory).createTransaction();
        final Transaction txMock2 = mockery.mock(Transaction.class, "Tx2");
        will(returnValue(txMock2));
        exactly(1).of(txMock2).addTransactionCompletionListener(listener);
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

  private Injector initializeInjector(final TransactionFactory factory) {
    final Injector injector = Guice.createInjector(new AbstractModule() {

      @Override
      protected void configure() {
        bind(TransactionManager.class).to(TransactionManagerImpl.class);
        bind(TransactionFactory.class).toInstance(factory);
      }
    });
    return injector;
  }
}
