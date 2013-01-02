package com.smartitengineering.cms.repo.dao.impl.tx;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.smartitengineering.cms.repo.dao.tx.Transaction;
import com.smartitengineering.cms.repo.dao.tx.TransactionException;
import com.smartitengineering.cms.repo.dao.tx.TransactionManager;
import com.smartitengineering.cms.repo.dao.tx.Transactional;
import java.lang.reflect.Method;
import org.aopalliance.intercept.MethodInvocation;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author imyousuf
 */
public class TransactionalInterceptorTest {

  public static class TransactionalSampleUse {

    @Transactional(propagationRequired = false)
    public void singleTransactionWithoutPropagation() {
    }

    @Transactional(isolated = true)
    public void singleTransactionWithIsolation() {
    }

    @Transactional(isolated = false)
    public void singleTransactionWithNonIsolation() {
    }

    @Transactional
    public void nestedTransactionWithNonPropagation() {
      singleTransactionWithoutPropagation();
    }

    @Transactional
    public void nestedTransactionWithPropagation() {
      singleTransactionWithIsolation();
    }

    public void nonTransactional() {
    }
  }
  private final Method propagtedIsolatedTransaction;
  private final Method propagtedNonIsolatedTransaction;
  private final Method propagtedIsolatedNestedTransaction;
  private final Method nonPropagtedIsolatedNestedTransaction;
  private final Method nonTransaction;
  private final TransactionalSampleUse sampleService;
  private Mockery mockery;
  private TransactionManager mockManager;
  private Injector injector;
  private TransactionalInterceptor interceptor;

