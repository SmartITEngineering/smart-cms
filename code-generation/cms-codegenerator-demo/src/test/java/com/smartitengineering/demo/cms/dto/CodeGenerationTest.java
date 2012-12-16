package com.smartitengineering.demo.cms.dto;

import collection.CollectionTest;
import collection.CollectionTestBean;
import com.embarcadero.edn.Customer;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.smartitengineering.cms.api.content.Content;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.factory.content.ContentLoader;
import com.smartitengineering.cms.api.factory.type.ContentTypeLoader;
import com.smartitengineering.cms.api.factory.type.WritableContentType;
import com.smartitengineering.cms.api.workspace.Workspace;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.cms.binder.guice.Initializer;
import com.smartitengineering.cms.repo.dao.impl.ExtendedReadDao;
import com.smartitengineering.dao.common.CommonDao;
import com.smartitengineering.dao.common.queryparam.FetchMode;
import com.smartitengineering.dao.common.queryparam.MatchMode;
import com.smartitengineering.dao.common.queryparam.Order;
import com.smartitengineering.dao.common.queryparam.QueryParameter;
import com.smartitengineering.dao.common.queryparam.QueryParameterFactory;
import com.smartitengineering.dao.hbase.ddl.HBaseTableGenerator;
import com.smartitengineering.dao.hbase.ddl.config.json.ConfigurationJsonParser;
import com.smartitengineering.dao.impl.hbase.HBaseConfigurationFactory;
import com.smartitengineering.util.rest.client.ApplicationWideClientFactoryImpl;
import com.smartitengineering.util.rest.client.ConnectionConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import junit.framework.Assert;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
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

  private static final Logger LOGGER = LoggerFactory.getLogger(CodeGenerationTest.class);
  public static final int SLEEP_DURATION = 3000;

  @Test
  public void testInjectionInitialization() {
    Injector injector = Guice.createInjector(new MasterModule());
    Assert.assertNotNull(injector);
    CommonDao<Person, String> dao = injector.getInstance(Key.get(new TypeLiteral<CommonDao<Person, String>>() {
    }));
    Assert.assertNotNull(dao);
  }

  @Test
  //@Ignore
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
  //@Ignore
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
  //@Ignore
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
  //@Ignore
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
    //Test all count
    Assert.assertEquals((long) size, service.getPersonCount());
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
    //Test query count
    Assert.assertEquals(size / 3, service.getSearchPersonCount(QueryParameterFactory.getOrderByParam(
        Person.PROPERTY_NATIONALID, Order.ASC), QueryParameterFactory.getFirstResultParam(0), QueryParameterFactory.
        getMaxResultsParam(size), QueryParameterFactory.getNestedParametersParam(
        Person.PROPERTY_NAME, FetchMode.DEFAULT, QueryParameterFactory.getStringLikePropertyParam(
        Name.PROPERTY_FIRSTNAME, "imr", MatchMode.START))));
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

  @Test
  //@Ignore
  public void testLocking() {
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
    int newNatId = 222;
    Assert.assertFalse(rPerson.isLockOwned());
    rPerson.lock();
    Assert.assertTrue(rPerson.isLockOwned());
    try {
      rPerson.setNationalId(newNatId);
      service.update(rPerson);
      Assert.assertTrue(rPerson.isLockOwned());
    }
    finally {
      rPerson.unlock();
    }
    Assert.assertFalse(rPerson.isLockOwned());
    rPerson = service.getById(person.getId());
    Assert.assertEquals(newNatId, rPerson.getNationalId().intValue());
  }

  @Test
  //@Ignore
  public void testPersistCollectionOfComposite() {
    Injector injector = Guice.createInjector(new MasterModule());
    CommonDao<CollectionTest, String> collectionTestDao =
                                      injector.getInstance(Key.get(new TypeLiteral<CommonDao<CollectionTest, String>>() {
    }));
    CollectionTest test = new CollectionTest();
    test.setCollId(Integer.MAX_VALUE);
    CollectionTest.CompField compField = new CollectionTest.CompField();
    compField.setCompFieldGeneral(Integer.MAX_VALUE);
    compField.setCompFieldId(Integer.MIN_VALUE);
    test.setCompField(Arrays.asList(compField));
    collectionTestDao.save(test);
    Assert.assertNotNull(test.getId());
    Assert.assertNotNull(test.getCompField());
    Assert.assertFalse(test.getCompField().isEmpty());
    CollectionTest readTest = collectionTestDao.getById(test.getId());
    Assert.assertEquals(Integer.MAX_VALUE, readTest.getCollId().intValue());
    Assert.assertNotNull(readTest.getCompField());
    Assert.assertFalse("Collection of composite field is empty", readTest.getCompField().isEmpty());
  }

  @Test
  //@Ignore
  public void testPersistCollectionOfString() {
    Injector injector = Guice.createInjector(new MasterModule());
    CommonDao<CollectionTest, String> collectionTestDao =
                                      injector.getInstance(Key.get(new TypeLiteral<CommonDao<CollectionTest, String>>() {
    }));
    CollectionTest test = new CollectionTest();
    test.setCollId(Integer.MAX_VALUE);
    test.setStringField(Arrays.asList("testString"));
    collectionTestDao.save(test);
    Assert.assertNotNull(test.getId());
    Assert.assertNotNull(test.getStringField());
    Assert.assertFalse(test.getStringField().isEmpty());
    CollectionTest readTest = collectionTestDao.getById(test.getId());
    Assert.assertEquals(Integer.MAX_VALUE, readTest.getCollId().intValue());
    Assert.assertNotNull(readTest.getStringField());
    Assert.assertFalse("Collection of String field is empty", readTest.getStringField().isEmpty());
    Assert.assertEquals(1, readTest.getStringField().size());
    final String next = readTest.getStringField().iterator().next();
    Assert.assertEquals("testString", next);
  }

  @Test
  //@Ignore
  public void testPersistCollectionOfContent() {
    Injector injector = Guice.createInjector(new MasterModule());
    CommonDao<CollectionTest, String> collectionTestDao =
                                      injector.getInstance(Key.get(new TypeLiteral<CommonDao<CollectionTest, String>>() {
    }));
    CommonDao<CollectionTestBean, String> collectionTestBeanDao =
                                          injector.getInstance(Key.get(new TypeLiteral<CommonDao<CollectionTestBean, String>>() {
    }));
    CollectionTestBean bean = new CollectionTestBean();
    bean.setCollId(Integer.SIZE);
    collectionTestBeanDao.save(bean);
    sleep();
    sleep();
    sleep();
    CollectionTest test = new CollectionTest();
    test.setCollId(Integer.MAX_VALUE);
    test.setConField(Arrays.asList(bean));
    collectionTestDao.save(test);
    Assert.assertNotNull(test.getId());
    Assert.assertNotNull(test.getConField());
    Assert.assertFalse(test.getConField().isEmpty());
    CollectionTest readTest = collectionTestDao.getById(test.getId());
    Assert.assertEquals(Integer.MAX_VALUE, readTest.getCollId().intValue());
    Assert.assertNotNull(readTest.getConField());
    Assert.assertFalse("Collection of content field is empty", readTest.getConField().isEmpty());
    Assert.assertEquals(1, readTest.getConField().size());
    final CollectionTestBean next = readTest.getConField().iterator().next();
    Assert.assertEquals(bean.getId(), next.getId());
    Assert.assertEquals(Integer.SIZE, next.getCollId().intValue());
  }

  @Test
  //@Ignore
  public void testPersistingCollectionOfNumbers() {
    Injector injector = Guice.createInjector(new MasterModule());
    CommonDao<CollectionTest, String> collectionTestDao =
                                      injector.getInstance(Key.get(new TypeLiteral<CommonDao<CollectionTest, String>>() {
    }));
    CollectionTest collectionTest = new CollectionTest();
    collectionTest.setCollId(Integer.MAX_VALUE);
    collectionTest.setIntegerField(Arrays.asList(0, 1, 2));
    collectionTest.setLongField(Arrays.asList(0l, 1l, 2l));
    collectionTest.setDoubleField(Arrays.asList(0.0, 1.0, 2.0));
    collectionTestDao.save(collectionTest);
    String id = collectionTest.getId();
    CollectionTest readCollections = collectionTestDao.getById(id);
    Assert.assertNotNull(readCollections);
    Assert.assertNotNull(readCollections.getIntegerField());
    Assert.assertNotNull(readCollections.getLongField());
    Assert.assertNotNull(readCollections.getDoubleField());
    Assert.assertEquals(3, readCollections.getIntegerField().size());
    Assert.assertEquals(3, readCollections.getDoubleField().size());
    Assert.assertEquals(3, readCollections.getLongField().size());
    Assert.assertTrue(collectionTest.getIntegerField().contains(new Integer(0)));
    Assert.assertTrue(collectionTest.getIntegerField().contains(new Integer(1)));
    Assert.assertTrue(collectionTest.getIntegerField().contains(new Integer(2)));
    Assert.assertTrue(collectionTest.getLongField().contains(new Long(0)));
    Assert.assertTrue(collectionTest.getLongField().contains(new Long(1)));
    Assert.assertTrue(collectionTest.getLongField().contains(new Long(2)));
    Assert.assertTrue(collectionTest.getDoubleField().contains(new Double(0)));
    Assert.assertTrue(collectionTest.getDoubleField().contains(new Double(1)));
    Assert.assertTrue(collectionTest.getDoubleField().contains(new Double(2)));
  }

  @Test
  //@Ignore
  public void testPersistingCollectionOfByteArray() {
    Injector injector = Guice.createInjector(new MasterModule());
    CommonDao<CollectionTest, String> collectionTestDao =
                                      injector.getInstance(Key.get(new TypeLiteral<CommonDao<CollectionTest, String>>() {
    }));
    CollectionTest collectionTest = new CollectionTest();
    collectionTest.setCollId(Integer.MAX_VALUE);
    collectionTest.setBinaryField(Arrays.asList(new byte[]{0}, new byte[]{1}, new byte[]{2}));
    collectionTestDao.save(collectionTest);
    String id = collectionTest.getId();
    CollectionTest readCollections = collectionTestDao.getById(id);
    Assert.assertNotNull(readCollections);
    Assert.assertNotNull(readCollections.getBinaryField());
    Assert.assertEquals(3, readCollections.getBinaryField().size());
    Set<Byte> selectedBytes = new HashSet<Byte>();
    for (byte[] singleVal : readCollections.getBinaryField()) {
      Assert.assertEquals(1, singleVal.length);
      Assert.assertTrue(singleVal[0] == 0 || singleVal[0] == 1 || singleVal[0] == 2);
      selectedBytes.add(singleVal[0]);
    }
    Assert.assertEquals(3, selectedBytes.size());
  }

  @Test
  //@Ignore
  public void testMultiExplicitSingleThreadedLocking() {
    LOGGER.info("Start Multiple Explicit Lock testing on Single Thread");
    long start = System.currentTimeMillis();
    Injector injector = Guice.createInjector(new MasterModule(), new ServiceModule());
    Assert.assertNotNull(injector);
    PersonService service = injector.getInstance(PersonService.class);
    Assert.assertNotNull(service);
    Person person = new Person();
    person.setNationalId(124124124);
    Name name = new ExtName();
    name.setFirstName("Imran");
    name.setLastName("Yousuf");
    name.setMiddleInitial("Md");
    person.setName(name);
    service.save(person);
    sleep();
    final String personId = person.getId();
    Assert.assertNotNull(personId);
    for (int i = 0; i < 200; ++i) {
      Person rPerson = service.getById(personId);
      Assert.assertNotNull(rPerson);
      int newNatId = 222 + i;
      if (i > 0) {
        Assert.assertEquals(newNatId - 1, rPerson.getNationalId().intValue());
      }
      Assert.assertFalse(rPerson.isLockOwned());
      rPerson.lock();
      Assert.assertTrue(rPerson.isLockOwned());
      try {
        rPerson.setNationalId(newNatId);
        service.update(rPerson);
        Assert.assertTrue(rPerson.isLockOwned());
      }
      finally {
        rPerson.unlock();
      }
      Assert.assertFalse(rPerson.isLockOwned());
      Person aPerson = service.getById(personId);
      Assert.assertNotNull(aPerson);
      Assert.assertEquals(newNatId, aPerson.getNationalId().intValue());
    }
    LOGGER.info("END Multiple Explicit Lock testing on Single Thread " + (System.currentTimeMillis() - start) + "ms");
  }

  @Test
  @Ignore
  public void testMultiExplicitMultiThreadedLocking() {
    LOGGER.info("Start Multiple Explicit Lock testing on Multi Thread");
    long start = System.currentTimeMillis();
    Injector injector = Guice.createInjector(new MasterModule(), new ServiceModule());
    Assert.assertNotNull(injector);
    final PersonService service = injector.getInstance(PersonService.class);
    Assert.assertNotNull(service);
    Person person = new Person();
    person.setNationalId(124124124);
    Name name = new ExtName();
    name.setFirstName("Imran");
    name.setLastName("Yousuf");
    name.setMiddleInitial("Md");
    person.setName(name);
    service.save(person);
    sleep();
    final String personId = person.getId();
    Assert.assertNotNull(personId);
    ExecutorService executorService = Executors.newFixedThreadPool(5);
    List<Future<?>> futures = new ArrayList<Future<?>>();
    for (int j = 0; j < 5; ++j) {
      final int threadIndex = j;
      futures.add(executorService.submit(new Runnable() {

        public void run() {
          LOGGER.info("Explicit Lock test START for " + threadIndex);
          for (int i = 0; i < 200; ++i) {
            String suffix = "" + threadIndex + " run index " + i + " for " + personId;
            LOGGER.info("Explicit Lock test on " + suffix);
            Person rPerson = service.getById(personId);
            Assert.assertNotNull(rPerson);
            Assert.assertFalse("Lock owned before locking on " + suffix, rPerson.isLockOwned());
            rPerson.lock();
            LOGGER.info("Explicit Lock got LOCK on " + suffix);
            Assert.assertTrue("No Explicit Lock for update " + suffix, rPerson.isLockOwned());
            try {
              int newNatId = 222 + i;
              rPerson.setNationalId(newNatId);
              service.update(rPerson);
              Assert.assertTrue("No Lock to unlock " + suffix, rPerson.isLockOwned());
            }
            finally {
              rPerson.unlock();
              LOGGER.info("Explicit Lock UNLOCK on " + threadIndex + " run index " + i);
            }
            Assert.assertFalse("Lock owned after unlock on " + suffix, rPerson.isLockOwned());
            Person aPerson = service.getById(personId);
            Assert.assertNotNull(aPerson);
          }
          LOGGER.info("Explicit Lock test END for " + threadIndex);
        }
      }));
    }
    for (Future<?> future : futures) {
      try {
        future.get();
      }
      catch (Exception ex) {
        LOGGER.error("Failed to complete thread task", ex);
        Assert.fail("Failed to complete task: " + ex.getMessage());
      }
    }
    LOGGER.info("END Multiple Explicit Lock testing on Multi Thread " + (System.currentTimeMillis() - start) + "ms");
    sleep();
    sleep();
    sleep();
  }

  @Test
  //@Ignore
  public void testMultiImplicitSingleThreadedLocking() {
    LOGGER.info("Start Multiple Implicit Lock testing on Single Thread");
    long start = System.currentTimeMillis();
    Injector injector = Guice.createInjector(new MasterModule(), new ServiceModule());
    Assert.assertNotNull(injector);
    PersonService service = injector.getInstance(PersonService.class);
    Assert.assertNotNull(service);
    Person person = new Person();
    person.setNationalId(124124124);
    Name name = new ExtName();
    name.setFirstName("Imran");
    name.setLastName("Yousuf");
    name.setMiddleInitial("Md");
    person.setName(name);
    service.save(person);
    sleep();
    final String personId = person.getId();
    Assert.assertNotNull(personId);
    for (int i = 0; i < 200; ++i) {
      Person rPerson = service.getById(personId);
      Assert.assertNotNull(rPerson);
      int newNatId = 222 + i;
      if (i > 0) {
        Assert.assertEquals(newNatId - 1, rPerson.getNationalId().intValue());
      }
      Assert.assertFalse(rPerson.isLockOwned());
      LOGGER.info("Setting national id " + newNatId + " for " + personId);
      rPerson.setNationalId(newNatId);
      service.update(rPerson);
      Assert.assertFalse(rPerson.isLockOwned());
      Person aPerson = service.getById(personId);
      Assert.assertNotNull(aPerson);
      final ContentLoader contentLoader =
                          SmartContentAPI.getInstance().getContentLoader();
      try {
        Content content = contentLoader.loadContent(contentLoader.createContentId(SmartContentAPI.getInstance().
            getWorkspaceApi().createWorkspaceId("p", "q"), personId.getBytes("UTF-8")));
        LOGGER.info("Value of " + Person.PROPERTY_NATIONALID + " in " + personId + " is " + content.getField(
            Person.PROPERTY_NATIONALID).getValue().toString());
      }
      catch (Exception ex) {
        LOGGER.error(ex.getMessage(), ex);
      }
      LOGGER.info("Asserting national id " + newNatId + " for " + personId);
      Assert.assertEquals("Update failed for " + personId + " expected " + newNatId + " received " + aPerson.
          getNationalId(), newNatId, aPerson.getNationalId().intValue());
    }
    LOGGER.info("END Multiple Implicit Lock testing on Single Thread - " + (System.currentTimeMillis() - start) + "ms");
  }

  @Test
  @Ignore
  public void testMultiImplicitMultiThreadedLocking() {
    LOGGER.info("Start Multiple Implicit Lock testing on Multi Thread");
    long start = System.currentTimeMillis();
    Injector injector = Guice.createInjector(new MasterModule(), new ServiceModule());
    Assert.assertNotNull(injector);
    final PersonService service = injector.getInstance(PersonService.class);
    Assert.assertNotNull(service);
    Person person = new Person();
    person.setNationalId(124124124);
    Name name = new ExtName();
    name.setFirstName("Imran");
    name.setLastName("Yousuf");
    name.setMiddleInitial("Md");
    person.setName(name);
    service.save(person);
    sleep();
    final String personId = person.getId();
    Assert.assertNotNull(personId);
    ExecutorService executorService = Executors.newFixedThreadPool(5);
    List<Future<?>> futures = new ArrayList<Future<?>>();
    for (int j = 0; j < 5; ++j) {
      final int threadIndex = j;
      futures.add(executorService.submit(new Runnable() {

        public void run() {
          for (int i = 0; i < 200; ++i) {
            String suffix = "" + threadIndex + " run index " + i + " for " + personId;
            Person rPerson = service.getById(personId);
            Assert.assertNotNull(rPerson);
            int newNatId = 222 + i;
            Assert.assertFalse("Implicit Lock has lock unexpectly before update for " + suffix, rPerson.isLockOwned());
            rPerson.setNationalId(newNatId);
            service.update(rPerson);
            Assert.assertFalse("Implicit Lock has lock unexpectly after update for " + suffix, rPerson.isLockOwned());
            Person aPerson = service.getById(personId);
            Assert.assertNotNull(aPerson);
            LOGGER.info("Implicit Lock test done for " + suffix);
          }
          LOGGER.info("Implicit Lock test END for " + threadIndex);
        }
      }));
    }
    for (Future<?> future : futures) {
      try {
        future.get();
      }
      catch (Exception ex) {
        LOGGER.error("Failed to complete thread task", ex);
        Assert.fail("Failed to complete task: " + ex.getMessage());
      }
    }
    LOGGER.info("END Multiple Implicit Lock testing on Multi Thread - " + (System.currentTimeMillis() - start) + "ms");
    sleep();
    sleep();
    sleep();
  }

  public static class ConfigurationModule extends AbstractModule {

    @Override
    protected void configure() {
      ConnectionConfig config = new ConnectionConfig();
      config.setBasicUri("");
      config.setContextPath("/");
      config.setHost("localhost");
      config.setPort(10080);
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
    @Inject
    private ExtendedReadDao<Person, String> extDao;

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

    public long getPersonCount() {
      return extDao.count();
    }

    public long getSearchPersonCount(QueryParameter... params) {
      return extDao.count(params);
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
