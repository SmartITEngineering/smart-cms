package com.smartitengineering.cms.repo.dao.impl.tx;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.smartitengineering.cms.repo.dao.impl.AbstractRepositoryDomain;
import com.smartitengineering.cms.repo.dao.tx.Transaction;
import com.smartitengineering.cms.repo.dao.tx.TransactionManager;
import com.smartitengineering.dao.common.CommonDao;
import com.smartitengineering.dao.common.CommonReadDao;
import com.smartitengineering.dao.common.CommonWriteDao;
import com.smartitengineering.dao.common.queryparam.QueryParameter;
import com.smartitengineering.domain.PersistentDTO;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Implements the Common CMS Content Repo Trasnactionable DAO.
 * @author imyousuf
 */
@Singleton
public class CommonTxDao<T extends AbstractRepositoryDomain<? extends PersistentDTO>> implements CommonDao<T, String> {

  private final CommonReadDao<T, String> readDao;
  private final CommonWriteDao<T> writeDao;
  private final TransactionService txService;
  private final TransactionManager txManager;
  private final TransactionInMemoryCache memCache;
  private final Class<T> entityClass;

  @Inject
  public CommonTxDao(@Transactionable CommonReadDao<T, String> readDao,
                     @Transactionable CommonWriteDao<T> writeDao, TransactionService txService,
                     TransactionManager txManager, TransactionInMemoryCache memCache, Class<T> entityClass) {
    this.readDao = readDao;
    this.writeDao = writeDao;
    this.txService = txService;
    this.txManager = txManager;
    this.memCache = memCache;
    this.entityClass = entityClass;
  }

  public Set<T> getByIds(List<String> ids) {
    LinkedHashSet<T> set = new LinkedHashSet<T>();
    if (ids != null) {
      for (String id : ids) {
        T dto = this.getById(id);
        set.add(dto);
      }
    }
    return set;
  }

  public T getById(String id) {
    if (isWithinTransaction()) {
      Transaction tx = txManager.getCurrentTransaction();
      T txDto = null;
      Pair<TransactionStoreKey, TransactionStoreValue> pair;
      if (tx.isIsolatedTransaction()) {
        pair = memCache.getValueForIsolatedTransaction(tx.getId(), entityClass.getName(), id);
      }
      else {
        pair = memCache.getValueForNonIsolatedTransaction(entityClass.getName(), id);
      }
      // It means the value is either deleted earlier in the transaction or has to been saved.
      if (pair != null && (pair.getValue().getOpState() == null || OpState.DELETE.equals(pair.getValue().getOpState()))) {
        return null;
      }
      if (pair == null) {
        return this.readDao.getById(id);
      }
      else {
        txDto = pair.getValue().<T>getCurrentState();
        return txDto;
      }
    }
    else {
      return this.readDao.getById(id);
    }
  }

  public T getSingle(QueryParameter... query) {
    return this.getSingle(Arrays.asList(query));
  }

  public T getSingle(List<QueryParameter> query) {
    T dto = readDao.getSingle(query);
    if (isWithinTransaction()) {
      if (dto != null) {
        return this.getById(dto.getId());
      }
      else {
        return null;
      }
    }
    else {
      return dto;
    }
  }

  public Set<T> getAll() {
    return this.readDao.getAll();
  }

  public List<T> getList(List<QueryParameter> query) {
    return this.readDao.getList(query);
  }

  public <OtherTemplate> OtherTemplate getOther(List<QueryParameter> query) {
    return this.readDao.<OtherTemplate>getOther(query);
  }

  public <OtherTemplate> List<OtherTemplate> getOtherList(List<QueryParameter> query) {
    return this.readDao.<OtherTemplate>getOtherList(query);
  }

  public List<T> getList(QueryParameter... query) {
    return this.readDao.getList(query);
  }

  public <OtherTemplate> OtherTemplate getOther(QueryParameter... query) {
    return this.readDao.<OtherTemplate>getOther(query);
  }

  public <OtherTemplate> List<OtherTemplate> getOtherList(QueryParameter... query) {
    return this.readDao.<OtherTemplate>getOtherList(query);
  }

  public void save(T... states) {
    if (states == null) {
      return;
    }
    if (isWithinTransaction()) {
      for (T state : states) {
        this.txService.save(getTxElement(state));
      }
    }
    else {
      this.writeDao.save(states);
    }
  }

  public void update(T... states) {
    if (states == null) {
      return;
    }
    if (isWithinTransaction()) {
      for (T state : states) {
        this.txService.update(getTxElement(state));
      }
    }
    else {
      this.writeDao.update(states);
    }
  }

  public void delete(T... states) {
    if (states == null) {
      return;
    }
    if (isWithinTransaction()) {
      for (T state : states) {
        this.txService.delete(getTxElement(state));
      }
    }
    else {
      this.writeDao.delete(states);
    }
  }

  private boolean isWithinTransaction() {
    return txManager.getCurrentTransaction() != null;
  }

  private TransactionElement<T> getTxElement(T dto) {
    Transaction tx = txManager.getCurrentTransaction();
    if (tx == null) {
      return null;
    }
    TransactionElement<T> element = new TransactionElement<T>();
    element.setDto(dto);
    element.setObjectType(dto.getClass());
    element.setReadDao(readDao);
    element.setWriteDao(writeDao);
    element.setTxId(tx.getId());
    return element;
  }
}
