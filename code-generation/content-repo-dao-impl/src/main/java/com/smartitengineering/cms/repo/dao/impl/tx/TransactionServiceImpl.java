package com.smartitengineering.cms.repo.dao.impl.tx;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.smartitengineering.cms.repo.dao.impl.AbstractRepositoryDomain;
import com.smartitengineering.cms.repo.dao.tx.TransactionException;
import com.smartitengineering.dao.common.CommonReadDao;
import com.smartitengineering.dao.common.CommonWriteDao;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
@Singleton
public class TransactionServiceImpl implements TransactionService {

  private final AtomicLong uniqueIdGen = new AtomicLong(Long.MIN_VALUE);
  private final TransactionInMemoryCache memCache;
  private final TransactionFactory factory;
  private final ConcurrentMap<String, Pair<CommonWriteDao<? extends AbstractRepositoryDomain>, CommonReadDao<? extends AbstractRepositoryDomain, String>>> daoCache;
  private final ConcurrentMap<String, AtomicInteger> opsCounter;
  private static final Logger LOGGER = LoggerFactory.getLogger(TransactionServiceImpl.class);

  @Inject
  public TransactionServiceImpl(TransactionInMemoryCache memCache,
                                TransactionFactory factory) {
    this.memCache = memCache;
    this.factory = factory;
    this.daoCache =
    new ConcurrentHashMap<String, Pair<CommonWriteDao<? extends AbstractRepositoryDomain>, CommonReadDao<? extends AbstractRepositoryDomain, String>>>();
    this.opsCounter = new ConcurrentHashMap<String, AtomicInteger>();
  }

  public String getNextTransactionId() {
    return String.valueOf(uniqueIdGen.incrementAndGet());
  }

  public void commit(String txId) {
    if (StringUtils.isBlank(txId)) {
      return;
    }
    List<Pair<TransactionStoreKey, TransactionStoreValue>> list = memCache.getTransactionParticipants(txId);
    if (list == null || list.isEmpty()) {
      return;
    }
    //Sort
    Collections.sort(list, new Comparator<Pair<TransactionStoreKey, TransactionStoreValue>>() {

      public int compare(Pair<TransactionStoreKey, TransactionStoreValue> o1,
                         Pair<TransactionStoreKey, TransactionStoreValue> o2) {
        return o1.getValue().getOpSequence() - o2.getValue().getOpSequence();
      }
    });
    //Commit
    Deque<Pair<TransactionStoreKey, TransactionStoreValue>> callStack =
                                                            new LinkedList<Pair<TransactionStoreKey, TransactionStoreValue>>();
    for (Pair<TransactionStoreKey, TransactionStoreValue> val : list) {
      TransactionStoreKey key = val.getKey();
      TransactionStoreValue value = val.getValue();
      try {
        final CommonWriteDao<? extends AbstractRepositoryDomain> writeDao = daoCache.get(key.getObjectType().getName()).
            getKey();
        OpState opState = value.getOpState();
        if (opState == null) {
          callStack.push(val);
          continue;
        }
        switch (opState) {
          case SAVE:
            writeDao.save(value.getCurrentState());
            break;
          case UPDATE:
            writeDao.update(value.getCurrentState());
            break;
          case DELETE:
            writeDao.delete(value.getCurrentState());
            break;
          default:
        }
        callStack.push(val);
      }
      catch (Exception ex) {
        LOGGER.warn("Exception trying to perform commit. Go for hard rollback", ex);
        rollback(callStack);
        throw new TransactionException(ex);
      }
      finally {
        completeTx(txId);
      }
    }
  }

  public void rollback(String txId) {
    completeTx(txId);
  }

  public <T extends AbstractRepositoryDomain> void save(TransactionElement<T> element) {
    String id = (String) element.getDto().getId();
    if (id == null) {
      id = UUID.randomUUID().toString();
      element.getDto().setId(id);
    }
    populateCache(element, OpState.SAVE);
  }

  public <T extends AbstractRepositoryDomain> void update(TransactionElement<T> element) {
    populateCache(element, OpState.UPDATE);
  }

  public <T extends AbstractRepositoryDomain> void delete(TransactionElement<T> element) {
    populateCache(element, OpState.DELETE);
  }

  /**
   * Cleanup all references and records of transaction id in the service implementation.
   * @param txId The transaction to complete
   */
  protected void completeTx(String txId) {
    opsCounter.remove(txId);
  }

