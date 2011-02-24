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
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.smartitengineering.cms.api.common.MediaType;
import com.smartitengineering.cms.api.content.Content;
import com.smartitengineering.cms.api.content.ContentId;
import com.smartitengineering.cms.api.event.EventListener;
import com.smartitengineering.cms.api.factory.content.WriteableContent;
import com.smartitengineering.cms.api.factory.type.WritableContentType;
import com.smartitengineering.cms.api.impl.DomainIdInstanceProviderImpl;
import com.smartitengineering.cms.api.impl.PersistableDomainFactoryImpl;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.spi.content.ContentSearcher;
import com.smartitengineering.cms.spi.content.PersistentContentReader;
import com.smartitengineering.cms.spi.content.UriProvider;
import com.smartitengineering.cms.spi.impl.DefaultLockHandler;
import com.smartitengineering.cms.spi.impl.content.ContentAdapterHelper;
import com.smartitengineering.cms.spi.impl.content.ContentObjectConverter;
import com.smartitengineering.cms.spi.impl.content.ContentPersistentService;
import com.smartitengineering.cms.spi.impl.content.PersistentContent;
import com.smartitengineering.cms.spi.impl.content.guice.ContentFilterConfigsProvider;
import com.smartitengineering.cms.spi.impl.content.guice.ContentSchemaBaseConfigProvider;
import com.smartitengineering.cms.spi.impl.content.search.ContentEventConsumerImpl;
import com.smartitengineering.cms.spi.impl.content.search.ContentEventListener;
import com.smartitengineering.cms.spi.impl.content.search.ContentEventPublicationListener;
import com.smartitengineering.cms.spi.impl.content.search.ContentHelper;
import com.smartitengineering.cms.spi.impl.content.search.ContentIdentifierQueryImpl;
import com.smartitengineering.cms.spi.impl.content.search.ContentSearcherImpl;
import com.smartitengineering.cms.spi.impl.content.search.SearchFieldNameGeneratorImpl;
import com.smartitengineering.cms.spi.impl.type.ContentTypeAdapterHelper;
import com.smartitengineering.cms.spi.impl.type.ContentTypeObjectConverter;
import com.smartitengineering.cms.spi.impl.type.ContentTypePersistentService;
import com.smartitengineering.cms.spi.impl.type.guice.ContentTypeSchemaBaseConfigProvider;
import com.smartitengineering.cms.spi.impl.type.PersistentContentType;
import com.smartitengineering.cms.spi.impl.type.validator.XMLSchemaBasedTypeValidator;
import com.smartitengineering.cms.spi.impl.type.guice.ContentTypeFilterConfigsProvider;
import com.smartitengineering.cms.spi.impl.type.validator.XMLContentTypeDefinitionParser;
import com.smartitengineering.cms.spi.impl.uri.UriProviderImpl;
import com.smartitengineering.cms.spi.lock.LockHandler;
import com.smartitengineering.cms.spi.persistence.PersistableDomainFactory;
import com.smartitengineering.cms.spi.persistence.PersistentService;
import com.smartitengineering.cms.spi.persistence.PersistentServiceRegistrar;
import com.smartitengineering.cms.spi.type.ContentTypeDefinitionParser;
import com.smartitengineering.cms.spi.type.ContentTypeDefinitionParsers;
import com.smartitengineering.cms.spi.type.PersistentContentTypeReader;
import com.smartitengineering.cms.spi.type.SearchFieldNameGenerator;
import com.smartitengineering.cms.spi.type.TypeValidator;
import com.smartitengineering.cms.spi.type.TypeValidators;
import com.smartitengineering.common.dao.search.CommonFreeTextPersistentDao;
import com.smartitengineering.common.dao.search.CommonFreeTextSearchDao;
import com.smartitengineering.common.dao.search.impl.CommonAsyncFreeTextPersistentDaoImpl;
import com.smartitengineering.common.dao.search.solr.SolrFreeTextPersistentDao;
import com.smartitengineering.common.dao.search.solr.SolrFreeTextSearchDao;
import com.smartitengineering.common.dao.search.solr.spi.ObjectIdentifierQuery;
import com.smartitengineering.dao.common.CommonReadDao;
import com.smartitengineering.dao.common.CommonWriteDao;
import com.smartitengineering.dao.common.cache.BasicKey;
import com.smartitengineering.dao.common.cache.dao.CacheableDao;
import com.smartitengineering.dao.common.cache.impl.CacheAPIFactory;
import com.smartitengineering.dao.impl.hbase.CommonDao;
import com.smartitengineering.dao.impl.hbase.spi.AsyncExecutorService;
import com.smartitengineering.dao.impl.hbase.spi.DomainIdInstanceProvider;
import com.smartitengineering.dao.impl.hbase.spi.FilterConfigs;
import com.smartitengineering.dao.impl.hbase.spi.LockAttainer;
import com.smartitengineering.dao.impl.hbase.spi.MergeService;
import com.smartitengineering.dao.impl.hbase.spi.ObjectRowConverter;
import com.smartitengineering.dao.impl.hbase.spi.SchemaInfoProvider;
import com.smartitengineering.dao.impl.hbase.spi.impl.DiffBasedMergeService;
import com.smartitengineering.dao.impl.hbase.spi.impl.LockAttainerImpl;
import com.smartitengineering.dao.impl.hbase.spi.impl.MixedExecutorServiceImpl;
import com.smartitengineering.dao.impl.hbase.spi.impl.SchemaInfoProviderBaseConfig;
import com.smartitengineering.dao.impl.hbase.spi.impl.SchemaInfoProviderImpl;
import com.smartitengineering.dao.solr.MultivalueMap;
import com.smartitengineering.dao.solr.ServerConfiguration;
import com.smartitengineering.dao.solr.ServerFactory;
import com.smartitengineering.dao.solr.SolrQueryDao;
import com.smartitengineering.dao.solr.SolrWriteDao;
import com.smartitengineering.dao.solr.impl.ServerConfigurationImpl;
import com.smartitengineering.dao.solr.impl.SingletonRemoteServerFactory;
import com.smartitengineering.dao.solr.impl.SolrDao;
import com.smartitengineering.events.async.api.EventConsumer;
import com.smartitengineering.events.async.api.EventPublisher;
import com.smartitengineering.events.async.api.EventSubscriber;
import com.smartitengineering.events.async.api.UriStorer;
import com.smartitengineering.events.async.api.impl.hub.EventPublisherImpl;
import com.smartitengineering.events.async.api.impl.hub.EventSubscriberImpl;
import com.smartitengineering.events.async.api.impl.hub.FileSystemUriStorer;
import com.smartitengineering.util.bean.adapter.AbstractAdapterHelper;
import com.smartitengineering.util.bean.adapter.GenericAdapter;
import com.smartitengineering.util.bean.adapter.GenericAdapterImpl;
import com.smartitengineering.util.rest.client.ConnectionConfig;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SPIModule extends PrivateModule {

  public static final String DEFAULT_LOCATION =
                             "http://github.com/smart-it/smart-cms/raw/master/" +
      "content-api-impl/src/main/resources/com/smartitengineering/cms/content/content-type-schema.xsd";
  public static final String DEFAULT_SOLR_URI = "http://localhost:8080/solr/";
  public static final String PREFIX_SEPARATOR_PROP_KEY = "com.smartitengineering.user.cache.prefixSeparator";
  public static final String PREFIX_SEPARATOR_PROP_DEFAULT = "|";
  private final String schemaLocationForContentType;
  private final String solrUri, uriPrefix, cacheConfigRsrc, cacheName, hubUri, atomFeedUri, cronExpression;
  private final String eventHubContextPath, eventHubBaseUri;
  private final String uriStoreFolder, uriStoreFileName;
  private final long waitTime, saveInterval, updateInterval, deleteInterval;
  private final boolean enableAsyncEvent, enableEventConsumption;
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  public SPIModule(Properties properties) {
    if (logger.isInfoEnabled()) {
      logger.info("Properties received: " + properties);
    }
    if (properties != null) {
      schemaLocationForContentType = properties.getProperty("com.smartitengineering.cms.schemaLocationForContentType",
                                                            DEFAULT_LOCATION);
      solrUri = properties.getProperty("com.smartitengineering.cms.solrUri", DEFAULT_SOLR_URI);
      long toLong = NumberUtils.toLong(properties.getProperty("com.smartitengineering.cms.waitTimeInSec"), 10L);
      waitTime = toLong > 0 ? toLong : 10l;
      toLong = NumberUtils.toLong(properties.getProperty("com.smartitengineering.cms.saveIntervalInSec"), 60L);
      saveInterval = toLong > 0 ? toLong : 60l;
      toLong = NumberUtils.toLong(properties.getProperty("com.smartitengineering.cms.updateIntervalInSec"), 60L);
      updateInterval = toLong > 0 ? toLong : 60l;
      toLong = NumberUtils.toLong(properties.getProperty("com.smartitengineering.cms.deleteIntervalInSec"), 60L);
      deleteInterval = toLong > 0 ? toLong : 60l;
      uriPrefix = properties.getProperty("com.smartitengineering.cms.uriPrefix", "/cms");
      cacheConfigRsrc = properties.getProperty("com.smartitengineering.cms.cache.resource",
                                               "com/smartitengineering/cms/binder/guice/ehcache.xml");
      cacheName = properties.getProperty("com.smartitengineering.cms.cache.name", "cmsCache");
      enableAsyncEvent = Boolean.parseBoolean(properties.getProperty("com.smartitengineering.cms.event.async", "true"));
      enableEventConsumption = Boolean.parseBoolean(properties.getProperty(
          "com.smartitengineering.cms.event.async.subscribe", "true"));
      hubUri = properties.getProperty("com.smartitengineering.cms.event.hubUri",
                                      "http://localhost:10080/hub/api/channels/test/hub");
      atomFeedUri = properties.getProperty("com.smartitengineering.cms.event.atomFeedUri",
                                           "http://localhost:10080/hub/api/channels/test/events");
      cronExpression = properties.getProperty("com.smartitengineering.cms.event.consumerCronExp", "0/1 * * * * ?");
      eventHubContextPath = properties.getProperty("com.smartitengineering.cms.event.contextPath", "/hub");
      eventHubBaseUri = properties.getProperty("com.smartitengineering.cms.event.baseUri", "/api");
      uriStoreFolder = properties.getProperty("com.smartitengineering.cms.event.storeFolder", "./target/cms/");
      uriStoreFileName = properties.getProperty("com.smartitengineering.cms.event.storeFileName", "cmsPollUri.txt");
    }
    else {
      schemaLocationForContentType = DEFAULT_LOCATION;
      solrUri = DEFAULT_SOLR_URI;
      waitTime = 10l;
      saveInterval = updateInterval = deleteInterval = 60l;
      uriPrefix = "/cms";
      cacheConfigRsrc = "com/smartitengineering/cms/binder/guice/ehcache.xml";
      cacheName = "cmsCache";
      enableAsyncEvent = true;
      enableEventConsumption = true;
      hubUri = "http://localhost:10080/hub/api/channels/test/hub";
      atomFeedUri = "http://localhost:10080/hub/api/channels/test/events";
      cronExpression = "0/1 * * * * ?";
      eventHubContextPath = "/hub";
      eventHubBaseUri = "/api";
      uriStoreFolder = "./target/cms/";
      uriStoreFileName = "cmsPollUri.txt";
    }
    logger.debug("SCHEMA Location " + schemaLocationForContentType);
  }

  @Override
  protected void configure() {
    bind(AsyncExecutorService.class).to(MixedExecutorServiceImpl.class).in(Singleton.class);
    binder().expose(AsyncExecutorService.class);
    bind(ExecutorService.class).toInstance(Executors.newCachedThreadPool());
    binder().expose(ExecutorService.class);
    bind(Integer.class).annotatedWith(Names.named("maxRows")).toInstance(new Integer(100));
    bind(Long.class).annotatedWith(Names.named("waitTime")).toInstance(waitTime);
    binder().expose(Long.class).annotatedWith(Names.named("waitTime"));
    bind(TimeUnit.class).annotatedWith(Names.named("unit")).toInstance(TimeUnit.SECONDS);
    binder().expose(TimeUnit.class).annotatedWith(Names.named("unit"));
    bind(Boolean.class).annotatedWith(Names.named("mergeEnabled")).toInstance(Boolean.TRUE);
    final Named named = Names.named("schemaLocationForContentTypeXml");
    bind(String.class).annotatedWith(named).toInstance(schemaLocationForContentType);
    binder().expose(String.class).annotatedWith(named);
    bind(DomainIdInstanceProvider.class).to(DomainIdInstanceProviderImpl.class).in(Scopes.SINGLETON);
    bind(SearchFieldNameGenerator.class).to(SearchFieldNameGeneratorImpl.class);
    binder().expose(SearchFieldNameGenerator.class);

    /*
     * Solr client
     * waitTime:long and ExecutorService.class from earlier config
     */
    bind(TimeUnit.class).annotatedWith(Names.named("waitTimeUnit")).toInstance(TimeUnit.SECONDS);
    bind(SolrQueryDao.class).to(SolrDao.class).in(Scopes.SINGLETON);
    bind(SolrWriteDao.class).to(SolrDao.class).in(Scopes.SINGLETON);
    bind(ServerFactory.class).to(SingletonRemoteServerFactory.class).in(Scopes.SINGLETON);
    bind(ServerConfiguration.class).to(ServerConfigurationImpl.class).in(Scopes.SINGLETON);
    bind(String.class).annotatedWith(Names.named("uri")).toInstance(solrUri);
    bind(Long.class).annotatedWith(Names.named("saveInterval")).toInstance(saveInterval);
    bind(Long.class).annotatedWith(Names.named("updateInterval")).toInstance(updateInterval);
    bind(Long.class).annotatedWith(Names.named("deleteInterval")).toInstance(deleteInterval);
    bind(TimeUnit.class).annotatedWith(Names.named("intervalTimeUnit")).toInstance(TimeUnit.SECONDS);

    /*
     * Start injection specific to common dao of content type
     */
    bind(new TypeLiteral<ObjectRowConverter<PersistentContentType>>() {
    }).to(ContentTypeObjectConverter.class).in(Singleton.class);
    bind(new TypeLiteral<CommonReadDao<PersistentContentType, ContentTypeId>>() {
    }).annotatedWith(Names.named("primaryCacheableReadDao")).to(new TypeLiteral<com.smartitengineering.dao.common.CommonDao<PersistentContentType, ContentTypeId>>() {
    }).in(Singleton.class);
    bind(new TypeLiteral<CommonWriteDao<PersistentContentType>>() {
    }).annotatedWith(Names.named("primaryCacheableWriteDao")).to(new TypeLiteral<com.smartitengineering.dao.common.CommonDao<PersistentContentType, ContentTypeId>>() {
    }).in(Singleton.class);
    binder().expose(new TypeLiteral<CommonWriteDao<PersistentContentType>>() {
    }).annotatedWith(Names.named("primaryCacheableWriteDao"));
    binder().expose(new TypeLiteral<CommonReadDao<PersistentContentType, ContentTypeId>>() {
    }).annotatedWith(Names.named("primaryCacheableReadDao"));
    bind(new TypeLiteral<CommonWriteDao<PersistentContentType>>() {
    }).to(new TypeLiteral<CacheableDao<PersistentContentType, ContentTypeId, String>>() {
    }).in(Singleton.class);
    bind(new TypeLiteral<CommonReadDao<PersistentContentType, ContentTypeId>>() {
    }).to(new TypeLiteral<CacheableDao<PersistentContentType, ContentTypeId, String>>() {
    }).in(Singleton.class);
    bind(new TypeLiteral<com.smartitengineering.dao.common.CommonDao<PersistentContentType, ContentTypeId>>() {
    }).to(new TypeLiteral<CommonDao<PersistentContentType, ContentTypeId>>() {
    }).in(Singleton.class);
    final TypeLiteral<SchemaInfoProviderImpl<PersistentContentType, ContentTypeId>> typeLiteral = new TypeLiteral<SchemaInfoProviderImpl<PersistentContentType, ContentTypeId>>() {
    };
    bind(new TypeLiteral<MergeService<PersistentContentType, ContentTypeId>>() {
    }).to(new TypeLiteral<DiffBasedMergeService<PersistentContentType, ContentTypeId>>() {
    });
    bind(new TypeLiteral<LockAttainer<PersistentContentType, ContentTypeId>>() {
    }).to(new TypeLiteral<LockAttainerImpl<PersistentContentType, ContentTypeId>>() {
    }).in(Scopes.SINGLETON);
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
    /*
     * Start injection specific to common dao of content
     */

    /*
     * Write Dao
     */
    bind(new TypeLiteral<CommonWriteDao<PersistentContent>>() {
    }).annotatedWith(Names.named("primaryCacheableWriteDao")).to(new TypeLiteral<CommonDao<PersistentContent, ContentId>>() {
    }).in(Singleton.class);
    binder().expose(new TypeLiteral<CommonWriteDao<PersistentContent>>() {
    }).annotatedWith(Names.named("primaryCacheableWriteDao"));
    bind(new TypeLiteral<CommonWriteDao<PersistentContent>>() {
    }).to(new TypeLiteral<CacheableDao<PersistentContent, ContentId, String>>() {
    }).in(Singleton.class);
    TypeLiteral<CommonFreeTextPersistentDao<Content>> prodLit =
                                                      new TypeLiteral<CommonFreeTextPersistentDao<Content>>() {
    };
    if (enableAsyncEvent && enableEventConsumption) {
      bind(prodLit).to(new TypeLiteral<SolrFreeTextPersistentDao<Content>>() {
      }).in(Scopes.SINGLETON);
      ConnectionConfig config = new ConnectionConfig();
      config.setBasicUri(eventHubBaseUri);
      config.setContextPath(eventHubContextPath);
      URI hub = URI.create(this.hubUri);
      config.setHost(hub.getHost());
      config.setPort(hub.getPort());
      bind(ConnectionConfig.class).toInstance(config);
      bind(UriStorer.class).to(FileSystemUriStorer.class);
      bind(String.class).annotatedWith(Names.named("pathToFolderOfUriStorer")).toInstance(uriStoreFolder);
      bind(String.class).annotatedWith(Names.named("fileNameOfUriStorer")).toInstance(uriStoreFileName);
      bind(EventSubscriber.class).to(EventSubscriberImpl.class);
      Multibinder<EventConsumer> listenerBinder = Multibinder.newSetBinder(binder(), new TypeLiteral<EventConsumer>() {
      });
      listenerBinder.addBinding().to(ContentEventConsumerImpl.class);
      bind(new TypeLiteral<Collection<EventConsumer>>() {
      }).to(new TypeLiteral<Set<EventConsumer>>() {
      });
    }
    else {
      bind(prodLit).to(new TypeLiteral<CommonAsyncFreeTextPersistentDaoImpl<Content>>() {
      }).in(Scopes.SINGLETON);
      bind(prodLit).annotatedWith(Names.named("primaryFreeTextPersistentDao")).to(new TypeLiteral<SolrFreeTextPersistentDao<Content>>() {
      }).in(Scopes.SINGLETON);
    }
    if (enableAsyncEvent) {
      Multibinder<EventListener> listenerBinder = Multibinder.newSetBinder(binder(), new TypeLiteral<EventListener>() {
      });
      listenerBinder.addBinding().to(ContentEventPublicationListener.class);
      bind(new TypeLiteral<Collection<EventListener>>() {
      }).to(new TypeLiteral<Set<EventListener>>() {
      });
      binder().expose(new TypeLiteral<Collection<EventListener>>() {
      });
      bind(String.class).annotatedWith(Names.named("channelHubUri")).toInstance(hubUri);
      bind(String.class).annotatedWith(Names.named("eventAtomFeedUri")).toInstance(atomFeedUri);
      bind(String.class).annotatedWith(Names.named("subscribtionCronExpression")).toInstance(cronExpression);
      bind(EventPublisher.class).to(EventPublisherImpl.class);
    }
    else {
      Multibinder<EventListener> listenerBinder = Multibinder.newSetBinder(binder(), new TypeLiteral<EventListener>() {
      });
      listenerBinder.addBinding().to(ContentEventListener.class);
      bind(new TypeLiteral<Collection<EventListener>>() {
      }).to(new TypeLiteral<Set<EventListener>>() {
      });
      binder().expose(new TypeLiteral<Collection<EventListener>>() {
      });
    }
    bind(new TypeLiteral<ObjectIdentifierQuery<Content>>() {
    }).to(ContentIdentifierQueryImpl.class).in(Scopes.SINGLETON);
    bind(new TypeLiteral<GenericAdapter<Content, MultivalueMap<String, Object>>>() {
    }).to(new TypeLiteral<GenericAdapterImpl<Content, MultivalueMap<String, Object>>>() {
    }).in(Scopes.SINGLETON);
    bind(new TypeLiteral<AbstractAdapterHelper<Content, MultivalueMap<String, Object>>>() {
    }).to(ContentHelper.class).in(Scopes.SINGLETON);
    bind(new TypeLiteral<CommonFreeTextSearchDao<Content>>() {
    }).to(new TypeLiteral<SolrFreeTextSearchDao<Content>>() {
    }).in(Scopes.SINGLETON);
    bind(ContentSearcher.class).to(ContentSearcherImpl.class).in(Scopes.SINGLETON);
    binder().expose(ContentSearcher.class);

    bind(new TypeLiteral<ObjectRowConverter<PersistentContent>>() {
    }).to(ContentObjectConverter.class).in(Singleton.class);
    bind(new TypeLiteral<CommonReadDao<PersistentContent, ContentId>>() {
    }).annotatedWith(Names.named("primaryCacheableReadDao")).to(new TypeLiteral<com.smartitengineering.dao.common.CommonDao<PersistentContent, ContentId>>() {
    }).in(Singleton.class);
    binder().expose(new TypeLiteral<CommonReadDao<PersistentContent, ContentId>>() {
    }).annotatedWith(Names.named("primaryCacheableReadDao"));
    bind(new TypeLiteral<CommonReadDao<PersistentContent, ContentId>>() {
    }).to(new TypeLiteral<CacheableDao<PersistentContent, ContentId, String>>() {
    }).in(Singleton.class);
    bind(new TypeLiteral<com.smartitengineering.dao.common.CommonDao<PersistentContent, ContentId>>() {
    }).to(new TypeLiteral<CommonDao<PersistentContent, ContentId>>() {
    }).in(Singleton.class);
    bind(new TypeLiteral<Class<ContentId>>() {
    }).toInstance(ContentId.class);
    final TypeLiteral<SchemaInfoProvider<PersistentContent, ContentId>> contentSchema =
                                                                        new TypeLiteral<SchemaInfoProvider<PersistentContent, ContentId>>() {
    };
    bind(contentSchema).to(new TypeLiteral<SchemaInfoProviderImpl<PersistentContent, ContentId>>() {
    }).in(Singleton.class);
    bind(new TypeLiteral<MergeService<PersistentContent, ContentId>>() {
    }).to(new TypeLiteral<DiffBasedMergeService<PersistentContent, ContentId>>() {
    });
    bind(new TypeLiteral<LockAttainer<PersistentContent, ContentId>>() {
    }).to(new TypeLiteral<LockAttainerImpl<PersistentContent, ContentId>>() {
    }).in(Scopes.SINGLETON);
    binder().expose(contentSchema);
    bind(new TypeLiteral<SchemaInfoProviderBaseConfig<PersistentContent>>() {
    }).toProvider(ContentSchemaBaseConfigProvider.class).in(Scopes.SINGLETON);
    bind(new TypeLiteral<FilterConfigs<PersistentContent>>() {
    }).toProvider(ContentFilterConfigsProvider.class).in(Scopes.SINGLETON);
    bind(new TypeLiteral<GenericAdapter<WriteableContent, PersistentContent>>() {
    }).to(new TypeLiteral<GenericAdapterImpl<WriteableContent, PersistentContent>>() {
    }).in(Scopes.SINGLETON);
    bind(new TypeLiteral<AbstractAdapterHelper<WriteableContent, PersistentContent>>() {
    }).to(ContentAdapterHelper.class).in(Scopes.SINGLETON);
    bind(PersistentContentReader.class).to(ContentPersistentService.class);
    binder().expose(PersistentContentReader.class);
    /*
     * End injection specific to common dao of content
     */
    MapBinder<MediaType, TypeValidator> validatorBinder = MapBinder.newMapBinder(binder(), MediaType.class,
                                                                                 TypeValidator.class);
    validatorBinder.addBinding(MediaType.APPLICATION_XML).to(XMLSchemaBasedTypeValidator.class);
    bind(TypeValidators.class).to(com.smartitengineering.cms.spi.impl.type.validator.TypeValidators.class);
    binder().expose(TypeValidators.class);
    MapBinder<Class, PersistentService> serviceBinder = MapBinder.newMapBinder(binder(), Class.class,
                                                                               PersistentService.class);
    serviceBinder.addBinding(WritableContentType.class).to(ContentTypePersistentService.class);
    serviceBinder.addBinding(WriteableContent.class).to(ContentPersistentService.class);
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
    binder().expose(ContentTypeDefinitionParsers.class);
    binder().expose(LockHandler.class);
    binder().expose(PersistableDomainFactory.class);
    bind(UriProvider.class).to(UriProviderImpl.class);
    bind(URI.class).annotatedWith(Names.named("cmsBaseUri")).toInstance(URI.create(uriPrefix));
    binder().expose(UriProvider.class);
    /*
     * Configure Cache
     */
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream(cacheConfigRsrc);
    if (inputStream == null) {
      throw new IllegalArgumentException("Cache configuration not available!");
    }
    CacheManager cacheManager = new CacheManager(inputStream);
    Cache cache = cacheManager.getCache(cacheName);
    if (cache == null) {
      throw new IllegalStateException("Could not retrieve cache!");
    }
    bind(Cache.class).toInstance(cache);
    binder().expose(Cache.class);
  }

  static <T extends Serializable> BasicKey<T> getKeyInstance(String keyPrefix, String prefixSeparator) {
    return CacheAPIFactory.<T>getBasicKey(keyPrefix, prefixSeparator);
  }
}
