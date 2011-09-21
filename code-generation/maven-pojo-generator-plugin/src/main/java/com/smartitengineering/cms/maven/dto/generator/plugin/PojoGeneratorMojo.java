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

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.PrivateModule;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import com.smartitengineering.cms.api.content.CompositeFieldValue;
import com.smartitengineering.cms.api.content.Content;
import com.smartitengineering.cms.api.content.ContentId;
import com.smartitengineering.cms.api.content.Field;
import com.smartitengineering.cms.api.content.FieldValue;
import com.smartitengineering.cms.api.content.MutableCompositeFieldValue;
import com.smartitengineering.cms.api.content.MutableField;
import com.smartitengineering.cms.api.content.MutableFieldValue;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.factory.content.ContentLoader;
import com.smartitengineering.cms.api.factory.content.WriteableContent;
import com.smartitengineering.cms.api.impl.type.ContentTypeImpl;
import com.smartitengineering.cms.api.impl.workspace.WorkspaceIdImpl;
import com.smartitengineering.cms.api.type.CollectionDataType;
import com.smartitengineering.cms.api.type.CompositeDataType;
import com.smartitengineering.cms.api.type.ContentDataType;
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.type.ContentType.DefinitionType;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.api.type.EnumDataType;
import com.smartitengineering.cms.api.type.FieldDef;
import com.smartitengineering.cms.api.type.FieldValueType;
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
import com.smartitengineering.domain.PersistentDTO;
import com.smartitengineering.util.bean.adapter.AbstractAdapterHelper;
import com.smartitengineering.util.bean.adapter.GenericAdapter;
import com.smartitengineering.util.bean.adapter.GenericAdapterImpl;
import com.sun.codemodel.ClassType;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JForLoop;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.codemodel.JWhileLoop;
import nu.xom.Element;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
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
   * Default package for aggregating modules
   * @parameter
   * @required
   */
  private String packageForGuiceMasterModule;
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
    if (StringUtils.isBlank(packageForGuiceMasterModule)) {
      throw new MojoExecutionException("Parameter 'packageForGuiceMasterModule' can not be blank");
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

  protected void generateIoCClasses(JCodeModel codeModel,
                                    Set<MutableContentType> types,
                                    Map<ContentTypeId, JDefinedClass> classes,
                                    Map<ContentTypeId, JDefinedClass> helpers,
                                    Map<ContentTypeId, JDefinedClass> modules) throws JClassAlreadyExistsException {
    // Create helpers and google guice module for concrete type definitions
    JClass privateModuleClass = codeModel.ref(PrivateModule.class);
    // Initialize common classes for Google Guice bindings
    JClass typeLiteral = codeModel.ref(TypeLiteral.class);
    JClass commonDao = codeModel.ref(CommonDao.class);
    JClass commonReadDao = codeModel.ref(CommonReadDao.class);
    JClass commonWriteDao = codeModel.ref(CommonWriteDao.class);
    JClass commonDaoImpl = codeModel.ref(RepositoryDaoImpl.class);
    JClass genericAdapter = codeModel.ref(GenericAdapter.class).narrow(Content.class);
    JClass classRef = codeModel.ref(Class.class);
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
        final JDefinedClass helperClass = generateHelper(type, codeModel, classes, types);
        helpers.put(contentTypeID, helperClass);
        {
          final String moduleClassName = new StringBuilder(contentTypeID.getNamespace()).append(".guice.").append(contentTypeID.
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
            final JClass beanClass = classRef.narrow(definedClass.wildcard());
            JDefinedClass classType = moduleClass._class(new StringBuilder(type.getContentTypeID().getName()).append(
                "ClassType").toString());
            classType._extends(typeLiteral.narrow(beanClass));
            block.add(JExpr.invoke("bind").arg(JExpr._new(classType)).invoke("toInstance").arg(definedClass.dotclass()));
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
          //String TypeLiteral
          final JDefinedClass stringType;
          {
            final JClass narrowedReadTypeLiteral = typeLiteral.narrow(String.class);
            stringType = moduleClass._class("StringType");
            stringType._extends(narrowedReadTypeLiteral);
          }
          //Traverse fields to make their DIs
          {
            Collection<FieldDef> defs = getAllFields(type, types).values();
            generateAssociationIoCForFields(defs, codeModel, stringType, typeLiteral, moduleClass, block, types, classes,
                                            type.getContentTypeID().getName(), new HashSet<ContentTypeId>());
          }
        }
      }
    }
    generateMasterModule(modules, codeModel);
  }

  protected Set<FieldDef> getAllComposedFields(CompositeDataType compositeDataType,
                                               Set<? extends ContentType> types) {
    Set<FieldDef> compositeFields = new LinkedHashSet<FieldDef>();
    if (compositeDataType.getEmbeddedContentType() != null) {
      ContentType currentType = getType(types, compositeDataType.getEmbeddedContentType().getTypeDef());
      if (currentType != null) {
        compositeFields.addAll(getAllFields(currentType, types).values());
      }
    }
    compositeFields.addAll(compositeDataType.getOwnComposition());
    return compositeFields;
  }

  protected Map<String, FieldDef> getAllFields(ContentType currentType, Set<? extends ContentType> types) {
    Map<String, FieldDef> defs = new LinkedHashMap<String, FieldDef>();
    boolean hasMore = true;
    while (hasMore) {
      Map<String, FieldDef> ownFields = currentType.getOwnFieldDefs();
      for (Entry<String, FieldDef> ownField : ownFields.entrySet()) {
        if (!defs.containsKey(ownField.getKey())) {
          defs.put(ownField.getKey(), ownField.getValue());
        }
      }
      if (currentType.getParent() != null) {
        final ContentTypeId parent = currentType.getParent();
        currentType = getType(types, parent);
        hasMore = currentType != null;
      }
      else {
        hasMore = false;
      }
    }
    return defs;
  }

  protected ContentType getType(Set<? extends ContentType> types, final ContentTypeId id) {
    for (ContentType type : types) {
      if (type.getContentTypeID().equals(id)) {
        return type;
      }
    }
    return null;
  }

  protected JFieldVar initializeDao(ContentTypeId typeId, JDefinedClass helperClass, JCodeModel model,
                                    Map<ContentTypeId, JDefinedClass> classes) {
    final JDefinedClass typeDef = classes.get(typeId);
    final String varClassName = typeDef.name();
    String readDaoName = new StringBuilder(new StringBuilder().append(
        ("" + varClassName.charAt(0)).toLowerCase()).append(varClassName.substring(1)).toString()).append(
        "ReadDaos").toString();
    JFieldVar readDaoVar = helperClass.fields().get(readDaoName);
    if (readDaoVar == null) {
      readDaoVar = helperClass.field(JMod.PRIVATE, model.ref(Map.class).narrow(String.class).narrow(model.ref(
          CommonReadDao.class).narrow(typeDef.wildcard()).narrow(
          String.class)), readDaoName);
      readDaoVar.annotate(Inject.class);
    }
    return readDaoVar;
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
      final ContentTypeId contentTypeID = type.getContentTypeID();
      classes.put(contentTypeID, generateClassForType(type, codeModel));
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
    generateIoCClasses(codeModel, types, classes, helpers, modules);
  }

  protected JDefinedClass generateHelper(final ContentType contentType, JCodeModel codeModel,
                                         Map<ContentTypeId, JDefinedClass> classes, Set<? extends ContentType> types)
      throws JClassAlreadyExistsException {
    ContentTypeId contentTypeID = contentType.getContentTypeID();
    final JDefinedClass definedClass = classes.get(contentTypeID);
    final JClass parentClass = codeModel.ref(AbstractRepoAdapterHelper.class);
    final JDefinedClass helperClass;
    final String helperClassName = new StringBuilder(contentTypeID.getNamespace()).append(".helpers.").append(contentTypeID.
        getName()).append("Helper").toString();
    helperClass = codeModel._class(helperClassName);
    helperClass._extends(parentClass.narrow(definedClass));
    ContentType currentType = contentType;
    Map<String, FieldDef> defs = getAllFields(currentType, types);
    {
      JMethod forwardConversion = helperClass.method(JMod.PROTECTED, JCodeModel.boxToPrimitive.get(Void.class),
                                                     "mergeContentIntoBean");
      JVar content = forwardConversion.param(Content.class, "fromBean");
      JVar toBean = forwardConversion.param(definedClass, "toBean");
      JBlock block = forwardConversion.body();
      generateForwardBlocks(defs.values(), block, content, toBean, codeModel, definedClass, helperClass, classes, types,
                            "");
    }
    {
      JMethod reverseConversion = helperClass.method(JMod.PROTECTED, JCodeModel.boxToPrimitive.get(Void.class),
                                                     "mergeBeanIntoContent");
      JVar fromBean = reverseConversion.param(definedClass, "fromBean");
      JVar wContent = reverseConversion.param(WriteableContent.class, "toBean");
      JBlock block = reverseConversion.body();
      JInvocation staticInvoke = codeModel.ref(SmartContentAPI.class).staticInvoke("getInstance");
      block._if(staticInvoke.eq(JExpr._null()))._then()._throw(JExpr._new(codeModel.ref(IllegalStateException.class)).
          arg("Smart Content API can not be null!"));
      JVar contentLoader = block.decl(JMod.FINAL, codeModel.ref(ContentLoader.class), "contentLoader",
                                      staticInvoke.invoke("getContentLoader"));
      JVar fieldDefs = block.decl(codeModel.ref(Map.class).narrow(String.class).narrow(FieldDef.class), "fieldDefs",
                                  wContent.invoke("getContentDefinition").invoke("getFieldDefs"));
      generateReverseBlocks(defs.values(), block, fromBean, wContent, contentLoader, fieldDefs, codeModel, definedClass,
                            classes, types, "");
    }
    {
      JMethod instanceCreation = helperClass.method(JMod.PROTECTED, definedClass, "newTInstance");
      JBlock block = instanceCreation.body();
      block._return(JExpr._new(definedClass));
    }
    return helperClass;
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
        case ENUM: {
          JDefinedClass enumClass = definedClass._enum(JMod.PUBLIC | JMod.STATIC, getterSetterSuffix);
          EnumDataType enumDataType = (EnumDataType) def.getValueDef();
          Collection<String> choices = enumDataType.getChoices();
          for (String choice : choices) {
            enumClass.enumConstant(choice);
          }
          fieldClass = null;
          jType = enumClass;
          break;
        }
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
            case ENUM: {
              JDefinedClass enumClass = definedClass._enum(JMod.PUBLIC | JMod.STATIC, getterSetterSuffix);
              EnumDataType enumDataType = (EnumDataType) collectionDataType.getItemDataType();
              Collection<String> choices = enumDataType.getChoices();
              for (String choice : choices) {
                enumClass.enumConstant(choice);
              }
              itemType = enumClass.fullName();
              break;
            }
            case COMPOSITE: {
              CompositeDataType compositeDataType = (CompositeDataType) collectionDataType.getItemDataType();
              ContentDataType composedOfContent = compositeDataType.getEmbeddedContentType();
              final JDefinedClass composedOfClass;
              if (composedOfContent != null) {
                composedOfClass = classes.get(composedOfContent.getTypeDef());
              }
              else {
                composedOfClass = null;
              }
              if (compositeDataType.getOwnComposition() != null && !compositeDataType.getOwnComposition().isEmpty()) {
                JDefinedClass compositeFieldClass = definedClass._class(JMod.STATIC | JMod.PUBLIC, getterSetterSuffix);
                if (composedOfContent != null) {
                  compositeFieldClass._extends(composedOfClass);
                }
                generateFields(compositeDataType.getOwnComposition(), compositeFieldClass, classes, codeModel);
                itemType = compositeFieldClass.fullName();
              }
              else if (composedOfContent != null) {
                itemType = composedOfClass.fullName();
              }
              else {
                itemType = "Object";
              }
              break;
            }
            default:
              itemType = "Object";
          }
          jType =
          codeModel.parseType(new StringBuilder("java.util.Collection<").append(itemType).append('>').toString());
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
      final JFieldVar propConstant = definedClass.field(JMod.PUBLIC | JMod.FINAL | JMod.STATIC, String.class,
                                                        new StringBuilder("PROPERTY").append('_').append(name.
          toUpperCase()).toString(), JExpr.lit(name));
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

  protected void generateForwardBlocks(Collection<FieldDef> values, JBlock block, JVar from, JVar toBean,
                                       JCodeModel model, JDefinedClass currentClass, JDefinedClass helperClass,
                                       Map<ContentTypeId, JDefinedClass> classes, Set<? extends ContentType> types,
                                       String prefix) {
    final JClass contentIdRef = model.ref(ContentId.class);
    for (FieldDef def : values) {
      final String name = def.getName();
      final String varName = getVarName(prefix, name);
      final String fieldVar = new StringBuilder(varName).append("Field").toString();
      JVar field = block.decl(model.ref(Field.class), fieldVar, from.invoke("getField").arg(name));
      JConditional conditional = block._if(field.ne(JExpr._null()));
      JBlock valBlock = conditional._then();
      switch (def.getValueDef().getType()) {
        case BOOLEAN:
          setField(valBlock, model, field, name, toBean, Boolean.class, prefix);
          break;
        case STRING:
          setField(valBlock, model, field, name, toBean, String.class, prefix);
          break;
        case ENUM: {
          final String getterSetterSuffix = new StringBuilder().append(("" + name.charAt(0)).toUpperCase()).append(name.
              substring(1)).toString();
          final String setterName = new StringBuilder("set").append(getterSetterSuffix).toString();
          final String fieldValVar = getVarName(prefix, new StringBuilder(name).append("Val").toString());
          JClass enumClass = ((JClass) currentClass.fields().get(name).type());
          JVar fieldVal = block.decl(model.ref(FieldValue.class).narrow(String.class), fieldValVar, field.invoke(
              "getValue"));
          JConditional valCond = block._if(fieldVal.ne(JExpr._null()));
          JBlock setBlock = valCond._then();
          setBlock.add(toBean.invoke(setterName).arg(enumClass.staticInvoke("valueOf").arg(fieldVal.invoke("getValue"))));
          break;
        }
        case DATE_TIME:
          setField(valBlock, model, field, name, toBean, Date.class, prefix);
          break;
        case INTEGER:
          setField(valBlock, model, field, name, toBean, Integer.class, prefix);
          break;
        case DOUBLE:
          setField(valBlock, model, field, name, toBean, Double.class, prefix);
          break;
        case LONG:
          setField(valBlock, model, field, name, toBean, Long.class, prefix);
          break;
        case OTHER:
          setField(valBlock, model, field, name, toBean, byte[].class, prefix);
          break;
        case CONTENT: {
          ContentDataType contentDataType = (ContentDataType) def.getValueDef();
          final JDefinedClass typeDef = classes.get(contentDataType.getTypeDef());
          if (typeDef != null) {
            JFieldVar readDaoVar = initializeDao(contentDataType.getTypeDef(), helperClass, model, classes);
            if (readDaoVar != null) {
              final String getterSetterSuffix = new StringBuilder().append(("" + name.charAt(0)).toUpperCase()).append(name.
                  substring(1)).toString();
              final String setterName = new StringBuilder("set").append(getterSetterSuffix).toString();
              final String fieldValVar = new StringBuilder(varName).append("Val").toString();
              JVar fieldVal = valBlock.decl(model.ref(FieldValue.class).narrow(contentIdRef), fieldValVar,
                                            field.invoke("getValue"));
              JConditional valCond = valBlock._if(fieldVal.ne(JExpr._null()));
              JBlock setBlock = valCond._then();
              final JVar contentIdVal = setBlock.decl(contentIdRef, getVarName(prefix, "contentIdVal"), fieldVal.invoke(
                  "getValue"));
              JConditional idValCond = setBlock._if(contentIdVal.ne(JExpr._null()));
              JBlock typeDaoBlock = idValCond._then();
              String readDaoName = new StringBuilder(new StringBuilder().append(
                  ("" + typeDef.name().charAt(0)).toLowerCase()).append(typeDef.name().substring(1)).toString()).append(
                  "ReadDao").toString();
              JVar typeDao = typeDaoBlock.decl(model.ref(CommonReadDao.class).narrow(typeDef.wildcard()).narrow(
                  String.class), readDaoName, readDaoVar.invoke("get").arg(contentIdVal.invoke("getContent").invoke(
                  "getContentDefinition").invoke("toString")));
              JConditional daoValCond = typeDaoBlock._if(typeDao.ne(JExpr._null()));
              daoValCond._then().add(toBean.invoke(setterName).arg(typeDao.invoke("getById").arg(
                  contentIdVal.invoke("toString"))));
            }
          }
          break;
        }
        case COMPOSITE: {
          CompositeDataType compositeDataType = (CompositeDataType) def.getValueDef();
          Set<FieldDef> compositeFields = getAllComposedFields(compositeDataType, types);
          JClass compFieldVal = model.ref(FieldValue.class).narrow(model.ref(Collection.class).narrow(Field.class));
          final JVar fieldVal = valBlock.decl(compFieldVal, new StringBuilder(varName).append("FieldVal").toString(),
                                              field.invoke("getValue"));
          JDefinedClass compositeDefinition = findClass(compositeDataType, currentClass, classes, name);
          JDefinedClass newCompositeDefintion = findConcreteClass(compositeDefinition, classes);
          if (compositeDefinition != null && newCompositeDefintion != null) {
            JBlock validCompVal = valBlock._if(fieldVal.ne(JExpr._null()))._then();
            final JVar compositeToBean = validCompVal.decl(compositeDefinition, varName, JExpr._new(
                newCompositeDefintion));
            final JClass ref = model.ref(CompositeFieldValue.class);
            JVar compositeFromBean = validCompVal.decl(ref, new StringBuilder(varName).append("Val").toString(), JExpr.
                cast(ref, fieldVal));
            String compPrefix = new StringBuilder(varName).append('_').toString();
            generateForwardBlocks(compositeFields, validCompVal, compositeFromBean, compositeToBean, model,
                                  compositeDefinition, helperClass, classes, types, compPrefix);
            final String getterSetterSuffix = new StringBuilder().append(("" + name.charAt(0)).toUpperCase()).append(name.
                substring(1)).toString();
            final String setterName = new StringBuilder("set").append(getterSetterSuffix).toString();
            validCompVal.add(toBean.invoke(setterName).arg(compositeToBean));
          }
          break;
        }
        case COLLECTION:
          CollectionDataType collectionDataType = (CollectionDataType) def.getValueDef();
          switch (collectionDataType.getItemDataType().getType()) {
            case BOOLEAN:
              setCollectionField(valBlock, model, field, Boolean.class, name, toBean, prefix);
              break;
            case STRING:
              setCollectionField(valBlock, model, field, String.class, name, toBean, prefix);
              break;
            case ENUM: {
              JClass itClass = (JClass) ((JClass) currentClass.fields().get(name).type()).getTypeParameters().get(0);
              JVar resultVal = valBlock.decl(model.ref(Collection.class).narrow(itClass), getVarName(prefix,
                                                                                                     "beanList"),
                                             JExpr._new(model.ref(ArrayList.class).narrow(itClass)));
              final JClass narrowedCollection = model.ref(Collection.class).narrow(FieldValue.class);
              JVar fieldVal = valBlock.decl(model.ref(FieldValue.class).narrow(narrowedCollection),
                                            getVarName(prefix, "fieldVal"), field.invoke("getValue"));
              JConditional collectionValCond = valBlock._if(fieldVal.ne(JExpr._null()));
              JBlock validValBlock = collectionValCond._then();
              JVar collectionVal = validValBlock.decl(narrowedCollection, getVarName(prefix, "collectionVal"), fieldVal.
                  invoke(
                  "getValue"));
              JConditional validCollectionCond = validValBlock._if(collectionVal.ne(JExpr._null()).cand(collectionVal.
                  invoke(
                  "isEmpty").not()));
              JBlock iterateBlock = validCollectionCond._then();
              JVar iterator = iterateBlock.decl(model.ref(Iterator.class).narrow(FieldValue.class),
                                                getVarName(prefix, "iterator"),
                                                collectionVal.invoke("iterator"));
              JWhileLoop loop = iterateBlock._while(iterator.invoke("hasNext"));
              JBlock loopBlock = loop.body();
              JVar val = loopBlock.decl(model.ref(FieldValue.class).narrow(String.class), getVarName(prefix, "val"),
                                        iterator.invoke("next"));
              JConditional singleValCond = loopBlock._if(val.ne(JExpr._null()));
              singleValCond._then().add(resultVal.invoke("add").arg(itClass.staticInvoke("valueOf").arg(val.invoke(
                  "getValue"))));
              final String getterSetterSuffix = new StringBuilder().append(("" + name.charAt(0)).toUpperCase()).append(name.
                  substring(1)).toString();
              final String setterName = new StringBuilder("set").append(getterSetterSuffix).toString();
              valBlock.add(toBean.invoke(setterName).arg(resultVal));
              break;
            }
            case DATE_TIME:
              setCollectionField(valBlock, model, field, Date.class, name, toBean, prefix);
              break;
            case INTEGER:
              setCollectionField(valBlock, model, field, Integer.class, name, toBean, prefix);
              break;
            case DOUBLE:
              setCollectionField(valBlock, model, field, Double.class, name, toBean, prefix);
              break;
            case LONG:
              setCollectionField(valBlock, model, field, Long.class, name, toBean, prefix);
              break;
            case OTHER:
              setCollectionField(valBlock, model, field, byte[].class, name, toBean, prefix);
              break;
            case CONTENT: {
              ContentDataType contentDataType = (ContentDataType) collectionDataType.getItemDataType();
              final JDefinedClass typeDef = classes.get(contentDataType.getTypeDef());
              if (typeDef != null) {
                JFieldVar readDaoVar = initializeDao(contentDataType.getTypeDef(), helperClass, model, classes);
                if (readDaoVar != null) {
                  JVar resultVal = valBlock.decl(model.ref(Collection.class).narrow(typeDef), getVarName(prefix,
                                                                                                         "beanList"),
                                                 JExpr._new(model.ref(ArrayList.class).narrow(typeDef)));
                  final JClass narrowedCollection = model.ref(Collection.class).narrow(FieldValue.class);
                  JVar fieldVal = valBlock.decl(model.ref(FieldValue.class).narrow(narrowedCollection),
                                                getVarName(prefix, "fieldVal"), field.invoke("getValue"));
                  JConditional collectionValCond = valBlock._if(fieldVal.ne(JExpr._null()));
                  JBlock validValBlock = collectionValCond._then();
                  JVar collectionVal = validValBlock.decl(narrowedCollection, getVarName(prefix, "collectionVal"),
                                                          fieldVal.invoke("getValue"));
                  JConditional validCollectionCond = validValBlock._if(collectionVal.ne(JExpr._null()).cand(collectionVal.
                      invoke("isEmpty").not()));
                  JBlock iterateBlock = validCollectionCond._then();
                  JVar iterator = iterateBlock.decl(model.ref(Iterator.class).narrow(FieldValue.class),
                                                    getVarName(prefix, "iterator"), collectionVal.invoke("iterator"));
                  JWhileLoop loop = iterateBlock._while(iterator.invoke("hasNext"));
                  JBlock loopBlock = loop.body();
                  JVar val = loopBlock.decl(model.ref(FieldValue.class).narrow(contentIdRef), getVarName(prefix, "val"),
                                            iterator.invoke("next"));
                  JConditional singleValCond = loopBlock._if(val.ne(JExpr._null()));
                  final JBlock _then = singleValCond._then();
                  final JVar contentIdVal = _then.decl(contentIdRef, getVarName(prefix, "contentIdVal"),
                                                       val.invoke("getValue"));
                  JConditional idValCond = _then._if(contentIdVal.ne(JExpr._null()));
                  JBlock typeDaoBlock = idValCond._then();
                  String readDaoName = new StringBuilder(new StringBuilder().append(
                      ("" + typeDef.name().charAt(0)).toLowerCase()).append(typeDef.name().substring(1)).toString()).
                      append(
                      "ReadDao").toString();
                  JVar typeDao = typeDaoBlock.decl(model.ref(CommonReadDao.class).narrow(typeDef.wildcard()).narrow(
                      String.class), readDaoName, readDaoVar.invoke("get").arg(contentIdVal.invoke("getContent").invoke(
                      "getContentDefinition").invoke("toString")));
                  JConditional daoValCond = typeDaoBlock._if(typeDao.ne(JExpr._null()));
                  daoValCond._then().add(resultVal.invoke("add").arg(typeDao.invoke("getById").arg(
                      contentIdVal.invoke("toString"))));
                  final String getterSetterSuffix = new StringBuilder().append(("" + name.charAt(0)).toUpperCase()).
                      append(name.substring(1)).toString();
                  final String setterName = new StringBuilder("set").append(getterSetterSuffix).toString();
                  valBlock.add(toBean.invoke(setterName).arg(resultVal));
                }
              }
              break;
            }
            case COMPOSITE: {
              CompositeDataType compositeDataType = (CompositeDataType) collectionDataType.getItemDataType();
              Set<FieldDef> compositeFields = new LinkedHashSet<FieldDef>();
              if (compositeDataType.getEmbeddedContentType() != null) {
                ContentType currentType = getType(types, compositeDataType.getEmbeddedContentType().getTypeDef());
                if (currentType != null) {
                  compositeFields.addAll(getAllFields(currentType, types).values());
                }
              }
              compositeFields.addAll(compositeDataType.getOwnComposition());
              JDefinedClass compositeDefinition = findClass(compositeDataType, currentClass, classes, name);
              JDefinedClass newCompositeDefintion = findConcreteClass(compositeDefinition, classes);
              if (compositeDefinition != null && newCompositeDefintion != null) {
                JVar resultVal = valBlock.decl(model.ref(Collection.class).narrow(newCompositeDefintion),
                                               getVarName(prefix, "beanList"),
                                               JExpr._new(model.ref(ArrayList.class).narrow(newCompositeDefintion)));
                final JClass narrowedCollection = model.ref(Collection.class).narrow(FieldValue.class);
                JVar fieldVal = valBlock.decl(model.ref(FieldValue.class).narrow(narrowedCollection),
                                              getVarName(prefix, "fieldVal"), field.invoke("getValue"));
                JConditional collectionValCond = valBlock._if(fieldVal.ne(JExpr._null()));
                JBlock validValBlock = collectionValCond._then();
                JVar collectionVal = validValBlock.decl(narrowedCollection, getVarName(prefix, "collectionVal"),
                                                        fieldVal.invoke("getValue"));
                JConditional validCollectionCond = validValBlock._if(collectionVal.ne(JExpr._null()).cand(collectionVal.
                    invoke("isEmpty").not()));
                JBlock iterateBlock = validCollectionCond._then();
                JVar iterator = iterateBlock.decl(model.ref(Iterator.class).narrow(FieldValue.class),
                                                  getVarName(prefix, "iterator"), collectionVal.invoke("iterator"));
                JWhileLoop loop = iterateBlock._while(iterator.invoke("hasNext"));
                JBlock loopBlock = loop.body();
                JVar val = loopBlock.decl(model.ref(FieldValue.class).narrow(model.ref(Collection.class).narrow(
                    Field.class)), getVarName(prefix, "val"), iterator.invoke("next"));
                final JBlock _then = loopBlock;
                JBlock validCompVal = _then._if(val.ne(JExpr._null()))._then();
                final JVar compositeToBean = validCompVal.decl(compositeDefinition, varName, JExpr._new(
                    newCompositeDefintion));
                final JClass ref = model.ref(CompositeFieldValue.class);
                JVar compositeFromBean = validCompVal.decl(ref, new StringBuilder(varName).append("Val").toString(),
                                                           JExpr.cast(ref, val));
                String compPrefix = new StringBuilder(varName).append('_').toString();
                generateForwardBlocks(compositeFields, validCompVal, compositeFromBean, compositeToBean, model,
                                      compositeDefinition, helperClass, classes, types, compPrefix);
                final String getterSetterSuffix = new StringBuilder().append(("" + name.charAt(0)).toUpperCase()).append(name.
                    substring(1)).toString();
                final String setterName = new StringBuilder("set").append(getterSetterSuffix).toString();
                validCompVal.add(resultVal.invoke("add").arg(compositeToBean));
                valBlock.add(toBean.invoke(setterName).arg(resultVal));
              }
            }
          }
          break;
        default:
      }
    }
  }

  protected void setCollectionField(JBlock valBlock, JCodeModel model, JVar field, final Class narrowClass,
                                    final String name, JVar toBean, String prefix) {
    final JVar collectionVal = addToCollection(valBlock, model, field, narrowClass, prefix);
    final String getterSetterSuffix = new StringBuilder().append(("" + name.charAt(0)).toUpperCase()).append(name.
        substring(1)).toString();
    final String setterName = new StringBuilder("set").append(getterSetterSuffix).toString();
    valBlock.add(toBean.invoke(setterName).arg(collectionVal));
  }

  protected String getVarName(String prefix, String literal) {
    return new StringBuilder(prefix).append(literal).toString();
  }

  protected JVar addToCollection(JBlock valBlock, JCodeModel model, JVar field, final Class narrowClass, String prefix) {
    JVar resultVal = valBlock.decl(model.ref(Collection.class).narrow(narrowClass), getVarName(prefix, "beanList"),
                                   JExpr._new(model.ref(ArrayList.class).narrow(narrowClass)));
    final JClass narrowedCollection = model.ref(Collection.class).narrow(FieldValue.class);
    JVar fieldVal = valBlock.decl(model.ref(FieldValue.class).narrow(narrowedCollection),
                                  getVarName(prefix, "fieldVal"), field.invoke("getValue"));
    JConditional collectionValCond = valBlock._if(fieldVal.ne(JExpr._null()));
    JBlock validValBlock = collectionValCond._then();
    JVar collectionVal = validValBlock.decl(narrowedCollection, getVarName(prefix, "collectionVal"), fieldVal.invoke(
        "getValue"));
    JConditional validCollectionCond = validValBlock._if(collectionVal.ne(JExpr._null()).cand(collectionVal.invoke(
        "isEmpty").not()));
    JBlock iterateBlock = validCollectionCond._then();
    JVar iterator = iterateBlock.decl(model.ref(Iterator.class).narrow(FieldValue.class), getVarName(prefix, "iterator"),
                                      collectionVal.invoke("iterator"));
    JWhileLoop loop = iterateBlock._while(iterator.invoke("hasNext"));
    JBlock loopBlock = loop.body();
    JVar val = loopBlock.decl(model.ref(FieldValue.class).narrow(narrowClass), getVarName(prefix, "val"), iterator.
        invoke("next"));
    JConditional singleValCond = loopBlock._if(val.ne(JExpr._null()));
    singleValCond._then().add(resultVal.invoke("add").arg(val.invoke("getValue")));
    return resultVal;
  }

  protected void setField(JBlock block, JCodeModel model, JVar field, final String name, JVar toBean, Class valType,
                          String prefix) {
    final String getterSetterSuffix = new StringBuilder().append(("" + name.charAt(0)).toUpperCase()).append(name.
        substring(1)).toString();
    final String setterName = new StringBuilder("set").append(getterSetterSuffix).toString();
    final String fieldValVar = getVarName(prefix, new StringBuilder(name).append("Val").toString());
    JVar fieldVal = block.decl(model.ref(FieldValue.class).narrow(valType), fieldValVar, field.invoke("getValue"));
    JConditional valCond = block._if(fieldVal.ne(JExpr._null()));
    JBlock setBlock = valCond._then();
    setBlock.add(toBean.invoke(setterName).arg(fieldVal.invoke("getValue")));
  }

  protected void addField(JBlock block, JCodeModel model, JVar field, final String name, JVar toBean, Class valType,
                          String prefix) {
    final String setterName = "add";
    final String fieldValVar = getVarName(prefix, new StringBuilder(name).append("Val").toString());
    JVar fieldVal = block.decl(model.ref(FieldValue.class).narrow(valType), fieldValVar, field.invoke("getValue"));
    JConditional valCond = block._if(fieldVal.ne(JExpr._null()));
    JBlock setBlock = valCond._then();
    setBlock.add(toBean.invoke(setterName).arg(fieldVal.invoke("getValue")));
  }

  protected JDefinedClass findClass(CompositeDataType compositeDataType, JDefinedClass currentClass,
                                    Map<ContentTypeId, JDefinedClass> classes, String fieldName) {
    final String getterSetterSuffix = new StringBuilder().append(("" + fieldName.charAt(0)).toUpperCase()).append(fieldName.
        substring(1)).toString();
    JDefinedClass jType = null;
    ContentDataType composedOfContent = compositeDataType.getEmbeddedContentType();
    if (compositeDataType.getOwnComposition() != null && !compositeDataType.getOwnComposition().isEmpty()) {
      Iterator<JDefinedClass> compositeFieldClasses = currentClass.classes();
      while (compositeFieldClasses.hasNext()) {
        JDefinedClass innerClass = compositeFieldClasses.next();
        if (innerClass.name().equals(getterSetterSuffix)) {
          jType = innerClass;
        }
      }
    }
    else if (composedOfContent != null) {
      jType = classes.get(composedOfContent.getTypeDef());
    }
    return jType;
  }

  protected JDefinedClass findConcreteClass(JDefinedClass compositeDefinition,
                                            Map<ContentTypeId, JDefinedClass> classes) {
    if (compositeDefinition.isAbstract()) {
      for (JDefinedClass clazz : classes.values()) {
        if (!clazz.isAbstract() && compositeDefinition.isAssignableFrom(clazz)) {
          return clazz;
        }
        else {
          JDefinedClass inInnerClass = findConcreteInInnerClass(compositeDefinition, clazz);
          if (inInnerClass != null) {
            return inInnerClass;
          }
        }
      }
      return null;
    }
    else {
      return compositeDefinition;
    }
  }

  protected JDefinedClass findConcreteInInnerClass(JDefinedClass compositeDefinition, JDefinedClass clazz) {
    final Iterator<JDefinedClass> classes = clazz.classes();
    while (classes.hasNext()) {
      JDefinedClass innerClass = classes.next();
      if (compositeDefinition.isAssignableFrom(innerClass)) {
        return innerClass;
      }
      else {
        JDefinedClass inInnerClass = findConcreteInInnerClass(compositeDefinition, innerClass);
        if (inInnerClass != null) {
          return inInnerClass;
        }
      }
    }
    return null;
  }

  protected void generateReverseBlocks(Collection<FieldDef> values, JBlock block, JVar fromBean, JVar wContent,
                                       JVar contentLoader, JVar fieldDefs, JCodeModel model, JDefinedClass currentClass,
                                       Map<ContentTypeId, JDefinedClass> classes, Set<? extends ContentType> types,
                                       String prefix) {
    for (FieldDef def : values) {
      final String name = def.getName();
      final String getterSetterSuffix = new StringBuilder().append(("" + name.charAt(0)).toUpperCase()).append(name.
          substring(1)).toString();
      final String getterName = new StringBuilder("get").append(getterSetterSuffix).toString();
      switch (def.getValueDef().getType()) {
        case BOOLEAN: {
          final String methodName = "createBooleanFieldValue";
          final Class valClass = Boolean.class;
          setSimpleField(block, fromBean, wContent, getterName, fieldDefs, name, model, contentLoader, valClass,
                         methodName, prefix);
          break;
        }
        case STRING: {
          final String methodName = "createStringFieldValue";
          final Class valClass = String.class;
          setSimpleField(block, fromBean, wContent, getterName, fieldDefs, name, model, contentLoader, valClass,
                         methodName, prefix);
          break;
        }
        case ENUM: {
          final String methodName = "createStringFieldValue";
          final Class valClass = String.class;
          JBlock nonNullBlock = block._if(fromBean.invoke(getterName).ne(JExpr._null()).cand(fieldDefs.invoke("get").arg(
              name).ne(JExpr._null())))._then();
          JVar mutableField = nonNullBlock.decl(model.ref(MutableField.class), getVarName(prefix, "mutableField"),
                                                contentLoader.invoke("createMutableField").arg(JExpr._null()).arg(fieldDefs.
              invoke("get").arg(name)));
          JVar mutableFieldValue = nonNullBlock.decl(model.ref(MutableFieldValue.class).narrow(valClass),
                                                     getVarName(prefix, "fieldVal"), contentLoader.invoke(methodName));
          nonNullBlock.add(mutableFieldValue.invoke("setValue").arg(fromBean.invoke(getterName).invoke("name")));
          nonNullBlock.add(mutableField.invoke("setValue").arg(mutableFieldValue));
          nonNullBlock.add(wContent.invoke("setField").arg(mutableField));
          break;
        }
        case DATE_TIME: {
          final String methodName = "createDateTimeFieldValue";
          final Class valClass = Date.class;
          setSimpleField(block, fromBean, wContent, getterName, fieldDefs, name, model, contentLoader, valClass,
                         methodName, prefix);
          break;
        }
        case INTEGER: {
          final String methodName = "createIntegerFieldValue";
          final Class valClass = Number.class;
          setSimpleField(block, fromBean, wContent, getterName, fieldDefs, name, model, contentLoader, valClass,
                         methodName, prefix);
          break;
        }
        case DOUBLE: {
          final String methodName = "createDoubleFieldValue";
          final Class valClass = Number.class;
          setSimpleField(block, fromBean, wContent, getterName, fieldDefs, name, model, contentLoader, valClass,
                         methodName, prefix);
          break;
        }
        case LONG: {
          final String methodName = "createLongFieldValue";
          final Class valClass = Number.class;
          setSimpleField(block, fromBean, wContent, getterName, fieldDefs, name, model, contentLoader, valClass,
                         methodName, prefix);
          break;
        }
        case OTHER: {
          final String methodName = "createOtherFieldValue";
          final Class valClass = byte[].class;
          setSimpleField(block, fromBean, wContent, getterName, fieldDefs, name, model, contentLoader, valClass,
                         methodName, prefix);
          break;
        }
        case CONTENT: {
          JBlock nonNullBlock = block._if(fromBean.invoke(getterName).ne(JExpr._null()).cand(fieldDefs.invoke("get").arg(
              name).ne(JExpr._null()).cand(fromBean.invoke(getterName).invoke("getId").ne(JExpr._null()))))._then();
          JVar mutableField = nonNullBlock.decl(model.ref(MutableField.class), getVarName(prefix, "mutableField"),
                                                contentLoader.invoke("createMutableField").arg(JExpr._null()).
              arg(fieldDefs.invoke("get").arg(name)));
          JVar mutableFieldValue = nonNullBlock.decl(model.ref(MutableFieldValue.class).narrow(ContentId.class),
                                                     getVarName(prefix, "fieldVal"),
                                                     contentLoader.invoke("createContentFieldValue"));
          nonNullBlock.add(mutableFieldValue.invoke("setValue").arg(JExpr._super().invoke("getContentId").arg(fromBean.
              invoke(getterName).invoke("getId")).arg(fromBean.invoke(getterName).invoke("getWorkspaceId"))));
          nonNullBlock.add(mutableField.invoke("setValue").arg(mutableFieldValue));
          nonNullBlock.add(wContent.invoke("setField").arg(mutableField));
          break;
        }
        case COMPOSITE: {
          CompositeDataType compositeDataType = (CompositeDataType) def.getValueDef();
          JBlock nonNullBlock = block._if(fromBean.invoke(getterName).ne(JExpr._null()).cand(fieldDefs.invoke("get").arg(
              name).ne(JExpr._null())))._then();
          JDefinedClass clazz = findClass(compositeDataType, currentClass, classes, name);
          JVar input = nonNullBlock.decl(clazz, getVarName(prefix, "compositeField"), fromBean.invoke(getterName));
          final JInvocation defArg = fieldDefs.invoke("get").arg(name);
          JVar fieldDef = nonNullBlock.decl(model.ref(FieldDef.class), getVarName(prefix, "def"), defArg);
          JVar compositeFieldDefs = nonNullBlock.decl(
              model.ref(Map.class).narrow(String.class).narrow(FieldDef.class), getVarName(prefix, "compositeFieldDefs"),
              JExpr.invoke(JExpr.cast(model.ref(CompositeDataType.class), fieldDef.invoke("getValueDef")),
                           "getComposedFieldDefs"));
          JVar mutableField =
               nonNullBlock.decl(model.ref(MutableField.class), getVarName(prefix, "mutableField"),
                                 contentLoader.invoke("createMutableField").arg(JExpr._null()).arg(fieldDef));
          JVar mutableFieldValue = nonNullBlock.decl(model.ref(MutableCompositeFieldValue.class),
                                                     getVarName(prefix, "fieldVal"),
                                                     contentLoader.invoke("createCompositeFieldValue"));
          Collection<FieldDef> defs = getAllComposedFields(compositeDataType, types);
          generateReverseBlocks(defs, nonNullBlock, input, mutableFieldValue, contentLoader, compositeFieldDefs, model,
                                clazz, classes, types, new StringBuilder(prefix).append(name).append('_').toString());
          nonNullBlock.add(mutableField.invoke("setValue").arg(mutableFieldValue));
          nonNullBlock.add(wContent.invoke("setField").arg(mutableField));
          break;
        }
        case COLLECTION:
          CollectionDataType collectionDataType = (CollectionDataType) def.getValueDef();
          switch (collectionDataType.getItemDataType().getType()) {
            case BOOLEAN: {
              final String methodName = "createBooleanFieldValue";
              final Class valClass = Boolean.class;
              setSimpleMultiField(block, fromBean, wContent, getterName, fieldDefs, name, model, contentLoader, valClass,
                                  methodName, prefix);
              break;
            }
            case STRING: {
              final String methodName = "createStringFieldValue";
              final Class valClass = String.class;
              setSimpleMultiField(block, fromBean, wContent, getterName, fieldDefs, name, model, contentLoader, valClass,
                                  methodName, prefix);
              break;
            }
            case ENUM: {
              final String methodName = "createStringFieldValue";
              final Class valClass = String.class;
              JBlock nonNullBlock = block._if(fromBean.invoke(getterName).ne(JExpr._null()).cand(fieldDefs.invoke("get").
                  arg(
                  name).ne(JExpr._null())))._then();
              JVar mutableField = nonNullBlock.decl(model.ref(MutableField.class), getVarName(prefix, "mutableField"),
                                                    contentLoader.invoke("createMutableField").arg(JExpr._null()).arg(fieldDefs.
                  invoke("get").arg(name)));
              JVar mutableFieldValue = nonNullBlock.decl(model.ref(MutableFieldValue.class).narrow(model.ref(
                  Collection.class).
                  narrow(model.ref(FieldValue.class))), getVarName(prefix, "fieldVals"), contentLoader.invoke(
                  "createCollectionFieldValue"));
              nonNullBlock.add(mutableField.invoke("setValue").arg(mutableFieldValue));
              nonNullBlock.add(wContent.invoke("setField").arg(mutableField));
              JVar collection = nonNullBlock.decl(model.ref(Collection.class).narrow(model.ref(FieldValue.class)),
                                                  getVarName(
                  prefix, "collectionVar"), JExpr._new(model.ref(ArrayList.class).narrow(FieldValue.class)));
              nonNullBlock.add(mutableFieldValue.invoke("setValue").arg(collection));
              final JForLoop forLoop = nonNullBlock._for();

              JType itClass = ((JClass) currentClass.fields().get(name).type()).getTypeParameters().get(0);
              JVar iterator_item = forLoop.init(model.ref(Iterator.class).narrow(itClass), getVarName(prefix, "i"),
                                                fromBean.invoke(getterName).invoke("iterator"));
              forLoop.test(iterator_item.invoke("hasNext"));
              final JBlock forBody = forLoop.body();
              JVar mutableItemFieldValue =
                   forBody.decl(model.ref(MutableFieldValue.class).narrow(valClass),
                                getVarName(prefix, "fieldVal"), contentLoader.invoke(methodName));
              forBody.add(mutableItemFieldValue.invoke("setValue").arg(iterator_item.invoke("next").invoke("name")));
              forBody.add(collection.invoke("add").arg(mutableItemFieldValue));
              break;
            }
            case DATE_TIME: {
              final String methodName = "createDateTimeFieldValue";
              final Class valClass = Date.class;
              setSimpleMultiField(block, fromBean, wContent, getterName, fieldDefs, name, model, contentLoader, valClass,
                                  methodName, prefix);
              break;
            }
            case INTEGER: {
              final String methodName = "createIntegerFieldValue";
              final Class valClass = Integer.class;
              setSimpleMultiField(block, fromBean, wContent, getterName, fieldDefs, name, model, contentLoader, valClass,
                                  methodName, prefix);
              break;
            }
            case DOUBLE: {
              final String methodName = "createDoubleFieldValue";
              final Class valClass = Double.class;
              setSimpleMultiField(block, fromBean, wContent, getterName, fieldDefs, name, model, contentLoader, valClass,
                                  methodName, prefix);
              break;
            }
            case LONG: {
              final String methodName = "createLongFieldValue";
              final Class valClass = Long.class;
              setSimpleMultiField(block, fromBean, wContent, getterName, fieldDefs, name, model, contentLoader, valClass,
                                  methodName, prefix);
              break;
            }
            case OTHER: {
              final String methodName = "createOtherFieldValue";
              final Class valClass = byte[].class;
              setSimpleMultiField(block, fromBean, wContent, getterName, fieldDefs, name, model, contentLoader, valClass,
                                  methodName, prefix);
              break;
            }
            case CONTENT: {
              JBlock nonNullBlock = block._if(fromBean.invoke(getterName).ne(JExpr._null()).cand(fieldDefs.invoke("get").
                  arg(
                  name).ne(JExpr._null())))._then();
              JVar mutableField = nonNullBlock.decl(model.ref(MutableField.class), getVarName(prefix, "mutableField"),
                                                    contentLoader.invoke("createMutableField").arg(JExpr._null()).arg(fieldDefs.
                  invoke("get").arg(name)));
              JVar mutableFieldValue = nonNullBlock.decl(model.ref(MutableFieldValue.class).narrow(model.ref(
                  Collection.class).narrow(model.ref(FieldValue.class))), getVarName(prefix, "fieldVals"),
                                                         contentLoader.invoke("createCollectionFieldValue"));
              nonNullBlock.add(mutableField.invoke("setValue").arg(mutableFieldValue));
              nonNullBlock.add(wContent.invoke("setField").arg(mutableField));
              JVar collection = nonNullBlock.decl(model.ref(Collection.class).narrow(model.ref(FieldValue.class)),
                                                  getVarName(prefix, "collectionVar"), JExpr._new(model.ref(
                  ArrayList.class).narrow(FieldValue.class)));
              nonNullBlock.add(mutableFieldValue.invoke("setValue").arg(collection));
              final JForLoop forLoop = nonNullBlock._for();
              final JClass narrowedDomain = model.ref(AbstractRepositoryDomain.class).narrow(model.ref(
                  PersistentDTO.class).narrow(model.ref(PersistentDTO.class).wildcard()).narrow(String.class).narrow(
                  Long.class).wildcard());
              JVar iterator_item = forLoop.init(model.ref(Iterator.class).narrow(narrowedDomain.wildcard()), getVarName(
                  prefix, "i"), fromBean.invoke(getterName).invoke("iterator"));
              forLoop.test(iterator_item.invoke("hasNext"));
              final JBlock forBody = forLoop.body();
              JVar nextVal = forBody.decl(narrowedDomain, getVarName(prefix, "domain"), iterator_item.invoke(
                  "next"));
              JVar mutableItemFieldValue =
                   forBody.decl(model.ref(MutableFieldValue.class).narrow(ContentId.class),
                                getVarName(prefix, "fieldVal"), contentLoader.invoke("createContentFieldValue"));
              forBody.add(mutableItemFieldValue.invoke("setValue").arg(JExpr._super().invoke("getContentId").arg(nextVal.
                  invoke("getId")).arg(nextVal.invoke("getWorkspaceId"))));
              forBody.add(collection.invoke("add").arg(mutableItemFieldValue));
              break;
            }
            case COMPOSITE: {
              CompositeDataType compositeDataType = (CompositeDataType) collectionDataType.getItemDataType();
              JBlock nonNullBlock = block._if(fromBean.invoke(getterName).ne(JExpr._null()).cand(fieldDefs.invoke("get").
                  arg(name).ne(JExpr._null())))._then();
              JDefinedClass clazz = findClass(compositeDataType, currentClass, classes, name);
              final JInvocation defArg = fieldDefs.invoke("get").arg(name);
              JVar fieldDef = nonNullBlock.decl(model.ref(FieldDef.class), getVarName(prefix, "def"), defArg);
              JVar compositeFieldDefs = nonNullBlock.decl(model.ref(Map.class).narrow(String.class).narrow(
                  FieldDef.class), getVarName(prefix, "compositeFieldDefs"),
                                                          JExpr.invoke(JExpr.cast(model.ref(CompositeDataType.class),
                                                                                  fieldDef.invoke("getValueDef")),
                                                                       "getComposedFieldDefs"));
              JVar mutableField = nonNullBlock.decl(model.ref(MutableField.class), getVarName(prefix, "mutableField"),
                                                    contentLoader.invoke("createMutableField").arg(JExpr._null()).arg(
                  fieldDef));
              JVar mutableFieldValue = nonNullBlock.decl(model.ref(MutableFieldValue.class).narrow(model.ref(
                  Collection.class).narrow(model.ref(FieldValue.class))), getVarName(prefix, "fieldVals"),
                                                         contentLoader.invoke("createCollectionFieldValue"));
              nonNullBlock.add(mutableField.invoke("setValue").arg(mutableFieldValue));
              nonNullBlock.add(wContent.invoke("setField").arg(mutableField));
              JVar collection = nonNullBlock.decl(model.ref(Collection.class).narrow(model.ref(FieldValue.class)),
                                                  getVarName(prefix, "collectionVar"), JExpr._new(model.ref(
                  ArrayList.class).narrow(FieldValue.class)));
              nonNullBlock.add(mutableFieldValue.invoke("setValue").arg(collection));
              final JForLoop forLoop = nonNullBlock._for();
              final JClass narrowedDomain = clazz;
              JVar iterator_item = forLoop.init(model.ref(Iterator.class).narrow(narrowedDomain), getVarName(
                  prefix, "i"), fromBean.invoke(getterName).invoke("iterator"));
              forLoop.test(iterator_item.invoke("hasNext"));
              final JBlock forBody = forLoop.body();
              JVar nextVal = forBody.decl(narrowedDomain, getVarName(prefix, "domain"), iterator_item.invoke(
                  "next"));
              JVar mutableItemFieldValue =
                   forBody.decl(model.ref(MutableCompositeFieldValue.class),
                                getVarName(prefix, "fieldVal"), contentLoader.invoke("createCompositeFieldValue"));
              Collection<FieldDef> defs = getAllComposedFields(compositeDataType, types);
              generateReverseBlocks(defs, forBody, nextVal, mutableItemFieldValue, contentLoader,
                                    compositeFieldDefs, model, clazz, classes, types,
                                    new StringBuilder(prefix).append(name).append('_').toString());

            }
          }
          break;
        default:
      }
    }
  }

  protected void setSimpleField(JBlock block, JVar fromBean, JVar wContent, final String getterName, JVar fieldDefs,
                                final String name, JCodeModel model, JVar contentLoader, final Class valClass,
                                final String methodName, String prefix) {
    JBlock nonNullBlock = block._if(fromBean.invoke(getterName).ne(JExpr._null()).cand(fieldDefs.invoke("get").arg(
        name).ne(JExpr._null())))._then();
    JVar mutableField = nonNullBlock.decl(model.ref(MutableField.class), getVarName(prefix, "mutableField"),
                                          contentLoader.invoke("createMutableField").arg(JExpr._null()).arg(fieldDefs.
        invoke("get").arg(name)));
    JVar mutableFieldValue = nonNullBlock.decl(model.ref(MutableFieldValue.class).narrow(valClass),
                                               getVarName(prefix, "fieldVal"), contentLoader.invoke(methodName));
    nonNullBlock.add(mutableFieldValue.invoke("setValue").arg(fromBean.invoke(getterName)));
    nonNullBlock.add(mutableField.invoke("setValue").arg(mutableFieldValue));
    nonNullBlock.add(wContent.invoke("setField").arg(mutableField));
  }

  protected void setSimpleMultiField(JBlock block, JVar fromBean, JVar wContent, final String getterName,
                                     JVar fieldDefs, final String name, JCodeModel model, JVar contentLoader,
                                     final Class valClass, final String methodName, String prefix) {
    JBlock nonNullBlock = block._if(fromBean.invoke(getterName).ne(JExpr._null()).cand(fieldDefs.invoke("get").arg(
        name).ne(JExpr._null())))._then();
    JVar mutableField = nonNullBlock.decl(model.ref(MutableField.class), getVarName(prefix, "mutableField"),
                                          contentLoader.invoke("createMutableField").arg(JExpr._null()).arg(fieldDefs.
        invoke("get").arg(name)));
    JVar mutableFieldValue = nonNullBlock.decl(model.ref(MutableFieldValue.class).narrow(model.ref(Collection.class).
        narrow(model.ref(FieldValue.class))), getVarName(prefix, "fieldVals"), contentLoader.invoke(
        "createCollectionFieldValue"));
    nonNullBlock.add(mutableField.invoke("setValue").arg(mutableFieldValue));
    nonNullBlock.add(wContent.invoke("setField").arg(mutableField));
    JVar collection = nonNullBlock.decl(model.ref(Collection.class).narrow(model.ref(FieldValue.class)), getVarName(
        prefix, "collectionVar"), JExpr._new(model.ref(ArrayList.class).narrow(FieldValue.class)));
    nonNullBlock.add(mutableFieldValue.invoke("setValue").arg(collection));
    final JForLoop forLoop = nonNullBlock._for();
    JVar iterator_item = forLoop.init(model.ref(Iterator.class).narrow(valClass), getVarName(prefix, "i"),
                                      fromBean.invoke(getterName).invoke("iterator"));
    forLoop.test(iterator_item.invoke("hasNext"));
    final JBlock forBody = forLoop.body();
    JVar mutableItemFieldValue = forBody.decl(model.ref(MutableFieldValue.class).narrow(valClass),
                                              getVarName(prefix, "fieldVal"), contentLoader.invoke(methodName));
    forBody.add(mutableItemFieldValue.invoke("setValue").arg(iterator_item.invoke("next")));
    forBody.add(collection.invoke("add").arg(mutableItemFieldValue));
  }

  protected void generateMasterModule(Map<ContentTypeId, JDefinedClass> modules, JCodeModel codeModel) throws
      JClassAlreadyExistsException {
    final String moduleClassName = new StringBuilder(packageForGuiceMasterModule).append(".MasterModule").toString();
    final JDefinedClass moduleClass = codeModel._class(moduleClassName);
    moduleClass._extends(codeModel.ref(AbstractModule.class));
    JMethod configureMethod = moduleClass.method(JMod.PUBLIC, JCodeModel.boxToPrimitive.get(Void.class),
                                                 "configure");
    JBlock block = configureMethod.body();
    for (JDefinedClass clazz : modules.values()) {
      block.invoke("install").arg(JExpr._new(clazz));
    }
  }

  private void generateAssociationIoCForFields(Collection<FieldDef> defs, JCodeModel model, JDefinedClass stringType,
                                               JClass typeLiteral, JDefinedClass moduleClass, JBlock block,
                                               Set<MutableContentType> types, Map<ContentTypeId, JDefinedClass> classes,
                                               String namePrefix, final Set<ContentTypeId> idsConfigdFor) throws
      JClassAlreadyExistsException {
    for (FieldDef def : defs) {
      final ContentDataType contentDataType;
      final String probablePrefix = new StringBuilder(namePrefix).append(def.getName().substring(0, 1).toUpperCase()).
          append(def.getName().substring(1)).toString();
      switch (def.getValueDef().getType()) {
        case CONTENT:
          contentDataType = (ContentDataType) def.getValueDef();
          break;
        case COLLECTION:
          CollectionDataType collectionDataType = (CollectionDataType) def.getValueDef();
          if (collectionDataType.getItemDataType().getType().equals(FieldValueType.CONTENT)) {
            contentDataType = (ContentDataType) collectionDataType.getItemDataType();
          }
          else if (collectionDataType.getItemDataType().getType().equals(FieldValueType.COMPOSITE)) {
            CompositeDataType compositeDataType = (CompositeDataType) collectionDataType.getItemDataType();
            generateAssociationIoCForFields(getAllComposedFields(compositeDataType, types), model, stringType,
                                            typeLiteral, moduleClass, block, types, classes, probablePrefix,
                                            idsConfigdFor);
            contentDataType = null;
          }
          else {
            contentDataType = null;
          }
          break;
        case COMPOSITE:
          CompositeDataType compositeDataType = (CompositeDataType) def.getValueDef();
          generateAssociationIoCForFields(getAllComposedFields(compositeDataType, types), model, stringType, typeLiteral,
                                          moduleClass, block, types, classes, probablePrefix, idsConfigdFor);
        default:
          contentDataType = null;
          break;
      }
      if (contentDataType != null && !idsConfigdFor.contains(contentDataType.getTypeDef())) {
        idsConfigdFor.add(contentDataType.getTypeDef());
        JBlock mapBindingBlock = block.block();
        JDefinedClass definedClass = classes.get(contentDataType.getTypeDef());
        final JClass assocType = model.ref(CommonReadDao.class).narrow(definedClass.wildcard()).narrow(String.class);
        final JClass narrowedReadTypeLiteral = typeLiteral.narrow(assocType);
        final JDefinedClass assocDaoTypeLit = moduleClass._class(new StringBuilder(probablePrefix).append("DaoType").
            toString());
        assocDaoTypeLit._extends(narrowedReadTypeLiteral);
        JVar mapBinderVar = mapBindingBlock.decl(model.ref(MapBinder.class).narrow(String.class).narrow(assocType),
                                                 "mapBinder", model.ref(MapBinder.class).staticInvoke("newMapBinder").
            arg(JExpr._this().invoke("binder")).arg(JExpr._new(stringType)).arg(JExpr._new(assocDaoTypeLit)));
        Collection<ContentType> concreteInstances = findConcreteInstanceOf(contentDataType.getTypeDef(), types);
        for (ContentType concreteInstance : concreteInstances) {
          JDefinedClass concClass = classes.get(concreteInstance.getContentTypeID());
          if (concClass == null) {
            continue;
          }
          final JClass concType = model.ref(CommonReadDao.class).narrow(concClass).narrow(String.class);
          final JClass narrowedConcReadTypeLiteral = typeLiteral.narrow(concType);
          final String name = concreteInstance.getContentTypeID().getName();
          final JDefinedClass concDaoTypeLit = moduleClass._class(new StringBuilder(probablePrefix).append(name.
              substring(0, 1).toUpperCase()).append(name.substring(1)).append("DaoType").
              toString());
          concDaoTypeLit._extends(narrowedConcReadTypeLiteral);
          mapBindingBlock.add(mapBinderVar.invoke("addBinding").arg(JExpr.lit(concreteInstance.getContentTypeID().
              toString())).invoke("to").arg(JExpr._new(concDaoTypeLit)));
        }
      }
    }
  }

  private Collection<ContentType> findConcreteInstanceOf(final ContentTypeId typeDef,
                                                         final Collection<? extends ContentType> types) {
    if (types == null || typeDef == null || types.isEmpty()) {
      return Collections.emptyList();
    }
    final List<ContentType> instanceOfTypes = new ArrayList<ContentType>();
    final Map<ContentTypeId, ContentType> typeMap = new HashMap<ContentTypeId, ContentType>();
    for (ContentType type : types) {
      typeMap.put(type.getContentTypeID(), type);
    }
    for (ContentType type : types) {
      if (type.getDefinitionType().equals(DefinitionType.CONCRETE_TYPE)) {
        boolean isInstanceOf = isInstanceOf(type, typeDef, typeMap);
        if (isInstanceOf) {
          instanceOfTypes.add(type);
        }
      }
    }
    return instanceOfTypes;
  }

  private boolean isInstanceOf(ContentType type, final ContentTypeId typeDef, Map<ContentTypeId, ContentType> types) {
    boolean isInstanceOf = false;
    if (type.getContentTypeID().equals(typeDef)) {
      isInstanceOf = true;
    }
    if (!isInstanceOf) {
      ContentTypeId parentId = type.getParent();
      if (!isInstanceOf && parentId != null && types.get(parentId) != null) {
        isInstanceOf = isInstanceOf(types.get(parentId), typeDef, types);
      }
    }
    return isInstanceOf;
  }
}