  /**
   * A hard rollback implementation, that will actually undo what has been done. This will specially occur in case of
   * commit if the commit causes some error in the DAO implementation; e.g., validation error.
   * @param opsPerformed The stack of operations to revert.
   */
  protected void rollback(Deque<Pair<TransactionStoreKey, TransactionStoreValue>> opsPerformed) {
    if (opsPerformed == null || opsPerformed.isEmpty()) {
      return;
    }
    Pair<TransactionStoreKey, TransactionStoreValue> val = opsPerformed.pop();
    do {
      TransactionStoreKey key = val.getKey();
      TransactionStoreValue value = val.getValue();
      if (opsPerformed.isEmpty()) {
        val = null;
      }
      else {
        val = opsPerformed.pop();
      }
      try {
        final CommonWriteDao<? extends AbstractRepositoryDomain> writeDao = daoCache.get(key.getObjectType().getName()).
            getKey();
        OpState opState = value.getOpState();
        if (opState != null) {
          opState = getHardRollbackState(opState);
        }
        else {
          continue;
        }
        switch (opState) {
          case SAVE:
            writeDao.save(value.getCurrentState());
            break;
          case UPDATE:
            writeDao.update(value.getCurrentState());
            break;
          case DELETE:
            writeDao.delete(value.getCurrentState());
            break;
          default:
        }
      }
      catch (Exception ex) {
        LOGGER.warn("Exception trying to perform a hard rollback. Ignoring and continuing", ex);
      }
    }
    while (val != null);
  }

  /**
   * Populate cache by either updating it or inserting it. Also in process implement state transition of the cached
   * object.
   * @param <T> The type of domain
   * @param element The transaction element to cache
   * @param currentOpState The current operatoin state
   */
  protected <T extends AbstractRepositoryDomain> void populateCache(TransactionElement<T> element,
                                                                    OpState currentOpState) {
    fillCacheIfNeeded(element);
    int nextOpIndex = getNextOpsIndex(element.getTxId());
    TransactionStoreKey key = factory.createTransactionStoreKey();
    key.setOpTimestamp(System.currentTimeMillis());
    key.setTransactionId(element.getTxId());
    key.setObjectType(element.getDto().getClass());
    key.setObjectId((String) element.getDto().getId());
    Pair<TransactionStoreKey, TransactionStoreValue> pair = memCache.getValueForIsolatedTransaction(key);
    if (pair == null) {
      TransactionStoreValue value = factory.createTransactionStoreValue();
      value.setCurrentState(element.getDto());
      value.setOpSequence(nextOpIndex);
      value.setOpState(currentOpState);
      if (!OpState.SAVE.equals(currentOpState)) {
        value.setOriginalState(element.getReadDao().getById(key.getObjectId()));
      }
      memCache.storeTransactionValue(key, value);
    }
    else {
      TransactionStoreValue value = pair.getValue();
      value.setCurrentState(element.getDto());
      value.setOpState(getActualStateAfterTransition(value.getOpState(), currentOpState));
    }
  }

  /**
   * Implements state transition and retrieves actual state relative to earlier state and current state.
   * @param oldOpState The old state
   * @param currentOpState The current op state
   * @return The actual state, IOW, the Operation to attain when written to repository. Null means the transaction op
   *         can be ignored.
   */
  protected OpState getActualStateAfterTransition(OpState oldOpState, OpState currentOpState) {
    if (oldOpState == null) {
      if (currentOpState == null) {
        return null;
      }
      else {
        switch (currentOpState) {
          case UPDATE:
          case DELETE:
            throw new IllegalStateException("Can not update/delete a bean that does not exist");
          case SAVE:
            return OpState.SAVE;
        }
      }
    }
    else {
      if (currentOpState == null) {
        throw new IllegalStateException("Can not update/delete a bean that does not exist");
      }
      else {
        switch (oldOpState) {
          case SAVE: {
            switch (currentOpState) {
              case SAVE:
                throw new IllegalStateException(
                    "Constraint violation exception, a object with same id has already been saved");
              case DELETE:
                return null;
              case UPDATE:
              default:
                return oldOpState;
            }
          }
          case UPDATE: {
            switch (currentOpState) {
              case SAVE:
                throw new IllegalStateException("Can not save an existing bean");
              case DELETE:
                return currentOpState;
              case UPDATE:
              default:
                return oldOpState;
            }
          }
          case DELETE: {
            switch (currentOpState) {
              case SAVE:
                return OpState.UPDATE;
              case UPDATE:
              case DELETE:
              default:
                throw new IllegalStateException(
                    "Can not update/delete a bean that has been deleted earlier in the transaction");
            }
          }
        }
      }
    }
    return currentOpState;
  }

  private OpState getHardRollbackState(OpState actualState) {
    switch (actualState) {
      case SAVE:
        return OpState.DELETE;
      case DELETE:
        return OpState.SAVE;
      case UPDATE:
        return OpState.UPDATE;
      default:
        return null;
    }
  }

  private <T extends AbstractRepositoryDomain> void fillCacheIfNeeded(TransactionElement<T> element) {
    daoCache.putIfAbsent(element.getObjectType().getName(),
                         new Pair<CommonWriteDao<? extends AbstractRepositoryDomain>, CommonReadDao<? extends AbstractRepositoryDomain, String>>(
        element.getWriteDao(), element.getReadDao()));
    opsCounter.putIfAbsent(element.getTxId(), new AtomicInteger(-1));
  }

  private int getNextOpsIndex(String txId) {
    return opsCounter.get(txId).incrementAndGet();
  }
}
