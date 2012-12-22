package com.smartitengineering.cms.repo.dao.impl.tx;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.smartitengineering.cms.repo.dao.tx.Transaction;
import com.smartitengineering.cms.repo.dao.tx.TransactionCompletionEvent;
import com.smartitengineering.cms.repo.dao.tx.TransactionCompletionListener;
import com.smartitengineering.cms.repo.dao.tx.TransactionException;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author imyousuf
 */
public class TransactionImplTest {

  private Mockery mockery;
  private TransactionService service;
  private TransactionCompletionListener listener;

  @Before
  public void setup() {
    mockery = new Mockery();
    service = mockery.mock(TransactionService.class);
    listener = mockery.mock(TransactionCompletionListener.class);
  }

  @Test
  public void testInitialization() {
    mockery.checking(new Expectations() {

      {
        exactly(1).of(service).getNextTransactionId();
        will(returnValue("1"));
      }
    });
    final Injector injector = initializeInjector();
    Assert.assertNotNull(injector.getInstance(Transaction.class));
    mockery.assertIsSatisfied();
  }

  @Test
  public void testEqualsAndHashCode() {
    mockery.checking(new Expectations() {

      {
        exactly(2).of(service).getNextTransactionId();
        will(returnValue("1"));
      }
    });
    final Injector injector = initializeInjector();
    final Transaction tx1 = injector.getInstance(Transaction.class);
    Assert.assertNotNull(tx1);
    final Transaction tx2 = injector.getInstance(Transaction.class);
    Assert.assertNotNull(tx2);
    Assert.assertEquals(tx1, tx2);
    Assert.assertEquals(tx1.hashCode(), tx2.hashCode());
    mockery.assertIsSatisfied();
  }

  @Test
  public void testCommit() {
    mockery.checking(new Expectations() {

      {
        exactly(1).of(service).getNextTransactionId();
        will(returnValue("1"));
        exactly(1).of(service).commit("1");
      }
    });
    final Injector injector = initializeInjector();
    final Transaction tx = injector.getInstance(Transaction.class);
    tx.commit();
    mockery.assertIsSatisfied();
  }

  @Test(expected = TransactionException.class)
  public void testCommitWithException() {
    mockery.checking(new Expectations() {

      {
        exactly(1).of(service).getNextTransactionId();
        will(returnValue("1"));
        exactly(1).of(service).commit("1");
        will(throwException(new TransactionException()));
      }
    });
    final Injector injector = initializeInjector();
    final Transaction tx = injector.getInstance(Transaction.class);
    RuntimeException ex = null;
    try {
      tx.commit();
    }
    catch (TransactionException tex) {
      ex = tex;
    }
    mockery.assertIsSatisfied();
    if (ex != null) {
      throw ex;
    }
  }

  @Test
  public void testRollback() {
    mockery.checking(new Expectations() {

      {
        exactly(1).of(service).getNextTransactionId();
        will(returnValue("1"));
        exactly(1).of(service).rollback("1");
      }
    });
    final Injector injector = initializeInjector();
    final Transaction tx = injector.getInstance(Transaction.class);
    tx.rollback();
    mockery.assertIsSatisfied();
  }

  @Test(expected = TransactionException.class)
  public void testRollbackWithException() {
    mockery.checking(new Expectations() {

      {
        exactly(1).of(service).getNextTransactionId();
        will(returnValue("1"));
        exactly(1).of(service).rollback("1");
        will(throwException(new TransactionException()));
      }
    });
    final Injector injector = initializeInjector();
    final Transaction tx = injector.getInstance(Transaction.class);
    RuntimeException ex = null;
    try {
      tx.rollback();
    }
    catch (TransactionException tex) {
      ex = tex;
    }
    mockery.assertIsSatisfied();
    if (ex != null) {
      throw ex;
    }
  }

  @Test
  public void testIsCompletedForCommit() {
    final Injector injector = initializeInjector();
    {
      mockery.checking(new Expectations() {

        {
          exactly(1).of(service).getNextTransactionId();
          will(returnValue("1"));
          exactly(1).of(service).commit("1");
        }
      });
      final Transaction tx = injector.getInstance(Transaction.class);
      Assert.assertFalse(tx.isCompleted());
      tx.commit();
      Assert.assertTrue(tx.isCompleted());
      mockery.assertIsSatisfied();
    }
  }

  @Test
  public void testIsCompletedForRollback() {
    final Injector injector = initializeInjector();
    {
      mockery.checking(new Expectations() {

        {
          exactly(1).of(service).getNextTransactionId();
          will(returnValue("1"));
          exactly(1).of(service).rollback("1");
        }
      });
      final Transaction tx2 = injector.getInstance(Transaction.class);
      Assert.assertFalse(tx2.isCompleted());
      tx2.rollback();
      Assert.assertTrue(tx2.isCompleted());
      mockery.assertIsSatisfied();
    }
  }

