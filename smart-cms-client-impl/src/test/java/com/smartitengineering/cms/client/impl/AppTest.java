package com.smartitengineering.cms.client.impl;

import com.google.inject.AbstractModule;
import com.smartitengineering.cms.binder.guice.Initializer;
import com.smartitengineering.cms.client.api.RootResource;
import com.smartitengineering.util.bean.guice.GuiceUtil;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseTestingUtility;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit test for simple App.
 */
public class AppTest {

  private static final HBaseTestingUtility TEST_UTIL = new HBaseTestingUtility();
  private static final Logger LOGGER = LoggerFactory.getLogger(AppTest.class);
  private static Server jettyServer;
  private static final int PORT = 10080;

  @BeforeClass
  public static void globalSetup() throws Exception {
    /*
     * Start HBase and initialize tables
     */
    try {
      TEST_UTIL.startMiniCluster();
    }
    catch (Exception ex) {
      LOGGER.error(ex.getMessage(), ex);
    }
    HBaseAdmin admin = new HBaseAdmin(TEST_UTIL.getConfiguration());
    HTableDescriptor workspaceTable = new HTableDescriptor("workspace");
    workspaceTable.addFamily(new HColumnDescriptor("self"));
    workspaceTable.addFamily(new HColumnDescriptor("repInfo"));
    workspaceTable.addFamily(new HColumnDescriptor("repData"));
    workspaceTable.addFamily(new HColumnDescriptor("varInfo"));
    workspaceTable.addFamily(new HColumnDescriptor("varData"));
    workspaceTable.addFamily(new HColumnDescriptor("friendlies"));
    admin.createTable(workspaceTable);

    /*
     * Ensure DIs done
     */
    Properties properties = new Properties();
    properties.setProperty(GuiceUtil.CONTEXT_NAME_PROP, "com.smartitengineering.dao.impl.hbase");
    properties.setProperty(GuiceUtil.IGNORE_MISSING_DEP_PROP, Boolean.toString(false));
    properties.setProperty(GuiceUtil.MODULES_LIST_PROP, ConfigurationModule.class.getName());
    GuiceUtil.getInstance(properties).register();
    Initializer.init();

    /*
     * Start web application container
     */
    jettyServer = new Server(PORT);
    final String webapp = "./src/test/webapp/";
    if (!new File(webapp).exists()) {
      throw new IllegalStateException("WebApp file/dir does not exist!");
    }
    WebAppContext webAppHandler = new WebAppContext(webapp, "/");
    jettyServer.setHandler(webAppHandler);
    jettyServer.start();
  }

  @AfterClass
  public static void globalTearDown() throws Exception {
    TEST_UTIL.shutdownMiniCluster();
    jettyServer.stop();
  }

  @Test
  public void testApp() throws URISyntaxException {
    RootResource resource = RootResourceImpl.getRoot(new URI("http://localhost:" + PORT + "/"));
    Assert.assertNotNull(resource);
    Assert.assertEquals(0, resource.getWorkspaces().size());
  }

  public static class ConfigurationModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(Configuration.class).toInstance(TEST_UTIL.getConfiguration());
    }
  }
}
