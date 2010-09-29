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
import com.smartitengineering.cms.api.WorkspaceId;
import com.smartitengineering.cms.api.common.MediaType;
import com.smartitengineering.cms.api.impl.PersistableDomainFactoryImpl;
import com.smartitengineering.cms.api.impl.WorkspaceAPIImpl;
import com.smartitengineering.cms.api.impl.type.ContentTypeLoaderImpl;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.api.type.ContentTypeLoader;
import com.smartitengineering.cms.api.type.FieldDef;
import com.smartitengineering.cms.api.type.MutableContentStatus;
import com.smartitengineering.cms.api.type.MutableContentType;
import com.smartitengineering.cms.api.type.MutableRepresentationDef;
import com.smartitengineering.cms.api.type.MutableResourceUri;
import com.smartitengineering.cms.api.type.RepresentationDef;
import com.smartitengineering.cms.api.type.ResourceUri.Type;
import com.smartitengineering.cms.api.type.TemplateType;
import com.smartitengineering.cms.api.type.ValidatorType;
import com.smartitengineering.cms.api.type.VariationDef;
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
import java.io.DataInput;
import java.io.DataOutput;
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
  public static final WorkspaceId TEST_WS_ID = new WorkspaceId() {

    @Override
    public String getGlobalNamespace() {
      return "test";
    }

    @Override
    public String getName() {
      return "testWS";
    }

    @Override
    public void writeExternal(DataOutput output) throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void readExternal(DataInput input) throws IOException, ClassNotFoundException {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int compareTo(WorkspaceId o) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String toString() {
      return "test:testWS";
    }
  };

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

  @Test
  public void testParsing() {
    logger.debug(":::::::::::::::::::::Starting My First Test::::::::::::::::::::::::::::");
    try {
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

  @Test
  public void testParsingSataus() throws Exception {
    Collection<MutableContentType> collection = init();
    Iterator<MutableContentType> iterator = collection.iterator();
    MutableContentType contentType = iterator.next();
    if (logger.isInfoEnabled()) {
      logger.debug(new StringBuffer("First status size is ").append(contentType.getStatuses().size()).toString());
    }
    Assert.assertEquals(3, contentType.getStatuses().size());
    String[] name = {"draft", "marketed", "obselete"};
    for (int i = 0; i < 3; i++) {
      MutableContentStatus contentStatus = SmartContentAPI.getInstance().getContentTypeLoader().
          createMutableContentStatus();
      contentStatus.setName(name[i]);
      Assert.assertEquals(contentStatus, contentType.getStatuses().get(name[i]));

    }
    contentType = iterator.next();
    if (logger.isInfoEnabled()) {
      logger.debug(new StringBuffer("2nd status size is ").append(contentType.getStatuses().size()).toString());
    }
    Assert.assertEquals(3, contentType.getStatuses().size());
    for (int i = 0; i < 3; i++) {
      MutableContentStatus contentStatus = SmartContentAPI.getInstance().getContentTypeLoader().
          createMutableContentStatus();
      contentStatus.setName(name[i]);
      Assert.assertEquals(contentStatus, contentType.getStatuses().get(name[i]));
    }
  }

  @Test
  public void testParsingFileds() throws Exception {
    Collection<MutableContentType> collection = init();
    Iterator<MutableContentType> iterator = collection.iterator();
    MutableContentType contentType = iterator.next();

    if (logger.isInfoEnabled()) {
      logger.debug(new StringBuffer("first Fileds size is ").append(contentType.getFieldDefs().size()).toString());
    }
    Assert.assertEquals(3, contentType.getFieldDefs().size());
    contentType = iterator.next();

    if (logger.isInfoEnabled()) {
      logger.debug(new StringBuffer("2nd Fileds size is ").append(contentType.getFieldDefs()).toString());
    }
    Assert.assertEquals(2, contentType.getFieldDefs().size());

  }

  @Test
  public void testParsingField() throws Exception {
    Collection<MutableContentType> collection = init();
    Iterator<MutableContentType> iterator = collection.iterator();
    Collection<FieldDef> fieldDefs = iterator.next().getMutableFieldDefs();
    Iterator<FieldDef> fieldIterator = fieldDefs.iterator();
    Assert.assertEquals(3, fieldDefs.size());
    FieldDef fieldDef = fieldIterator.next();

    //parsing fieldA

    Assert.assertEquals("fieldA", fieldDef.getName());
    Assert.assertEquals(2, fieldDef.getVariations().size());

    Collection<VariationDef> variationDefs = fieldDef.getVariations();
    Iterator<VariationDef> variationIterator = variationDefs.iterator();
    VariationDef variationDef = variationIterator.next();

    Assert.assertEquals("avar", variationDef.getName());
    Assert.assertEquals(TemplateType.VELOCITY, variationDef.getTemplateType());
    Assert.assertEquals("some/type", variationDef.getMIMEType());
    Assert.assertEquals(Type.EXTERNAL, variationDef.getResourceUri().getType());
    Assert.assertEquals("http://some/uri", variationDef.getResourceUri().getValue());

//    Assert.assertEquals(variationDef, variationDef1);          /*not working*/
    variationDef = variationIterator.next();

    Assert.assertEquals("anothervar", variationDef.getName());
    Assert.assertEquals(TemplateType.JAVASCRIPT, variationDef.getTemplateType());
    Assert.assertEquals("some/type", variationDef.getMIMEType());
    Assert.assertEquals(Type.INTERNAL, variationDef.getResourceUri().getType());
    Assert.assertEquals("internalvar", variationDef.getResourceUri().getValue());

    //end parsing fieldA

    fieldDef = fieldIterator.next();
    Assert.assertEquals("fieldB", fieldDef.getName());
    Assert.assertEquals(ValidatorType.JAVASCRIPT, fieldDef.getCustomValidator().geType());
    Assert.assertEquals(Type.INTERNAL, fieldDef.getCustomValidator().getUri().getType());
    Assert.assertEquals("internalvar", fieldDef.getCustomValidator().getUri().getValue());
    Assert.assertEquals(Boolean.TRUE, fieldDef.isRequired());

    fieldDef = fieldIterator.next();
    Assert.assertEquals("fieldC", fieldDef.getName());
    Assert.assertEquals(ValidatorType.GROOVY, fieldDef.getCustomValidator().geType());
    Assert.assertEquals(Type.EXTERNAL, fieldDef.getCustomValidator().getUri().getType());
    Assert.assertEquals("http://some/uri", fieldDef.getCustomValidator().getUri().getValue());
    fieldDefs.clear();
    fieldDefs = iterator.next().getMutableFieldDefs();
    Assert.assertEquals(2, fieldDefs.size());
    Iterator<FieldDef> newFieldIterator = fieldDefs.iterator();
    FieldDef newFieldDef = newFieldIterator.next();
    Assert.assertEquals("image", newFieldDef.getName());
    Assert.assertEquals(Boolean.TRUE, newFieldDef.getSearchDefinition().isIndexed());
    Assert.assertEquals(Boolean.FALSE, newFieldDef.getSearchDefinition().isStored());
    Assert.assertEquals("a", newFieldDef.getSearchDefinition().getBoostConfig());
    Assert.assertEquals(Boolean.TRUE, newFieldDef.isFieldStandaloneUpdateAble());
    newFieldDef = newFieldIterator.next();
    Assert.assertEquals("altText", newFieldDef.getName());
    Assert.assertEquals(Boolean.TRUE, newFieldDef.isFieldStandaloneUpdateAble());
  }

  @Test
  public void testParsingRepresentations() throws Exception {
    Collection<MutableContentType> collection = init();
    Iterator<MutableContentType> iterator = collection.iterator();
    MutableContentType contentType = iterator.next();
    logger.debug("First ContentType contains " + contentType.getRepresentationDefs().size() + " no of representations.");
    logger.debug("First ContentType contains " + contentType.getRepresentationDefs().size()
        + " no of representations.");
    Assert.assertEquals(0, contentType.getRepresentationDefs().size());
    contentType = iterator.next();
    logger.debug("2nd ContentType contains " + contentType.getRepresentationDefs().size() + " no of representations.");
    logger.debug("2nd ContentType contains " + contentType.getRepresentationDefs().size()
        + " no of representations.");
    Assert.assertEquals(2, contentType.getRepresentationDefs().size());


    MutableResourceUri resourceUri = SmartContentAPI.getInstance().getContentTypeLoader().createMutableResourceUri();
    resourceUri.setType(Type.EXTERNAL);
    resourceUri.setValue("http://some/uri");


    MutableRepresentationDef def = SmartContentAPI.getInstance().getContentTypeLoader().createMutableRepresentationDef();
    def.setName("arep");
    def.setMIMEType("some/type");
    def.setResourceUri(resourceUri);
    def.setTemplateType(TemplateType.VELOCITY);


    final RepresentationDef defFromXml = contentType.getRepresentationDefs().get("arep");
    logger.debug(def.getName() + " Name " + defFromXml.getName());
    logger.debug(def.getMIMEType() + " mime type " + defFromXml.getMIMEType());
    logger.debug(def.getResourceUri().getType().name() + " Uri Type "
        + defFromXml.getResourceUri().getType().name());
    logger.debug(def.getResourceUri().getValue() + " RESOURCE URI " + defFromXml.getResourceUri().getValue());
    logger.debug(def.getTemplateType().name() + " TEMPLATE TYPE " + defFromXml.getTemplateType().name());
    logger.debug(def.hashCode() + " Hash COde " + defFromXml.hashCode());
//    Assert.assertEquals(def, defFromXml);

    Assert.assertEquals(def.getName(), defFromXml.getName());
    Assert.assertEquals(def.getMIMEType(), defFromXml.getMIMEType());
    Assert.assertEquals(def.getResourceUri().getType(), defFromXml.getResourceUri().getType());
    Assert.assertEquals(def.getResourceUri().getValue(), defFromXml.getResourceUri().getValue());
    Assert.assertEquals(def.getTemplateType(), defFromXml.getTemplateType());

    MutableResourceUri resourceUri1 = SmartContentAPI.getInstance().getContentTypeLoader().createMutableResourceUri();
    resourceUri1.setType(Type.INTERNAL);
    resourceUri1.setValue("internalrep");


    MutableRepresentationDef def1 =
                             SmartContentAPI.getInstance().getContentTypeLoader().createMutableRepresentationDef();
    def1.setName("anotherrep");
    def1.setMIMEType("some/type");
    def1.setTemplateType(TemplateType.RUBY);
    def1.setResourceUri(resourceUri1);


    final RepresentationDef defFromXml1 = contentType.getRepresentationDefs().get("anotherrep");
    logger.debug(def1.getName() + " Name " + defFromXml1.getName());
    logger.debug(def1.getMIMEType() + " mime type " + defFromXml1.getMIMEType());
    logger.debug(def1.getResourceUri().getType().name() + " Uri Type "
        + defFromXml1.getResourceUri().getType().name());
    logger.debug(def1.getResourceUri().getValue() + " RESOURCE URI " + defFromXml1.getResourceUri().getValue());
    logger.debug(def1.getTemplateType().name() + " TEMPLATE TYPE " + defFromXml1.getTemplateType().name());
    logger.debug(def1.hashCode() + " Hash COde " + defFromXml1.hashCode());
    //Assert.assertEquals(def1, defFromXml1);

    Assert.assertEquals(def1.getName(), defFromXml1.getName());
    Assert.assertEquals(def1.getMIMEType(), defFromXml1.getMIMEType());
    Assert.assertEquals(def1.getResourceUri().getType(), defFromXml1.getResourceUri().getType());
    Assert.assertEquals(def1.getResourceUri().getValue(), defFromXml1.getResourceUri().getValue());
    Assert.assertEquals(def1.getTemplateType(), defFromXml1.getTemplateType());
  }

  protected Collection<MutableContentType> init() throws NullPointerException, IOException {
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream("content-type-def-1.xml");
    Assert.assertNotNull(inputStream);
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
