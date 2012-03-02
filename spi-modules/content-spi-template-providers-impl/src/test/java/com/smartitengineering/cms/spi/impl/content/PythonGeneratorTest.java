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
import com.smartitengineering.cms.api.factory.workspace.WorkspaceAPI;
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
import com.smartitengineering.cms.spi.impl.content.template.PythonRepresentationGenerator;
import com.smartitengineering.cms.spi.impl.content.template.PythonValidatorGenerator;
import com.smartitengineering.cms.spi.impl.content.template.PythonVariationGenerator;
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
public class PythonGeneratorTest {

  public static final String CONTENT = "content";
  private Mockery mockery;
  public static final String REP_NAME = "test";
  private final transient Logger logger = LoggerFactory.getLogger(getClass());

  @Before
  public void setupAPIAndSPI() throws ClassNotFoundException {
    mockery = new JUnit3Mockery();
    GroovyGeneratorTest.setupAPI(mockery);
  }

  @Test
  public void testPythonRepGeneration() throws IOException {
    TypeRepresentationGenerator generator = new PythonRepresentationGenerator();
    final RepresentationTemplate template = mockery.mock(RepresentationTemplate.class);
    WorkspaceAPIImpl impl = new WorkspaceAPIImpl() {

      @Override
      public RepresentationTemplate getRepresentationTemplate(WorkspaceId id, String name) {
        return template;
      }
    };
    impl.setRepresentationGenerators(Collections.singletonMap(TemplateType.JASPER, generator));
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
        will(returnValue(TemplateType.JASPER));
        exactly(1).of(template).getTemplate();
        will(returnValue(
            IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("scripts/python/test-script.py"))));
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
    Assert.assertEquals(CONTENT, StringUtils.newStringUtf8(representation.getRepresentation()));
    Assert.assertEquals(GroovyGeneratorTest.MIME_TYPE, representation.getMimeType());
    mockery.assertIsSatisfied();
  }

  @Test
  public void testPythonValGenerationWithFalseAssertion() throws IOException {
    TypeFieldValidator generator = new PythonValidatorGenerator();
    final ValidatorTemplate template = mockery.mock(ValidatorTemplate.class);
    WorkspaceAPIImpl impl = new WorkspaceAPIImpl() {

      @Override
      public ValidatorTemplate getValidatorTemplate(WorkspaceId workspaceId, String name) {
        return template;
      }
    };
    impl.setValidatorGenerators(Collections.singletonMap(ValidatorType.PYTHON, generator));
    ValidatorProvider provider = new ValidatorProviderImpl();
    registerBeanFactory(impl);
    final Content content = mockery.mock(Content.class, "valContent");
    final Field field = mockery.mock(Field.class, "valField");
    final FieldValue value = mockery.mock(FieldValue.class, "valFieldVal");
    mockery.checking(new Expectations() {

      {
        exactly(1).of(template).getTemplate();
        will(returnValue(
            IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("scripts/python/val-script.py"))));
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
        will(returnValue(ValidatorType.PYTHON));
        exactly(1).of(valDef).getParameters();
        will(returnValue(Collections.emptyMap()));
      }
    });
    Assert.assertFalse(provider.isValidField(content, field));
    mockery.assertIsSatisfied();
  }

  @Test
  public void testPythonValGenerationWithTrueAssertion() throws IOException {
    TypeFieldValidator generator = new PythonValidatorGenerator();
    final ValidatorTemplate template = mockery.mock(ValidatorTemplate.class, "valTemp");
    WorkspaceAPIImpl impl = new WorkspaceAPIImpl() {

      @Override
      public ValidatorTemplate getValidatorTemplate(WorkspaceId workspaceId, String name) {
        return template;
      }
    };
    impl.setValidatorGenerators(Collections.singletonMap(ValidatorType.GROOVY, generator));
    ValidatorProvider provider = new ValidatorProviderImpl();
    registerBeanFactory(impl);
    final Content content = mockery.mock(Content.class, "valContent2");
    final Field field = mockery.mock(Field.class, "valField2");
    final FieldValue value = mockery.mock(FieldValue.class, "valFieldVal2");
    mockery.checking(new Expectations() {

      {
        exactly(1).of(template).getTemplate();
        will(returnValue(
            IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("scripts/python/val-script-2.py"))));
        exactly(1).of(value).getValue();
        will(returnValue(CONTENT));
        exactly(1).of(field).getValue();
        will(returnValue(value));
        FieldDef fieldDef = mockery.mock(FieldDef.class, "valFieldDef2");
        exactly(1).of(field).getFieldDef();
        will(returnValue(fieldDef));
        ValidatorDef valDef = mockery.mock(ValidatorDef.class, "valDef2");
        exactly(1).of(fieldDef).getCustomValidators();
        will(returnValue(Collections.singleton(valDef)));
        final ResourceUri rUri = mockery.mock(ResourceUri.class, "valRUri2");
        exactly(1).of(valDef).getUri();
        will(returnValue(rUri));
        exactly(1).of(rUri).getValue();
        will(returnValue("iUri"));
        exactly(1).of(rUri).getType();
        will(returnValue(ResourceUri.Type.INTERNAL));
        final ContentId contentId = mockery.mock(ContentId.class, "valContentId2");
        exactly(1).of(content).getContentId();
        will(returnValue(contentId));
        final WorkspaceId wId = mockery.mock(WorkspaceId.class, "valWId2");
        exactly(1).of(contentId).getWorkspaceId();
        will(returnValue(wId));
        exactly(1).of(template).getTemplateType();
        will(returnValue(ValidatorType.GROOVY));
        exactly(1).of(valDef).getParameters();
        will(returnValue(Collections.emptyMap()));
      }
    });
    Assert.assertTrue(provider.isValidField(content, field));
    mockery.assertIsSatisfied();
  }

  @Test
  public void testPythonVarGeneration() throws IOException {
    TypeVariationGenerator generator = new PythonVariationGenerator();
    final VariationTemplate template = mockery.mock(VariationTemplate.class);
    WorkspaceAPIImpl impl = new WorkspaceAPIImpl() {

      @Override
      public VariationTemplate getVariationTemplate(WorkspaceId id, String name) {
        return template;
      }
    };
    impl.setVariationGenerators(Collections.singletonMap(TemplateType.PYTHON, generator));
    VariationProvider provider = new VariationProviderImpl();
    registerBeanFactory(impl);
    final Field field = mockery.mock(Field.class, "varField");
    final FieldValue value = mockery.mock(FieldValue.class, "varFieldVal");
    final FieldDef fieldDef = mockery.mock(FieldDef.class);
    final Map<String, VariationDef> vars = mockery.mock(Map.class, "varMap");
    final VariationDef def = mockery.mock(VariationDef.class);
    final Content content = mockery.mock(Content.class, "varContent");
    mockery.checking(new Expectations() {

      {
        exactly(1).of(template).getTemplateType();
        will(returnValue(TemplateType.PYTHON));
        exactly(1).of(template).getTemplate();
        will(returnValue(
            IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("scripts/python/var-script.py"))));
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
        exactly(1).of(def).getMIMEType();
        will(returnValue(GroovyGeneratorTest.MIME_TYPE));
        exactly(1).of(def).getParameters();
        will(returnValue(Collections.emptyMap()));
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
    mockery.assertIsSatisfied();
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
}
