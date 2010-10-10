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
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.smartitengineering.cms.api.common.MediaType;
import com.smartitengineering.cms.api.factory.type.WritableContentType;
import com.smartitengineering.cms.api.impl.DomainIdInstanceProviderImpl;
import com.smartitengineering.cms.api.impl.PersistableDomainFactoryImpl;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.spi.impl.DefaultLockHandler;
import com.smartitengineering.cms.spi.impl.type.ContentTypeAdapterHelper;
import com.smartitengineering.cms.spi.impl.type.ContentTypeObjectConverter;
import com.smartitengineering.cms.spi.impl.type.ContentTypePersistentService;
import com.smartitengineering.cms.spi.impl.type.guice.ContentTypeSchemaBaseConfigProvider;
import com.smartitengineering.cms.spi.impl.type.PersistentContentType;
import com.smartitengineering.cms.spi.impl.type.validator.XMLSchemaBasedTypeValidator;
import com.smartitengineering.cms.spi.impl.type.guice.ContentTypeFilterConfigsProvider;
import com.smartitengineering.cms.spi.impl.type.validator.XMLContentTypeDefinitionParser;
import com.smartitengineering.cms.spi.lock.LockHandler;
import com.smartitengineering.cms.spi.persistence.PersistableDomainFactory;
import com.smartitengineering.cms.spi.persistence.PersistentService;
import com.smartitengineering.cms.spi.persistence.PersistentServiceRegistrar;
import com.smartitengineering.cms.spi.type.ContentTypeDefinitionParser;
import com.smartitengineering.cms.spi.type.ContentTypeDefinitionParsers;
import com.smartitengineering.cms.spi.type.PersistentContentTypeReader;
import com.smartitengineering.cms.spi.type.TypeValidator;
import com.smartitengineering.cms.spi.type.TypeValidators;
import com.smartitengineering.dao.common.CommonReadDao;
import com.smartitengineering.dao.common.CommonWriteDao;
import com.smartitengineering.dao.impl.hbase.CommonDao;
import com.smartitengineering.dao.impl.hbase.spi.AsyncExecutorService;
import com.smartitengineering.dao.impl.hbase.spi.DomainIdInstanceProvider;
import com.smartitengineering.dao.impl.hbase.spi.FilterConfigs;
import com.smartitengineering.dao.impl.hbase.spi.MergeService;
import com.smartitengineering.dao.impl.hbase.spi.ObjectRowConverter;
import com.smartitengineering.dao.impl.hbase.spi.SchemaInfoProvider;
import com.smartitengineering.dao.impl.hbase.spi.impl.DiffBasedMergeService;
import com.smartitengineering.dao.impl.hbase.spi.impl.MixedExecutorServiceImpl;
import com.smartitengineering.dao.impl.hbase.spi.impl.SchemaInfoProviderBaseConfig;
import com.smartitengineering.dao.impl.hbase.spi.impl.SchemaInfoProviderImpl;
import com.smartitengineering.util.bean.adapter.AbstractAdapterHelper;
import com.smartitengineering.util.bean.adapter.GenericAdapter;
import com.smartitengineering.util.bean.adapter.GenericAdapterImpl;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SPIModule extends PrivateModule {

  public static final String DEFAULT_LOCATION =
                             "http://github.com/smart-it/smart-cms/raw/master/" +
      "content-api-impl/src/main/resources/com/smartitengineering/cms/content/content-type-schema.xsd";
  private final String schemaLocationForContentType;
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  public SPIModule(Properties properties) {
    if (properties != null) {
      schemaLocationForContentType = properties.getProperty("com.smartitengineering.cms.schemaLocationForContentType",
                                                            DEFAULT_LOCATION);
    }
    else {
      schemaLocationForContentType = DEFAULT_LOCATION;
    }
    logger.debug("SCHEMA Location " + schemaLocationForContentType);
  }

  @Override
  protected void configure() {
    bind(AsyncExecutorService.class).to(MixedExecutorServiceImpl.class).in(Singleton.class);
    binder().expose(AsyncExecutorService.class);
    bind(ExecutorService.class).toInstance(Executors.newCachedThreadPool());
    binder().expose(ExecutorService.class);
    bind(Integer.class).annotatedWith(Names.named("maxRows")).toInstance(new Integer(50));
    bind(Long.class).annotatedWith(Names.named("waitTime")).toInstance(new Long(10));
    binder().expose(Long.class).annotatedWith(Names.named("waitTime"));
    bind(TimeUnit.class).annotatedWith(Names.named("unit")).toInstance(TimeUnit.SECONDS);
    binder().expose(TimeUnit.class).annotatedWith(Names.named("unit"));
    bind(Boolean.class).annotatedWith(Names.named("mergeEnabled")).toInstance(Boolean.TRUE);
    bind(MergeService.class).to(DiffBasedMergeService.class).in(Singleton.class);
    binder().expose(MergeService.class);
    final Named named = Names.named("schemaLocationForContentTypeXml");
    bind(String.class).annotatedWith(named).toInstance(schemaLocationForContentType);
    binder().expose(String.class).annotatedWith(named);
    /*
     * Start injection specific to common dao of content type
     */
    bind(new TypeLiteral<ObjectRowConverter<PersistentContentType>>() {
    }).to(ContentTypeObjectConverter.class).in(Singleton.class);
    bind(new TypeLiteral<CommonReadDao<PersistentContentType, ContentTypeId>>() {
    }).to(new TypeLiteral<com.smartitengineering.dao.common.CommonDao<PersistentContentType, ContentTypeId>>() {
    }).in(Singleton.class);
    bind(new TypeLiteral<CommonWriteDao<PersistentContentType>>() {
    }).to(new TypeLiteral<com.smartitengineering.dao.common.CommonDao<PersistentContentType, ContentTypeId>>() {
    }).in(Singleton.class);
    bind(new TypeLiteral<com.smartitengineering.dao.common.CommonDao<PersistentContentType, ContentTypeId>>() {
    }).to(new TypeLiteral<CommonDao<PersistentContentType, ContentTypeId>>() {
    }).in(Singleton.class);
    final TypeLiteral<SchemaInfoProviderImpl<PersistentContentType, ContentTypeId>> typeLiteral = new TypeLiteral<SchemaInfoProviderImpl<PersistentContentType, ContentTypeId>>() {
    };
    bind(new TypeLiteral<Class<ContentTypeId>>() {
    }).toInstance(ContentTypeId.class);
    bind(new TypeLiteral<SchemaInfoProvider<PersistentContentType, ContentTypeId>>() {
    }).to(typeLiteral).in(Singleton.class);
    bind(new TypeLiteral<SchemaInfoProviderBaseConfig<PersistentContentType>>() {
    }).toProvider(ContentTypeSchemaBaseConfigProvider.class).in(Scopes.SINGLETON);
    bind(new TypeLiteral<FilterConfigs<PersistentContentType>>() {
    }).toProvider(ContentTypeFilterConfigsProvider.class).in(Scopes.SINGLETON);
    bind(new TypeLiteral<GenericAdapter<WritableContentType, PersistentContentType>>() {
    }).to(new TypeLiteral<GenericAdapterImpl<WritableContentType, PersistentContentType>>() {
    }).in(Scopes.SINGLETON);
    bind(new TypeLiteral<AbstractAdapterHelper<WritableContentType, PersistentContentType>>() {
    }).to(ContentTypeAdapterHelper.class).in(Scopes.SINGLETON);
    bind(PersistentContentTypeReader.class).to(ContentTypePersistentService.class);
    binder().expose(PersistentContentTypeReader.class);
    /*
     * End injection specific to common dao of content type
     */
    MapBinder<MediaType, TypeValidator> validatorBinder = MapBinder.newMapBinder(binder(), MediaType.class,
                                                                                 TypeValidator.class);
    validatorBinder.addBinding(MediaType.APPLICATION_XML).to(XMLSchemaBasedTypeValidator.class);
    bind(TypeValidators.class).to(com.smartitengineering.cms.spi.impl.type.validator.TypeValidators.class);
    binder().expose(TypeValidators.class);
    MapBinder<Class, PersistentService> serviceBinder = MapBinder.newMapBinder(binder(), Class.class,
                                                                               PersistentService.class);
    serviceBinder.addBinding(WritableContentType.class).to(ContentTypePersistentService.class);
    bind(PersistentServiceRegistrar.class).to(
        com.smartitengineering.cms.spi.impl.PersistentServiceRegistrar.class);
    binder().expose(PersistentServiceRegistrar.class);
    MapBinder<MediaType, ContentTypeDefinitionParser> parserBinder =
                                                      MapBinder.newMapBinder(binder(), MediaType.class,
                                                                             ContentTypeDefinitionParser.class);
    parserBinder.addBinding(MediaType.APPLICATION_XML).to(XMLContentTypeDefinitionParser.class);
    bind(ContentTypeDefinitionParsers.class).to(
        com.smartitengineering.cms.spi.impl.type.validator.ContentTypeDefinitionParsers.class);
    bind(LockHandler.class).to(DefaultLockHandler.class).in(Scopes.SINGLETON);
    bind(PersistableDomainFactory.class).to(PersistableDomainFactoryImpl.class).in(Scopes.SINGLETON);
    bind(DomainIdInstanceProvider.class).to(DomainIdInstanceProviderImpl.class).in(Scopes.SINGLETON);
    binder().expose(ContentTypeDefinitionParsers.class);
    binder().expose(LockHandler.class);
    binder().expose(PersistableDomainFactory.class);
    binder().expose(DomainIdInstanceProvider.class);
  }
}
