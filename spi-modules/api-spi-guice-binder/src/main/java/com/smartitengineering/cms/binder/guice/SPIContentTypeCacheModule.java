/*
 *
 * This is a simple Content Management System (CMS)
 * Copyright (C) 2011 Imran M Yousuf (imyousuf@smartitengineering.com)
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

import com.google.inject.PrivateModule;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.spi.impl.type.PersistentContentType;
import com.smartitengineering.dao.common.cache.BasicKey;
import com.smartitengineering.dao.common.cache.CacheServiceProvider;
import com.smartitengineering.dao.common.cache.dao.CacheKeyGenearator;
import com.smartitengineering.dao.common.cache.dao.CacheableDao;
import com.smartitengineering.dao.common.cache.dao.impl.CacheableDaoImpl;
import com.smartitengineering.dao.common.cache.dao.impl.IdToStringCacheKeyGenerator;
import com.smartitengineering.dao.common.cache.impl.ehcache.EhcacheCacheServiceProviderImpl;
import java.util.Properties;
import net.sf.ehcache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class SPIContentTypeCacheModule extends PrivateModule {

  private final String prefixSeparator;
  private final String cacheName;
  private final static String CACHE_NAME_PROP_KEY = "com.smartitengineering.cms.cache.type.name";
  private final static String CACHE_NAME_DEFAULT = "typeCache";
  protected final transient Logger logger = LoggerFactory.getLogger(getClass());

  public SPIContentTypeCacheModule(Properties properties) {
    prefixSeparator = properties.getProperty(SPIModule.PREFIX_SEPARATOR_PROP_KEY,
                                             SPIModule.PREFIX_SEPARATOR_PROP_DEFAULT);
    cacheName = properties.getProperty(CACHE_NAME_PROP_KEY, CACHE_NAME_DEFAULT);
    if (logger.isDebugEnabled()) {
      logger.debug("Cache name " + cacheName);
    }

  }

  @Override
  protected void configure() {
    bind(new TypeLiteral<CacheServiceProvider<String, PersistentContentType>>() {
    }).to(new TypeLiteral<EhcacheCacheServiceProviderImpl<String, PersistentContentType>>() {
    });
    bind(new TypeLiteral<BasicKey<String>>() {
    }).toInstance(SPIModule.<String>getKeyInstance("ContentType", prefixSeparator));
    bind(new TypeLiteral<CacheableDao<PersistentContentType, ContentTypeId, String>>() {
    }).to(new TypeLiteral<CacheableDaoImpl<PersistentContentType, ContentTypeId, String>>() {
    }).in(Singleton.class);
    bind(new TypeLiteral<CacheKeyGenearator<PersistentContentType, ContentTypeId, String>>() {
    }).to(new TypeLiteral<IdToStringCacheKeyGenerator<PersistentContentType, ContentTypeId>>() {
    }).in(Singleton.class);
    binder().expose(new TypeLiteral<CacheableDao<PersistentContentType, ContentTypeId, String>>() {
    });
    bind(String.class).annotatedWith(Names.named("trialCacheName")).toInstance(cacheName);
    bind(Cache.class).toProvider(CacheProvider.class);
  }
}
