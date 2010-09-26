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
import com.smartitengineering.cms.api.workspace.WorkspaceAPI;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.cms.api.common.MediaType;
import com.smartitengineering.cms.api.impl.PersistableDomainFactoryImpl;
import com.smartitengineering.cms.api.impl.workspace.WorkspaceAPIImpl;
import com.smartitengineering.cms.api.impl.type.ContentTypeLoaderImpl;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.api.type.ContentTypeLoader;
import com.smartitengineering.cms.api.type.MutableContentType;
import com.smartitengineering.cms.spi.SmartContentSPI;
import com.smartitengineering.cms.spi.impl.content.PersistentServiceRegistrar;
import com.smartitengineering.cms.spi.impl.type.validator.ContentTypeDefinitionParsers;
import com.smartitengineering.cms.spi.impl.type.validator.TypeValidators;
import com.smartitengineering.cms.spi.impl.type.validator.XMLContentTypeDefinitionParser;
import com.smartitengineering.cms.spi.impl.type.validator.XMLSchemaBasedTypeValidator;
import com.smartitengineering.cms.spi.lock.Key;
import com.smartitengineering.cms.spi.lock.LockHandler;
import com.smartitengineering.cms.spi.persistence.PersistableDomainFactory;
import com.smartitengineering.cms.spi.persistence.PersistentService;
import com.smartitengineering.cms.spi.type.ContentTypeDefinitionParser;
import com.smartitengineering.cms.spi.type.PersistentContentTypeReader;
import com.smartitengineering.cms.spi.type.TypeValidator;
import com.smartitengineering.util.bean.guice.GuiceUtil;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class XMLContentTypeDefnitionParserTest {

  private Logger logger = LoggerFactory.getLogger(getClass());
  public static WorkspaceId TEST_WS_ID;

  @BeforeClass
  public static void setupAPIAndSPI() throws ClassNotFoundException {
    Properties properties = new Properties();
    properties.setProperty(GuiceUtil.CONTEXT_NAME_PROP, SmartContentAPI.CONTEXT_NAME + "," + SmartContentSPI.SPI_CONTEXT);
    properties.setProperty(GuiceUtil.IGNORE_MISSING_DEP_PROP, Boolean.toString(true));
    properties.setProperty(GuiceUtil.MODULES_LIST_PROP, TestModule.class.getName());
    GuiceUtil.getInstance(properties).register();
    TEST_WS_ID = SmartContentAPI.getInstance().getWorkspaceApi().createWorkspaceId("test");
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

  @Test
  public void testParsing() {
    logger.debug(":::::::::::::::::::::Starting My First Test::::::::::::::::::::::::::::");
    try {
      InputStream inputStream = getClass().getClassLoader().getResourceAsStream("content-type-def-1.xml");
      Assert.assertNotNull(inputStream);
      XMLContentTypeDefinitionParser parser = new XMLContentTypeDefinitionParser();
      Collection collection = parser.parseStream(TEST_WS_ID, inputStream);
      Assert.assertNotNull(collection);
      Assert.assertFalse(collection.isEmpty());
      Assert.assertEquals(2, collection.size());
      inputStream = getClass().getClassLoader().getResourceAsStream("content-type-def-1.xml");
      init();
    }
    catch (Exception e) {
      logger.error(e.getMessage(), e);
      Assert.fail("Should not get exceptions");
    }
    logger.debug(":::::::::::::::::::::Ending My First Test::::::::::::::::::::::::::::");
  }

  @Test
  public void testParsingContentId() throws Exception {
    Collection<MutableContentType> collection = init();
    Iterator<MutableContentType> iterator = collection.iterator();
    MutableContentType contentType = iterator.next();
    Assert.assertEquals(SmartContentAPI.getInstance().getContentTypeLoader().createContentTypeId(TEST_WS_ID, "asdfasdf1",
                                                                                                 "XYZ"), contentType.
        getContentTypeID());
    contentType = iterator.next();
    Assert.assertEquals(SmartContentAPI.getInstance().getContentTypeLoader().createContentTypeId(TEST_WS_ID, "jpeg",
                                                                                                 "RST"), contentType.
        getContentTypeID());
  }

  protected Collection<MutableContentType> init() throws NullPointerException, IOException {
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream("content-type-def-1.xml");
    Collection<MutableContentType> collection;
    collection =
    SmartContentAPI.getInstance().getContentTypeLoader().
        parseContentTypes(TEST_WS_ID, inputStream, MediaType.APPLICATION_XML);
    Assert.assertNotNull(collection);
    Assert.assertFalse(collection.isEmpty());
    Assert.assertEquals(2, collection.size());
    return collection;
  }

  public static class TestModule extends AbstractModule {

    private Mockery mockery = new Mockery();

    @Override
    protected void configure() {
      bind(PersistableDomainFactory.class).to(PersistableDomainFactoryImpl.class).in(Scopes.SINGLETON);
      bind(ContentTypeLoader.class).annotatedWith(Names.named("apiContentTypeLoader")).to(ContentTypeLoaderImpl.class);
      bind(WorkspaceAPI.class).annotatedWith(Names.named("apiWorkspaceApi")).to(WorkspaceAPIImpl.class);
      bind(String.class).annotatedWith(Names.named("globalNamespace")).toInstance("testWS");
      MapBinder<MediaType, TypeValidator> validatorBinder = MapBinder.newMapBinder(binder(), MediaType.class,
                                                                                   TypeValidator.class);
      validatorBinder.addBinding(MediaType.APPLICATION_XML).to(XMLSchemaBasedTypeValidator.class);
      bind(com.smartitengineering.cms.spi.type.TypeValidators.class).to(TypeValidators.class);
      MapBinder<MediaType, ContentTypeDefinitionParser> parserBinder =
                                                        MapBinder.newMapBinder(binder(), MediaType.class,
                                                                               ContentTypeDefinitionParser.class);
      parserBinder.addBinding(MediaType.APPLICATION_XML).to(XMLContentTypeDefinitionParser.class);
      bind(com.smartitengineering.cms.spi.type.ContentTypeDefinitionParsers.class).to(ContentTypeDefinitionParsers.class);
      final LockHandler handler = mockery.mock(LockHandler.class);
      bind(LockHandler.class).toInstance(handler);
      final PersistentContentTypeReader mockReader = mockery.mock(PersistentContentTypeReader.class);
      bind(PersistentContentTypeReader.class).toInstance(mockReader);
      mockery.checking(new Expectations() {

        {
          allowing(handler).register(with(any(Key.class)));
          allowing(mockReader).readContentTypeFromPersistentStorage(with(this.<ContentTypeId[]>anything()));
          will(returnValue(Collections.emptyList()));
        }
      });
      MapBinder<Class, PersistentService> serviceBinder = MapBinder.newMapBinder(binder(), Class.class,
                                                                                 PersistentService.class);
      serviceBinder.addBinding(MutableContentType.class).toInstance(mockery.mock(PersistentService.class));
      bind(com.smartitengineering.cms.spi.persistence.PersistentServiceRegistrar.class).to(
          PersistentServiceRegistrar.class);
    }
  }
}
