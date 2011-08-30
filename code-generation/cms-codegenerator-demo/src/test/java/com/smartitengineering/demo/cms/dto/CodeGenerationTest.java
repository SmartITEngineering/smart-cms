package com.smartitengineering.demo.cms.dto;

import com.embarcadero.edn.Customer;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.factory.type.ContentTypeLoader;
import com.smartitengineering.cms.api.factory.type.WritableContentType;
import com.smartitengineering.cms.api.workspace.Workspace;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.cms.binder.guice.Initializer;
import com.smartitengineering.dao.common.CommonDao;
import com.smartitengineering.dao.common.queryparam.FetchMode;
import com.smartitengineering.dao.common.queryparam.MatchMode;
import com.smartitengineering.dao.common.queryparam.Order;
import com.smartitengineering.dao.common.queryparam.QueryParameter;
import com.smartitengineering.dao.common.queryparam.QueryParameterFactory;
import com.smartitengineering.dao.hbase.ddl.HBaseTableGenerator;
import com.smartitengineering.dao.hbase.ddl.config.json.ConfigurationJsonParser;
import com.smartitengineering.util.bean.guice.GuiceUtil;
import com.smartitengineering.util.rest.client.ApplicationWideClientFactoryImpl;
import com.smartitengineering.util.rest.client.ConnectionConfig;
import com.smartitengineering.util.rest.client.jersey.cache.CacheableClient;
import com.sun.jersey.api.client.Client;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import junit.framework.Assert;
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
    jettyServer.setHandler(handlerList);
    jettyServer.setSendDateHeader(true);
    jettyServer.start();

    /*
     * Setup client properties
     */
    System.setProperty(ApplicationWideClientFactoryImpl.TRACE, "true");

    Client client = CacheableClient.create();
    client.resource("http://localhost:7090/api/channels/test").header(HttpHeaders.CONTENT_TYPE,
                                                                      MediaType.APPLICATION_JSON).put(
        "{\"name\":\"test\"}");
    LOGGER.info("Created test channel!");
    /*
     * Setup workspaces
     */
    {
      final WorkspaceId wId;
      try {
        wId = SmartContentAPI.getInstance().getWorkspaceApi().createWorkspace("p", "q");
      }
      catch (Exception ex) {
        LOGGER.info("Exception getting feed resoruce", ex);
        throw new RuntimeException(ex);
      }
      boolean valid = false;
      try {
        Workspace workspace = wId.getWorkspae();
        final ContentTypeLoader contentTypeLoader = SmartContentAPI.getInstance().getContentTypeLoader();
        Collection<WritableContentType> types;
        types = contentTypeLoader.parseContentTypes(workspace.getId(), CodeGenerationTest.class.getClassLoader().
            getResourceAsStream("content-type-def-with-composition.xml"),
                                                    com.smartitengineering.cms.api.common.MediaType.APPLICATION_XML);
        for (WritableContentType type : types) {
          type.put();
        }
        types = contentTypeLoader.parseContentTypes(workspace.getId(), CodeGenerationTest.class.getClassLoader().
            getResourceAsStream("content-type-example.xml"),
                                                    com.smartitengineering.cms.api.common.MediaType.APPLICATION_XML);
        for (WritableContentType type : types) {
          type.put();
        }
        valid = true;
      }
      catch (Exception ex) {
        LOGGER.error("Error creating test workspace for templates", ex);
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
    person.setNationalId(123123123);
    Name name = new ExtName();
    name.setFirstName("Imran");
    name.setLastName("Yousuf");
    name.setMiddleInitial("M");
    person.setName(name);
    service.save(person);
    sleep();
    Assert.assertNotNull(person.getId());
    Person rPerson = service.getById(person.getId());
    Assert.assertNotNull(rPerson);
    Assert.assertNotNull(rPerson.getId());
    Assert.assertNotNull(rPerson.getWorkspaceId());
    Assert.assertNotNull(rPerson.getName());
    Assert.assertEquals(person.getNationalId(), rPerson.getNationalId());
    Assert.assertEquals(person.getName().getFirstName(), rPerson.getName().getFirstName());
    Assert.assertEquals(person.getName().getLastName(), rPerson.getName().getLastName());
    Assert.assertEquals(person.getName().getMiddleInitial(), rPerson.getName().getMiddleInitial());
    service.delete(rPerson);
    rPerson = service.getById(person.getId());
    Assert.assertNull(rPerson);
  }

  @Test
  public void testGetAll() {
    Injector injector = Guice.createInjector(new MasterModule(), new ServiceModule());
    Assert.assertNotNull(injector);
    PersonService service = injector.getInstance(PersonService.class);
    Assert.assertNotNull(service);
    List<Person> list = new ArrayList<Person>();
    int size = 103;
    for (int i = 0; i < size; ++i) {
      Person person = new Person();
      person.setNationalId(i);
      Name name = new ExtName();
      name.setFirstName("Imran");
      name.setLastName("Yousuf");
      name.setMiddleInitial("M");
      person.setName(name);
      list.add(person);
      service.save(person);
    }
    sleep();
    sleep();
    List<Person> all = new ArrayList<Person>(service.getAll());
    Assert.assertEquals(size, all.size());
    Set<Integer> nIds = new HashSet<Integer>();
    for (int i = 0; i < size; ++i) {
      Person rPerson = all.get(i);
      nIds.add(rPerson.getNationalId());
      service.delete(rPerson);
      rPerson = service.getById(rPerson.getId());
      Assert.assertNull(rPerson);
    }
    Assert.assertEquals(size, nIds.size());
    sleep();
  }

  @Test
  public void testPersistCustomer() {
    Customer customer = new Customer();
    final String id = "customer1@testdomain.com";
    customer.setId(id);
    customer.setAddress("Test address");
    customer.setName("Test Customer 1");
    Injector injector = Guice.createInjector(new com.embarcadero.edn.MasterModule());
    CommonDao<Customer, String> dao = injector.getInstance(Key.get(new TypeLiteral<CommonDao<Customer, String>>() {
    }));
    Assert.assertNotNull(dao);
    dao.save(customer);
    Customer readCustomer = dao.getById(id);
    Assert.assertNotNull(readCustomer);
    sleep();
  }

  @Test
  public void testSearch() {
    Injector injector = Guice.createInjector(new MasterModule(), new ServiceModule());
    Assert.assertNotNull(injector);
    PersonService service = injector.getInstance(PersonService.class);
    Assert.assertNotNull(service);
    List<Person> list = new ArrayList<Person>();
    int size = 102;
    String[] names = new String[]{"Imran", "Mahdi", "Hasan"};
    for (int i = 0; i < size; ++i) {
      Person person = new Person();
      person.setNationalId(i);
      Name name = new ExtName();
      name.setFirstName(names[i % 3]);
      name.setLastName("Yousuf");
      person.setName(name);
      list.add(person);
      service.save(person);
    }
    sleep();
    sleep();
    //Test limit
    List<Person> all = new ArrayList<Person>(service.search(QueryParameterFactory.getOrderByParam(
        Person.PROPERTY_NATIONALID, Order.ASC), QueryParameterFactory.getFirstResultParam(10), QueryParameterFactory.
        getMaxResultsParam(10)));
    Assert.assertEquals(10, all.size());
    debug();
    for (int i = 0; i < 10; ++i) {
      Person rPerson = all.get(i);
      Assert.assertEquals(i + 10, rPerson.getNationalId().intValue());
    }
    //Test string with starts 
    all = new ArrayList<Person>(service.search(QueryParameterFactory.getOrderByParam(Person.PROPERTY_NATIONALID,
                                                                                     Order.ASC), QueryParameterFactory.
        getFirstResultParam(0), QueryParameterFactory.getMaxResultsParam(size), QueryParameterFactory.
        getNestedParametersParam(Person.PROPERTY_NAME, FetchMode.DEFAULT, QueryParameterFactory.
        getStringLikePropertyParam(Name.PROPERTY_FIRSTNAME, "imr", MatchMode.START))));
    Assert.assertEquals(size / 3, all.size());
    // Delete ALL created data
    all = new ArrayList<Person>(service.getAll());
    Set<Integer> nIds = new HashSet<Integer>();
    for (int i = 0; i < size; ++i) {
      Person rPerson = all.get(i);
      nIds.add(rPerson.getNationalId());
      service.delete(rPerson);
      rPerson = service.getById(rPerson.getId());
      Assert.assertNull(rPerson);
    }
    Assert.assertEquals(size, nIds.size());
    sleep();
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

    public Person getById(String id) {
      return dao.getById(id);
    }

    public void update(Person person) {
      dao.update(person);
    }

    public void delete(Person person) {
      dao.delete(person);
    }

    public Set<Person> getAll() {
      return dao.getAll();
    }

    public Collection<Person> search(QueryParameter... params) {
      return dao.getList(params);
    }
  }

  protected void sleep() {
    try {
      Thread.sleep(SLEEP_DURATION);
    }
    catch (InterruptedException ex) {
      LOGGER.warn(ex.getMessage(), ex);
    }
  }

  protected void debug() {
    try {
      if (Boolean.parseBoolean(System.getProperty("codeg.debug"))) {
        Thread.sleep(100000);
      }
    }
    catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }
}
