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
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;
import com.smartitengineering.cms.api.common.TemplateType;
import com.smartitengineering.cms.api.factory.content.ContentLoader;
import com.smartitengineering.cms.api.factory.event.EventRegistrar;
import com.smartitengineering.cms.api.factory.type.ContentTypeLoader;
import com.smartitengineering.cms.api.factory.workspace.WorkspaceAPI;
import com.smartitengineering.cms.api.impl.workspace.WorkspaceAPIImpl;
import com.smartitengineering.cms.api.impl.content.ContentLoaderImpl;
import com.smartitengineering.cms.api.impl.event.EventRegistrarImpl;
import com.smartitengineering.cms.api.impl.type.ContentTypeLoaderImpl;
import com.smartitengineering.cms.api.type.ValidatorType;
import com.smartitengineering.cms.spi.content.template.ContentCoProcessorGenerator;
import com.smartitengineering.cms.spi.content.template.TypeFieldValidator;
import com.smartitengineering.cms.spi.content.template.TypeRepresentationGenerator;
import com.smartitengineering.cms.spi.content.template.TypeVariationGenerator;
import com.smartitengineering.cms.spi.impl.content.template.GroovyContentCoProcessorGenerator;
import com.smartitengineering.cms.spi.impl.content.template.GroovyRepresentationGenerator;
import com.smartitengineering.cms.spi.impl.content.template.GroovyValidatorGenerator;
import com.smartitengineering.cms.spi.impl.content.template.GroovyVariationGenerator;
import com.smartitengineering.cms.spi.impl.content.template.JavascriptContentCoProcessorGenerator;
import com.smartitengineering.cms.spi.impl.content.template.JavascriptRepresentationGenerator;
import com.smartitengineering.cms.spi.impl.content.template.JavascriptValidatorGenerator;
import com.smartitengineering.cms.spi.impl.content.template.JavascriptVariationGenerator;
import com.smartitengineering.cms.spi.impl.content.template.PythonValidatorGenerator;
import com.smartitengineering.cms.spi.impl.content.template.RubyContentCoProcessorGenerator;
import com.smartitengineering.cms.spi.impl.content.template.RubyRepresentationGenerator;
import com.smartitengineering.cms.spi.impl.content.template.RubyValidatorGenerator;
import com.smartitengineering.cms.spi.impl.content.template.RubyVariationGenerator;
import com.smartitengineering.cms.spi.impl.content.template.VelocityRepresentationGenerator;
import com.smartitengineering.cms.spi.impl.content.template.VelocityVariationGenerator;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class APIModule extends AbstractModule {

  private final Properties properties;
  private final Logger logger = LoggerFactory.getLogger(getClass());

  public APIModule(Properties properties) {
    if (properties != null) {
      this.properties = properties;
    }
    else {
      this.properties = new Properties();
    }
  }

  @Override
  protected void configure() {
    bind(ContentTypeLoader.class).annotatedWith(Names.named("apiContentTypeLoader")).to(ContentTypeLoaderImpl.class);
    bind(ContentLoader.class).annotatedWith(Names.named("apiContentLoader")).to(ContentLoaderImpl.class);
    bind(WorkspaceAPI.class).annotatedWith(Names.named("apiWorkspaceApi")).to(WorkspaceAPIImpl.class);
    bind(EventRegistrar.class).annotatedWith(Names.named("apiEventRegistrar")).to(EventRegistrarImpl.class);
    final String globalNamespace = properties.getProperty("com.smartitengineering.cms.globalNamespace");
    if (logger.isDebugEnabled()) {
      logger.debug(new StringBuilder("Global Namespace ").append(globalNamespace).toString());
    }
    bind(String.class).annotatedWith(Names.named("globalNamespace")).toInstance(globalNamespace);
    MapBinder<TemplateType, ContentCoProcessorGenerator> ccpGenBinder =
                                                         MapBinder.newMapBinder(binder(), TemplateType.class,
                                                                                ContentCoProcessorGenerator.class);
    ccpGenBinder.addBinding(TemplateType.RUBY).to(RubyContentCoProcessorGenerator.class).in(Singleton.class);
    ccpGenBinder.addBinding(TemplateType.GROOVY).to(GroovyContentCoProcessorGenerator.class).in(Singleton.class);
    ccpGenBinder.addBinding(TemplateType.JAVASCRIPT).to(JavascriptContentCoProcessorGenerator.class).in(Singleton.class);
    MapBinder<TemplateType, TypeRepresentationGenerator> typeGenBinder =
                                                         MapBinder.newMapBinder(binder(), TemplateType.class,
                                                                                TypeRepresentationGenerator.class);
    typeGenBinder.addBinding(TemplateType.RUBY).to(RubyRepresentationGenerator.class);
    typeGenBinder.addBinding(TemplateType.GROOVY).to(GroovyRepresentationGenerator.class);
    typeGenBinder.addBinding(TemplateType.JAVASCRIPT).to(JavascriptRepresentationGenerator.class);
    typeGenBinder.addBinding(TemplateType.VELOCITY).to(VelocityRepresentationGenerator.class);
    MapBinder<TemplateType, TypeVariationGenerator> typeVarGenBinder =
                                                    MapBinder.newMapBinder(binder(), TemplateType.class,
                                                                           TypeVariationGenerator.class);
    typeVarGenBinder.addBinding(TemplateType.RUBY).to(RubyVariationGenerator.class);
    typeVarGenBinder.addBinding(TemplateType.GROOVY).to(GroovyVariationGenerator.class);
    typeVarGenBinder.addBinding(TemplateType.JAVASCRIPT).to(JavascriptVariationGenerator.class);
    typeVarGenBinder.addBinding(TemplateType.VELOCITY).to(VelocityVariationGenerator.class);
    MapBinder<ValidatorType, TypeFieldValidator> typeFieldValBinder =
                                                 MapBinder.newMapBinder(binder(), ValidatorType.class,
                                                                        TypeFieldValidator.class);
    typeFieldValBinder.addBinding(ValidatorType.RUBY).to(RubyValidatorGenerator.class);
    typeFieldValBinder.addBinding(ValidatorType.GROOVY).to(GroovyValidatorGenerator.class);
    typeFieldValBinder.addBinding(ValidatorType.JAVASCRIPT).to(JavascriptValidatorGenerator.class);
    typeFieldValBinder.addBinding(ValidatorType.PYTHON).to(PythonValidatorGenerator.class);

  }
}
