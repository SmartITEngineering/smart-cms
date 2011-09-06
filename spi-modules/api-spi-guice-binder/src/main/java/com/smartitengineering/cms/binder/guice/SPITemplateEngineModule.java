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
import com.google.inject.name.Names;
import com.smartitengineering.cms.api.common.TemplateType;
import com.smartitengineering.cms.api.content.template.TypeFieldValidator;
import com.smartitengineering.cms.api.content.template.TypeRepresentationGenerator;
import com.smartitengineering.cms.api.content.template.TypeVariationGenerator;
import com.smartitengineering.cms.api.impl.content.template.GroovyRepresentationGenerator;
import com.smartitengineering.cms.api.impl.content.template.GroovyValidatorGenerator;
import com.smartitengineering.cms.api.impl.content.template.GroovyVariationGenerator;
import com.smartitengineering.cms.api.impl.content.template.JavascriptRepresentationGenerator;
import com.smartitengineering.cms.api.impl.content.template.JavascriptValidatorGenerator;
import com.smartitengineering.cms.api.impl.content.template.JavascriptVariationGenerator;
import com.smartitengineering.cms.api.impl.content.template.RubyRepresentationGenerator;
import com.smartitengineering.cms.api.impl.content.template.RubyValidatorGenerator;
import com.smartitengineering.cms.api.impl.content.template.RubyVariationGenerator;
import com.smartitengineering.cms.api.impl.content.template.VelocityRepresentationGenerator;
import com.smartitengineering.cms.api.impl.content.template.VelocityVariationGenerator;
import com.smartitengineering.cms.api.type.ValidatorType;
import com.smartitengineering.cms.spi.content.RepresentationProvider;
import com.smartitengineering.cms.spi.content.ValidatorProvider;
import com.smartitengineering.cms.spi.content.VariationProvider;
import com.smartitengineering.cms.spi.impl.content.PersistentRepresentationProviderImpl;
import com.smartitengineering.cms.spi.impl.content.PersistentVariationProviderImpl;
import com.smartitengineering.cms.spi.impl.content.RepresentationProviderImpl;
import com.smartitengineering.cms.spi.impl.content.ValidatorProviderImpl;
import com.smartitengineering.cms.spi.impl.content.VariationProviderImpl;
import com.smartitengineering.cms.spi.impl.content.guice.RepresentationFilterConfigsProvider;
import com.smartitengineering.cms.spi.impl.content.guice.RepresentationSchemaBaseConfigProvider;
import com.smartitengineering.cms.spi.impl.content.guice.VariationFilterConfigsProvider;
import com.smartitengineering.cms.spi.impl.content.guice.VariationSchemaBaseConfigProvider;
import com.smartitengineering.cms.spi.impl.content.template.persistent.PersistableResourceDomainIdProviderImpl;
import com.smartitengineering.cms.spi.impl.content.template.persistent.PersistentRepresentation;
import com.smartitengineering.cms.spi.impl.content.template.persistent.PersistentVariation;
import com.smartitengineering.cms.spi.impl.content.template.persistent.RepresentationObjectConverter;
import com.smartitengineering.cms.spi.impl.content.template.persistent.TemplateId;
import com.smartitengineering.cms.spi.impl.content.template.persistent.VariationObjectConverter;
import com.smartitengineering.dao.common.CommonReadDao;
import com.smartitengineering.dao.common.CommonWriteDao;
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

/**
 *
 * @author imyousuf
 */
public class SPITemplateEngineModule extends PrivateModule {

