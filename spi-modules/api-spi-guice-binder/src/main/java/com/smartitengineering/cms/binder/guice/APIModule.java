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
import com.smartitengineering.cms.api.content.template.ContentCoProcessorGenerator;
import com.smartitengineering.cms.api.factory.content.ContentLoader;
import com.smartitengineering.cms.api.factory.event.EventRegistrar;
import com.smartitengineering.cms.api.factory.type.ContentTypeLoader;
import com.smartitengineering.cms.api.factory.workspace.WorkspaceAPI;
import com.smartitengineering.cms.api.impl.workspace.WorkspaceAPIImpl;
import com.smartitengineering.cms.api.impl.content.ContentLoaderImpl;
import com.smartitengineering.cms.api.impl.content.template.GroovyContentCoProcessorGenerator;
import com.smartitengineering.cms.api.impl.content.template.JavascriptContentCoProcessorGenerator;
import com.smartitengineering.cms.api.impl.content.template.RubyContentCoProcessorGenerator;
import com.smartitengineering.cms.api.impl.event.EventRegistrarImpl;
import com.smartitengineering.cms.api.impl.type.ContentTypeLoaderImpl;
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
  }
}
