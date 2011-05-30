/*
 *
 * This is a simple Content Management System (CMS)
 * Copyright (C) 2011  Imran M Yousuf (imyousuf@smartitengineering.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.smartitengineering.cms.binder.guice;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class CacheProvider implements Provider<Cache> {

  @Inject
  @Named("trialCacheName")
  private String trialName;
  @Inject
  @Named("defaultCache")
  private Cache defaultCache;
  @Inject
  private CacheManager manager;
  protected final transient Logger logger = LoggerFactory.getLogger(getClass());

  public CacheProvider() {
  }

  public Cache get() {
    if (logger.isInfoEnabled()) {
      logger.info("Trying for cache with name " + trialName);
    }
    if (StringUtils.isNotBlank(trialName)) {
      try {
        Cache cache = manager.getCache(trialName);
        if (cache != null) {
          if (logger.isInfoEnabled()) {
            logger.info("Return special cache with name " + trialName);
          }
          return cache;
        }
      }
      catch (Exception ex) {
        logger.warn(ex.getMessage(), ex);
      }
    }
    logger.info("Return default cache!");
    return defaultCache;
  }
}
