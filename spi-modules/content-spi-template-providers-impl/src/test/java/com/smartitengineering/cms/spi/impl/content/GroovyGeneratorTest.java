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
package com.smartitengineering.cms.spi.impl.content;

import com.smartitengineering.cms.api.common.TemplateType;
import com.smartitengineering.cms.api.content.Content;
import com.smartitengineering.cms.api.content.ContentId;
import com.smartitengineering.cms.api.content.Field;
import com.smartitengineering.cms.api.content.FieldValue;
import com.smartitengineering.cms.api.content.Representation;
import com.smartitengineering.cms.api.content.Variation;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.factory.content.ContentLoader;
import com.smartitengineering.cms.api.factory.workspace.WorkspaceAPI;
import com.smartitengineering.cms.api.impl.content.RepresentationImpl;
import com.smartitengineering.cms.api.impl.content.VariationImpl;
import com.smartitengineering.cms.api.impl.workspace.WorkspaceAPIImpl;
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.type.FieldDef;
import com.smartitengineering.cms.api.type.RepresentationDef;
import com.smartitengineering.cms.api.type.ResourceUri;
import com.smartitengineering.cms.api.type.ValidatorDef;
import com.smartitengineering.cms.api.type.ValidatorType;
import com.smartitengineering.cms.api.type.VariationDef;
import com.smartitengineering.cms.api.workspace.RepresentationTemplate;
import com.smartitengineering.cms.api.workspace.ValidatorTemplate;
import com.smartitengineering.cms.api.workspace.VariationTemplate;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.cms.spi.content.RepresentationProvider;
import com.smartitengineering.cms.spi.content.ValidatorProvider;
import com.smartitengineering.cms.spi.content.VariationProvider;
import com.smartitengineering.cms.spi.content.template.TypeFieldValidator;
import com.smartitengineering.cms.spi.content.template.TypeRepresentationGenerator;
import com.smartitengineering.cms.spi.content.template.TypeVariationGenerator;
import com.smartitengineering.cms.spi.impl.content.VelocityGeneratorTest.Threads;
import com.smartitengineering.cms.spi.impl.content.template.GroovyRepresentationGenerator;
import com.smartitengineering.cms.spi.impl.content.template.GroovyValidatorGenerator;
import com.smartitengineering.cms.spi.impl.content.template.GroovyVariationGenerator;
import com.smartitengineering.util.bean.BeanFactoryRegistrar;
import com.smartitengineering.util.bean.SimpleBeanFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.io.IOUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit3.JUnit3Mockery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class GroovyGeneratorTest {

  public static final String CONTENT = "content";
  private Mockery mockery;
  public static final String REP_NAME = "test";
  public static final String MIME_TYPE = "application/json";
  private final transient Logger logger = LoggerFactory.getLogger(getClass());

  @Before
  public void setupAPIAndSPI() throws ClassNotFoundException {
    mockery = new JUnit3Mockery();
    setupAPI(mockery);
  }

  public static void setupAPI(final Mockery mockery) throws ClassNotFoundException {
    final ContentLoader mock = mockery.mock(ContentLoader.class);
    mockery.checking(new Expectations() {

      {
        allowing(mock).createMutableRepresentation(this.<ContentId>with(Expectations.<ContentId>anything()));
        will(returnValue(new RepresentationImpl(null)));
        allowing(mock).createMutableVariation(this.<ContentId>with(Expectations.<ContentId>anything()),
                                              this.<FieldDef>with(Expectations.<FieldDef>anything()));
        will(returnValue(new VariationImpl(null, null)));
      }
    });
    SimpleBeanFactory simpleBeanFactory = new SimpleBeanFactory(Collections.<String, Object>singletonMap(
        "apiContentLoader", mock));
    BeanFactoryRegistrar.registerBeanFactory(SmartContentAPI.CONTEXT_NAME, simpleBeanFactory);
  }

  @Test
  public void testGroovyRepGeneration() throws IOException {
    TypeRepresentationGenerator generator = new GroovyRepresentationGenerator();
    final RepresentationTemplate template = mockery.mock(RepresentationTemplate.class);
    WorkspaceAPIImpl impl = new WorkspaceAPIImpl() {

      @Override
      public RepresentationTemplate getRepresentationTemplate(WorkspaceId id, String name) {
        return template;
      }
    };
    impl.setRepresentationGenerators(Collections.singletonMap(TemplateType.GROOVY, generator));
    RepresentationProvider provider = new RepresentationProviderImpl();
    final WorkspaceAPI api = impl;
    registerBeanFactory(api);
    final Content content = mockery.mock(Content.class);
    final Field field = mockery.mock(Field.class);
    final FieldValue value = mockery.mock(FieldValue.class);
    final ContentType type = mockery.mock(ContentType.class);
    final Map<String, RepresentationDef> reps = mockery.mock(Map.class, "repMap");
    final RepresentationDef def = mockery.mock(RepresentationDef.class);
    mockery.checking(new Expectations() {

      {
        exactly(1).of(template).getTemplateType();
        will(returnValue(TemplateType.GROOVY));
        exactly(1).of(template).getTemplate();
        will(returnValue(
            IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream(
            "scripts/groovy/GroovyTestRepresentationGenerator.groovy"))));
        exactly(1).of(template).getName();
        will(returnValue(REP_NAME));
        exactly(1).of(value).getValue();
        will(returnValue(CONTENT));
        exactly(1).of(field).getValue();
        will(returnValue(value));
        exactly(1).of(content).getField(this.<String>with(Expectations.<String>anything()));
        will(returnValue(field));
        exactly(1).of(content).getContentDefinition();
        will(returnValue(type));
        final ContentId contentId = mockery.mock(ContentId.class);
        exactly(2).of(content).getContentId();
        will(returnValue(contentId));
        final WorkspaceId wId = mockery.mock(WorkspaceId.class);
        exactly(1).of(contentId).getWorkspaceId();
        will(returnValue(wId));
        exactly(2).of(type).getRepresentationDefs();
        will(returnValue(reps));
        exactly(2).of(reps).get(with(REP_NAME));
        will(returnValue(def));
        exactly(1).of(def).getParameters();
        will(returnValue(Collections.emptyMap()));
        exactly(1).of(def).getMIMEType();
        will(returnValue(GroovyGeneratorTest.MIME_TYPE));
        final ResourceUri rUri = mockery.mock(ResourceUri.class);
        exactly(1).of(def).getResourceUri();
        will(returnValue(rUri));
        exactly(1).of(rUri).getValue();
        will(returnValue("iUri"));
      }
    });
    Representation representation = provider.getRepresentation(REP_NAME, type, content);
    Assert.assertNotNull(representation);
    Assert.assertEquals(REP_NAME, representation.getName());
    Assert.assertEquals(GroovyGeneratorTest.MIME_TYPE, representation.getMimeType());
    Assert.assertEquals(CONTENT, StringUtils.newStringUtf8(representation.getRepresentation()));
  }

  protected void registerBeanFactory(final WorkspaceAPI api) {
    try {
      SmartContentAPI mainApi = SmartContentAPI.getInstance();
      Class apiClass = mainApi.getClass();
      java.lang.reflect.Field field = apiClass.getDeclaredField("workspaceApi");
      field.setAccessible(true);
      field.set(mainApi, api);
    }
    catch (Exception ex) {
      throw new IllegalArgumentException(ex);
    }
  }

  @Test
  public void testGroovyVarGeneration() throws IOException {
    TypeVariationGenerator generator = new GroovyVariationGenerator();
    final VariationTemplate template = mockery.mock(VariationTemplate.class);
    WorkspaceAPIImpl impl = new WorkspaceAPIImpl() {

      @Override
      public VariationTemplate getVariationTemplate(WorkspaceId id, String name) {
        return template;
      }
    };
    impl.setVariationGenerators(Collections.singletonMap(TemplateType.GROOVY, generator));
    VariationProvider provider = new VariationProviderImpl();
    registerBeanFactory(impl);
    final Field field = mockery.mock(Field.class, "varField");
    final FieldValue value = mockery.mock(FieldValue.class, "varFieldVal");
    final Content content = mockery.mock(Content.class, "varContent");
    final FieldDef fieldDef = mockery.mock(FieldDef.class);
    final Map<String, VariationDef> vars = mockery.mock(Map.class, "varMap");
    final VariationDef def = mockery.mock(VariationDef.class);
    mockery.checking(new Expectations() {

      {
        exactly(1).of(template).getTemplateType();
        will(returnValue(TemplateType.GROOVY));
        exactly(1).of(template).getTemplate();
        will(returnValue(
            IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream(
            "scripts/groovy/GroovyTestVariationGenerator.groovy"))));
        exactly(1).of(template).getName();
        will(returnValue(REP_NAME));
        exactly(1).of(value).getValue();
        will(returnValue(CONTENT));
        exactly(1).of(field).getValue();
        will(returnValue(value));
        exactly(1).of(field).getFieldDef();
        will(returnValue(fieldDef));
        final ContentId contentId = mockery.mock(ContentId.class, "varId");
        exactly(2).of(content).getContentId();
        will(returnValue(contentId));
        final WorkspaceId wId = mockery.mock(WorkspaceId.class, "varWId");
        exactly(1).of(contentId).getWorkspaceId();
        will(returnValue(wId));
        exactly(1).of(fieldDef).getVariations();
        will(returnValue(vars));
        exactly(1).of(vars).get(with(REP_NAME));
        will(returnValue(def));
        exactly(1).of(def).getParameters();
        will(returnValue(Collections.emptyMap()));
        exactly(1).of(def).getMIMEType();
        will(returnValue(GroovyGeneratorTest.MIME_TYPE));
        final ResourceUri rUri = mockery.mock(ResourceUri.class, "varRUri");
        exactly(1).of(def).getResourceUri();
        will(returnValue(rUri));
        exactly(1).of(rUri).getValue();
        will(returnValue("iUri"));
      }
    });
    Variation representation = provider.getVariation(REP_NAME, content, field);
    Assert.assertNotNull(representation);
    Assert.assertEquals(REP_NAME, representation.getName());
    Assert.assertEquals(GroovyGeneratorTest.MIME_TYPE, representation.getMimeType());
    Assert.assertEquals(CONTENT, StringUtils.newStringUtf8(representation.getVariation()));
  }

  @Test
  public void testGroovyValGeneration() throws IOException {
    TypeFieldValidator generator = new GroovyValidatorGenerator();
    final ValidatorTemplate template = mockery.mock(ValidatorTemplate.class);
    WorkspaceAPIImpl impl = new WorkspaceAPIImpl() {

      @Override
      public ValidatorTemplate getValidatorTemplate(WorkspaceId workspaceId, String name) {
        return template;
      }
    };
    impl.setValidatorGenerators(Collections.singletonMap(ValidatorType.GROOVY, generator));
    ValidatorProvider provider = new ValidatorProviderImpl();
    registerBeanFactory(impl);
    final Content content = mockery.mock(Content.class, "valContent");
    final Field field = mockery.mock(Field.class, "valField");
    final FieldValue value = mockery.mock(FieldValue.class, "valFieldVal");
    mockery.checking(new Expectations() {

      {
        exactly(1).of(template).getTemplate();
        will(returnValue(
            IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream(
            "scripts/groovy/GroovyTestValidatorGenerator.groovy"))));
        exactly(1).of(value).getValue();
        will(returnValue(CONTENT));
        exactly(1).of(field).getValue();
        will(returnValue(value));
        FieldDef fieldDef = mockery.mock(FieldDef.class, "valFieldDef");
        exactly(1).of(field).getFieldDef();
        will(returnValue(fieldDef));
        ValidatorDef valDef = mockery.mock(ValidatorDef.class, "valDef");
        exactly(1).of(fieldDef).getCustomValidators();
        will(returnValue(Collections.singleton(valDef)));
        exactly(1).of(field).getName();
        will(returnValue(REP_NAME));
        final ResourceUri rUri = mockery.mock(ResourceUri.class, "valRUri");
        exactly(1).of(valDef).getUri();
        will(returnValue(rUri));
        exactly(1).of(rUri).getValue();
        will(returnValue("iUri"));
        exactly(1).of(rUri).getType();
        will(returnValue(ResourceUri.Type.INTERNAL));
        final ContentId contentId = mockery.mock(ContentId.class, "valContentId");
        exactly(1).of(content).getContentId();
        will(returnValue(contentId));
        final WorkspaceId wId = mockery.mock(WorkspaceId.class, "valWId");
        exactly(1).of(contentId).getWorkspaceId();
        will(returnValue(wId));
        exactly(1).of(template).getTemplateType();
        will(returnValue(ValidatorType.GROOVY));
        exactly(1).of(valDef).getParameters();
        will(returnValue(Collections.emptyMap()));
      }
    });
    Assert.assertFalse(provider.isValidField(content, field));
  }

  @Test
  public void testMultiGroovyRepGeneration() throws IOException {
    TypeRepresentationGenerator generator = new GroovyRepresentationGenerator();
    final RepresentationTemplate template = mockery.mock(RepresentationTemplate.class);
    WorkspaceAPIImpl impl = new WorkspaceAPIImpl() {

      @Override
      public RepresentationTemplate getRepresentationTemplate(WorkspaceId id, String name) {
        return template;
      }
    };
    impl.setRepresentationGenerators(Collections.singletonMap(TemplateType.GROOVY, generator));
    final RepresentationProvider provider = new RepresentationProviderImpl();
    final WorkspaceAPI api = impl;
    registerBeanFactory(api);
    final Content content = mockery.mock(Content.class);
    final Field field = mockery.mock(Field.class);
    final FieldValue value = mockery.mock(FieldValue.class);
    final ContentType type = mockery.mock(ContentType.class);
    final Map<String, RepresentationDef> reps = mockery.mock(Map.class, "repMap");
    final RepresentationDef def = mockery.mock(RepresentationDef.class);
    final int threadCount = new Random().nextInt(100);
    logger.info("Number of parallel threads " + threadCount);
    mockery.checking(new Expectations() {

      {
        exactly(threadCount).of(template).getTemplateType();
        will(returnValue(TemplateType.GROOVY));
        exactly(threadCount).of(template).getTemplate();
        final byte[] toByteArray = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream(
            "scripts/groovy/GroovyTestRepresentationGenerator.groovy"));
        will(returnValue(toByteArray));
        exactly(threadCount).of(template).getName();
        will(returnValue(REP_NAME));
        for (int i = 0; i < threadCount; ++i) {
          exactly(1).of(value).getValue();
          will(returnValue(String.valueOf(i)));
        }
        exactly(threadCount).of(field).getValue();
        will(returnValue(value));
        exactly(threadCount).of(content).getField(with(Expectations.<String>anything()));
        will(returnValue(field));
        exactly(threadCount).of(content).getContentDefinition();
        will(returnValue(type));
        final ContentId contentId = mockery.mock(ContentId.class);
        exactly(2 * threadCount).of(content).getContentId();
        will(returnValue(contentId));
        final WorkspaceId wId = mockery.mock(WorkspaceId.class);
        exactly(threadCount).of(contentId).getWorkspaceId();
        will(returnValue(wId));
        exactly(2 * threadCount).of(type).getRepresentationDefs();
        will(returnValue(reps));
        exactly(2 * threadCount).of(reps).get(with(REP_NAME));
        will(returnValue(def));
        exactly(threadCount).of(def).getParameters();
        will(returnValue(Collections.emptyMap()));
        exactly(threadCount).of(def).getMIMEType();
        will(returnValue(GroovyGeneratorTest.MIME_TYPE));
        final ResourceUri rUri = mockery.mock(ResourceUri.class);
        exactly(threadCount).of(def).getResourceUri();
        will(returnValue(rUri));
        exactly(threadCount).of(rUri).getValue();
        will(returnValue("iUri"));
      }
    });
    final Set<String> set = Collections.synchronizedSet(new LinkedHashSet<String>(threadCount));
    final List<String> list = Collections.synchronizedList(new ArrayList<String>(threadCount));
    final AtomicInteger integer = new AtomicInteger(0);
    Threads group = new Threads();
    for (int i = 0; i < threadCount; ++i) {
      group.addThread(new Thread(new Runnable() {

        public void run() {
          Representation representation = provider.getRepresentation(REP_NAME, type, content);
          Assert.assertNotNull(representation);
          Assert.assertEquals(REP_NAME, representation.getName());
          final String rep = StringUtils.newStringUtf8(representation.getRepresentation());
          list.add(rep);
          set.add(rep);
          Assert.assertEquals(GroovyGeneratorTest.MIME_TYPE, representation.getMimeType());
          integer.addAndGet(1);
        }
      }));
    }
    group.start();
    try {
      group.join();
    }
    catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
    }
    logger.info("Generated reps list: " + list);
    logger.info("Generated reps set: " + set);
    Assert.assertEquals(threadCount, integer.get());
    Assert.assertEquals(threadCount, list.size());
    Assert.assertEquals(threadCount, set.size());
  }
}
