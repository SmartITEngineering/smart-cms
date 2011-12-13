package com.smartitengineering.cms.repo.dao.impl;

import com.smartitengineering.dao.common.queryparam.QueryParameter;

/**
 *
 * @author imyousuf
 */
public interface ExtendedReadDao<T, I> {

  long count(QueryParameter... params);
}
