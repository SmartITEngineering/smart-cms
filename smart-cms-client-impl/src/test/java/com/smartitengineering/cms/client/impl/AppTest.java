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
package com.smartitengineering.cms.client.impl;

import com.google.inject.AbstractModule;
import com.smartitengineering.cms.binder.guice.Initializer;
import com.smartitengineering.cms.client.api.RootResource;
import com.smartitengineering.cms.client.api.WorkspaceContentResouce;
import com.smartitengineering.cms.client.api.WorkspaceFeedResource;
import com.smartitengineering.cms.ws.common.domains.Workspace;
import com.smartitengineering.cms.ws.common.domains.WorkspaceImpl.WorkspaceIdImpl;
import com.smartitengineering.util.bean.guice.GuiceUtil;
import com.smartitengineering.util.rest.client.ApplicationWideClientFactoryImpl;
import com.smartitengineering.util.rest.client.ClientUtil;
import com.smartitengineering.util.rest.client.ConnectionConfig;
import com.smartitengineering.util.rest.client.ResourceLink;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
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

  private static final int PORT = 10080;
  public static final String DEFAULT_NS = "com.smartitengineering";
  public static final String ROOT_URI_STRING = "http://localhost:" + PORT + "/";
  public static final String TEST = "test";
  public static final String TEST_NS = "testNS";
  private static final HBaseTestingUtility TEST_UTIL = new HBaseTestingUtility();
  private static final Logger LOGGER = LoggerFactory.getLogger(AppTest.class);
  private static Server jettyServer;

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
    final String webapp = "./src/test/webapp/";
    if (!new File(webapp).exists()) {
      throw new IllegalStateException("WebApp file/dir does not exist!");
    }
    WebAppContext webAppHandler = new WebAppContext(webapp, "/");
    jettyServer.setHandler(webAppHandler);
    jettyServer.setSendDateHeader(true);
    jettyServer.start();

    /*
     * Setup client properties
     */
    System.setProperty(ApplicationWideClientFactoryImpl.TRACE, "true");
  }

  @AfterClass
  public static void globalTearDown() throws Exception {
    TEST_UTIL.shutdownMiniCluster();
    jettyServer.stop();
  }

  @Test
  public void testStartup() throws URISyntaxException {
    RootResource resource = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    Assert.assertNotNull(resource);
    Assert.assertEquals(0, resource.getWorkspaces().size());
  }

  @Test
  public void testCreationAndRetrievalWithNameOnly() throws URISyntaxException {
    RootResource resource = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    final MultivaluedMap<String, String> map = new MultivaluedMapImpl();
    map.add("name", TEST);
    ClientResponse response = resource.post(MediaType.APPLICATION_FORM_URLENCODED, map, ClientResponse.Status.CREATED);
    ResourceLink link = ClientUtil.createResourceLink(WorkspaceContentResouce.WORKSPACE_CONTENT, response.getLocation(),
                                                      MediaType.APPLICATION_JSON);
    WorkspaceContentResouce workspaceContentResource = new WorkspaceContentResourceImpl(resource, link);
    Assert.assertNotNull(workspaceContentResource.getLastReadStateOfEntity());
    Workspace workspace = workspaceContentResource.getLastReadStateOfEntity();
    Assert.assertEquals(TEST, workspace.getId().getName());
    Assert.assertEquals(DEFAULT_NS, workspace.getId().getGlobalNamespace());
    Collection<WorkspaceContentResouce> resources = resource.getWorkspaces();
    Assert.assertEquals(1, resource.getWorkspaces().size());
    workspaceContentResource = resources.iterator().next();
    workspace = workspaceContentResource.getLastReadStateOfEntity();
    Assert.assertEquals(TEST, workspace.getId().getName());
    Assert.assertEquals(DEFAULT_NS, workspace.getId().getGlobalNamespace());
  }

  @Test
  public void testCreationAndRetrievalWithNamespace() throws URISyntaxException {
    RootResource resource = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    final MultivaluedMap<String, String> map = new MultivaluedMapImpl();
    map.add("name", TEST);
    map.add("namespace", TEST_NS);
    ClientResponse response = resource.post(MediaType.APPLICATION_FORM_URLENCODED, map, ClientResponse.Status.CREATED);
    ResourceLink link = ClientUtil.createResourceLink(WorkspaceContentResouce.WORKSPACE_CONTENT, response.getLocation(),
                                                      MediaType.APPLICATION_JSON);
    WorkspaceContentResouce workspaceContentResource = new WorkspaceContentResourceImpl(resource, link);
    Assert.assertNotNull(workspaceContentResource.getLastReadStateOfEntity());
    Workspace workspace = workspaceContentResource.getLastReadStateOfEntity();
    Assert.assertEquals(TEST, workspace.getId().getName());
    Assert.assertEquals(TEST_NS, workspace.getId().getGlobalNamespace());
    Collection<WorkspaceContentResouce> resources = resource.getWorkspaces();
    Assert.assertEquals(2, resource.getWorkspaces().size());
    workspaceContentResource = resources.iterator().next();
    workspace = workspaceContentResource.getLastReadStateOfEntity();
    Assert.assertEquals(TEST, workspace.getId().getName());
    Assert.assertEquals(TEST_NS, workspace.getId().getGlobalNamespace());
  }

  @Test
  public void testConditionalRootResourceGet() throws Exception {
    final String uri = ROOT_URI_STRING;
    testConditionalGetUsingLastModified(uri);
  }

  @Test
  public void testConditionalWorkspaceContentResourceGet() throws Exception {
    final String uri = ROOT_URI_STRING;
    RootResource resource = RootResourceImpl.getRoot(new URI(uri));
    Collection<WorkspaceContentResouce> resouces = resource.getWorkspaces();
    for (WorkspaceContentResouce contentResouce : resouces) {
      testConditionalGetUsingLastModified(contentResouce.getUri().toString());
    }
  }

  @Test
  public void testCreateWorkspace() throws Exception {
    WorkspaceIdImpl workspaceId = new WorkspaceIdImpl();
    workspaceId.setName("this.is.a.test");
    workspaceId.setGlobalNamespace("a-test-namespace");


//    WorkspaceIdImpl workspaceId = new WorkspaceIdImpl();
//    workspaceId.setName("this is a test");
//    workspaceId.setGlobalNamespace("a test namespace");

    RootResource resource = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    Workspace workspace = resource.createWorkspace(workspaceId);
    Assert.assertEquals(workspaceId.getName(), workspace.getId().getName());
    Assert.assertEquals(workspaceId.getGlobalNamespace(), workspace.getId().getGlobalNamespace());
    Assert.assertEquals(new Date().toString(), workspace.getCreationDate().toString());
  }

  @Test
  public void testWorkspaceExists() throws Exception {
    final RootResource root = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    root.get();
    final Iterator<WorkspaceFeedResource> iterator = root.getWorkspaceFeeds().iterator();
    WorkspaceFeedResource feedResource = iterator.next();
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(new StringBuffer("Total no of workspace created in test : ").append(root.getWorkspaceFeeds().size()).
          toString());
    }
    Assert.assertNotNull(feedResource);
    feedResource = iterator.next();
    Assert.assertNotNull(feedResource);
    feedResource = iterator.next();
    Assert.assertNotNull(feedResource);
    Assert.assertEquals(3, root.getWorkspaceFeeds().size());
  }

  protected void testConditionalGetUsingLastModified(final String uri) throws IOException {
    HttpClient client = new HttpClient();
    GetMethod method = new GetMethod(uri);
    client.executeMethod(method);
    Assert.assertEquals(200, method.getStatusCode());
    Header date = method.getResponseHeader(HttpHeaders.LAST_MODIFIED);
    String dateStr = date.getValue();
    Header ifDate = new Header(HttpHeaders.IF_MODIFIED_SINCE, dateStr);
    method = new GetMethod(uri);
    method.addRequestHeader(ifDate);
    client.executeMethod(method);
    Assert.assertEquals(304, method.getStatusCode());
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
}
