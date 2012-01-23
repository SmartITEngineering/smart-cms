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
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.factory.workspace.WorkspaceAPI;
import com.smartitengineering.cms.api.impl.workspace.WorkspaceAPIImpl;
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.type.RepresentationDef;
import com.smartitengineering.cms.api.type.ResourceUri;
import com.smartitengineering.cms.api.workspace.RepresentationTemplate;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.cms.spi.content.RepresentationProvider;
import com.smartitengineering.cms.spi.content.template.TypeRepresentationGenerator;
import com.smartitengineering.cms.spi.impl.content.template.JasperRepresentationGenerator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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
public class JasperGeneratorTest {

  private static final String CONTENT = "field value";
  private Mockery mockery;
  private static final String REP_NAME = "test";
  private final transient Logger logger = LoggerFactory.getLogger(getClass());

  @Before
  public void setupAPIAndSPI() throws ClassNotFoundException {
    mockery = new JUnit3Mockery();
    GroovyGeneratorTest.setupAPI(mockery);
  }

  @Test
  public void testJasperRepGeneration() throws IOException {
    TypeRepresentationGenerator generator = new JasperRepresentationGenerator();
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
    final Content content = mockery.mock(Content.class, "Content");
    final Field field = mockery.mock(Field.class, "FIeld");
    final FieldValue value = mockery.mock(FieldValue.class, "FieldValue");
    final Map<String, Field> fieldMap = mockery.mock(Map.class, "Field Map");
    final ContentType type = mockery.mock(ContentType.class, "Content Type");
    final Map<String, RepresentationDef> reps = mockery.mock(Map.class, "repMap");
    final RepresentationDef def = mockery.mock(RepresentationDef.class, "RepDef");
    final ContentId contentId = mockery.mock(ContentId.class, "Content Id");
    final String[] formats = new String[]{JasperRepresentationGenerator.DOCX, JasperRepresentationGenerator.ODS,
      JasperRepresentationGenerator.ODT, JasperRepresentationGenerator.PDF, JasperRepresentationGenerator.RTF,
      JasperRepresentationGenerator.TXT, JasperRepresentationGenerator.XLS, JasperRepresentationGenerator.XML};
    final Map<String, String> map = new HashMap<String, String>();
    mockery.checking(new Expectations() {

      {
        exactly(1 + formats.length).of(template).getTemplateType();
        will(returnValue(TemplateType.JASPER));
        exactly(1 + formats.length).of(template).getTemplate();
        will(returnValue(
            IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("scripts/jasper/test.xml"))));
        exactly(1 + formats.length).of(template).getName();
        will(returnValue(REP_NAME));
        exactly(1 + formats.length).of(value).getValue();
        will(returnValue(CONTENT));
        exactly(1 + formats.length).of(field).getValue();
        will(returnValue(value));
        exactly(1 + formats.length).of(fieldMap).get(with(Expectations.<String>anything()));
        will(returnValue(field));
        exactly(1 + formats.length).of(content).getFields();
        will(returnValue(fieldMap));
        exactly(3 + formats.length * 3).of(content).getContentDefinition();
        will(returnValue(type));
        exactly(3 + formats.length * 3).of(content).getContentId();
        will(returnValue(contentId));
        final WorkspaceId wId = mockery.mock(WorkspaceId.class);
        exactly(2 + formats.length * 2).of(contentId).getWorkspaceId();
        will(returnValue(wId));
        exactly(2 + formats.length * 2).of(type).getRepresentationDefs();
        will(returnValue(reps));
        exactly(2 + formats.length * 2).of(reps).get(with(REP_NAME));
        will(returnValue(def));
        exactly(1 + formats.length).of(def).getParameters();
        will(returnValue(map));
        exactly(1 + formats.length).of(def).getMIMEType();
        will(returnValue(GroovyGeneratorTest.MIME_TYPE));
        exactly(1 + formats.length).of(type).getDisplayName();
        will(returnValue("Display Name"));
        final ResourceUri rUri = mockery.mock(ResourceUri.class);
        exactly(1 + formats.length).of(def).getResourceUri();
        will(returnValue(rUri));
        exactly(1 + formats.length).of(rUri).getValue();
        will(returnValue("iUri"));
      }
    });
    map.put("output", "csv");
    Representation representation = provider.getRepresentation(REP_NAME, type, content);
    FileOutputStream stream = new FileOutputStream(new File("./target/test.csv"));
    stream.write(representation.getRepresentation());
    stream.close();
    FileInputStream inputStream = new FileInputStream(new File("./src/test/resources/scripts/jasper/test.csv"));
    Assert.assertArrayEquals(IOUtils.toByteArray(inputStream), representation.getRepresentation());
    inputStream.close();
    for (String format : formats) {
      map.clear();
      map.put("output", format);
      representation = provider.getRepresentation(REP_NAME, type, content);
      stream = new FileOutputStream(new File("./target/test." + format));
      stream.write(representation.getRepresentation());
      stream.close();
    }
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
