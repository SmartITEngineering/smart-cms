package com.smartitengineering.demo.cms.dto;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.smartitengineering.cms.binder.guice.Initializer;
import com.smartitengineering.cms.client.api.RootResource;
import com.smartitengineering.cms.client.api.WorkspaceFeedResource;
import com.smartitengineering.cms.client.impl.RootResourceImpl;
import com.smartitengineering.cms.ws.common.domains.Workspace;
import com.smartitengineering.cms.ws.common.domains.WorkspaceImpl.WorkspaceIdImpl;
import com.smartitengineering.dao.common.CommonDao;
import com.smartitengineering.dao.hbase.ddl.HBaseTableGenerator;
import com.smartitengineering.dao.hbase.ddl.config.json.ConfigurationJsonParser;
import com.smartitengineering.util.bean.guice.GuiceUtil;
import com.smartitengineering.util.rest.client.ApplicationWideClientFactoryImpl;
import com.smartitengineering.util.rest.client.ConnectionConfig;
import com.smartitengineering.util.rest.client.jersey.cache.CacheableClient;
import com.sun.jersey.api.client.Client;
import java.io.File;
import java.net.URI;
import java.util.Properties;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import junit.framework.Assert;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseTestingUtility;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.ExtName;
import test.Name;
import test.Person;
import test.di.MasterModule;

/**
 * Unit test for simple App.
 */
public class CodeGenerationTest {

  private static final HBaseTestingUtility TEST_UTIL = new HBaseTestingUtility();
  private static final Logger LOGGER = LoggerFactory.getLogger(CodeGenerationTest.class);
  public static final int SLEEP_DURATION = 3000;
  private static final int PORT = 10080;
  public static final String DEFAULT_NS = "com.smartitengineering";
  public static final String ROOT_URI_STRING = "http://localhost:" + PORT + "/cms/";
  public static final String TEST = "test";
  public static final String TEST_NS = "testNS";
  private static Server jettyServer;

  @BeforeClass
  public static void globalSetup() throws Exception {
    /*
     * Start HBase and initialize tables
     */
    //-Djavax.xml.parsers.DocumentBuilderFactory=com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl
    System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
                       "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
    try {
      TEST_UTIL.startMiniCluster();
    }
    catch (Exception ex) {
      LOGGER.error(ex.getMessage(), ex);
    }
    /**
     * Generate the HBase tables if necessary, skips if table exists.
     * Initialize guice modules as specified in the configuration properties
     * In a web app these can reside within a context listener
     */
    new HBaseTableGenerator(ConfigurationJsonParser.getConfigurations(CodeGenerationTest.class.getClassLoader().
        getResourceAsStream("com/smartitengineering/cms/spi/impl/schema.json")), TEST_UTIL.getConfiguration(), true).
        generateTables();

    /*
     * The following additional DI is to provide the test HBase connection so that it does not search for
     * connection configuration in classpath
     */
    Properties properties = new Properties();
    properties.setProperty(GuiceUtil.CONTEXT_NAME_PROP,
                           "com.smartitengineering.dao.impl.hbase,com.smartitengineering.user.client");
    properties.setProperty(GuiceUtil.IGNORE_MISSING_DEP_PROP, Boolean.TRUE.toString());
    properties.setProperty(GuiceUtil.MODULES_LIST_PROP, ConfigurationModule.class.getName());
    GuiceUtil.getInstance(properties).register();
    Initializer.init();

    /*
     * Start web application container
     */
    jettyServer = new Server(PORT);
    HandlerList handlerList = new HandlerList();
    /*
     * The following is for solr for later, when this is to be used it
     */
    System.setProperty("solr.solr.home", "./target/sample-conf/");
    Handler solr = new WebAppContext("./target/solr/", "/solr");
    handlerList.addHandler(solr);
    final String webapp = "../../smart-cms-client-impl/src/test/webapp/";
    if (!new File(webapp).exists()) {
      throw new IllegalStateException("WebApp file/dir does not exist!");
    }
    WebAppContext webAppHandler = new WebAppContext(webapp, "/cms");
    handlerList.addHandler(webAppHandler);
    jettyServer.setHandler(handlerList);
    jettyServer.setSendDateHeader(true);
    jettyServer.start();

    /*
     * Setup client properties
     */
    System.setProperty(ApplicationWideClientFactoryImpl.TRACE, "true");

    Client client = CacheableClient.create();
    client.resource("http://localhost:9090/api/channels/test").header(HttpHeaders.CONTENT_TYPE,
                                                                      MediaType.APPLICATION_JSON).put(
        "{\"name\":\"test\"}");
    LOGGER.info("Created test channel!");
    /*
     * Setup workspaces
     */
    {
      RootResource resource = RootResourceImpl.getRoot(URI.create(ROOT_URI_STRING));
      resource.get();
      WorkspaceFeedResource feedResource;
      try {
        feedResource = resource.getTemplates().getWorkspaceResource("p", "q");
      }
      catch (Exception ex) {
        feedResource = null;
        LOGGER.info("Exception getting feed resoruce", ex);
      }
      boolean valid = false;
      if (feedResource == null) {
        try {
          Workspace workspace = resource.createWorkspace(new WorkspaceIdImpl("p", "q"));
          resource = RootResourceImpl.getRoot(URI.create(ROOT_URI_STRING));
          resource.get();
          feedResource = resource.getTemplates().getWorkspaceResource(workspace.getId().getGlobalNamespace(), workspace.
              getId().getName());
          String contentTypeXml = IOUtils.toString(CodeGenerationTest.class.getClassLoader().getResourceAsStream(
              "content-type-def-with-composition.xml"));
          feedResource.getContentTypes().createContentType(contentTypeXml);
          valid = true;
        }
        catch (Exception ex) {
          LOGGER.error("Error creating test workspace for templates", ex);
        }
      }
      else {
        valid = true;
      }
      Assert.assertTrue(valid);
    }
  }

  @AfterClass
  public static void globalTearDown() throws Exception {
    jettyServer.stop();
    TEST_UTIL.shutdownMiniCluster();
  }

  @Test
  public void testInjectionInitialization() {
    Injector injector = Guice.createInjector(new MasterModule());
    Assert.assertNotNull(injector);
    CommonDao<Person, String> dao = injector.getInstance(Key.get(new TypeLiteral<CommonDao<Person, String>>() {
    }));
    Assert.assertNotNull(dao);
  }

  @Test
  public void testSimpleDomainPersistence() {
    Injector injector = Guice.createInjector(new MasterModule(), new ServiceModule());
    Assert.assertNotNull(injector);
    PersonService service = injector.getInstance(PersonService.class);
    Assert.assertNotNull(service);
    Person person = new Person();
    person.setNationalId("123123123");
    Name name = new ExtName();
    name.setFirstName("Imran");
    name.setLastName("Yousuf");
    name.setMiddleInitial("M");
    person.setName(name);
    service.save(person);
    Assert.assertNotNull(person.getId());
  }

  public static class ConfigurationModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(Configuration.class).toInstance(TEST_UTIL.getConfiguration());
      ConnectionConfig config = new ConnectionConfig();
      config.setBasicUri("");
      config.setContextPath("/");
      config.setHost("localhost");
      config.setPort(PORT);
      bind(ConnectionConfig.class).toInstance(config);
    }
  }

  public static class ServiceModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(PersonService.class);
    }
  }

  public static class PersonService {

    @Inject
    private CommonDao<Person, String> dao;

    public void save(Person person) {
      dao.save(person);
    }
  }
}
