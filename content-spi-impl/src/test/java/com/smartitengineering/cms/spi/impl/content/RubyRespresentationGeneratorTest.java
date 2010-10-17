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

import com.smartitengineering.cms.api.content.Content;
import com.smartitengineering.cms.api.content.Field;
import com.smartitengineering.cms.api.content.FieldValue;
import com.smartitengineering.cms.api.content.Representation;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.factory.content.ContentLoader;
import com.smartitengineering.cms.api.impl.content.RepresentationImpl;
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.type.RepresentationDef;
import com.smartitengineering.cms.api.workspace.RepresentationTemplate;
import com.smartitengineering.cms.spi.impl.content.template.RubyRepresentationGenerator;
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
public class RubyRespresentationGeneratorTest {

  public static final String CONTENT = "content";
  private static final Mockery mockery = new JUnit3Mockery();
  public static final String REP_NAME = "test";

  @BeforeClass
  public static void setupAPIAndSPI() throws ClassNotFoundException {
    final ContentLoader mock = mockery.mock(ContentLoader.class);
    mockery.checking(new Expectations() {

      {
        exactly(1).of(mock).createMutableRepresentation();
        will(returnValue(new RepresentationImpl()));
      }
    });
    if (SmartContentAPI.getInstance() == null) {
      SimpleBeanFactory simpleBeanFactory = new SimpleBeanFactory(Collections.<String, Object>singletonMap(
          "apiContentLoader", mock));
      BeanFactoryRegistrar.registerBeanFactory(SmartContentAPI.CONTEXT_NAME, simpleBeanFactory);
    }
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
        exactly(1).of(type).getRepresentationDefs();
        will(returnValue(reps));
        exactly(1).of(reps).get(with(REP_NAME));
        will(returnValue(def));
        exactly(1).of(def).getMIMEType();
        will(returnValue(GroovyRespresentationGeneratorTest.MIME_TYPE));
      }
    });
    Representation representation = generator.getRepresentation(template, content);
    Assert.assertEquals(REP_NAME, representation.getName());
    Assert.assertEquals(CONTENT, StringUtils.newStringUtf8(representation.getRepresentation()));
    Assert.assertEquals(GroovyRespresentationGeneratorTest.MIME_TYPE, representation.getMimeType());
  }
}