  public TransactionalInterceptorTest() {
    try {
      propagtedIsolatedTransaction = TransactionalSampleUse.class.getDeclaredMethod("singleTransactionWithIsolation");
      propagtedNonIsolatedTransaction = TransactionalSampleUse.class.getDeclaredMethod(
          "singleTransactionWithNonIsolation");
      propagtedIsolatedNestedTransaction = TransactionalSampleUse.class.getDeclaredMethod(
          "nestedTransactionWithPropagation");
      nonPropagtedIsolatedNestedTransaction = TransactionalSampleUse.class.getDeclaredMethod(
          "singleTransactionWithoutPropagation");
      nonTransaction = TransactionalSampleUse.class.getDeclaredMethod("nonTransactional");
      sampleService = new TransactionalSampleUse();
    }
    catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  @Before
  public void setup() {
    mockery = new Mockery();
    mockManager = mockery.mock(TransactionManager.class);
    injector = Guice.createInjector(new AbstractModule() {

      @Override
      protected void configure() {
        bind(TransactionManager.class).toInstance(mockManager);
        bind(TransactionalInterceptor.class);
      }
    });
    interceptor = injector.getInstance(TransactionalInterceptor.class);
  }

  @Test
  public void testNullMethod() throws Throwable {
    final MethodInvocation invocation = mockery.mock(MethodInvocation.class);
    mockery.checking(new Expectations() {

      {
        exactly(1).of(invocation).getMethod();
        will(returnValue(null));
      }
    });
    interceptor.invoke(invocation);
    mockery.assertIsSatisfied();
  }

  @Test
  public void testNullAnnotation() throws Throwable {
    final MethodInvocation invocation = mockery.mock(MethodInvocation.class);
    mockery.checking(new Expectations() {

      {
        exactly(1).of(invocation).getMethod();
        will(returnValue(nonTransaction));
        exactly(1).of(invocation).proceed();
        will(returnValue(sampleService));
      }
    });
    Assert.assertSame(sampleService, interceptor.invoke(invocation));
    mockery.assertIsSatisfied();
  }

  @Test
  public void testNewTransactionWithSuccess() throws Throwable {
    final MethodInvocation invocation = mockery.mock(MethodInvocation.class);
    mockery.checking(new Expectations() {

      {
        Sequence seq = mockery.sequence("newTx");
        exactly(1).of(invocation).getMethod();
        will(returnValue(propagtedIsolatedTransaction));
        inSequence(seq);
        exactly(1).of(mockManager).getCurrentTransaction();
        will(returnValue(null));
        inSequence(seq);
        Transaction tx = mockery.mock(Transaction.class);
        exactly(1).of(mockManager).beginTransaction(true);
        will(returnValue(tx));
        inSequence(seq);
        exactly(1).of(invocation).proceed();
        will(returnValue(sampleService));
        inSequence(seq);
        exactly(1).of(tx).commit();
        inSequence(seq);
      }
    });
    Assert.assertSame(sampleService, interceptor.invoke(invocation));
    mockery.assertIsSatisfied();
  }

  @Test(expected = TransactionException.class)
  public void testNewTransactionWithError() throws Throwable {
    final MethodInvocation invocation = mockery.mock(MethodInvocation.class);
    mockery.checking(new Expectations() {

      {
        Sequence seq = mockery.sequence("newTxWithError");
        exactly(1).of(invocation).getMethod();
        will(returnValue(propagtedIsolatedTransaction));
        inSequence(seq);
        exactly(1).of(mockManager).getCurrentTransaction();
        will(returnValue(null));
        inSequence(seq);
        Transaction tx = mockery.mock(Transaction.class);
        exactly(1).of(mockManager).beginTransaction(true);
        will(returnValue(tx));
        inSequence(seq);
        exactly(1).of(invocation).proceed();
        will(throwException(new TransactionException(new NullPointerException())));
        inSequence(seq);
        exactly(1).of(tx).rollback();
        inSequence(seq);
      }
    });
    try {
      interceptor.invoke(invocation);
    }
    finally {
      mockery.assertIsSatisfied();
    }
  }

  @Test
  public void testNewNonIsolatedTransactionWithSuccess() throws Throwable {
    final MethodInvocation invocation = mockery.mock(MethodInvocation.class);
    mockery.checking(new Expectations() {

      {
        Sequence seq = mockery.sequence("newTx");
        exactly(1).of(invocation).getMethod();
        will(returnValue(propagtedNonIsolatedTransaction));
        inSequence(seq);
        exactly(1).of(mockManager).getCurrentTransaction();
        will(returnValue(null));
        inSequence(seq);
        Transaction tx = mockery.mock(Transaction.class);
        exactly(1).of(mockManager).beginTransaction(false);
        will(returnValue(tx));
        inSequence(seq);
        exactly(1).of(invocation).proceed();
        will(returnValue(sampleService));
        inSequence(seq);
        exactly(1).of(tx).commit();
        inSequence(seq);
      }
    });
    Assert.assertSame(sampleService, interceptor.invoke(invocation));
    mockery.assertIsSatisfied();
  }

  @Test
  public void testNestedTransactionWithoutPropagationWithSuccess() throws Throwable {
    final MethodInvocation invocation = mockery.mock(MethodInvocation.class);
    mockery.checking(new Expectations() {

      {
        Sequence seq = mockery.sequence("newTx");
        Transaction tx = mockery.mock(Transaction.class, "tx0");
        exactly(1).of(mockManager).beginTransaction(true);
        will(returnValue(tx));
        inSequence(seq);
        exactly(1).of(invocation).getMethod();
        will(returnValue(nonPropagtedIsolatedNestedTransaction));
        inSequence(seq);
        exactly(1).of(mockManager).getCurrentTransaction();
        will(returnValue(tx));
        inSequence(seq);
        Transaction tx1 = mockery.mock(Transaction.class, "tx1");
        exactly(1).of(mockManager).beginTransaction(true);
        will(returnValue(tx1));
        inSequence(seq);
        exactly(1).of(invocation).proceed();
        will(returnValue(sampleService));
        inSequence(seq);
        exactly(1).of(tx1).commit();
        inSequence(seq);
        exactly(1).of(tx).commit();
        inSequence(seq);
      }
    });
    TransactionManager manager = injector.getInstance(TransactionManager.class);
    Transaction tx = manager.beginTransaction(true);
    try {
      Assert.assertSame(sampleService, interceptor.invoke(invocation));
    }
    finally {
      tx.commit();
    }
    mockery.assertIsSatisfied();
  }

  @Test
  public void testNestedTransactionWithPropagationWithSuccess() throws Throwable {
    final MethodInvocation invocation = mockery.mock(MethodInvocation.class);
    mockery.checking(new Expectations() {

      {
        Sequence seq = mockery.sequence("newTx");
        Transaction tx = mockery.mock(Transaction.class, "tx0");
        exactly(1).of(mockManager).beginTransaction(true);
        will(returnValue(tx));
        inSequence(seq);
        exactly(1).of(invocation).getMethod();
        will(returnValue(propagtedIsolatedNestedTransaction));
        inSequence(seq);
        exactly(1).of(mockManager).getCurrentTransaction();
        will(returnValue(tx));
        inSequence(seq);
        exactly(1).of(invocation).proceed();
        will(returnValue(sampleService));
        inSequence(seq);
        exactly(1).of(tx).commit();
        inSequence(seq);
      }
    });
    TransactionManager manager = injector.getInstance(TransactionManager.class);
    Transaction tx = manager.beginTransaction(true);
    try {
      Assert.assertSame(sampleService, interceptor.invoke(invocation));
    }
    finally {
      tx.commit();
    }
    mockery.assertIsSatisfied();
  }
}
