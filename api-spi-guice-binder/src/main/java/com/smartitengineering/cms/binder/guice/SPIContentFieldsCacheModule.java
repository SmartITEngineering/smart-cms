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
import com.smartitengineering.cms.api.content.ContentId;
import com.smartitengineering.cms.spi.impl.content.PersistentContentFields;
import com.smartitengineering.dao.common.cache.BasicKey;
import com.smartitengineering.dao.common.cache.CacheServiceProvider;
import com.smartitengineering.dao.common.cache.dao.CacheKeyGenearator;
import com.smartitengineering.dao.common.cache.dao.CacheableDao;
import com.smartitengineering.dao.common.cache.dao.impl.CacheableDaoImpl;
import com.smartitengineering.dao.common.cache.dao.impl.IdToStringCacheKeyGenerator;
import com.smartitengineering.dao.common.cache.impl.ehcache.EhcacheCacheServiceProviderImpl;
import java.util.Properties;

/**
 *
 * @author imyousuf
 */
public class SPIContentFieldsCacheModule extends PrivateModule {

  private final String prefixSeparator;

  public SPIContentFieldsCacheModule(Properties properties) {
    prefixSeparator = properties.getProperty(SPIModule.PREFIX_SEPARATOR_PROP_KEY,
                                             SPIModule.PREFIX_SEPARATOR_PROP_DEFAULT);
  }

  @Override
  protected void configure() {
    bind(new TypeLiteral<CacheServiceProvider<String, PersistentContentFields>>() {
    }).to(new TypeLiteral<EhcacheCacheServiceProviderImpl<String, PersistentContentFields>>() {
    });
    bind(new TypeLiteral<BasicKey<String>>() {
    }).toInstance(SPIModule.<String>getKeyInstance("ContentFields", prefixSeparator));
    bind(new TypeLiteral<CacheableDao<PersistentContentFields, ContentId, String>>() {
    }).to(new TypeLiteral<CacheableDaoImpl<PersistentContentFields, ContentId, String>>() {
    }).in(Singleton.class);
    bind(new TypeLiteral<CacheKeyGenearator<PersistentContentFields, ContentId, String>>() {
    }).to(new TypeLiteral<IdToStringCacheKeyGenerator<PersistentContentFields, ContentId>>() {
    }).in(Singleton.class);
    binder().expose(new TypeLiteral<CacheableDao<PersistentContentFields, ContentId, String>>() {
    });
  }
}
