/*
 *
 * This is a simple Content Management System (CMS)
 * Copyright (C) 2011  Imran M Yousuf (imyousuf@smartitengineering.com)
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
package com.smartitengineering.cms.maven.dto.generator.plugin;

import com.smartitengineering.cms.api.impl.type.ContentTypeImpl;
import com.smartitengineering.cms.api.impl.workspace.WorkspaceIdImpl;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.api.type.MutableContentType;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.cms.type.xml.XMLParserIntrospector;
import com.smartitengineering.cms.type.xml.XmlParser;
import com.sun.codemodel.JCodeModel;
import nu.xom.Element;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.project.MavenProject;

/**
 * Goal which touches a timestamp file. Configure any one of contentTypesResourceConfig, contentTypeResourceConfig and
 * contentTypeResource. Not configuring any one of them or more than one would result runtime exception.
 *
 * @goal generate
 * 
 * @phase generate-sources
 */
public class PojoGeneratorMojo extends AbstractMojo {

  /**
   * Location of the file.
   * @parameter expression="${generated.sources.directory}"
   * @required
   */
  private File outputDirectory;
  /**
   * A collection of content types for generating POJOs for beans in multiple workspaces. Content types could either be
   * a file or a classpath resource. If either is missing it would result a classpath exception
   * @parameter
   */
  private Map<String, String> contentTypesResourceConfig;
  /**
   * A content type file
   * @parameter
   */
  private File contentTypeResource;
  /**
   * The Maven project instance for the executing project.
   *
   * @parameter expression="${project}"
   * @required
   */
  private MavenProject project;

  public void execute()
      throws MojoExecutionException {
    File f = outputDirectory;
    if (!f.exists()) {
      f.mkdirs();
    }
    if ((contentTypesResourceConfig == null || contentTypesResourceConfig.isEmpty()) && contentTypeResource == null) {
      throw new MojoExecutionException("Parameters not specified properly. Any one and only one of " +
          "contentTypesResourceConfig, contentTypeResourceConfig and contentTypeResource must be specified!");
    }
    JCodeModel codeModel = new JCodeModel();
    Set<MutableContentType> types = new LinkedHashSet<MutableContentType>();
    if (contentTypeResource != null) {
      if ((!contentTypeResource.exists() || !contentTypeResource.isFile())) {
        throw new MojoExecutionException("'contentTypeResource' file either does not exist or is not a file: " +
            (contentTypeResource == null ? null : contentTypeResource.getAbsolutePath()));
      }
      final WorkspaceIdImpl dummyIdImpl = new WorkspaceIdImpl();
      dummyIdImpl.setGlobalNamespace("a");
      dummyIdImpl.setName("b");
      types.addAll(parseContentType(dummyIdImpl, contentTypeResource));
    }
    else {
      for (Entry<String, String> contentTypeRsrc : contentTypesResourceConfig.entrySet()) {
        final String key = contentTypeRsrc.getKey();
        if (StringUtils.isBlank(key)) {
          throw new MojoExecutionException("'contentTypesResourceConfig' can not have empty key");
        }
        final String[] wId = key.split(":");
        if (wId.length != 2) {
          throw new MojoExecutionException("'contentTypesResourceConfig' key must be in 'string:string' format: " + key);
        }
        final String value = contentTypeRsrc.getValue();
        if (StringUtils.isBlank(value)) {
          throw new MojoExecutionException("'contentTypesResourceConfig' can not have empty resource config");
        }
        File file = new File(value);
        if (file.exists() && file.isFile()) {
          WorkspaceIdImpl idImpl = new WorkspaceIdImpl();
          idImpl.setGlobalNamespace(wId[0]);
          idImpl.setName(wId[1]);
          types.addAll(parseContentType(idImpl, file));
        }
        else {
          throw new MojoExecutionException("'contentTypesResourceConfig' can not have a resource config that is neither" +
              "a file nor a classpath resoruce: " + value);
        }
      }
    }
    try {
      generateCode(codeModel, types);
      codeModel.build(outputDirectory);
      project.addCompileSourceRoot(outputDirectory.getAbsolutePath());
    }
    catch (Exception ex) {
      throw new MojoExecutionException(ex.getMessage(), ex);
    }
  }

  protected Collection<MutableContentType> parseContentType(final WorkspaceId id, File file) throws
      MojoExecutionException {
    try {
      final XmlParser parser = new XmlParser(id, new FileInputStream(file),
                                             new XMLParserIntrospector() {

        public MutableContentType createMutableContentType() {
          return new ContentTypeImpl();
        }

        public void processMutableContentType(MutableContentType type, Element element) {
        }
      });
      return parser.parse();
    }
    catch (Exception ex) {
      throw new MojoExecutionException(ex.getMessage(), ex);
    }
  }

  private void generateCode(JCodeModel codeModel, Set<MutableContentType> types) throws Exception {
    for (MutableContentType type : types) {
      ContentTypeId typeId = type.getContentTypeID();
      codeModel._class(new StringBuilder(typeId.getNamespace()).append('.').append(typeId.getName()).toString());
    }
  }
}
