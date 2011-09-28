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
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.smartitengineering.cms.api.common.CacheableResource;
import com.smartitengineering.cms.api.impl.type.WorkspaceResourceCacheKey;
import com.smartitengineering.dao.common.cache.BasicKey;
import com.smartitengineering.dao.common.cache.CacheServiceProvider;
import com.smartitengineering.dao.common.cache.impl.ehcache.EhcacheCacheServiceProviderImpl;
import java.util.Properties;
import net.sf.ehcache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class WorkspaceResourceCacheModule extends PrivateModule {

  private final static String CACHE_NAME_PROP_KEY = "com.smartitengineering.cms.cache.workspace.resources.name";
  private final static String CACHE_NAME_DEFAULT = "workspaceResourcesCache";
  private final String prefixSeparator;
  private final String cacheName;
  private final Logger logger = LoggerFactory.getLogger(getClass());

  public WorkspaceResourceCacheModule(Properties properties) {
    if (properties == null) {
      properties = new Properties();
    }
    prefixSeparator = properties.getProperty(SPIModule.PREFIX_SEPARATOR_PROP_KEY,
                                             SPIModule.PREFIX_SEPARATOR_PROP_DEFAULT);
    cacheName = properties.getProperty(CACHE_NAME_PROP_KEY, CACHE_NAME_DEFAULT);
    logger.info("Cache name " + cacheName);

  }

  @Override
  protected void configure() {
    final TypeLiteral<CacheServiceProvider<WorkspaceResourceCacheKey, CacheableResource>> csp =
                                                                                          new TypeLiteral<CacheServiceProvider<WorkspaceResourceCacheKey, CacheableResource>>() {
    };
    bind(csp).to(new TypeLiteral<EhcacheCacheServiceProviderImpl<WorkspaceResourceCacheKey, CacheableResource>>() {
    });
    binder().expose(csp);
    bind(new TypeLiteral<BasicKey<WorkspaceResourceCacheKey>>() {
    }).toInstance(SPIModule.<WorkspaceResourceCacheKey>getKeyInstance("WorkspaceResources", prefixSeparator));
    bind(String.class).annotatedWith(Names.named("trialCacheName")).toInstance(cacheName);
    bind(Cache.class).toProvider(CacheProvider.class);
  }
}
