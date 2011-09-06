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
package com.smartitengineering.cms.api.impl.content;

import com.smartitengineering.cms.api.content.Content;
import com.smartitengineering.cms.api.content.ContentId;
import com.smartitengineering.cms.api.content.Field;
import com.smartitengineering.cms.api.content.FieldValue;
import com.smartitengineering.cms.api.content.Representation;
import com.smartitengineering.cms.api.content.Variation;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.factory.content.ContentLoader;
import com.smartitengineering.cms.api.impl.content.RepresentationImpl;
import com.smartitengineering.cms.api.impl.content.VariationImpl;
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.type.FieldDef;
import com.smartitengineering.cms.api.type.RepresentationDef;
import com.smartitengineering.cms.api.type.VariationDef;
import com.smartitengineering.cms.api.workspace.RepresentationTemplate;
import com.smartitengineering.cms.api.workspace.ValidatorTemplate;
import com.smartitengineering.cms.api.workspace.VariationTemplate;
import com.smartitengineering.cms.api.content.template.TypeFieldValidator;
import com.smartitengineering.cms.api.content.template.TypeRepresentationGenerator;
import com.smartitengineering.cms.api.content.template.TypeVariationGenerator;
import com.smartitengineering.cms.api.impl.content.template.GroovyRepresentationGenerator;
import com.smartitengineering.cms.api.impl.content.template.GroovyValidatorGenerator;
import com.smartitengineering.cms.api.impl.content.template.GroovyVariationGenerator;
import com.smartitengineering.util.bean.BeanFactoryRegistrar;
import com.smartitengineering.util.bean.SimpleBeanFactory;
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
public class GroovyGeneratorTest {

  public static final String CONTENT = "content";
  private static final Mockery mockery = new JUnit3Mockery();
  public static final String REP_NAME = "test";
  public static final String MIME_TYPE = "application/json";

  @BeforeClass
  public static void setupAPIAndSPI() throws ClassNotFoundException {
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
    final Content content = mockery.mock(Content.class);
    final Field field = mockery.mock(Field.class);
    final FieldValue value = mockery.mock(FieldValue.class);
    final ContentType type = mockery.mock(ContentType.class);
    final Map<String, RepresentationDef> reps = mockery.mock(Map.class, "repMap");
    final RepresentationDef def = mockery.mock(RepresentationDef.class);
    mockery.checking(new Expectations() {

      {
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
        exactly(1).of(content).getContentId();
        will(returnValue(mockery.mock(ContentId.class)));
        exactly(1).of(type).getRepresentationDefs();
        will(returnValue(reps));
        exactly(1).of(reps).get(with(REP_NAME));
        will(returnValue(def));
        exactly(1).of(def).getMIMEType();
        will(returnValue(GroovyGeneratorTest.MIME_TYPE));
      }
    });
    Representation representation = generator.getRepresentation(template, content, REP_NAME,
                                                                Collections.<String, String>emptyMap());
    Assert.assertNotNull(representation);
    Assert.assertEquals(REP_NAME, representation.getName());
    Assert.assertEquals(GroovyGeneratorTest.MIME_TYPE, representation.getMimeType());
    Assert.assertEquals(CONTENT, StringUtils.newStringUtf8(representation.getRepresentation()));
  }

  @Test
  public void testGroovyVarGeneration() throws IOException {
    TypeVariationGenerator generator = new GroovyVariationGenerator();
    final VariationTemplate template = mockery.mock(VariationTemplate.class);
    final Field field = mockery.mock(Field.class, "varField");
    final FieldValue value = mockery.mock(FieldValue.class, "varFieldVal");
    final Content content = mockery.mock(Content.class, "varContent");
    final FieldDef fieldDef = mockery.mock(FieldDef.class);
    final Map<String, VariationDef> vars = mockery.mock(Map.class, "varMap");
    final VariationDef def = mockery.mock(VariationDef.class);
    mockery.checking(new Expectations() {

      {
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
        exactly(1).of(content).getContentId();
        will(returnValue(mockery.mock(ContentId.class, "varId")));
        exactly(1).of(fieldDef).getVariations();
        will(returnValue(vars));
        exactly(1).of(vars).get(with(REP_NAME));
        will(returnValue(def));
        exactly(1).of(def).getMIMEType();
        will(returnValue(GroovyGeneratorTest.MIME_TYPE));
      }
    });
    Variation representation = generator.getVariation(template, content, field, REP_NAME, Collections.<String, String>
        emptyMap());
    Assert.assertNotNull(representation);
    Assert.assertEquals(REP_NAME, representation.getName());
    Assert.assertEquals(GroovyGeneratorTest.MIME_TYPE, representation.getMimeType());
    Assert.assertEquals(CONTENT, StringUtils.newStringUtf8(representation.getVariation()));
  }

  @Test
  public void testGroovyValGeneration() throws IOException {
    TypeFieldValidator generator = new GroovyValidatorGenerator();
    final ValidatorTemplate template = mockery.mock(ValidatorTemplate.class);
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
      }
    });
    Assert.assertFalse(generator.isValid(template, field, Collections.<String, String>emptyMap()));
  }
}
