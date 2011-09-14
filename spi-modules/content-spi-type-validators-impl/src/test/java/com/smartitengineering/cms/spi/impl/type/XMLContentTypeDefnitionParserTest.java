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
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.factory.workspace.WorkspaceAPI;
import com.smartitengineering.cms.api.type.ContentCoProcessorDef;
import com.smartitengineering.cms.api.type.ContentType.ContentProcessingPhase;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.cms.api.common.MediaType;
import com.smartitengineering.cms.api.common.TemplateType;
import com.smartitengineering.cms.api.content.template.ContentCoProcessorGenerator;
import com.smartitengineering.cms.api.exception.InvalidReferenceException;
import com.smartitengineering.cms.api.factory.content.ContentLoader;
import com.smartitengineering.cms.api.impl.PersistableDomainFactoryImpl;
import com.smartitengineering.cms.api.impl.workspace.WorkspaceAPIImpl;
import com.smartitengineering.cms.api.impl.type.ContentTypeLoaderImpl;
import com.smartitengineering.cms.api.type.CollectionDataType;
import com.smartitengineering.cms.api.type.ContentDataType;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.api.factory.type.ContentTypeLoader;
import com.smartitengineering.cms.api.factory.type.WritableContentType;
import com.smartitengineering.cms.api.impl.content.ContentLoaderImpl;
import com.smartitengineering.cms.api.type.CompositeDataType;
import com.smartitengineering.cms.api.type.EnumDataType;
import com.smartitengineering.cms.api.type.FieldDef;
import com.smartitengineering.cms.api.type.FieldValueType;
import com.smartitengineering.cms.api.type.MutableContentStatus;
import com.smartitengineering.cms.api.type.MutableContentType;
import com.smartitengineering.cms.api.type.MutableRepresentationDef;
import com.smartitengineering.cms.api.type.MutableResourceUri;
import com.smartitengineering.cms.api.type.RepresentationDef;
import com.smartitengineering.cms.api.type.ResourceUri.Type;
import com.smartitengineering.cms.api.type.VariationDef;
import com.smartitengineering.cms.spi.SmartContentSPI;
import com.smartitengineering.cms.spi.impl.PersistentServiceRegistrar;
import com.smartitengineering.cms.spi.impl.ContentTypeDefinitionParsers;
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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
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

  private transient final Logger logger = LoggerFactory.getLogger(getClass());
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
      init();
    }
    catch (Exception e) {
      logger.error(e.getMessage(), e);
      Assert.fail("Should not get exceptions");
    }
    logger.debug(":::::::::::::::::::::Ending My First Test::::::::::::::::::::::::::::");
  }

  @Test
  public void testParsingContentId() throws Exception { //done
    Collection<WritableContentType> collection = init();
    Iterator<WritableContentType> iterator = collection.iterator();
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
  public void testDisplayName() throws Exception {  //done
    Collection<WritableContentType> collection = init();
    Iterator<WritableContentType> iterator = collection.iterator();
    MutableContentType contentType = iterator.next();
    contentType = iterator.next();
    if (logger.isInfoEnabled()) {
      logger.debug(new StringBuffer("ContentType Dssplay Name is ").append(contentType.getDisplayName()).toString());
    }
    Assert.assertEquals("JPEG Image", contentType.getDisplayName());
  }

  @Test
  public void testParent() throws Exception { //done
    Collection<WritableContentType> collections = init();
    Iterator<WritableContentType> iterator = collections.iterator();
    MutableContentType contentType = iterator.next();
    if (logger.isInfoEnabled()) {
      logger.debug(new StringBuffer("Parent's Name is ").append(contentType.getParent().getName()).toString());
      logger.debug(new StringBuffer("Parent's NameSpace is ").append(contentType.getParent().getNamespace()).toString());
    }
    Assert.assertEquals("jpeg", contentType.getParent().getNamespace());
    Assert.assertEquals("RST", contentType.getParent().getName());
  }

  @Test
  public void testParsingSataus() throws Exception {  //done
    Collection<WritableContentType> collection = init();
    Iterator<WritableContentType> iterator = collection.iterator();
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
  public void testParsingFileds() throws Exception {  //done
    Collection<WritableContentType> collection = init();
    Iterator<WritableContentType> iterator = collection.iterator();
    MutableContentType contentType = iterator.next();

    if (logger.isInfoEnabled()) {
      logger.debug(new StringBuffer("first Fileds size is ").append(contentType.getOwnFieldDefs().size()).toString());
    }
    Assert.assertEquals(4, contentType.getOwnFieldDefs().size());
    contentType = iterator.next();

    if (logger.isInfoEnabled()) {
      logger.debug(new StringBuffer("2nd Fileds size is ").append(contentType.getOwnFieldDefs()).toString());
    }
    Assert.assertEquals(2, contentType.getOwnFieldDefs().size());

  }

  @Test
  public void testParsingField() throws Exception {// done without value
    Collection<WritableContentType> collection = init();
    Iterator<WritableContentType> iterator = collection.iterator();
    Collection<FieldDef> fieldDefs = iterator.next().getMutableFieldDefs();
    Iterator<FieldDef> fieldIterator = fieldDefs.iterator();
    Assert.assertEquals(4, fieldDefs.size());
    FieldDef fieldDef = fieldIterator.next();



    Assert.assertEquals(2, fieldDef.getVariations().size());
    Assert.assertEquals(FieldValueType.CONTENT.name(), fieldDef.getValueDef().getType().name());

    ContentDataType contentDataType = (ContentDataType) fieldDef.getValueDef();
    Assert.assertEquals("XYZ", contentDataType.getTypeDef().getName());
    Assert.assertEquals("asdfasdf1", contentDataType.getTypeDef().getNamespace());
    Assert.assertEquals(true, contentDataType.isAvaialbleForSearch());
    Assert.assertEquals("test", contentDataType.getBidirectionalFieldName());
    Assert.assertEquals("testWS", contentDataType.getTypeDef().getWorkspace().getGlobalNamespace());
    Assert.assertEquals("test", contentDataType.getTypeDef().getWorkspace().getName());

    Assert.assertEquals("fieldA", fieldDef.getName());
    Assert.assertEquals(2, fieldDef.getVariations().size());

    Collection<VariationDef> variationDefs = fieldDef.getVariations().values();
    Iterator<VariationDef> variationIterator = variationDefs.iterator();
    VariationDef variationDef = variationIterator.next();

    if (logger.isInfoEnabled()) {
      logger.debug(new StringBuffer("First ContentType's first field's 1st variation name is ").append(
          variationDef.getName()).toString());
      logger.debug(new StringBuffer("First ContentType's first field's 1st variation ResourceUri's type is ").append(
          variationDef.getResourceUri().getType()).toString());
      logger.debug(new StringBuffer("First ContentType's first field's 1st variation ResourceUri's value is ").append(
          variationDef.getResourceUri().getValue()).toString());
    }

    Assert.assertEquals("avar", variationDef.getName());
    Assert.assertEquals("some/type", variationDef.getMIMEType());
    Assert.assertEquals(Type.EXTERNAL, variationDef.getResourceUri().getType());
    Assert.assertEquals("http://some/uri", variationDef.getResourceUri().getValue());


    variationDef = variationIterator.next();

    if (logger.isInfoEnabled()) {
      logger.debug(new StringBuffer("First ContentType's first field's 2nd variation name is ").append(
          variationDef.getName()).toString());
      logger.debug(new StringBuffer("First ContentType's first field's 2nd variation ResourceUri's type is ").append(
          variationDef.getResourceUri().getType()).toString());
      logger.debug(new StringBuffer("First ContentType's first field's 2nd variation ResourceUri's value is ").append(
          variationDef.getResourceUri().getValue()).toString());
    }

    Assert.assertEquals("anothervar", variationDef.getName());
    Assert.assertEquals("some/type", variationDef.getMIMEType());
    Assert.assertEquals(Type.INTERNAL, variationDef.getResourceUri().getType());
    Assert.assertEquals("internalvar", variationDef.getResourceUri().getValue());



    fieldDef = fieldIterator.next();

    if (logger.isInfoEnabled()) {
      logger.debug(new StringBuffer("First ContentType's 2nd field's validition name is ").append(
          fieldDef.getName()).toString());
      if (fieldDef.getCustomValidators().size() > 0) {
        logger.debug(new StringBuffer("First ContentType's 2nd field's validition ResourceUri's type is ").append(
            fieldDef.getCustomValidators().iterator().next().getUri().getType()).toString());
        logger.debug(new StringBuffer("First ContentType's 2nd field's validition ResourceUri's value is ").append(
            fieldDef.getCustomValidators().iterator().next().getUri().getValue()).toString());
      }
      else {
        logger.debug("No custom validator");
      }
      logger.debug(new StringBuffer("First ContentType's 2nd field's validition Require value is ").append(fieldDef.
          isRequired()).toString());
    }
    Assert.assertEquals(FieldValueType.COLLECTION.name(), fieldDef.getValueDef().getType().name());

    CollectionDataType collectionDataType = (CollectionDataType) fieldDef.getValueDef();
    Assert.assertEquals(10, collectionDataType.getMaxSize());
    Assert.assertEquals(Integer.MIN_VALUE, collectionDataType.getMinSize());
    Assert.assertEquals(FieldValueType.INTEGER.name(), collectionDataType.getItemDataType().getType().name());

    Assert.assertEquals("fieldB", fieldDef.getName());
    Assert.assertEquals(1, fieldDef.getCustomValidators().size());
    Assert.assertEquals(Type.INTERNAL, fieldDef.getCustomValidators().iterator().next().getUri().getType());
    Assert.assertEquals("internalvar", fieldDef.getCustomValidators().iterator().next().getUri().getValue());
    Assert.assertEquals(Boolean.TRUE, fieldDef.isRequired());

    fieldDef = fieldIterator.next();

    if (logger.isInfoEnabled()) {
      logger.debug(new StringBuffer("First ContentType's 3rd field's validition name is ").append(
          fieldDef.getName()).toString());
      if (fieldDef.getCustomValidators().size() > 0) {
        logger.debug(new StringBuffer("First ContentType's 3rd field's validition ResourceUri's type is ").append(
            fieldDef.getCustomValidators().iterator().next().getUri().getType()).toString());
        logger.debug(new StringBuffer("First ContentType's 3rd field's validition ResourceUri's value is ").append(
            fieldDef.getCustomValidators().iterator().next().getUri().getValue()).toString());
      }
      else {
        logger.debug("No custom validator");
      }
    }
    Assert.assertEquals(FieldValueType.COLLECTION.name(), fieldDef.getValueDef().getType().name());

    CollectionDataType collectionDataType1 = (CollectionDataType) fieldDef.getValueDef();
    Assert.assertEquals(Integer.MAX_VALUE, collectionDataType1.getMaxSize());
    Assert.assertEquals(2, collectionDataType1.getMinSize());

    Assert.assertEquals(FieldValueType.CONTENT.name(), collectionDataType1.getItemDataType().getType().name());
    ContentDataType contentDataType1 = (ContentDataType) collectionDataType1.getItemDataType();

    Assert.assertNull(contentDataType1.getBidirectionalFieldName());
    Assert.assertEquals("RST", contentDataType1.getTypeDef().getName());
    Assert.assertEquals("jpeg", contentDataType1.getTypeDef().getNamespace());
    Assert.assertEquals(false, contentDataType1.isAvaialbleForSearch());
    Assert.assertEquals("testWS", contentDataType1.getTypeDef().getWorkspace().getGlobalNamespace());
    Assert.assertEquals("test", contentDataType1.getTypeDef().getWorkspace().getName());

    Assert.assertEquals("fieldC", fieldDef.getName());
    Assert.assertEquals(Type.EXTERNAL, fieldDef.getCustomValidators().iterator().next().getUri().getType());
    Assert.assertEquals("http://some/uri", fieldDef.getCustomValidators().iterator().next().getUri().getValue());
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
  public void testParsingRepresentations() throws Exception { //done
    Collection<WritableContentType> collection = init();
    Iterator<WritableContentType> iterator = collection.iterator();
    MutableContentType contentType = iterator.next();
    if (logger.isInfoEnabled()) {
      logger.debug(new StringBuffer("First ContentType contains ").append(contentType.getRepresentationDefs().size()).
          append(" no of representations.").toString());

    }
    Assert.assertEquals(0, contentType.getRepresentationDefs().size());
    contentType = iterator.next();
    if (logger.isInfoEnabled()) {
      logger.debug(new StringBuffer("2nd ContentType contains ").append(contentType.getRepresentationDefs().size()).
          append(" no of representations.").toString());
    }
    Assert.assertEquals(2, contentType.getRepresentationDefs().size());


    MutableResourceUri resourceUri = SmartContentAPI.getInstance().getContentTypeLoader().createMutableResourceUri();
    resourceUri.setType(Type.EXTERNAL);
    resourceUri.setValue("http://some/uri");


    MutableRepresentationDef def = SmartContentAPI.getInstance().getContentTypeLoader().createMutableRepresentationDef();
    def.setName("arep");
    def.setMIMEType("some/type");
    def.setResourceUri(resourceUri);


    final RepresentationDef defFromXml = contentType.getRepresentationDefs().get("arep");
    logger.debug(def.getName() + " Name " + defFromXml.getName());
    logger.debug(def.getMIMEType() + " mime type " + defFromXml.getMIMEType());
    logger.debug(def.getResourceUri().getType().name() + " Uri Type " + defFromXml.getResourceUri().getType().name());
    logger.debug(def.getResourceUri().getValue() + " RESOURCE URI " + defFromXml.getResourceUri().getValue());
    logger.debug(def.hashCode() + " Hash COde " + defFromXml.hashCode());


    Assert.assertEquals(def.getName(), defFromXml.getName());
    Assert.assertEquals(def.getMIMEType(), defFromXml.getMIMEType());
    Assert.assertEquals(def.getResourceUri().getType(), defFromXml.getResourceUri().getType());
    Assert.assertEquals(def.getResourceUri().getValue(), defFromXml.getResourceUri().getValue());

    MutableResourceUri resourceUri1 = SmartContentAPI.getInstance().getContentTypeLoader().createMutableResourceUri();
    resourceUri1.setType(Type.INTERNAL);
    resourceUri1.setValue("internalrep");


    MutableRepresentationDef def1 =
                             SmartContentAPI.getInstance().getContentTypeLoader().createMutableRepresentationDef();
    def1.setName("anotherrep");
    def1.setMIMEType("some/type");
    def1.setResourceUri(resourceUri1);


    final RepresentationDef defFromXml1 = contentType.getRepresentationDefs().get("anotherrep");
    logger.debug(def1.getName() + " Name " + defFromXml1.getName());
    logger.debug(def1.getMIMEType() + " mime type " + defFromXml1.getMIMEType());
    logger.debug(def1.getResourceUri().getType().name() + " Uri Type " + defFromXml1.getResourceUri().getType().name());
    logger.debug(def1.getResourceUri().getValue() + " RESOURCE URI " + defFromXml1.getResourceUri().getValue());
    logger.debug(def1.hashCode() + " Hash COde " + defFromXml1.hashCode());


    Assert.assertEquals(def1.getName(), defFromXml1.getName());
    Assert.assertEquals(def1.getMIMEType(), defFromXml1.getMIMEType());
    Assert.assertEquals(def1.getResourceUri().getType(), defFromXml1.getResourceUri().getType());
    Assert.assertEquals(def1.getResourceUri().getValue(), defFromXml1.getResourceUri().getValue());
  }

  @Test
  public void testInvalidParent() {

    System.out.println(":::::::::::::::::::::::::: TESTING PARENT ::::::::::::::::::::::::::");

    InputStream inputStream = getClass().getClassLoader().getResourceAsStream("Invalid-Parent.xml");
    Assert.assertNotNull(inputStream);
    Collection<WritableContentType> collection = new ArrayList<WritableContentType>();
    try {
      collection = SmartContentAPI.getInstance().getContentTypeLoader().parseContentTypes(TEST_WS_ID, inputStream,
                                                                                          MediaType.APPLICATION_XML);
      Assert.fail("Parent is Invalid");
    }
    catch (InvalidReferenceException e) {
    }
    catch (Exception ex) {
      Assert.fail("Unexpected exception! " + ex.getMessage());
    }
  }

  @Test
  public void testInvalidMinSize() {

    System.out.println(":::::::::::::::::::::::::: TESTING MAX MIN ::::::::::::::::::::::::::");

    InputStream inputStream = getClass().getClassLoader().getResourceAsStream("TestMinMax.xml");
    Assert.assertNotNull(inputStream);
    Collection<WritableContentType> collection = new ArrayList<WritableContentType>();
    try {
      collection = SmartContentAPI.getInstance().getContentTypeLoader().parseContentTypes(TEST_WS_ID, inputStream,
                                                                                          MediaType.APPLICATION_XML);
      Assert.fail("minSize is Grater than maxSize");
    }
    catch (InvalidReferenceException e) {
    }
    catch (Exception ex) {
      Assert.fail("Unexpected exception! " + ex.getMessage());
    }
  }

  @Test
  public void testInvalidValueType() {

    System.out.println(":::::::::::::::::::::::::: TESTING COLLECTION VALUE ::::::::::::::::::::::::::");

    InputStream inputStream = getClass().getClassLoader().getResourceAsStream("InvalidValueType.xml");
    Assert.assertNotNull(inputStream);
    Collection<WritableContentType> collection = new ArrayList<WritableContentType>();
    try {
      collection = SmartContentAPI.getInstance().getContentTypeLoader().parseContentTypes(TEST_WS_ID, inputStream,
                                                                                          MediaType.APPLICATION_XML);
      Assert.fail("Invalid Value type inside the collection");
    }
    catch (InvalidReferenceException e) {
    }
    catch (Exception ex) {
      Assert.fail("Unexpected exception! " + ex.getMessage());
    }
  }

  @Test
  public void testParsingContentWorkspace() throws Exception {
    Collection<WritableContentType> collection = init();
    Iterator<WritableContentType> iterator = collection.iterator();
    Collection<FieldDef> fieldDefs = iterator.next().getMutableFieldDefs();
    Iterator<FieldDef> fieldIterator = fieldDefs.iterator();
    Assert.assertEquals(4, fieldDefs.size());
    FieldDef fieldDef = fieldIterator.next();
    ContentDataType contentDataType = (ContentDataType) fieldDef.getValueDef();
    Assert.assertEquals("testWS", contentDataType.getTypeDef().getWorkspace().getGlobalNamespace());
    Assert.assertEquals("test", contentDataType.getTypeDef().getWorkspace().getName());
  }

  @Test
  public void testParsingEnumFields() throws Exception {
    Collection<WritableContentType> collection = init("content-type-def-shopping.xml");
    Assert.assertEquals(6, collection.size());
    Iterator<WritableContentType> iterator = collection.iterator();
    boolean foundFieldDef = false;
    while (iterator.hasNext()) {
      WritableContentType type = iterator.next();
      if (type.getContentTypeID().getName().equals("Product")) {
        foundFieldDef = true;
        FieldDef enumDef = type.getFieldDefs().get("enumTest");
        Assert.assertNotNull(enumDef);
        Assert.assertEquals(FieldValueType.ENUM, enumDef.getValueDef().getType());
        Assert.assertTrue(enumDef.getValueDef() instanceof EnumDataType);
        EnumDataType enumDataType = (EnumDataType) enumDef.getValueDef();
        Collection<String> choices = enumDataType.getChoices();
        Assert.assertEquals(3, choices.size());
        Assert.assertTrue(choices.contains(String.valueOf(1)));
        Assert.assertTrue(choices.contains(String.valueOf(2)));
        Assert.assertTrue(choices.contains(String.valueOf(3)));
      }
    }
    Assert.assertTrue(foundFieldDef);
  }

  @Test
  public void testParsingContentCoProcessors() throws Exception {
    System.out.println(":::::::::::::::::::::::::: TEST PARSING CONTENT CO PROCESSOR ::::::::::::::::::::::::::");
    Collection<WritableContentType> collection = init("content-type-def-shopping.xml");
    Assert.assertEquals(6, collection.size());
    Iterator<WritableContentType> iterator = collection.iterator();
    boolean foundFieldDef = false;
    while (iterator.hasNext()) {
      WritableContentType type = iterator.next();
      if (type.getContentTypeID().getName().equals("Author")) {
        foundFieldDef = true;
        final Map<ContentProcessingPhase, Collection<ContentCoProcessorDef>> contentCoProcessorDefs =
                                                                             type.getContentCoProcessorDefs();
        Assert.assertNotNull(contentCoProcessorDefs);
        Assert.assertFalse(contentCoProcessorDefs.isEmpty());
        Assert.assertNotNull(contentCoProcessorDefs.get(ContentProcessingPhase.READ));
        Assert.assertNotNull(contentCoProcessorDefs.get(ContentProcessingPhase.WRITE));
        Assert.assertFalse(contentCoProcessorDefs.get(ContentProcessingPhase.READ).isEmpty());
        Assert.assertFalse(contentCoProcessorDefs.get(ContentProcessingPhase.WRITE).isEmpty());
        Assert.assertEquals(2, contentCoProcessorDefs.get(ContentProcessingPhase.READ).size());
        Assert.assertEquals(1, contentCoProcessorDefs.get(ContentProcessingPhase.WRITE).size());
        final Iterator<ContentCoProcessorDef> readItr =
                                              contentCoProcessorDefs.get(ContentProcessingPhase.READ).iterator();
        ContentCoProcessorDef def = readItr.next();
        Assert.assertEquals("testr", def.getName());
        Assert.assertNull(def.getMIMEType());
        Assert.assertEquals(0, def.getPriority());
        Assert.assertEquals(1, def.getParameters().size());
        Assert.assertEquals("v", def.getParameters().get("k"));
        Assert.assertEquals("test", def.getResourceUri().getValue());
        def = readItr.next();
        Assert.assertEquals("testr1", def.getName());
        Assert.assertNull(def.getMIMEType());
        Assert.assertEquals(1, def.getPriority());
        Assert.assertEquals(1, def.getParameters().size());
        Assert.assertEquals("v1", def.getParameters().get("k1"));
        Assert.assertEquals("test1", def.getResourceUri().getValue());
        def = contentCoProcessorDefs.get(ContentProcessingPhase.WRITE).iterator().next();
        Assert.assertEquals("testw", def.getName());
        Assert.assertNull(def.getMIMEType());
        Assert.assertEquals(0, def.getPriority());
        Assert.assertEquals(1, def.getParameters().size());
        Assert.assertEquals("v2", def.getParameters().get("k2"));
        Assert.assertEquals("test2", def.getResourceUri().getValue());
      }
    }
    Assert.assertTrue(foundFieldDef);
  }

  @Test
  public void testParsingCompositeFields() throws Exception {
    Collection<WritableContentType> collection = init("content-type-def-wih-composition.xml");
    Assert.assertEquals(2, collection.size());
    Iterator<WritableContentType> iterator = collection.iterator();
    Collection<FieldDef> fieldDefs = iterator.next().getMutableFieldDefs();
    Assert.assertEquals(7, fieldDefs.size());
    Collection<FieldDef> compositeFieldDefs = iterator.next().getMutableFieldDefs();
    Assert.assertEquals(3, compositeFieldDefs.size());
    final Iterator<FieldDef> mainIterator = compositeFieldDefs.iterator();
    FieldDef field = mainIterator.next();
    Assert.assertEquals(FieldValueType.COMPOSITE, field.getValueDef().getType());
    CompositeDataType compositeDataType = (CompositeDataType) field.getValueDef();
    Assert.assertNotNull(compositeDataType.getEmbeddedContentType());
    Assert.assertEquals(2, compositeDataType.getOwnComposition().size());
    Iterator<FieldDef> compositionIterator = compositeDataType.getOwnComposition().iterator();
    compositionIterator.next();
    FieldDef collectionFieldDef = compositionIterator.next();
    Assert.assertEquals(FieldValueType.COLLECTION, collectionFieldDef.getValueDef().getType());
    CollectionDataType collectionDataType = (CollectionDataType) collectionFieldDef.getValueDef();
    Assert.assertEquals(FieldValueType.COMPOSITE, collectionDataType.getItemDataType().getType());
    compositeDataType = (CompositeDataType) collectionDataType.getItemDataType();
    Assert.assertEquals(2, compositeDataType.getOwnComposition().size());
    final Iterator<FieldDef> ownComposition = compositeDataType.getOwnComposition().iterator();
    Assert.assertEquals("Number", ownComposition.next().getName());
    Assert.assertEquals("availability", ownComposition.next().getName());
    field = mainIterator.next();
    Assert.assertEquals(FieldValueType.COMPOSITE, field.getValueDef().getType());
    compositeDataType = (CompositeDataType) field.getValueDef();
    Assert.assertNotNull(compositeDataType.getEmbeddedContentType());
    Assert.assertEquals(0, compositeDataType.getOwnComposition().size());
    field = mainIterator.next();
    Assert.assertEquals(FieldValueType.COLLECTION, field.getValueDef().getType());
    collectionDataType = (CollectionDataType) field.getValueDef();
    Assert.assertEquals(FieldValueType.COMPOSITE, collectionDataType.getItemDataType().getType());
    compositeDataType = (CompositeDataType) collectionDataType.getItemDataType();
    Assert.assertNull(compositeDataType.getEmbeddedContentType());
    Assert.assertEquals(2, compositeDataType.getOwnComposition().size());
  }

  protected Collection<WritableContentType> init() throws Exception {
    final Collection<WritableContentType> init = init("content-type-def-1.xml");
    Assert.assertEquals(2, init.size());
    return init;
  }

  protected Collection<WritableContentType> init(String classpathResource) throws Exception {
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream(classpathResource);
    Assert.assertNotNull(inputStream);
    Collection<WritableContentType> collection;
    collection = SmartContentAPI.getInstance().getContentTypeLoader().parseContentTypes(TEST_WS_ID, inputStream,
                                                                                        MediaType.APPLICATION_XML);
    Assert.assertNotNull(collection);
    Assert.assertFalse(collection.isEmpty());
    return collection;
  }

  public static class TestModule extends AbstractModule {

    private Mockery mockery = new Mockery();

    @Override
    protected void configure() {
      bind(PersistableDomainFactory.class).to(PersistableDomainFactoryImpl.class).in(Scopes.SINGLETON);
      bind(ContentTypeLoader.class).annotatedWith(Names.named("apiContentTypeLoader")).to(ContentTypeLoaderImpl.class);
      bind(ContentLoader.class).annotatedWith(Names.named("apiContentLoader")).to(ContentLoaderImpl.class);
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
          allowing(mockReader).readContentTypeFromPersistentStorage(with(Expectations.<ContentTypeId[]>anything()));
          will(returnValue(Collections.emptyList()));
        }
      });
      MapBinder<Class, PersistentService> serviceBinder = MapBinder.newMapBinder(binder(), Class.class,
                                                                                 PersistentService.class);
      serviceBinder.addBinding(WritableContentType.class).toInstance(mockery.mock(PersistentService.class));
      MapBinder<TemplateType, ContentCoProcessorGenerator> ccpgBinder =
                                                           MapBinder.newMapBinder(binder(), TemplateType.class,
                                                                                  ContentCoProcessorGenerator.class);
      ccpgBinder.addBinding(TemplateType.GROOVY).toInstance(mockery.mock(ContentCoProcessorGenerator.class));
      bind(com.smartitengineering.cms.spi.persistence.PersistentServiceRegistrar.class).to(
          PersistentServiceRegistrar.class);
    }
  }
}
