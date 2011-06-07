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

import com.google.inject.PrivateModule;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.smartitengineering.cms.api.content.Content;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.factory.content.WriteableContent;
import com.smartitengineering.cms.api.impl.type.ContentTypeImpl;
import com.smartitengineering.cms.api.impl.workspace.WorkspaceIdImpl;
import com.smartitengineering.cms.api.type.CollectionDataType;
import com.smartitengineering.cms.api.type.CompositeDataType;
import com.smartitengineering.cms.api.type.ContentDataType;
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.type.ContentType.DefinitionType;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.api.type.FieldDef;
import com.smartitengineering.cms.api.type.MutableContentType;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.cms.repo.dao.impl.AbstractRepoAdapterHelper;
import com.smartitengineering.cms.repo.dao.impl.AbstractRepositoryDomain;
import com.smartitengineering.cms.repo.dao.impl.RepositoryDaoImpl;
import com.smartitengineering.cms.type.xml.XMLParserIntrospector;
import com.smartitengineering.cms.type.xml.XmlParser;
import com.smartitengineering.dao.common.CommonDao;
import com.smartitengineering.dao.common.CommonReadDao;
import com.smartitengineering.dao.common.CommonWriteDao;
import com.smartitengineering.util.bean.adapter.AbstractAdapterHelper;
import com.smartitengineering.util.bean.adapter.GenericAdapter;
import com.smartitengineering.util.bean.adapter.GenericAdapterImpl;
import com.sun.codemodel.ClassType;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
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
   * The workspace id for the specified content type resource. It will be used to DI in the adapter and DAO Impl
   * @parameter
   * @required
   */
  private String workspaceId;
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
      if (!contentTypeResource.exists() || !contentTypeResource.isFile()) {
        throw new MojoExecutionException("'contentTypeResource' file either does not exist or is not a file: " +
            (contentTypeResource == null ? null : contentTypeResource.getAbsolutePath()));
      }
      if (StringUtils.isBlank(workspaceId) || workspaceId.split(":").length != 2) {
        throw new MojoExecutionException("Workspace ID not specified or is not in format 'namespace:name'");
      }
      String wIds[] = workspaceId.split(":");
      final WorkspaceIdImpl dummyIdImpl = new WorkspaceIdImpl();
      dummyIdImpl.setGlobalNamespace(wIds[0]);
      dummyIdImpl.setName(wIds[1]);
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
    Map<ContentTypeId, JDefinedClass> helpers = new LinkedHashMap<ContentTypeId, JDefinedClass>();
    Map<ContentTypeId, JDefinedClass> modules = new LinkedHashMap<ContentTypeId, JDefinedClass>();
    // Create the classes
    for (MutableContentType type : types) {
      classes.put(type.getContentTypeID(), generateClassForType(type, codeModel));
    }
    // Set parent content types as appropriate
    for (MutableContentType type : types) {
      final JDefinedClass typeClass = classes.get(type.getContentTypeID());
      if (type.getParent() != null) {
        final JClass parentClass = classes.get(type.getParent());
        typeClass._extends(parentClass);
      }
    }
    // Create members and their accessors
    for (MutableContentType type : types) {
      generateFields(type.getOwnFieldDefs().values(), classes.get(type.getContentTypeID()), classes, codeModel);
    }
    // Create helpers and google guice module for concrete type definitions
    JClass parentClass = codeModel.ref(AbstractRepoAdapterHelper.class);
    JClass privateModuleClass = codeModel.ref(PrivateModule.class);
    // Initialize common classes for Google Guice bindings
    JClass typeLiteral = codeModel.ref(TypeLiteral.class);
    JClass commonDao = codeModel.ref(CommonDao.class);
    JClass commonReadDao = codeModel.ref(CommonReadDao.class);
    JClass commonWriteDao = codeModel.ref(CommonWriteDao.class);
    JClass commonDaoImpl = codeModel.ref(RepositoryDaoImpl.class);
    JClass genericAdapter = codeModel.ref(GenericAdapter.class).narrow(Content.class);
    JClass genericAdapterImpl = codeModel.ref(GenericAdapterImpl.class).narrow(Content.class);
    JClass abstractHelper = codeModel.ref(AbstractAdapterHelper.class).narrow(Content.class);
    JClass singletonScope = codeModel.ref(Singleton.class);
    JClass workspaceIdClass = codeModel.ref(WorkspaceId.class);
    JClass apiClass = codeModel.ref(SmartContentAPI.class);
    String[] wId = workspaceId.split(":");
    for (MutableContentType type : types) {
      if (type.getDefinitionType().equals(DefinitionType.CONCRETE_TYPE)) {
        final ContentTypeId contentTypeID = type.getContentTypeID();
        JDefinedClass definedClass = classes.get(contentTypeID);
        final JDefinedClass helperClass;
        {
          final String helperClassName = new StringBuilder(contentTypeID.getNamespace()).append('.').append(contentTypeID.
              getName()).append("Helper").toString();
          helperClass = codeModel._class(helperClassName);
          helpers.put(contentTypeID, helperClass);
          helperClass._extends(parentClass.narrow(definedClass));
          {
            JMethod forwardConversion = helperClass.method(JMod.PROTECTED, JCodeModel.boxToPrimitive.get(Void.class),
                                                           "mergeContentIntoBean");
            forwardConversion.param(Content.class, "fromBean");
            forwardConversion.param(definedClass, "toBean");
          }
          {
            JMethod reverseConversion = helperClass.method(JMod.PROTECTED, JCodeModel.boxToPrimitive.get(Void.class),
                                                           "mergeBeanIntoContent");
            reverseConversion.param(definedClass, "fromBean");
            reverseConversion.param(WriteableContent.class, "toBean");
          }
          {
            JMethod instanceCreation = helperClass.method(JMod.PROTECTED, definedClass, "newTInstance");
            JBlock block = instanceCreation.body();
            block._return(JExpr._new(definedClass));
          }
        }
        {
          final String moduleClassName = new StringBuilder(contentTypeID.getNamespace()).append('.').append(contentTypeID.
              getName()).append("Module").toString();
          final JDefinedClass moduleClass = codeModel._class(moduleClassName);
          modules.put(contentTypeID, moduleClass);
          moduleClass._extends(privateModuleClass);
          JMethod configureMethod = moduleClass.method(JMod.PUBLIC, JCodeModel.boxToPrimitive.get(Void.class),
                                                       "configure");
          JBlock block = configureMethod.body();
          final JDefinedClass commonDaoType;
          {
            final JClass narrowedCommonDaoTypeLiteral = typeLiteral.narrow(commonDao.narrow(definedClass).narrow(
                String.class));
            commonDaoType = moduleClass._class(new StringBuilder(type.getContentTypeID().getName()).append(
                "CommonDaoType").toString());
            commonDaoType._extends(narrowedCommonDaoTypeLiteral);
            final JClass narrowedDaoImplTypeLiteral = typeLiteral.narrow(commonDaoImpl.narrow(definedClass));
            final JDefinedClass daoImplType = moduleClass._class(new StringBuilder(type.getContentTypeID().getName()).
                append("DaoImplType").toString());
            daoImplType._extends(narrowedDaoImplTypeLiteral);
            block.add(JExpr.invoke("bind").arg(JExpr._new(commonDaoType)).invoke("to").arg(JExpr._new(daoImplType)).
                invoke("in").arg(singletonScope.dotclass()));
            block.add(JExpr.invoke("binder").invoke("expose").arg(JExpr._new(commonDaoType)));
          }
          {
            final JClass narrowedReadTypeLiteral = typeLiteral.narrow(commonReadDao.narrow(definedClass).narrow(
                String.class));
            final JDefinedClass readDaoType = moduleClass._class(new StringBuilder(type.getContentTypeID().getName()).
                append("ReadDaoType").toString());
            readDaoType._extends(narrowedReadTypeLiteral);
            block.add(JExpr.invoke("bind").arg(JExpr._new(readDaoType)).invoke("to").arg(JExpr._new(commonDaoType)).
                invoke("in").arg(singletonScope.dotclass()));
            block.add(JExpr.invoke("binder").invoke("expose").arg(JExpr._new(readDaoType)));
          }
          {
            final JClass narrowedWriteTypeLiteral = typeLiteral.narrow(commonWriteDao.narrow(definedClass));
            final JDefinedClass writeDaoType = moduleClass._class(new StringBuilder(type.getContentTypeID().getName()).
                append("WriteDaoType").toString());
            writeDaoType._extends(narrowedWriteTypeLiteral);
            block.add(JExpr.invoke("bind").arg(JExpr._new(writeDaoType)).invoke("to").arg(JExpr._new(commonDaoType)).
                invoke("in").arg(singletonScope.dotclass()));
            block.add(JExpr.invoke("binder").invoke("expose").arg(JExpr._new(writeDaoType)));
          }
          {
            JClass lGenericAdapter = genericAdapter.narrow(definedClass);
            JClass lGenericAdapterImpl = genericAdapterImpl.narrow(definedClass);
            JClass lAbstractHelper = abstractHelper.narrow(definedClass);
            final JDefinedClass adapterHelperType = moduleClass._class(new StringBuilder(
                type.getContentTypeID().getName()).append("AdapterType").toString());
            adapterHelperType._extends(typeLiteral.narrow(lGenericAdapter));
            final JDefinedClass adapterImplType = moduleClass._class(new StringBuilder(type.getContentTypeID().getName()).
                append("AdapterImplType").toString());
            adapterImplType._extends(typeLiteral.narrow(lGenericAdapterImpl));
            final JDefinedClass abstractHelperType = moduleClass._class(new StringBuilder(type.getContentTypeID().
                getName()).append("AdapterHelperType").toString());
            abstractHelperType._extends(typeLiteral.narrow(lAbstractHelper));
            final JDefinedClass helperType = moduleClass._class(new StringBuilder(type.getContentTypeID().getName()).
                append("AdapterHelperImplType").toString());
            helperType._extends(typeLiteral.narrow(helperClass));
            block.add(JExpr.invoke("bind").arg(JExpr._new(adapterHelperType)).invoke("to").arg(JExpr._new(
                adapterImplType)).invoke("in").arg(singletonScope.dotclass()));
            block.add(JExpr.invoke("bind").arg(JExpr._new(abstractHelperType)).invoke("to").arg(JExpr._new(helperType)).
                invoke("in").arg(singletonScope.dotclass()));
            final JInvocation workspaceApiInvocation = apiClass.staticInvoke("getInstance").invoke("getWorkspaceApi").
                invoke("createWorkspaceId");
            final JInvocation _new = workspaceApiInvocation.arg(wId[0]).arg(wId[1]);
            block.add(JExpr.invoke("bind").arg(workspaceIdClass.dotclass()).invoke("toInstance").arg(_new));
          }
        }
      }
    }
  }

  protected JDefinedClass generateClassForType(MutableContentType type, JCodeModel codeModel) throws
      JClassAlreadyExistsException {
    ContentTypeId typeId = type.getContentTypeID();
    int mod = JMod.PUBLIC;
    final DefinitionType defType = type.getDefinitionType();
    if (defType.equals(ContentType.DefinitionType.ABSTRACT_COMPONENT)) {
      // Extend nothing
      mod = mod | JMod.ABSTRACT;
    }
    else if (defType.equals(ContentType.DefinitionType.ABSTRACT_TYPE)) {
      mod = mod | JMod.ABSTRACT;
    }
    JDefinedClass definedClass = codeModel._class(mod, new StringBuilder(typeId.getNamespace()).append('.').append(typeId.
        getName()).toString(), ClassType.CLASS);
    if (defType.equals(ContentType.DefinitionType.ABSTRACT_TYPE) || defType.equals(
        ContentType.DefinitionType.CONCRETE_TYPE)) {
      JClass clazz = codeModel.ref(AbstractRepositoryDomain.class);
      definedClass._extends(clazz.narrow(definedClass));
    }
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
