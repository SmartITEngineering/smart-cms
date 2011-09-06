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
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.type.FieldDef;
import com.smartitengineering.cms.api.type.RepresentationDef;
import com.smartitengineering.cms.api.type.VariationDef;
import com.smartitengineering.cms.api.workspace.RepresentationTemplate;
import com.smartitengineering.cms.api.workspace.ValidatorTemplate;
import com.smartitengineering.cms.api.workspace.VariationTemplate;
import com.smartitengineering.cms.api.content.template.TypeFieldValidator;
import com.smartitengineering.cms.api.content.template.TypeVariationGenerator;
import com.smartitengineering.cms.api.impl.content.template.RubyRepresentationGenerator;
import com.smartitengineering.cms.api.impl.content.template.RubyValidatorGenerator;
import com.smartitengineering.cms.api.impl.content.template.RubyVariationGenerator;
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
public class RubyGeneratorTest {

  public static final String CONTENT = "content";
  private static final Mockery mockery = new JUnit3Mockery();
  public static final String REP_NAME = "test";

  @BeforeClass
  public static void setupAPIAndSPI() throws ClassNotFoundException {
    GroovyGeneratorTest.setupAPI(mockery);
  }

  @Test
  public void testRubyRepGeneration() throws IOException {
    RubyRepresentationGenerator generator = new RubyRepresentationGenerator();
    final RepresentationTemplate template = mockery.mock(RepresentationTemplate.class);
    final Content content = mockery.mock(Content.class);
    final Field field = mockery.mock(Field.class);
    final FieldValue value = mockery.mock(FieldValue.class);
    final ContentType type = mockery.mock(ContentType.class);
    final Map<String, RepresentationDef> reps = mockery.mock(Map.class, "repMap");
    final RepresentationDef def = mockery.mock(RepresentationDef.class);
    final Map<String, Field> fieldMap = mockery.mock(Map.class);
    mockery.checking(new Expectations() {

      {
        exactly(1).of(template).getTemplate();
        will(returnValue(
            IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("scripts/ruby/test-script.rb"))));
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
    Assert.assertEquals(REP_NAME, representation.getName());
    Assert.assertEquals(CONTENT, StringUtils.newStringUtf8(representation.getRepresentation()));
    Assert.assertEquals(GroovyGeneratorTest.MIME_TYPE, representation.getMimeType());
  }

  @Test
  public void testRubyVarGeneration() throws IOException {
    TypeVariationGenerator generator = new RubyVariationGenerator();
    final VariationTemplate template = mockery.mock(VariationTemplate.class);
    final Field field = mockery.mock(Field.class, "varField");
    final FieldValue value = mockery.mock(FieldValue.class, "varFieldVal");
    final FieldDef fieldDef = mockery.mock(FieldDef.class);
    final Map<String, VariationDef> vars = mockery.mock(Map.class, "varMap");
    final VariationDef def = mockery.mock(VariationDef.class);
    final Content content = mockery.mock(Content.class, "varContent");
    mockery.checking(new Expectations() {

      {
        exactly(1).of(template).getTemplate();
        will(returnValue(
            IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("scripts/ruby/var-script.rb"))));
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
  public void testRubyValGeneration() throws IOException {
    TypeFieldValidator generator = new RubyValidatorGenerator();
    final ValidatorTemplate template = mockery.mock(ValidatorTemplate.class);
    final Field field = mockery.mock(Field.class, "valField");
    final FieldValue value = mockery.mock(FieldValue.class, "valFieldVal");
    mockery.checking(new Expectations() {

      {
        exactly(1).of(template).getTemplate();
        will(returnValue(
            IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("scripts/ruby/val-script.rb"))));
        exactly(1).of(value).getValue();
        will(returnValue(CONTENT));
        exactly(1).of(field).getValue();
        will(returnValue(value));
      }
    });
    Assert.assertFalse(generator.isValid(template, field, Collections.<String, String>emptyMap()));
  }
}
