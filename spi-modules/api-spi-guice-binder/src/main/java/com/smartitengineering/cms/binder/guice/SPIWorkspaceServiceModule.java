/*
 *
 * This is a simple Content Management System (CMS)
 * Copyright (C) 2010  Imran M Yousuf (imyousuf@smartitengineering.com)
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
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.smartitengineering.cms.api.impl.DomainIdInstanceProviderImpl;
import com.smartitengineering.cms.api.workspace.Workspace;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.cms.spi.impl.cache.WorkspaceServiceCacheImpl;
import com.smartitengineering.cms.spi.impl.workspace.PersistentWorkspace;
import com.smartitengineering.cms.spi.impl.workspace.WorkspaceAdapterHelper;
import com.smartitengineering.cms.spi.impl.workspace.WorkspaceObjectConverter;
import com.smartitengineering.cms.spi.impl.workspace.WorkspaceServiceImpl;
import com.smartitengineering.cms.spi.impl.workspace.guice.WorkspaceFilterConfigsProvider;
import com.smartitengineering.cms.spi.impl.workspace.guice.WorkspaceSchemaBaseConfigProvider;
import com.smartitengineering.cms.spi.workspace.WorkspaceService;
import com.smartitengineering.dao.common.CommonReadDao;
import com.smartitengineering.dao.common.CommonWriteDao;
import com.smartitengineering.dao.common.cache.BasicKey;
import com.smartitengineering.dao.common.cache.CacheServiceProvider;
import com.smartitengineering.dao.common.cache.impl.ehcache.EhcacheCacheServiceProviderImpl;
import com.smartitengineering.dao.impl.hbase.CommonDao;
import com.smartitengineering.dao.impl.hbase.spi.DomainIdInstanceProvider;
import com.smartitengineering.dao.impl.hbase.spi.FilterConfigs;
import com.smartitengineering.dao.impl.hbase.spi.LockAttainer;
import com.smartitengineering.dao.impl.hbase.spi.MergeService;
import com.smartitengineering.dao.impl.hbase.spi.ObjectRowConverter;
import com.smartitengineering.dao.impl.hbase.spi.SchemaInfoProvider;
import com.smartitengineering.dao.impl.hbase.spi.impl.DiffBasedMergeService;
import com.smartitengineering.dao.impl.hbase.spi.impl.LockAttainerImpl;
import com.smartitengineering.dao.impl.hbase.spi.impl.SchemaInfoProviderBaseConfig;
import com.smartitengineering.dao.impl.hbase.spi.impl.SchemaInfoProviderImpl;
import com.smartitengineering.util.bean.adapter.AbstractAdapterHelper;
import com.smartitengineering.util.bean.adapter.GenericAdapter;
import com.smartitengineering.util.bean.adapter.GenericAdapterImpl;
import java.util.Properties;
import net.sf.ehcache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class SPIWorkspaceServiceModule extends PrivateModule {

  private final String prefixSeparator;
  private final String cacheName;
  private final static String CACHE_NAME_PROP_KEY = "com.smartitengineering.cms.cache.ws.name";
  private final static String CACHE_NAME_DEFAULT = "workspaceCache";
  protected final transient Logger logger = LoggerFactory.getLogger(getClass());

  public SPIWorkspaceServiceModule(Properties properties) {
    prefixSeparator = properties.getProperty(SPIModule.PREFIX_SEPARATOR_PROP_KEY,
                                             SPIModule.PREFIX_SEPARATOR_PROP_DEFAULT);
    cacheName = properties.getProperty(CACHE_NAME_PROP_KEY, CACHE_NAME_DEFAULT);
    logger.info("Cache name " + cacheName);

  }

  @Override
  protected void configure() {
    bind(Boolean.class).annotatedWith(Names.named("mergeEnabled")).toInstance(Boolean.FALSE);
    bind(Integer.class).annotatedWith(Names.named("maxRows")).toInstance(new Integer(200));
    bind(DomainIdInstanceProvider.class).to(DomainIdInstanceProviderImpl.class).in(Scopes.SINGLETON);
    /*
     * Start injection specific to common dao of workspace
     */
    bind(WorkspaceService.class).annotatedWith(Names.named("primary")).to(WorkspaceServiceImpl.class).in(Singleton.class);
    bind(new TypeLiteral<ObjectRowConverter<PersistentWorkspace>>() {
    }).to(WorkspaceObjectConverter.class).in(Singleton.class);
    bind(new TypeLiteral<CommonReadDao<PersistentWorkspace, WorkspaceId>>() {
    }).to(new TypeLiteral<com.smartitengineering.dao.common.CommonDao<PersistentWorkspace, WorkspaceId>>() {
    }).in(Singleton.class);
    bind(new TypeLiteral<CommonWriteDao<PersistentWorkspace>>() {
    }).to(new TypeLiteral<com.smartitengineering.dao.common.CommonDao<PersistentWorkspace, WorkspaceId>>() {
    }).in(Singleton.class);
    bind(new TypeLiteral<com.smartitengineering.dao.common.CommonDao<PersistentWorkspace, WorkspaceId>>() {
    }).to(new TypeLiteral<CommonDao<PersistentWorkspace, WorkspaceId>>() {
    }).in(Singleton.class);
    final TypeLiteral<SchemaInfoProviderImpl<PersistentWorkspace, WorkspaceId>> wTypeLiteral =
                                                                                new TypeLiteral<SchemaInfoProviderImpl<PersistentWorkspace, WorkspaceId>>() {
    };
    bind(new TypeLiteral<Class<WorkspaceId>>() {
    }).toInstance(WorkspaceId.class);
    bind(new TypeLiteral<SchemaInfoProvider<PersistentWorkspace, WorkspaceId>>() {
    }).to(wTypeLiteral).in(Singleton.class);
    bind(new TypeLiteral<MergeService<PersistentWorkspace, WorkspaceId>>() {
    }).to(new TypeLiteral<DiffBasedMergeService<PersistentWorkspace, WorkspaceId>>() {
    });
    bind(new TypeLiteral<LockAttainer<PersistentWorkspace, WorkspaceId>>() {
    }).to(new TypeLiteral<LockAttainerImpl<PersistentWorkspace, WorkspaceId>>() {
    }).in(Scopes.SINGLETON);
    bind(new TypeLiteral<SchemaInfoProviderBaseConfig<PersistentWorkspace>>() {
    }).toProvider(WorkspaceSchemaBaseConfigProvider.class).in(Scopes.SINGLETON);
    bind(new TypeLiteral<FilterConfigs<PersistentWorkspace>>() {
    }).toProvider(WorkspaceFilterConfigsProvider.class).in(Scopes.SINGLETON);
    bind(new TypeLiteral<GenericAdapter<Workspace, PersistentWorkspace>>() {
    }).to(new TypeLiteral<GenericAdapterImpl<Workspace, PersistentWorkspace>>() {
    }).in(Scopes.SINGLETON);
    bind(new TypeLiteral<AbstractAdapterHelper<Workspace, PersistentWorkspace>>() {
    }).to(WorkspaceAdapterHelper.class).in(Scopes.SINGLETON);
    bind(new TypeLiteral<CacheServiceProvider<String, Workspace>>() {
    }).to(new TypeLiteral<EhcacheCacheServiceProviderImpl<String, Workspace>>() {
    });
    bind(new TypeLiteral<BasicKey<String>>() {
    }).toInstance(SPIModule.<String>getKeyInstance("Workspace", prefixSeparator));
    bind(WorkspaceService.class).to(WorkspaceServiceCacheImpl.class).in(Singleton.class);
    binder().expose(WorkspaceService.class);
    bind(String.class).annotatedWith(Names.named("trialCacheName")).toInstance(cacheName);
    bind(Cache.class).toProvider(CacheProvider.class);
    /*
     * End injection specific to common dao of workspace
     */
  }
}
