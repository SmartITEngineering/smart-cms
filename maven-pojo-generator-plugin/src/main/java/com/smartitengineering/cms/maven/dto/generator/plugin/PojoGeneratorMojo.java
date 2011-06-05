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
import com.smartitengineering.cms.api.type.CollectionDataType;
import com.smartitengineering.cms.api.type.CompositeDataType;
import com.smartitengineering.cms.api.type.ContentDataType;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.api.type.FieldDef;
import com.smartitengineering.cms.api.type.MutableContentType;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.cms.type.xml.XMLParserIntrospector;
import com.smartitengineering.cms.type.xml.XmlParser;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import nu.xom.Element;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
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

  protected void generateCode(JCodeModel codeModel, Set<MutableContentType> types) throws Exception {
    Map<ContentTypeId, JDefinedClass> classes = new LinkedHashMap<ContentTypeId, JDefinedClass>();
    for (MutableContentType type : types) {
      classes.put(type.getContentTypeID(), generateClassForType(type, codeModel));
    }
    for (MutableContentType type : types) {
      final JDefinedClass typeClass = classes.get(type.getContentTypeID());
      if (type.getParent() != null) {
        final JClass parentClass = classes.get(type.getParent());
        typeClass._extends(parentClass);
      }
    }
    for (MutableContentType type : types) {
      generateFields(type.getOwnFieldDefs().values(), classes.get(type.getContentTypeID()), classes, codeModel);
    }
  }

  protected JDefinedClass generateClassForType(MutableContentType type, JCodeModel codeModel) throws
      JClassAlreadyExistsException {
    ContentTypeId typeId = type.getContentTypeID();
    JDefinedClass definedClass = codeModel._class(new StringBuilder(typeId.getNamespace()).append('.').append(typeId.
        getName()).toString());
    return definedClass;
  }

  protected void generateFields(Collection<FieldDef> fields, JDefinedClass definedClass,
                                Map<ContentTypeId, JDefinedClass> classes, JCodeModel codeModel) throws
      JClassAlreadyExistsException, ClassNotFoundException {
    for (FieldDef def : fields) {
      final Class fieldClass;
      final JType jType;
      final String name = def.getName();
      final String getterSetterSuffix = new StringBuilder().append(("" + name.charAt(0)).toUpperCase()).append(name.
          substring(1)).toString();
      switch (def.getValueDef().getType()) {
        case BOOLEAN:
          fieldClass = Boolean.class;
          jType = null;
          break;
        case STRING:
          fieldClass = String.class;
          jType = null;
          break;
        case CONTENT:
          ContentDataType contentDataType = (ContentDataType) def.getValueDef();
          fieldClass = null;
          final ContentTypeId typeDef = contentDataType.getTypeDef();
          jType = classes.get(typeDef);
          break;
        case DATE_TIME:
          fieldClass = Date.class;
          jType = null;
          break;
        case INTEGER:
          fieldClass = Integer.class;
          jType = null;
          break;
        case DOUBLE:
          fieldClass = Double.class;
          jType = null;
          break;
        case LONG:
          fieldClass = Long.class;
          jType = null;
          break;
        case OTHER:
          fieldClass = byte[].class;
          jType = null;
          break;
        case COLLECTION:
          fieldClass = null;
          final String itemType;
          CollectionDataType collectionDataType = (CollectionDataType) def.getValueDef();
          switch (collectionDataType.getItemDataType().getType()) {
            case BOOLEAN:
              itemType = Boolean.class.getName();
              break;
            case STRING:
              itemType = String.class.getName();
              break;
            case CONTENT:
              itemType = classes.get(((ContentDataType) collectionDataType.getItemDataType()).getTypeDef()).fullName();
              break;
            case DATE_TIME:
              itemType = Date.class.getName();
              break;
            case INTEGER:
              itemType = Integer.class.getName();
              break;
            case DOUBLE:
              itemType = Double.class.getName();
              break;
            case LONG:
              itemType = Long.class.getName();
              break;
            case OTHER:
              itemType = byte[].class.getName();
              break;
            case COMPOSITE: {
              CompositeDataType compositeDataType = (CompositeDataType) collectionDataType.getItemDataType();
              ContentDataType composedOfContent = compositeDataType.getEmbeddedContentType();
              if (compositeDataType.getOwnComposition() != null && !compositeDataType.getOwnComposition().isEmpty()) {
                JDefinedClass compositeFieldClass = definedClass._class(JMod.STATIC | JMod.PUBLIC, getterSetterSuffix);
                if (composedOfContent != null) {
                  compositeFieldClass._extends(classes.get(composedOfContent.getTypeDef()));
                }
                generateFields(compositeDataType.getOwnComposition(), compositeFieldClass, classes, codeModel);
                itemType = compositeFieldClass.fullName();
              }
              else if (composedOfContent != null) {
                itemType = classes.get(composedOfContent.getTypeDef()).fullName();
              }
              else {
                itemType = "Object";
              }
              break;
            }
            default:
              itemType = "Object";
          }
          jType = codeModel.parseType(new StringBuilder("java.util.List<").append(itemType).append('>').toString());
          break;
        case COMPOSITE: {
          CompositeDataType compositeDataType = (CompositeDataType) def.getValueDef();
          ContentDataType composedOfContent = compositeDataType.getEmbeddedContentType();
          if (compositeDataType.getOwnComposition() != null && !compositeDataType.getOwnComposition().isEmpty()) {
            fieldClass = null;
            JDefinedClass compositeFieldClass = definedClass._class(JMod.STATIC | JMod.PUBLIC, getterSetterSuffix);
            if (composedOfContent != null) {
              compositeFieldClass._extends(classes.get(composedOfContent.getTypeDef()));
            }
            generateFields(compositeDataType.getOwnComposition(), compositeFieldClass, classes, codeModel);
            jType = compositeFieldClass;
          }
          else if (composedOfContent != null) {
            fieldClass = null;
            jType = classes.get(composedOfContent.getTypeDef());
          }
          else {
            fieldClass = Object.class;
            jType = null;
          }
          break;
        }
        default:
          fieldClass = Object.class;
          jType = null;
      }
      final String getterName = new StringBuilder("get").append(getterSetterSuffix).toString();
      final String setterName = new StringBuilder("set").append(getterSetterSuffix).toString();
      final JFieldVar fieldVar;
      final JMethod getterMethod;
      final JMethod setterMethod;
      final JVar paramVar;
      if (jType == null) {
        fieldVar = definedClass.field(JMod.PROTECTED, fieldClass, name);
        getterMethod = definedClass.method(JMod.PUBLIC, fieldClass, getterName);
        setterMethod = definedClass.method(JMod.PUBLIC, JCodeModel.boxToPrimitive.get(Void.class), setterName);
        paramVar = setterMethod.param(fieldClass, name);
      }
      else {
        fieldVar = definedClass.field(JMod.PROTECTED, jType, name);
        getterMethod = definedClass.method(JMod.PUBLIC, jType, getterName);
        setterMethod = definedClass.method(JMod.PUBLIC, JCodeModel.boxToPrimitive.get(Void.class), setterName);
        paramVar = setterMethod.param(jType, name);
      }
      final JBlock getterBlock = getterMethod.body();
      getterBlock._return(fieldVar);
      JBlock setterBlock = setterMethod.body();
      setterBlock.assign(JExpr._this().ref(fieldVar), paramVar);
    }
  }
}