  @Override
  protected void configure() {
    bind(Boolean.class).annotatedWith(Names.named("mergeEnabled")).toInstance(Boolean.TRUE);
    bind(Integer.class).annotatedWith(Names.named("maxRows")).toInstance(new Integer(5));
    bind(DomainIdInstanceProvider.class).to(PersistableResourceDomainIdProviderImpl.class).in(Scopes.SINGLETON);
    /*
     * DI related to template engine
     */
    /*
     * Representation
     */
    bind(RepresentationProvider.class).to(PersistentRepresentationProviderImpl.class);
    bind(RepresentationProvider.class).annotatedWith(Names.named("mainProvider")).to(RepresentationProviderImpl.class);
    binder().expose(RepresentationProvider.class);
    MapBinder<TemplateType, TypeRepresentationGenerator> typeGenBinder =
                                                         MapBinder.newMapBinder(binder(), TemplateType.class,
                                                                                TypeRepresentationGenerator.class);
    typeGenBinder.addBinding(TemplateType.RUBY).to(RubyRepresentationGenerator.class);
    typeGenBinder.addBinding(TemplateType.GROOVY).to(GroovyRepresentationGenerator.class);
    typeGenBinder.addBinding(TemplateType.JAVASCRIPT).to(JavascriptRepresentationGenerator.class);
    typeGenBinder.addBinding(TemplateType.VELOCITY).to(VelocityRepresentationGenerator.class);
    bind(new TypeLiteral<ObjectRowConverter<PersistentRepresentation>>() {
    }).to(RepresentationObjectConverter.class).in(Singleton.class);
    bind(new TypeLiteral<CommonReadDao<PersistentRepresentation, TemplateId>>() {
    }).to(new TypeLiteral<com.smartitengineering.dao.common.CommonDao<PersistentRepresentation, TemplateId>>() {
    }).in(Singleton.class);
    bind(new TypeLiteral<CommonWriteDao<PersistentRepresentation>>() {
    }).to(new TypeLiteral<com.smartitengineering.dao.common.CommonDao<PersistentRepresentation, TemplateId>>() {
    }).in(Singleton.class);
    bind(new TypeLiteral<com.smartitengineering.dao.common.CommonDao<PersistentRepresentation, TemplateId>>() {
    }).to(new TypeLiteral<CommonDao<PersistentRepresentation, TemplateId>>() {
    }).in(Singleton.class);
    final TypeLiteral<SchemaInfoProviderImpl<PersistentRepresentation, TemplateId>> rTypeLiteral =
                                                                                    new TypeLiteral<SchemaInfoProviderImpl<PersistentRepresentation, TemplateId>>() {
    };
    bind(new TypeLiteral<MergeService<PersistentRepresentation, TemplateId>>() {
    }).to(new TypeLiteral<DiffBasedMergeService<PersistentRepresentation, TemplateId>>() {
    });
    bind(new TypeLiteral<LockAttainer<PersistentRepresentation, TemplateId>>() {
    }).to(new TypeLiteral<LockAttainerImpl<PersistentRepresentation, TemplateId>>() {
    }).in(Scopes.SINGLETON);
    bind(new TypeLiteral<Class<TemplateId>>() {
    }).toInstance(TemplateId.class);
    bind(new TypeLiteral<SchemaInfoProvider<PersistentRepresentation, TemplateId>>() {
    }).to(rTypeLiteral).in(Singleton.class);
    bind(new TypeLiteral<SchemaInfoProviderBaseConfig<PersistentRepresentation>>() {
    }).toProvider(RepresentationSchemaBaseConfigProvider.class).in(Scopes.SINGLETON);
    bind(new TypeLiteral<FilterConfigs<PersistentRepresentation>>() {
    }).toProvider(RepresentationFilterConfigsProvider.class).in(Scopes.SINGLETON);
    /*
     * Variation
     */
    bind(VariationProvider.class).to(PersistentVariationProviderImpl.class);
    bind(VariationProvider.class).annotatedWith(Names.named("mainProvider")).to(VariationProviderImpl.class);
    binder().expose(VariationProvider.class);
    MapBinder<TemplateType, TypeVariationGenerator> typeVarGenBinder =
                                                    MapBinder.newMapBinder(binder(), TemplateType.class,
                                                                           TypeVariationGenerator.class);
    typeVarGenBinder.addBinding(TemplateType.RUBY).to(RubyVariationGenerator.class);
    typeVarGenBinder.addBinding(TemplateType.GROOVY).to(GroovyVariationGenerator.class);
    typeVarGenBinder.addBinding(TemplateType.JAVASCRIPT).to(JavascriptVariationGenerator.class);
    typeVarGenBinder.addBinding(TemplateType.VELOCITY).to(VelocityVariationGenerator.class);
    bind(new TypeLiteral<ObjectRowConverter<PersistentVariation>>() {
    }).to(VariationObjectConverter.class).in(Singleton.class);
    bind(new TypeLiteral<CommonReadDao<PersistentVariation, TemplateId>>() {
    }).to(new TypeLiteral<com.smartitengineering.dao.common.CommonDao<PersistentVariation, TemplateId>>() {
    }).in(Singleton.class);
    bind(new TypeLiteral<CommonWriteDao<PersistentVariation>>() {
    }).to(new TypeLiteral<com.smartitengineering.dao.common.CommonDao<PersistentVariation, TemplateId>>() {
    }).in(Singleton.class);
    bind(new TypeLiteral<com.smartitengineering.dao.common.CommonDao<PersistentVariation, TemplateId>>() {
    }).to(new TypeLiteral<CommonDao<PersistentVariation, TemplateId>>() {
    }).in(Singleton.class);
    final TypeLiteral<SchemaInfoProviderImpl<PersistentVariation, TemplateId>> vTypeLiteral =
                                                                               new TypeLiteral<SchemaInfoProviderImpl<PersistentVariation, TemplateId>>() {
    };
    bind(new TypeLiteral<SchemaInfoProvider<PersistentVariation, TemplateId>>() {
    }).to(vTypeLiteral).in(Singleton.class);
    bind(new TypeLiteral<MergeService<PersistentVariation, TemplateId>>() {
    }).to(new TypeLiteral<DiffBasedMergeService<PersistentVariation, TemplateId>>() {
    });
    bind(new TypeLiteral<LockAttainer<PersistentVariation, TemplateId>>() {
    }).to(new TypeLiteral<LockAttainerImpl<PersistentVariation, TemplateId>>() {
    }).in(Scopes.SINGLETON);
    bind(new TypeLiteral<SchemaInfoProviderBaseConfig<PersistentVariation>>() {
    }).toProvider(VariationSchemaBaseConfigProvider.class).in(Scopes.SINGLETON);
    bind(new TypeLiteral<FilterConfigs<PersistentVariation>>() {
    }).toProvider(VariationFilterConfigsProvider.class).in(Scopes.SINGLETON);
    /*
     * Field validator
     */
    bind(ValidatorProvider.class).to(ValidatorProviderImpl.class);
    binder().expose(ValidatorProvider.class);
    MapBinder<ValidatorType, TypeFieldValidator> typeFieldValBinder =
                                                 MapBinder.newMapBinder(binder(), ValidatorType.class,
                                                                        TypeFieldValidator.class);
    typeFieldValBinder.addBinding(ValidatorType.RUBY).to(RubyValidatorGenerator.class);
    typeFieldValBinder.addBinding(ValidatorType.GROOVY).to(GroovyValidatorGenerator.class);
    typeFieldValBinder.addBinding(ValidatorType.JAVASCRIPT).to(JavascriptValidatorGenerator.class);
  }
}
