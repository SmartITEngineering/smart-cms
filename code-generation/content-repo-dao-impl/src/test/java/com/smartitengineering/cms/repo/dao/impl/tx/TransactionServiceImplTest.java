package com.smartitengineering.cms.repo.dao.impl.tx;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author imyousuf
 */
public class TransactionServiceImplTest {

  private Injector injector;

  @Before
  public void setup() {
    injector = Guice.createInjector(new DemoDomainMasterModule());
  }

  @Test
  public void testAtomicLongIncrementForMaxValue() {
    Assert.assertEquals(Long.MIN_VALUE, new AtomicLong(Long.MAX_VALUE).incrementAndGet());
  }

  @Test
  public void testConcurrentIdGeneration() {
    final TransactionService service = injector.getInstance(TransactionService.class);
    final Set<String> txIds = new ConcurrentSkipListSet<String>();
    final int threadCount = 10;
    final int loopCount = 100000;
    List<Thread> threads = new ArrayList<Thread>();
    long start = System.currentTimeMillis();
    for (int i = 0; i < threadCount; ++i) {
      Thread thread = new Thread(new Runnable() {

        public void run() {
          for (int j = 0; j < loopCount; ++j) {
            txIds.add(service.getNextTransactionId());
          }
        }
      });
      thread.start();
      threads.add(thread);
    }
    for (Thread thread : threads) {
      try {
        thread.join();
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }
    long end = System.currentTimeMillis();
    Assert.assertEquals((threadCount * loopCount), txIds.size());
    System.out.println("Time taken to generate " + (threadCount * loopCount) + " ids is " + (end - start));
  }
}