  @Test
  public void testAddListenerWithCommitSuccess() {
    final Injector injector = initializeInjector();
    {
      mockery.checking(new Expectations() {

        {
          exactly(1).of(service).getNextTransactionId();
          will(returnValue("1"));
          exactly(1).of(service).commit("1");
          Transaction tx = new TransactionImpl(new TransactionService() {

            public String getNextTransactionId() {
              return "1";
            }

            public void commit(String txId) {
              throw new UnsupportedOperationException("Not supported yet.");
            }

            public void rollback(String txId) {
              throw new UnsupportedOperationException("Not supported yet.");
            }
          });
          TransactionCompletionEvent event = new TransactionCompletionEvent(
              TransactionCompletionEvent.CompletionEvent.COMMIT, true, tx);
          exactly(1).of(listener).transactionComplete(with(equal(event)));
        }
      });
      final Transaction tx = injector.getInstance(Transaction.class);
      tx.addTransactionCompletionListener(listener);
      Assert.assertFalse(tx.isCompleted());
      tx.commit();
      Assert.assertTrue(tx.isCompleted());
      mockery.assertIsSatisfied();
    }
  }

  @Test
  public void testAddListenerWithCommitFailure() {
    final Injector injector = initializeInjector();
    {
      mockery.checking(new Expectations() {

        {
          exactly(1).of(service).getNextTransactionId();
          will(returnValue("1"));
          exactly(1).of(service).commit("1");
          will(throwException(new TransactionException()));
          Transaction tx = new TransactionImpl(new TransactionService() {

            public String getNextTransactionId() {
              return "1";
            }

            public void commit(String txId) {
              throw new UnsupportedOperationException("Not supported yet.");
            }

            public void rollback(String txId) {
              throw new UnsupportedOperationException("Not supported yet.");
            }
          });
          TransactionCompletionEvent event = new TransactionCompletionEvent(
              TransactionCompletionEvent.CompletionEvent.COMMIT, false, tx);
          exactly(1).of(listener).transactionComplete(with(equal(event)));
        }
      });
      final Transaction tx = injector.getInstance(Transaction.class);
      tx.addTransactionCompletionListener(listener);
      Assert.assertFalse(tx.isCompleted());
      try {
        tx.commit();
      }
      catch (Exception ex) {
      }
      Assert.assertTrue(tx.isCompleted());
      mockery.assertIsSatisfied();
    }
  }

  @Test
  public void testAddListenerWithRollbackSuccess() {
    final Injector injector = initializeInjector();
    {
      mockery.checking(new Expectations() {

        {
          exactly(1).of(service).getNextTransactionId();
          will(returnValue("1"));
          exactly(1).of(service).rollback("1");
          Transaction tx = new TransactionImpl(new TransactionService() {

            public String getNextTransactionId() {
              return "1";
            }

            public void commit(String txId) {
              throw new UnsupportedOperationException("Not supported yet.");
            }

            public void rollback(String txId) {
              throw new UnsupportedOperationException("Not supported yet.");
            }
          });
          TransactionCompletionEvent event = new TransactionCompletionEvent(
              TransactionCompletionEvent.CompletionEvent.ROLLBACK, true, tx);
          exactly(1).of(listener).transactionComplete(with(equal(event)));
        }
      });
      final Transaction tx = injector.getInstance(Transaction.class);
      tx.addTransactionCompletionListener(listener);
      Assert.assertFalse(tx.isCompleted());
      tx.rollback();
      Assert.assertTrue(tx.isCompleted());
      mockery.assertIsSatisfied();
    }
  }

  @Test
  public void testAddListenerRollbackFailure() {
    final Injector injector = initializeInjector();
    {
      mockery.checking(new Expectations() {

        {
          exactly(1).of(service).getNextTransactionId();
          will(returnValue("1"));
          exactly(1).of(service).rollback("1");
          will(throwException(new TransactionException()));
          Transaction tx = new TransactionImpl(new TransactionService() {

            public String getNextTransactionId() {
              return "1";
            }

            public void commit(String txId) {
              throw new UnsupportedOperationException("Not supported yet.");
            }

            public void rollback(String txId) {
              throw new UnsupportedOperationException("Not supported yet.");
            }
          });
          TransactionCompletionEvent event = new TransactionCompletionEvent(
              TransactionCompletionEvent.CompletionEvent.ROLLBACK, false, tx);
          exactly(1).of(listener).transactionComplete(with(equal(event)));
        }
      });
      final Transaction tx = injector.getInstance(Transaction.class);
      tx.addTransactionCompletionListener(listener);
      Assert.assertFalse(tx.isCompleted());
      try {
        tx.rollback();
      }
      catch (Exception ex) {
      }
      Assert.assertTrue(tx.isCompleted());
      mockery.assertIsSatisfied();
    }
  }

  @Test
  public void testRemoveListener() {
    final Injector injector = initializeInjector();
    {
      mockery.checking(new Expectations() {

        {
          exactly(1).of(service).getNextTransactionId();
          will(returnValue("1"));
          exactly(1).of(service).commit("1");

        }
      });
      final Transaction tx = injector.getInstance(Transaction.class);
      tx.addTransactionCompletionListener(listener);
      Assert.assertFalse(tx.isCompleted());
      tx.removeTransactionCompletionListener(listener);
      tx.commit();
      Assert.assertTrue(tx.isCompleted());
      mockery.assertIsSatisfied();
    }
  }

  private Injector initializeInjector() {
    final Injector injector = Guice.createInjector(new AbstractModule() {

      @Override
      protected void configure() {
        bind(TransactionService.class).toInstance(service);
        bind(Transaction.class).to(TransactionImpl.class);
      }
    });
    return injector;
  }
}
