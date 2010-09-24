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
package com.smartitengineering.cms.spi.impl.type;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;
import com.smartitengineering.cms.api.SmartContentAPI;
import com.smartitengineering.cms.api.WorkspaceAPI;
import com.smartitengineering.cms.api.common.MediaType;
import com.smartitengineering.cms.api.impl.PersistableDomainFactoryImpl;
import com.smartitengineering.cms.api.impl.WorkspaceAPIImpl;
import com.smartitengineering.cms.api.impl.type.ContentTypeLoaderImpl;
import com.smartitengineering.cms.api.type.ContentTypeLoader;
import com.smartitengineering.cms.spi.SmartContentSPI;
import com.smartitengineering.cms.spi.impl.type.validator.ContentTypeDefinitionParsers;
import com.smartitengineering.cms.spi.impl.type.validator.TypeValidators;
import com.smartitengineering.cms.spi.impl.type.validator.XMLSchemaBasedTypeValidator;
import com.smartitengineering.cms.spi.persistence.PersistableDomainFactory;
import com.smartitengineering.cms.spi.type.ContentTypeDefinitionParser;
import com.smartitengineering.cms.spi.type.TypeValidator;
import com.smartitengineering.cms.spi.type.XMLContentTypeDefinitionParser;
import com.smartitengineering.util.bean.guice.GuiceUtil;
import java.util.Properties;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author imyousuf
 */
public class XMLContentTypeDefnitionParserTest {

  @BeforeClass
  public static void setupAPIAndSPI() throws ClassNotFoundException {
    Properties properties = new Properties();
    properties.setProperty(GuiceUtil.CONTEXT_NAME_PROP, SmartContentAPI.CONTEXT_NAME + "," + SmartContentSPI.SPI_CONTEXT);
    properties.setProperty(GuiceUtil.IGNORE_MISSING_DEP_PROP, Boolean.toString(true));
    properties.setProperty(GuiceUtil.MODULES_LIST_PROP, TestModule.class.getName());
    GuiceUtil.getInstance(properties).register();
  }

  @Test
  public void testInjection() {
    Assert.assertNotNull(SmartContentAPI.getInstance().getContentTypeLoader());
    Assert.assertNotNull(SmartContentAPI.getInstance().getWorkspaceApi());
    Assert.assertNotNull(SmartContentSPI.getInstance().getPersistableDomainFactory());
    Assert.assertNotNull(SmartContentSPI.getInstance().getContentTypeDefinitionParsers().getParsers().get(
        MediaType.APPLICATION_XML));
    Assert.assertNotNull(
        SmartContentSPI.getInstance().getTypeValidators().getValidators().get(MediaType.APPLICATION_XML));
  }

  public static class TestModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(PersistableDomainFactory.class).to(PersistableDomainFactoryImpl.class).in(Scopes.SINGLETON);
      bind(ContentTypeLoader.class).annotatedWith(Names.named("apiContentTypeLoader")).to(ContentTypeLoaderImpl.class);
      bind(WorkspaceAPI.class).annotatedWith(Names.named("apiWorkspaceApi")).to(WorkspaceAPIImpl.class);
      MapBinder<MediaType, TypeValidator> validatorBinder = MapBinder.newMapBinder(binder(), MediaType.class,
                                                                                   TypeValidator.class);
      validatorBinder.addBinding(MediaType.APPLICATION_XML).to(XMLSchemaBasedTypeValidator.class);
      bind(com.smartitengineering.cms.spi.type.TypeValidators.class).to(TypeValidators.class);
      MapBinder<MediaType, ContentTypeDefinitionParser> parserBinder =
                                                        MapBinder.newMapBinder(binder(), MediaType.class,
                                                                               ContentTypeDefinitionParser.class);
      parserBinder.addBinding(MediaType.APPLICATION_XML).to(XMLContentTypeDefinitionParser.class);
      bind(com.smartitengineering.cms.spi.type.ContentTypeDefinitionParsers.class).to(ContentTypeDefinitionParsers.class);
    }
  }
}
