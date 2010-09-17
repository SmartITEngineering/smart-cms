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

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;
import com.smartitengineering.cms.api.common.MediaType;
import com.smartitengineering.cms.api.type.MutableContentType;
import com.smartitengineering.cms.spi.impl.InjectableCommonDao;
import com.smartitengineering.cms.spi.impl.type.ContentTypeAdapterHelper;
import com.smartitengineering.cms.spi.impl.type.ContentTypeObjectConverter;
import com.smartitengineering.cms.spi.impl.type.ContentTypePersistentService;
import com.smartitengineering.cms.spi.impl.type.PersistableContentType;
import com.smartitengineering.cms.spi.impl.type.XMLSchemaBasedTypeValidator;
import com.smartitengineering.cms.spi.persistence.PersistentService;
import com.smartitengineering.cms.spi.persistence.PersistentServiceRegistrar;
import com.smartitengineering.cms.spi.type.ContentTypeDefinitionParser;
import com.smartitengineering.cms.spi.type.ContentTypeDefinitionParsers;
import com.smartitengineering.cms.spi.type.PersistentContentTypeReader;
import com.smartitengineering.cms.spi.type.TypeValidator;
import com.smartitengineering.cms.spi.type.TypeValidators;
import com.smartitengineering.dao.impl.hbase.CommonDao;
import com.smartitengineering.dao.impl.hbase.spi.AsyncExecutorService;
import com.smartitengineering.dao.impl.hbase.spi.ObjectRowConverter;
import com.smartitengineering.dao.impl.hbase.spi.SchemaInfoProvider;
import com.smartitengineering.dao.impl.hbase.spi.impl.MixedExecutorServiceImpl;
import com.smartitengineering.dao.impl.hbase.spi.impl.SchemaInfoProviderImpl;
import com.smartitengineering.util.bean.adapter.GenericAdapter;
import com.smartitengineering.util.bean.adapter.GenericAdapterImpl;

public class SPIModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(AsyncExecutorService.class).to(MixedExecutorServiceImpl.class).in(Singleton.class);
    bind(new TypeLiteral<ObjectRowConverter<PersistableContentType>>() {
    }).to(ContentTypeObjectConverter.class).in(Singleton.class);
    bind(new TypeLiteral<CommonDao<PersistableContentType, String>>() {
    }).to(new TypeLiteral<InjectableCommonDao<PersistableContentType, String>>() {
    }).in(Singleton.class);
    SchemaInfoProviderImpl<PersistableContentType> contentTypeInfoProvider =
                                                   new SchemaInfoProviderImpl<PersistableContentType>();
    bind(new TypeLiteral<SchemaInfoProvider<PersistableContentType>>() {
    }).toInstance(contentTypeInfoProvider);
    GenericAdapterImpl<MutableContentType, PersistableContentType> adapter =
                                                                   new GenericAdapterImpl<MutableContentType, PersistableContentType>();
    adapter.setHelper(new ContentTypeAdapterHelper());
    bind(new TypeLiteral<GenericAdapter<MutableContentType, PersistableContentType>>() {
    }).toInstance(adapter);
    bind(Integer.class).annotatedWith(Names.named("maxRows")).toInstance(new Integer(50));
    MapBinder<MediaType, TypeValidator> validatorBinder = MapBinder.newMapBinder(binder(), MediaType.class,
                                                                                 TypeValidator.class);
    validatorBinder.addBinding(MediaType.APPLICATION_XML).to(XMLSchemaBasedTypeValidator.class);
    bind(TypeValidators.class).to(com.smartitengineering.cms.spi.impl.type.TypeValidators.class);
    MapBinder<Class, PersistentService> serviceBinder = MapBinder.newMapBinder(binder(), Class.class,
                                                                               PersistentService.class);
    serviceBinder.addBinding(MutableContentType.class).to(ContentTypePersistentService.class);
    bind(PersistentServiceRegistrar.class).to(
        com.smartitengineering.cms.spi.impl.content.PersistentServiceRegistrar.class);
    MapBinder<MediaType, ContentTypeDefinitionParser> parserBinder =
                                                      MapBinder.newMapBinder(binder(), MediaType.class,
                                                                             ContentTypeDefinitionParser.class);
    bind(ContentTypeDefinitionParsers.class).to(
        com.smartitengineering.cms.spi.impl.type.ContentTypeDefinitionParsers.class);
    bind(PersistentContentTypeReader.class).to(ContentTypePersistentService.class);
  }
}
