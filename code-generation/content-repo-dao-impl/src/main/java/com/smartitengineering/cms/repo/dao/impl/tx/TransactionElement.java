package com.smartitengineering.cms.repo.dao.impl.tx;

import com.smartitengineering.cms.repo.dao.impl.AbstractRepositoryDomain;
import com.smartitengineering.dao.common.CommonReadDao;
import com.smartitengineering.dao.common.CommonWriteDao;

/**
 *
 * @author imyousuf
 */
public class TransactionElement<T extends AbstractRepositoryDomain> {

  private Class<T> objectType;
  private CommonWriteDao<T> writeDao;
  private CommonReadDao<T, String> readDao;
  private String txId;
  private T dto;

  public T getDto() {
    return dto;
  }

  public void setDto(T dto) {
    this.dto = dto;
  }

  public Class<T> getObjectType() {
    return objectType;
  }

  public void setObjectType(Class objectType) {
    this.objectType = objectType;
  }

  public CommonReadDao<T, String> getReadDao() {
    return readDao;
  }

  public void setReadDao(CommonReadDao<T, String> readDao) {
    this.readDao = readDao;
  }

  public String getTxId() {
    return txId;
  }

  public void setTxId(String txId) {
    this.txId = txId;
  }

  public CommonWriteDao<T> getWriteDao() {
    return writeDao;
  }

  public void setWriteDao(CommonWriteDao<T> writeDao) {
    this.writeDao = writeDao;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final TransactionElement<T> other =
                                (TransactionElement<T>) obj;
    if (this.objectType != other.objectType && (this.objectType == null || !this.objectType.equals(other.objectType))) {
      return false;
    }
    if (this.writeDao != other.writeDao && (this.writeDao == null || !this.writeDao.equals(other.writeDao))) {
      return false;
    }
    if (this.readDao != other.readDao && (this.readDao == null || !this.readDao.equals(other.readDao))) {
      return false;
    }
    if ((this.txId == null) ? (other.txId != null) : !this.txId.equals(other.txId)) {
      return false;
    }
    if (this.dto != other.dto && (this.dto == null || !this.dto.equals(other.dto))) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 47 * hash + (this.objectType != null ? this.objectType.hashCode() : 0);
    hash = 47 * hash + (this.writeDao != null ? this.writeDao.hashCode() : 0);
    hash = 47 * hash + (this.readDao != null ? this.readDao.hashCode() : 0);
    hash = 47 * hash + (this.txId != null ? this.txId.hashCode() : 0);
    hash = 47 * hash + (this.dto != null ? this.dto.hashCode() : 0);
    return hash;
  }
}
