package com.smartitengineering.demo.cms.dto;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.smartitengineering.cms.binder.guice.Initializer;
import com.smartitengineering.dao.common.CommonDao;
import com.smartitengineering.dao.hbase.ddl.HBaseTableGenerator;
import com.smartitengineering.dao.hbase.ddl.config.json.ConfigurationJsonParser;
import com.smartitengineering.util.bean.guice.GuiceUtil;
import java.util.Properties;
import junit.framework.Assert;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseTestingUtility;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.Person;
import test.di.MasterModule;

/**
 * Unit test for simple App.
 */
public class CodeGenerationTest {

  private static final HBaseTestingUtility TEST_UTIL = new HBaseTestingUtility();
  private static final Logger LOGGER = LoggerFactory.getLogger(CodeGenerationTest.class);

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
                           "com.smartitengineering.dao.impl.hbase");
    properties.setProperty(GuiceUtil.IGNORE_MISSING_DEP_PROP, Boolean.TRUE.toString());
    properties.setProperty(GuiceUtil.MODULES_LIST_PROP, ConfigurationModule.class.getName());
    GuiceUtil.getInstance(properties).register();
  }

  @AfterClass
  public static void globalTearDown() throws Exception {
    TEST_UTIL.shutdownMiniCluster();
  }

  public static class ConfigurationModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(Configuration.class).toInstance(TEST_UTIL.getConfiguration());
    }
  }

  @Test
  public void testInjectionInitialization() {
    Initializer.init();
    Injector injector = Guice.createInjector(new MasterModule());
    Assert.assertNotNull(injector);
    CommonDao<Person, String> dao = injector.getInstance(Key.get(new TypeLiteral<CommonDao<Person, String>>() {
    }));
    Assert.assertNotNull(dao);
  }
}
