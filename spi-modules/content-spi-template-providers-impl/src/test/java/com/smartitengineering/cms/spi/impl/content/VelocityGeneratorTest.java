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
import com.smartitengineering.cms.api.type.VariationDef;
import com.smartitengineering.cms.api.workspace.RepresentationTemplate;
import com.smartitengineering.cms.api.workspace.VariationTemplate;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.cms.spi.content.RepresentationProvider;
import com.smartitengineering.cms.spi.content.VariationProvider;
import com.smartitengineering.cms.spi.content.template.TypeRepresentationGenerator;
import com.smartitengineering.cms.spi.content.template.TypeVariationGenerator;
import com.smartitengineering.cms.spi.impl.content.template.VelocityRepresentationGenerator;
import com.smartitengineering.cms.spi.impl.content.template.VelocityVariationGenerator;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.io.IOUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit3.JUnit3Mockery;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author imyousuf
 */
public class VelocityGeneratorTest {

  public static final String CONTENT = "content";
  private static final Mockery mockery = new JUnit3Mockery();
  public static final String REP_NAME = "test";

  @BeforeClass
  public static void setupAPIAndSPI() throws ClassNotFoundException {
    GroovyGeneratorTest.setupAPI(mockery);
  }

  @Test
  public void testVelocityRepGeneration() throws IOException {
    TypeRepresentationGenerator generator = new VelocityRepresentationGenerator();
    final RepresentationTemplate template = mockery.mock(RepresentationTemplate.class);
    WorkspaceAPIImpl impl = new WorkspaceAPIImpl() {

      @Override
      public RepresentationTemplate getRepresentationTemplate(WorkspaceId id, String name) {
        return template;
      }
    };
    impl.setRepresentationGenerators(Collections.singletonMap(TemplateType.VELOCITY, generator));
    RepresentationProvider provider = new RepresentationProviderImpl();
    final WorkspaceAPI api = impl;
    registerBeanFactory(api);
    final Content content = mockery.mock(Content.class);
    final Field field = mockery.mock(Field.class);
    final FieldValue value = mockery.mock(FieldValue.class);
    final Map<String, Field> fieldMap = mockery.mock(Map.class);
    final ContentType type = mockery.mock(ContentType.class);
    final Map<String, RepresentationDef> reps = mockery.mock(Map.class, "repMap");
    final RepresentationDef def = mockery.mock(RepresentationDef.class);
    mockery.checking(new Expectations() {

      {
        exactly(1).of(template).getTemplateType();
        will(returnValue(TemplateType.VELOCITY));
        exactly(1).of(template).getTemplate();
        will(returnValue(
            IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("scripts/velocity/test-template.vm"))));
        exactly(1).of(template).getName();
        will(returnValue(REP_NAME));
        exactly(1).of(value).getValue();
        will(returnValue(CONTENT));
        exactly(1).of(field).getValue();
        will(returnValue(value));
        exactly(1).of(fieldMap).get(with(Expectations.<String>anything()));
        will(returnValue(field));
        exactly(1).of(content).getFields();
        will(returnValue(fieldMap));
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
  }

  @Test
  public void testVelocityVarGeneration() throws IOException {
    TypeVariationGenerator generator = new VelocityVariationGenerator();
    final VariationTemplate template = mockery.mock(VariationTemplate.class);
    WorkspaceAPIImpl impl = new WorkspaceAPIImpl() {

      @Override
      public VariationTemplate getVariationTemplate(WorkspaceId id, String name) {
        return template;
      }
    };
    impl.setVariationGenerators(Collections.singletonMap(TemplateType.VELOCITY, generator));
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
        will(returnValue(TemplateType.VELOCITY));
        exactly(1).of(template).getTemplate();
        will(returnValue(
            IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("scripts/velocity/var-template.vm"))));
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
