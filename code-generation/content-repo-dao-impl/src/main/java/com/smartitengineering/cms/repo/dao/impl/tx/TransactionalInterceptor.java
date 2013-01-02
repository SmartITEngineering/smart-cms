package com.smartitengineering.cms.repo.dao.impl.tx;

import com.google.inject.Inject;
import com.smartitengineering.cms.repo.dao.tx.Transaction;
import com.smartitengineering.cms.repo.dao.tx.TransactionManager;
import com.smartitengineering.cms.repo.dao.tx.Transactional;
import java.lang.reflect.Method;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * Implements AOP wrapper around {@link Transactional transact annotation}.
 * @author imyousuf
 */
public class TransactionalInterceptor implements MethodInterceptor {

  @Inject
  private TransactionManager manager;

  public Object invoke(MethodInvocation invocation) throws Throwable {
    final Method method = invocation.getMethod();
    if(method == null) {
      return null;
    }
    Transactional txa = method.getAnnotation(Transactional.class);
    if (txa == null) {
      return invocation.proceed();
    }
    Transaction tx = manager.getCurrentTransaction();
    final boolean transactionStarted;
    if (tx == null || !txa.propagationRequired()) {
      tx = manager.beginTransaction(txa.isolated());
      transactionStarted = true;
    }
    else {
      transactionStarted = false;
    }
    try {
      Object obj = invocation.proceed();
      if (transactionStarted) {
        tx.commit();
      }
      return obj;
    }
    catch (Exception ex) {
      if (transactionStarted) {
        tx.rollback();
      }
      throw ex;
    }
  }
}
