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
import com.smartitengineering.cms.api.common.TemplateType;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.factory.type.WritableContentType;
import com.smartitengineering.cms.api.type.ContentStatus;
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.api.type.FieldDef;
import com.smartitengineering.cms.api.type.RepresentationDef;
import com.smartitengineering.cms.api.type.VariationDef;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.cms.binder.guice.Initializer;
import com.smartitengineering.cms.client.api.ContainerResource;
import com.smartitengineering.cms.client.api.ContentResource;
import com.smartitengineering.cms.client.api.ContentTypeResource;
import com.smartitengineering.cms.client.api.ContentTypesResource;
import com.smartitengineering.cms.client.api.RootResource;
import com.smartitengineering.cms.client.api.WorkspaceContentResouce;
import com.smartitengineering.cms.client.api.WorkspaceFeedResource;
import com.smartitengineering.cms.client.api.WorkspaceFriendsResource;
import com.smartitengineering.cms.client.api.WorkspaceRepresentationResource;
import com.smartitengineering.cms.client.api.WorkspaceRepresentationsResource;
import com.smartitengineering.cms.client.api.WorkspaceVariationResource;
import com.smartitengineering.cms.client.api.WorkspaceVariationsResource;
import com.smartitengineering.cms.ws.common.domains.Content;
import com.smartitengineering.cms.ws.common.domains.Field;
import com.smartitengineering.cms.ws.common.domains.ResourceTemplateImpl;
import com.smartitengineering.cms.ws.common.domains.Workspace;
import com.smartitengineering.cms.ws.common.domains.WorkspaceImpl.WorkspaceIdImpl;
import com.smartitengineering.dao.hbase.ddl.HBaseTableGenerator;
import com.smartitengineering.dao.hbase.ddl.config.json.ConfigurationJsonParser;
import com.smartitengineering.util.bean.guice.GuiceUtil;
import com.smartitengineering.util.rest.client.ApplicationWideClientFactoryImpl;
import com.smartitengineering.util.rest.client.ClientUtil;
import com.smartitengineering.util.rest.client.ConnectionConfig;
import com.smartitengineering.util.rest.client.ResourceLink;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.abdera.model.Feed;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseTestingUtility;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
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
    //-Djavax.xml.parsers.DocumentBuilderFactory=com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl
    System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
                       "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
    try {
      TEST_UTIL.startMiniCluster();
    }
    catch (Exception ex) {
      LOGGER.error(ex.getMessage(), ex);
    }
    new HBaseTableGenerator(ConfigurationJsonParser.getConfigurations(AppTest.class.getClassLoader().getResourceAsStream(
        "com/smartitengineering/cms/spi/impl/schema.json")), TEST_UTIL.getConfiguration(), true).generateTables();

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
    HandlerList handlerList = new HandlerList();
    final String webapp = "./src/test/webapp/";
    if (!new File(webapp).exists()) {
      throw new IllegalStateException("WebApp file/dir does not exist!");
    }
    WebAppContext webAppHandler = new WebAppContext(webapp, "/");
    handlerList.addHandler(webAppHandler);
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
    workspaceId.setName("this is a test");
    workspaceId.setGlobalNamespace("a test namespace");

    RootResource resource = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    int size = resource.getWorkspaces().size();
    Workspace workspace = resource.createWorkspace(workspaceId);
    Assert.assertEquals(workspaceId.getName(), workspace.getId().getName());
    Assert.assertEquals(workspaceId.getGlobalNamespace(), workspace.getId().getGlobalNamespace());
    Feed feed = resource.get();
    Assert.assertNotNull(feed);
    Assert.assertEquals(size + 1, resource.getWorkspaces().size());
  }

  @Test
  public void testWorkspaceExists() throws Exception {
    final RootResource root = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    root.get();
    final Iterator<WorkspaceFeedResource> iterator = root.getWorkspaceFeeds().iterator();
    WorkspaceFeedResource feedResource = iterator.next();
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(new StringBuilder("Total no of workspace created in test : ").append(root.getWorkspaceFeeds().size()).
          toString());
    }
    Assert.assertNotNull(feedResource);
    feedResource = iterator.next();
    Assert.assertNotNull(feedResource);
    feedResource = iterator.next();
    Assert.assertNotNull(feedResource);
    Assert.assertEquals(3, root.getWorkspaceFeeds().size());
  }

  @Test
  public void testAddFriend() throws Exception {
    final RootResource rootResource = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    rootResource.get();
    final Iterator<WorkspaceFeedResource> iterator = rootResource.getWorkspaceFeeds().iterator();
    WorkspaceFeedResource feedResource = iterator.next();
    WorkspaceFriendsResource friendsResource = feedResource.getFriends();
    friendsResource.addFriend(new URI("http://localhost:10080/w/com.smartitengineering/test"));
    friendsResource.get();
    Collection<URI> frdUri = friendsResource.getLastReadStateOfEntity();
    Iterator<URI> frdUris = frdUri.iterator();
    Assert.assertEquals(1, frdUri.size());
    Assert.assertEquals("http://localhost:10080/w/com.smartitengineering/test", frdUris.next().toASCIIString());
    friendsResource.addFriend(URI.create("/w/a%20test%20namespace/this%20is%20a%20test"));
    friendsResource.addFriend(new URI("w/testNS/test"));
    WorkspaceFriendsResource newFriendsResource = feedResource.getFriends();
    Collection<URI> collection = newFriendsResource.get();
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(new StringBuilder("Total no of friend workspace after adding a friend are : ").append(
          collection.size()).
          toString());
    }
    Assert.assertEquals(3, collection.size());
    frdUris = collection.iterator();
    String friendWorkspace = frdUris.next().toASCIIString();
    Assert.assertEquals("http://localhost:10080/w/a%20test%20namespace/this%20is%20a%20test", friendWorkspace);
    LOGGER.debug(new StringBuilder("First friend workspace is : ").append(friendWorkspace).toString());
    friendWorkspace = frdUris.next().toASCIIString();
    Assert.assertEquals("http://localhost:10080/w/com.smartitengineering/test", friendWorkspace);
    LOGGER.debug(new StringBuilder("Second friend workspace is : ").append(friendWorkspace).toString());
    friendWorkspace = frdUris.next().toASCIIString();
    Assert.assertEquals("http://localhost:10080/w/testNS/test", friendWorkspace);
    LOGGER.debug(new StringBuilder("Third friend workspace is : ").append(friendWorkspace).toString());
  }

  @Test
  public void testDeleteFriend() throws Exception {
    final RootResource rootResource = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    rootResource.get();
    final Iterator<WorkspaceFeedResource> iterator = rootResource.getWorkspaceFeeds().iterator();
    WorkspaceFeedResource feedResource = iterator.next();
    WorkspaceFriendsResource friendsResource = feedResource.getFriends();
    friendsResource.deleteFriend(new URI("http://localhost:10080/w/com.smartitengineering/test"));
    friendsResource.get();
    Collection<URI> frdUri = friendsResource.getLastReadStateOfEntity();
    Iterator<URI> frdUris = frdUri.iterator();
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(new StringBuilder("Total no of friend workspace after deleting a friend is : ").append(frdUri.size()).
          toString());
    }
    Assert.assertEquals(2, frdUri.size());
    String friendWorkspace = frdUris.next().toASCIIString();
    Assert.assertEquals("http://localhost:10080/w/a%20test%20namespace/this%20is%20a%20test", friendWorkspace);
    LOGGER.debug(new StringBuilder("First friend workspace is : ").append(friendWorkspace).toString());
    friendWorkspace = frdUris.next().toASCIIString();
    Assert.assertEquals("http://localhost:10080/w/testNS/test", friendWorkspace);
    LOGGER.debug(new StringBuilder("Second friend workspace is : ").append(friendWorkspace).toString());
  }

  @Test
  public void testReplaceAllFriends() throws Exception {
    Collection<URI> uris = new ArrayList<URI>();
    final RootResource rootResource = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    final MultivaluedMap<String, String> map = new MultivaluedMapImpl();
    map.add("name", "additional");
    map.add("namespace", "atest2");
    rootResource.post(MediaType.APPLICATION_FORM_URLENCODED, map, ClientResponse.Status.CREATED);
    rootResource.get();
    final Iterator<WorkspaceFeedResource> iterator = rootResource.getWorkspaceFeeds().iterator();
    WorkspaceFeedResource feedResource = iterator.next();
    WorkspaceFriendsResource friendsResource = feedResource.getFriends();

    uris.add(new URI("http://localhost:10080/w/atest2/additional"));
    uris.add(new URI("http://localhost:10080/w/com.smartitengineering/test"));

    friendsResource.replaceAllFriends(uris);
    friendsResource.get();
    Collection<URI> frdUri = friendsResource.getLastReadStateOfEntity();
    Iterator<URI> frdUris = frdUri.iterator();
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(new StringBuilder("Total no of friend workspace after replace all friends are : ").append(
          frdUri.size()).toString());
    }
    Assert.assertEquals(2, frdUri.size());
    final String friendWS1 = frdUris.next().toASCIIString();
    Assert.assertEquals("http://localhost:10080/w/atest2/additional", friendWS1);
    LOGGER.debug(new StringBuilder("First friend after replacing is : ").append(friendWS1).toString());
    final String friendWS2 = frdUris.next().toASCIIString();
    Assert.assertEquals("http://localhost:10080/w/com.smartitengineering/test", friendWS2);
    LOGGER.debug(new StringBuilder("Second friend after replacing is : ").append(friendWS2).toString());
  }

  @Test
  public void testDeleteAllFriends() throws Exception {
    final RootResource rootResource = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    rootResource.get();
    final Iterator<WorkspaceFeedResource> iterator = rootResource.getWorkspaceFeeds().iterator();
    WorkspaceFeedResource feedResource = iterator.next();
    WorkspaceFriendsResource friendsResource = feedResource.getFriends();
    friendsResource.deleteAllFriends();
    friendsResource.get();
    Collection<URI> frdUri = friendsResource.getLastReadStateOfEntity();
    Assert.assertNull(frdUri);
  }

  @Test
  public void testCreateRepresentation() throws Exception {

    ResourceTemplateImpl template = new ResourceTemplateImpl();
    String temp = "Template";
    template.setName("rep");
    final byte[] bytes = temp.getBytes();
    template.setTemplate(bytes);
    template.setTemplateType(TemplateType.JAVASCRIPT.toString());

    RootResource resource = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    Collection<WorkspaceFeedResource> workspaceFeedResources = resource.getWorkspaceFeeds();
    Iterator<WorkspaceFeedResource> iterator = workspaceFeedResources.iterator();
    WorkspaceFeedResource feedResource = iterator.next();
    WorkspaceRepresentationsResource representationsResource = feedResource.getRepresentations();
    WorkspaceRepresentationResource representationResource = representationsResource.createRepresentations(template);
    Assert.assertEquals("rep", representationResource.get().getName());
    Assert.assertEquals(temp, new String(representationResource.get().getTemplate()));
    Assert.assertEquals(TemplateType.JAVASCRIPT.toString(), representationResource.get().getTemplateType());
  }

  @Test
  public void testCreateVariation() throws Exception {

    ResourceTemplateImpl template = new ResourceTemplateImpl();
    String temp = "variationTemplate";
    template.setName("variation");
    final byte[] bytes = temp.getBytes();
    template.setTemplate(bytes);
    template.setTemplateType(TemplateType.VELOCITY.toString());

    RootResource resource = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    Collection<WorkspaceFeedResource> workspaceFeedResources = resource.getWorkspaceFeeds();
    Iterator<WorkspaceFeedResource> iterator = workspaceFeedResources.iterator();
    WorkspaceFeedResource feedResource = iterator.next();
    WorkspaceVariationsResource variationsResource = feedResource.getVariations();
    WorkspaceVariationResource variationResource = variationsResource.createVariation(template);
    Assert.assertEquals("variation", variationResource.get().getName());
    Assert.assertEquals(temp, new String(variationResource.get().getTemplate()));
    Assert.assertEquals(TemplateType.VELOCITY.toString(), variationResource.get().getTemplateType());
  }

  @Test
  public void testUpdateRepresentation() throws Exception {
    LOGGER.info(":::::::::::::: UPDATE REPRESENTATION RESOURCE TEST ::::::::::::::");
    ResourceTemplateImpl template = new ResourceTemplateImpl();
    String temp = "newTemplate";
    final byte[] bytes = temp.getBytes();
    template.setTemplate(bytes);
    template.setTemplateType(TemplateType.RUBY.toString());

    RootResource resource = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    Collection<WorkspaceFeedResource> workspaceFeedResources = resource.getWorkspaceFeeds();
    Iterator<WorkspaceFeedResource> iterator = workspaceFeedResources.iterator();
    WorkspaceFeedResource feedResource = iterator.next();

    Collection<WorkspaceRepresentationResource> representationResources = feedResource.getRepresentations().
        getRepresentationsResources();
    Assert.assertEquals(1, representationResources.size());
    Iterator<WorkspaceRepresentationResource> representationIterator = representationResources.iterator();
    WorkspaceRepresentationResource representationResource = representationIterator.next();

    representationResource.update(template);
    Assert.assertEquals("rep", representationResource.get().getName());
    Assert.assertEquals(temp, new String(representationResource.get().getTemplate()));
    Assert.assertEquals(TemplateType.RUBY.toString(), representationResource.get().getTemplateType());
    resource.getWorkspaceFeeds();
    WorkspaceRepresentationResource secondRepresentationResource = resource.getWorkspaceFeeds().iterator().next().
        getRepresentations().getRepresentationsResources().iterator().next();
    template.setTemplateType(TemplateType.VELOCITY.name());
    secondRepresentationResource.update(template);
    Assert.assertEquals(TemplateType.VELOCITY.name(), secondRepresentationResource.get().getTemplateType());
    try {
      representationResource.update(template);
      Assert.fail("Should not have been able to update!");
    }
    catch (UniformInterfaceException ex) {
      //Exception expected
      representationResource.get();
      representationResource.update(template);
    }
  }

  @Test
  public void testUpdateVariation() throws Exception {
    LOGGER.info(":::::::::::::: UPDATE VARIATION RESOURCE TEST ::::::::::::::");
    ResourceTemplateImpl template = new ResourceTemplateImpl();
    String temp = "newTemplate";
    final byte[] bytes = temp.getBytes();
    template.setTemplate(bytes);
    template.setTemplateType(TemplateType.RUBY.toString());

    RootResource resource = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    Collection<WorkspaceFeedResource> workspaceFeedResources = resource.getWorkspaceFeeds();
    Iterator<WorkspaceFeedResource> iterator = workspaceFeedResources.iterator();
    WorkspaceFeedResource feedResource = iterator.next();

    Collection<WorkspaceVariationResource> variationResources = feedResource.getVariations().getVariationResources();
    Assert.assertEquals(1, variationResources.size());
    Iterator<WorkspaceVariationResource> VariationIterator = variationResources.iterator();
    WorkspaceVariationResource variationResource = VariationIterator.next();
    variationResource.update(template);
    Assert.assertEquals("variation", variationResource.get().getName());
    Assert.assertEquals(temp, new String(variationResource.get().getTemplate()));
    Assert.assertEquals(TemplateType.RUBY.toString(), variationResource.get().getTemplateType());
    resource.getWorkspaceFeeds();
    WorkspaceVariationResource secondVariationResource = resource.getWorkspaceFeeds().iterator().next().
        getVariations().getVariationResources().iterator().next();
    template.setTemplateType(TemplateType.VELOCITY.name());
    secondVariationResource.update(template);
    Assert.assertEquals(TemplateType.VELOCITY.name(), secondVariationResource.get().getTemplateType());
    try {
      variationResource.update(template);
      Assert.fail("Should not have been able to update!");
    }
    catch (UniformInterfaceException ex) {
      //Exception expected
      variationResource.get();
      variationResource.update(template);
    }
  }

  @Test
  public void testDeleteRepresentation() throws Exception {
    LOGGER.info(":::::::::::::: DELETE REPRESENTATION RESOURCE TEST ::::::::::::::");
    ResourceTemplateImpl template = new ResourceTemplateImpl();
    String temp = "Template2";
    template.setName("rep2");
    final byte[] bytes = temp.getBytes();
    template.setTemplate(bytes);
    template.setTemplateType(TemplateType.JAVASCRIPT.toString());
    RootResource resource = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    Collection<WorkspaceFeedResource> workspaceFeedResources = resource.getWorkspaceFeeds();
    Iterator<WorkspaceFeedResource> iterator = workspaceFeedResources.iterator();
    WorkspaceFeedResource feedResource = iterator.next();
    final WorkspaceRepresentationsResource representationsResource = feedResource.getRepresentations();

    Collection<WorkspaceRepresentationResource> representationResources = representationsResource.
        getRepresentationsResources();
    Assert.assertEquals(1, representationResources.size());
    representationsResource.createRepresentations(template);
    Iterator<WorkspaceRepresentationResource> representationIterator = representationResources.iterator();
    WorkspaceRepresentationResource representationResource = representationIterator.next();

    representationResource.delete(ClientResponse.Status.ACCEPTED);
    Collection<WorkspaceRepresentationResource> secondRepresentationResources = resource.getWorkspaceFeeds().iterator().
        next().getRepresentations().getRepresentationsResources();
    Assert.assertEquals(1, secondRepresentationResources.size());
  }

  @Test
  public void testDeleteVariation() throws Exception {
    LOGGER.info(":::::::::::::: DELETE VARIATION RESOURCE TEST ::::::::::::::");
    ResourceTemplateImpl template = new ResourceTemplateImpl();
    String temp = "Template2";
    template.setName("aaavar2");
    final byte[] bytes = temp.getBytes();
    template.setTemplate(bytes);
    template.setTemplateType(TemplateType.VELOCITY.name());
    RootResource resource = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    Collection<WorkspaceFeedResource> workspaceFeedResources = resource.getWorkspaceFeeds();
    Iterator<WorkspaceFeedResource> iterator = workspaceFeedResources.iterator();
    WorkspaceFeedResource feedResource = iterator.next();
    final WorkspaceVariationsResource variationsResource = feedResource.getVariations();

    Collection<WorkspaceVariationResource> variationResources = variationsResource.getVariationResources();
    Assert.assertEquals(1, variationResources.size());
    variationsResource.createVariation(template);
    variationsResource.get();
    variationResources = variationsResource.getVariationResources();
    Iterator<WorkspaceVariationResource> variationIterator = variationResources.iterator();
    WorkspaceVariationResource variationResource = variationIterator.next();

    variationResource.delete(ClientResponse.Status.ACCEPTED);
    Collection<WorkspaceVariationResource> secondVariationResources = resource.getWorkspaceFeeds().iterator().
        next().getVariations().getVariationResources();
    Assert.assertEquals(1, secondVariationResources.size());
  }

  @Test
  public void testCreateContentType() throws Exception {
    LOGGER.info(":::::::::::::: CREATE CONTENT_TYPE RESOURCE TEST ::::::::::::::");

    WorkspaceId workspaceId = SmartContentAPI.getInstance().getWorkspaceApi().createWorkspaceId("atest2", "additional");

    String XML = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("content-type-def-shopping.xml"));
    RootResource resource = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    Collection<WorkspaceFeedResource> workspaceFeedResources = resource.getWorkspaceFeeds();
    Iterator<WorkspaceFeedResource> iterator = workspaceFeedResources.iterator();
    WorkspaceFeedResource feedResource = iterator.next();
    ContentTypesResource contentTypesResource = feedResource.getContentTypes();
    contentTypesResource.get();
    Collection<ContentTypeResource> collection1 = contentTypesResource.getContentTypes();
    Assert.assertEquals(0, collection1.size());
    contentTypesResource.createContentType(XML);
    contentTypesResource.get();
    Collection<ContentTypeResource> collection = contentTypesResource.getContentTypes();
    Assert.assertEquals(3, collection.size());

    InputStream inputStream = IOUtils.toInputStream(XML);

    String NAME_SPACE = "com.smartitengineering.smart-shopping.content";

    Collection<WritableContentType> contentTypes = SmartContentAPI.getInstance().getContentTypeLoader().
        parseContentTypes(workspaceId, inputStream, com.smartitengineering.cms.api.common.MediaType.APPLICATION_XML);
    String[] name = {"Book", "Publisher", "Author"};
    Collection<ContentType> HBaseContentTypes = new ArrayList<ContentType>();
    for (int i = 0; i < 3; i++) {

      ContentTypeId contentTypeId = SmartContentAPI.getInstance().getContentTypeLoader().createContentTypeId(
          workspaceId,
          NAME_SPACE,
          name[i]);

      ContentType contentTypeTest =
                  SmartContentAPI.getInstance().getContentTypeLoader().loadContentType(contentTypeId);
      HBaseContentTypes.add(contentTypeTest);
    }
    Assert.assertEquals(contentTypes.size(), HBaseContentTypes.size());

    Iterator<WritableContentType> iterator1 = contentTypes.iterator();
    Iterator<ContentType> iterator2 = HBaseContentTypes.iterator();
    for (int i = 0; i < 3; i++) {
      ContentType servedContentType = iterator1.next();
      ContentType getContentType = iterator2.next();
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Content Type Name : " + getContentType.getContentTypeID().getName());
      }
      Assert.assertEquals(servedContentType.getContentTypeID().getName(), getContentType.getContentTypeID().getName());
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Content Type Namespace : " + getContentType.getContentTypeID().getNamespace());
      }
      Assert.assertEquals(servedContentType.getContentTypeID().getNamespace(), getContentType.getContentTypeID().
          getNamespace());
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Content Type Global Namespace : " + getContentType.getContentTypeID().getWorkspace().
            getGlobalNamespace());
      }
      Assert.assertEquals(servedContentType.getContentTypeID().getWorkspace().getGlobalNamespace(), getContentType.
          getContentTypeID().getWorkspace().getGlobalNamespace());
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Content Type ID Global Name : " + getContentType.getContentTypeID().getWorkspace().getName());
      }
      Assert.assertEquals(servedContentType.getContentTypeID().getWorkspace().getName(), getContentType.getContentTypeID().
          getWorkspace().getName());
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Content Type Display Name : " + getContentType.getDisplayName());
      }
      Assert.assertEquals(servedContentType.getDisplayName(), getContentType.getDisplayName());
      if (getContentType.getParent() != null) {
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Content Type Parent's Name : " + getContentType.getParent().getName());
        }
        Assert.assertEquals(servedContentType.getParent().getName(), getContentType.getParent().getName());
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Content Type Parent's Namespace : " + getContentType.getParent().getNamespace());
        }
        Assert.assertEquals(servedContentType.getParent().getNamespace(), getContentType.getParent().getNamespace());
        Assert.assertEquals(servedContentType.getParent().getWorkspace().getGlobalNamespace(), getContentType.getParent().
            getWorkspace().getGlobalNamespace());
        Assert.assertEquals(servedContentType.getParent().getWorkspace().getName(), getContentType.getParent().
            getWorkspace().getName());
      }
      Assert.assertEquals(servedContentType.getStatuses().size(), getContentType.getStatuses().size());
      Set statusKeys = servedContentType.getStatuses().keySet();
      for (Iterator ite = statusKeys.iterator(); ite.hasNext();) {
        String key = (String) ite.next();
        ContentStatus servedContentStatus = servedContentType.getStatuses().get(key);
        ContentStatus getContentStatus = getContentType.getStatuses().get(key);
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Content Status : " + getContentStatus.getName());
        }
        Assert.assertEquals(servedContentStatus.getName(), getContentStatus.getName());
      }

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Size of Representations Defs : " + getContentType.getRepresentationDefs());
      }

      Assert.assertEquals(servedContentType.getRepresentations().size(), getContentType.getRepresentations().size());

      Assert.assertEquals(servedContentType.getRepresentationDefs().size(),
                          getContentType.getRepresentationDefs().size());

      Map<String, RepresentationDef> servedRepresentationDefs = servedContentType.getRepresentationDefs();
      Set keys = servedRepresentationDefs.keySet();
      for (Iterator ite = keys.iterator(); ite.hasNext();) {
        String key = (String) ite.next();
        RepresentationDef servedRepresentationDef = servedContentType.getRepresentationDefs().get(key);
        RepresentationDef getRepresentationDef = getContentType.getRepresentationDefs().get(key);

        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Representation MimeType : " + getRepresentationDef.getMIMEType());
        }

        Assert.assertEquals(servedRepresentationDef.getMIMEType(), getRepresentationDef.getMIMEType());

        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Representation Name : " + getRepresentationDef.getName());
        }

        Assert.assertEquals(servedRepresentationDef.getName(), getRepresentationDef.getName());

        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Representation URI : " + getRepresentationDef.getResourceUri().getValue());
        }

        Assert.assertEquals(servedRepresentationDef.getResourceUri().getValue(), getRepresentationDef.getResourceUri().
            getValue());

        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Representation URI Type : " + getRepresentationDef.getResourceUri().getType().name());
        }

        Assert.assertEquals(servedRepresentationDef.getResourceUri().getType().name(), getRepresentationDef.
            getResourceUri().getType().name());
      }

      Assert.assertEquals(servedContentType.getFieldDefs().size(), getContentType.getFieldDefs().size());
      Map<String, FieldDef> servedFieldDefs = servedContentType.getFieldDefs();
      Set fieldKeys = servedFieldDefs.keySet();
      for (Iterator ite = fieldKeys.iterator(); ite.hasNext();) {
        String key = (String) ite.next();
        FieldDef servedFieldDef = servedContentType.getFieldDefs().get(key);
        FieldDef getFieldDef = getContentType.getFieldDefs().get(key);

        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Field Name : " + getFieldDef.getName());
        }

        Assert.assertEquals(servedFieldDef.getName(), getFieldDef.getName());
        Assert.assertEquals(servedFieldDef.getCustomValidator().geType().name(), getFieldDef.getCustomValidator().
            geType().
            name());
        Assert.assertEquals(servedFieldDef.getCustomValidator().getUri().getType().name(), getFieldDef.
            getCustomValidator().getUri().getType().name());
        Assert.assertEquals(servedFieldDef.getCustomValidator().getUri().getValue(), getFieldDef.getCustomValidator().
            getUri().getValue());

        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Field Search Def : " + getFieldDef.getSearchDefinition().toString());
        }

        Assert.assertEquals(getFieldDef.getSearchDefinition().toString(), getFieldDef.getSearchDefinition().toString());

        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Field value Name : " + getFieldDef.getValueDef().getType().name());
        }

        Assert.assertEquals(servedFieldDef.getValueDef().getType().name(), getFieldDef.getValueDef().getType().name());

        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Field isUpdateable : " + getFieldDef.isFieldStandaloneUpdateAble());
        }

        Assert.assertEquals(servedFieldDef.isFieldStandaloneUpdateAble(), getFieldDef.isFieldStandaloneUpdateAble());

        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Field isRequired : " + getFieldDef.isRequired());
        }

        Assert.assertEquals(servedFieldDef.isRequired(), getFieldDef.isRequired());
        Collection<VariationDef> servedVariationDefs = servedFieldDef.getVariations().values();
        Collection<VariationDef> getVariationDefs = getFieldDef.getVariations().values();

        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Size of Variation Def : " + getFieldDef.getVariations().size());
        }

        Assert.assertEquals(servedVariationDefs.size(), getVariationDefs.size());
        Iterator<VariationDef> iterator3 = getVariationDefs.iterator();
        for (VariationDef servedVariationDef : servedVariationDefs) {
          VariationDef getVariationDef = iterator3.next();
          Assert.assertEquals(servedVariationDef.getMIMEType(), getVariationDef.getMIMEType());

          if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Variation Name : " + getVariationDef.getName());
          }

          Assert.assertEquals(servedVariationDef.getName(), getVariationDef.getName());

          if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Variation URI Type : " + getVariationDef.getResourceUri().getType().name());
          }

          Assert.assertEquals(servedVariationDef.getResourceUri().getType().name(), getVariationDef.getResourceUri().
              getType().name());

          if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Variation URI : " + getVariationDef.getResourceUri().getValue());
          }

          Assert.assertEquals(servedVariationDef.getResourceUri().getValue(),
                              getVariationDef.getResourceUri().getValue());
        }
      }
    }
  }

  @Test
  public void testCreateContentTypeWithInvalidXML() throws Exception {
    LOGGER.info(":::::::::::::: CREATE CONTENT_TYPE RESOURCE WITH INVALID XML TEST ::::::::::::::");
    String XML = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("InvalidValueType.xml"));
    RootResource resource = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    Collection<WorkspaceFeedResource> workspaceFeedResources = resource.getWorkspaceFeeds();
    Iterator<WorkspaceFeedResource> iterator = workspaceFeedResources.iterator();
    WorkspaceFeedResource feedResource = iterator.next();
    ContentTypesResource contentTypesResource = feedResource.getContentTypes();
    contentTypesResource.get();
    try {
      contentTypesResource.createContentType(XML);
      Assert.fail("Should not be able to create!");
    }
    catch (UniformInterfaceException ex) {
      Assert.assertEquals(400, ex.getResponse().getStatus());
    }
  }

  @Test
  public void testUpdateContentType() throws Exception {
    LOGGER.info(":::::::::::::: UPDATE CONTENT_TYPE RESOURCE TEST ::::::::::::::");

    WorkspaceId workspaceId = SmartContentAPI.getInstance().getWorkspaceApi().createWorkspaceId("atest2", "additional");

    String XML = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("Update-shopping.xml"));
    RootResource resource = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    Collection<WorkspaceFeedResource> workspaceFeedResources = resource.getWorkspaceFeeds();
    Iterator<WorkspaceFeedResource> iterator = workspaceFeedResources.iterator();
    WorkspaceFeedResource feedResource = iterator.next();
    ContentTypesResource contentTypesResource = feedResource.getContentTypes();
    contentTypesResource.get();
    Collection<ContentTypeResource> collection1 = contentTypesResource.getContentTypes();
    Assert.assertEquals(3, collection1.size());
    contentTypesResource.createContentType(XML);
    contentTypesResource.get();
    Collection<ContentTypeResource> collection = contentTypesResource.getContentTypes();
    Assert.assertEquals(3, collection.size());

    InputStream inputStream = IOUtils.toInputStream(XML);

    String NAME_SPACE = "com.smartitengineering.smart-shopping.content";

    Collection<WritableContentType> contentTypes = SmartContentAPI.getInstance().getContentTypeLoader().
        parseContentTypes(workspaceId, inputStream, com.smartitengineering.cms.api.common.MediaType.APPLICATION_XML);
    String[] name = {"Book", "Publisher", "Author"};
    Collection<ContentType> HBaseContentTypes = new ArrayList<ContentType>();
    for (int i = 0; i < 3; i++) {

      ContentTypeId contentTypeId = SmartContentAPI.getInstance().getContentTypeLoader().createContentTypeId(
          workspaceId,
          NAME_SPACE,
          name[i]);

      ContentType contentTypeTest =
                  SmartContentAPI.getInstance().getContentTypeLoader().loadContentType(contentTypeId);
      HBaseContentTypes.add(contentTypeTest);
    }
    Assert.assertEquals(contentTypes.size(), HBaseContentTypes.size());

    Iterator<WritableContentType> iterator1 = contentTypes.iterator();
    Iterator<ContentType> iterator2 = HBaseContentTypes.iterator();
    for (int i = 0; i < 3; i++) {
      ContentType servedContentType = iterator1.next();
      ContentType getContentType = iterator2.next();
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Content Type Name : " + getContentType.getContentTypeID().getName());
      }
      Assert.assertEquals(servedContentType.getContentTypeID().getName(), getContentType.getContentTypeID().getName());
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Content Type Namespace : " + getContentType.getContentTypeID().getNamespace());
      }
      Assert.assertEquals(servedContentType.getContentTypeID().getNamespace(), getContentType.getContentTypeID().
          getNamespace());
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Content Type Global Namespace : " + getContentType.getContentTypeID().getWorkspace().
            getGlobalNamespace());
      }
      Assert.assertEquals(servedContentType.getContentTypeID().getWorkspace().getGlobalNamespace(), getContentType.
          getContentTypeID().getWorkspace().getGlobalNamespace());
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Content Type ID Global Name : " + getContentType.getContentTypeID().getWorkspace().getName());
      }
      Assert.assertEquals(servedContentType.getContentTypeID().getWorkspace().getName(), getContentType.getContentTypeID().
          getWorkspace().getName());
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Content Type Display Name : " + getContentType.getDisplayName());
      }
      Assert.assertEquals(servedContentType.getDisplayName(), getContentType.getDisplayName());
      if (getContentType.getParent() != null) {
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Content Type Parent's Name : " + getContentType.getParent().getName());
        }
        Assert.assertEquals(servedContentType.getParent().getName(), getContentType.getParent().getName());
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Content Type Parent's Namespace : " + getContentType.getParent().getNamespace());
        }
        Assert.assertEquals(servedContentType.getParent().getNamespace(), getContentType.getParent().getNamespace());
        Assert.assertEquals(servedContentType.getParent().getWorkspace().getGlobalNamespace(), getContentType.getParent().
            getWorkspace().getGlobalNamespace());
        Assert.assertEquals(servedContentType.getParent().getWorkspace().getName(), getContentType.getParent().
            getWorkspace().getName());
      }
      Assert.assertEquals(servedContentType.getStatuses().size(), getContentType.getStatuses().size());
      Set statusKeys = servedContentType.getStatuses().keySet();
      for (Iterator ite = statusKeys.iterator(); ite.hasNext();) {
        String key = (String) ite.next();
        ContentStatus servedContentStatus = servedContentType.getStatuses().get(key);
        ContentStatus getContentStatus = getContentType.getStatuses().get(key);
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Content Status : " + getContentStatus.getName());
        }
        Assert.assertEquals(servedContentStatus.getName(), getContentStatus.getName());
      }

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Size of Representations Defs : " + getContentType.getRepresentationDefs());
      }

      Assert.assertEquals(servedContentType.getRepresentations().size(), getContentType.getRepresentations().size());

      Assert.assertEquals(servedContentType.getRepresentationDefs().size(),
                          getContentType.getRepresentationDefs().size());

      Map<String, RepresentationDef> servedRepresentationDefs = servedContentType.getRepresentationDefs();
      Set keys = servedRepresentationDefs.keySet();
      for (Iterator ite = keys.iterator(); ite.hasNext();) {
        String key = (String) ite.next();
        RepresentationDef servedRepresentationDef = servedContentType.getRepresentationDefs().get(key);
        RepresentationDef getRepresentationDef = getContentType.getRepresentationDefs().get(key);

        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Representation MimeType : " + getRepresentationDef.getMIMEType());
        }

        Assert.assertEquals(servedRepresentationDef.getMIMEType(), getRepresentationDef.getMIMEType());

        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Representation Name : " + getRepresentationDef.getName());
        }

        Assert.assertEquals(servedRepresentationDef.getName(), getRepresentationDef.getName());

        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Representation URI : " + getRepresentationDef.getResourceUri().getValue());
        }

        Assert.assertEquals(servedRepresentationDef.getResourceUri().getValue(), getRepresentationDef.getResourceUri().
            getValue());

        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Representation URI Type : " + getRepresentationDef.getResourceUri().getType().name());
        }

        Assert.assertEquals(servedRepresentationDef.getResourceUri().getType().name(), getRepresentationDef.
            getResourceUri().getType().name());
      }

      Assert.assertEquals(servedContentType.getFieldDefs().size(), getContentType.getFieldDefs().size());
      Map<String, FieldDef> servedFieldDefs = servedContentType.getFieldDefs();
      Set fieldKeys = servedFieldDefs.keySet();
      for (Iterator ite = fieldKeys.iterator(); ite.hasNext();) {
        String key = (String) ite.next();
        FieldDef servedFieldDef = servedContentType.getFieldDefs().get(key);
        FieldDef getFieldDef = getContentType.getFieldDefs().get(key);

        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Field Name : " + getFieldDef.getName());
        }

        Assert.assertEquals(servedFieldDef.getName(), getFieldDef.getName());
        Assert.assertEquals(servedFieldDef.getCustomValidator().geType().name(), getFieldDef.getCustomValidator().
            geType().
            name());
        Assert.assertEquals(servedFieldDef.getCustomValidator().getUri().getType().name(), getFieldDef.
            getCustomValidator().getUri().getType().name());
        Assert.assertEquals(servedFieldDef.getCustomValidator().getUri().getValue(), getFieldDef.getCustomValidator().
            getUri().getValue());

        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Field Search Def : " + getFieldDef.getSearchDefinition().toString());
        }

        Assert.assertEquals(getFieldDef.getSearchDefinition().toString(), getFieldDef.getSearchDefinition().toString());

        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Field value Name : " + getFieldDef.getValueDef().getType().name());
        }

        Assert.assertEquals(servedFieldDef.getValueDef().getType().name(), getFieldDef.getValueDef().getType().name());

        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Field isUpdateable : " + getFieldDef.isFieldStandaloneUpdateAble());
        }

        Assert.assertEquals(servedFieldDef.isFieldStandaloneUpdateAble(), getFieldDef.isFieldStandaloneUpdateAble());

        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Field isRequired : " + getFieldDef.isRequired());
        }

        Assert.assertEquals(servedFieldDef.isRequired(), getFieldDef.isRequired());
        Collection<VariationDef> servedVariationDefs = servedFieldDef.getVariations().values();
        Collection<VariationDef> getVariationDefs = getFieldDef.getVariations().values();

        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Size of Variation Def : " + getFieldDef.getVariations().size());
        }

        Assert.assertEquals(servedVariationDefs.size(), getVariationDefs.size());
        Iterator<VariationDef> iterator3 = getVariationDefs.iterator();
        for (VariationDef servedVariationDef : servedVariationDefs) {
          VariationDef getVariationDef = iterator3.next();
          Assert.assertEquals(servedVariationDef.getMIMEType(), getVariationDef.getMIMEType());

          if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Variation Name : " + getVariationDef.getName());
          }

          Assert.assertEquals(servedVariationDef.getName(), getVariationDef.getName());

          if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Variation URI Type : " + getVariationDef.getResourceUri().getType().name());
          }

          Assert.assertEquals(servedVariationDef.getResourceUri().getType().name(), getVariationDef.getResourceUri().
              getType().name());

          if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Variation URI : " + getVariationDef.getResourceUri().getValue());
          }

          Assert.assertEquals(servedVariationDef.getResourceUri().getValue(),
                              getVariationDef.getResourceUri().getValue());
        }
      }
    }
  }

  @Test
  public void testCreateContent() throws Exception {
    LOGGER.info(":::::::::::::: CREATE CONTENT RESOURCE TEST ::::::::::::::");
    ObjectMapper mapper = new ObjectMapper();
    String JSON = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("Content.json"));
    InputStream stream = IOUtils.toInputStream(JSON);
    Content content = mapper.readValue(stream, Content.class);
    Assert.assertNotNull(content);
    RootResource resource = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    Collection<WorkspaceFeedResource> workspaceFeedResources = resource.getWorkspaceFeeds();
    Iterator<WorkspaceFeedResource> iterator = workspaceFeedResources.iterator();
    WorkspaceFeedResource feedResource = iterator.next();
    ContentResource contentResource = feedResource.getContents().createContentResource(content);
    Content content1 = contentResource.get();
    Assert.assertNotNull(content1);
    Assert.assertEquals(content.getParentContentUri(), content1.getParentContentUri());
    Assert.assertEquals(content.getStatus(), content1.getStatus());
    Assert.assertEquals(content.getFields().size(), content1.getFields().size());
    final Field field = content.getFields().iterator().next();
    final Field field1 = content1.getFields().iterator().next();
    Assert.assertEquals(field.getName(), field1.getName());
    Assert.assertEquals(field.getValue().getType(), field.getValue().getType());
    Assert.assertEquals(field.getValue().getValue(), field.getValue().getValue());
  }

  @Test
  public void testAddContainerContent() throws Exception {
    LOGGER.info(":::::::::::::: CREATE CONTENT IN CONTAINER RESOURCE TEST ::::::::::::::");
    ObjectMapper mapper = new ObjectMapper();
    String JSON = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("Update-Content.json"));
    InputStream stream = IOUtils.toInputStream(JSON);
    Content content = mapper.readValue(stream, Content.class);
    Assert.assertNotNull(content);
    RootResource resource = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    Collection<WorkspaceFeedResource> workspaceFeedResources = resource.getWorkspaceFeeds();
    Iterator<WorkspaceFeedResource> iterator = workspaceFeedResources.iterator();
    WorkspaceFeedResource feedResource = iterator.next();
    ContentResource contentResource = feedResource.getContents().createContentResource(content);
    Content content1 = contentResource.get();
    Assert.assertNotNull(content1);
    Assert.assertEquals(content.getParentContentUri(), content1.getParentContentUri());
    Assert.assertEquals(content.getStatus(), content1.getStatus());
    Assert.assertEquals(content.getFields().size(), content1.getFields().size());
    final Field field = content.getFields().iterator().next();
    final Field field1 = content1.getFields().iterator().next();
    Assert.assertEquals(field.getName(), field1.getName());
    Assert.assertEquals(field.getValue().getType(), field.getValue().getType());
    Assert.assertEquals(field.getValue().getValue(), field.getValue().getValue());

    feedResource.get();
    ContainerResource containerResource = feedResource.getContents().getContainer().iterator().next();
    containerResource.createContainer(contentResource.getUri());
    Assert.assertEquals(1, containerResource.getContainerContents().size());
    Assert.assertEquals(contentResource.getUri().toASCIIString(), containerResource.getContainerContents().iterator().
        next().getUri().toASCIIString());
  }

  @Test
  public void testUpdateCointainerContent() throws Exception {
    LOGGER.info(":::::::::::::: UPDATE CONTAINER CONTENT RESOURCE TEST ::::::::::::::");

    Collection<URI> contentUri = new ArrayList<URI>();
    ObjectMapper mapper = new ObjectMapper();
    String JSON = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("Content.json"));
    InputStream stream = IOUtils.toInputStream(JSON);
    Content content = mapper.readValue(stream, Content.class);
    Assert.assertNotNull(content);
    RootResource resource = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    Collection<WorkspaceFeedResource> workspaceFeedResources = resource.getWorkspaceFeeds();
    Iterator<WorkspaceFeedResource> iterator = workspaceFeedResources.iterator();
    WorkspaceFeedResource feedResource = iterator.next();
    ContentResource contentResource = feedResource.getContents().createContentResource(content);
    feedResource.get();
    ContainerResource containerResource = feedResource.getContents().getContainer().iterator().next();
    contentUri.add(contentResource.getUri());
    containerResource.updateContainer(contentUri);
    Assert.assertEquals(1, containerResource.getContainerContents().size());
    Assert.assertEquals(contentResource.getUri().toASCIIString(), containerResource.getContainerContents().iterator().
        next().getUri().toASCIIString());
  }

  @Test
  public void testDeleteContainerContent() throws Exception {
    LOGGER.info(":::::::::::::: UPDATE CONTAINER CONTENT RESOURCE TEST ::::::::::::::");
    RootResource rootResource = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    Collection<WorkspaceFeedResource> workspaceFeedResources = rootResource.getWorkspaceFeeds();
    Iterator<WorkspaceFeedResource> iterator = workspaceFeedResources.iterator();
    WorkspaceFeedResource feedResource = iterator.next();
    feedResource.get();
    ContainerResource containerResource = feedResource.getContents().getContainer().iterator().next();
    containerResource.delete(ClientResponse.Status.ACCEPTED);
    Assert.assertEquals(0, containerResource.getContainerContents().size());
  }

  @Test
  public void testUpdateContent() throws Exception {
    LOGGER.info(":::::::::::::: UPDATE CONTENT RESOURCE TEST ::::::::::::::");
    ObjectMapper mapper = new ObjectMapper();
    String JSON = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("Content.json"));
    InputStream stream = IOUtils.toInputStream(JSON);
    Content content = mapper.readValue(stream, Content.class);
    Assert.assertNotNull(content);
    ObjectMapper mapper1 = new ObjectMapper();
    String JSON1 = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("Update-Content.json"));
    InputStream stream1 = IOUtils.toInputStream(JSON1);
    Content content1 = mapper1.readValue(stream1, Content.class);
    Assert.assertNotNull(content1);
    RootResource resource = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    Collection<WorkspaceFeedResource> workspaceFeedResources = resource.getWorkspaceFeeds();
    Iterator<WorkspaceFeedResource> iterator = workspaceFeedResources.iterator();
    WorkspaceFeedResource feedResource = iterator.next();
    feedResource.getContents().createContentResource(content).update(content1);
  }

  @Test
  public void testDeleteContent() throws Exception {
    LOGGER.info(":::::::::::::: DELETE CONTENT RESOURCE TEST ::::::::::::::");
    ObjectMapper mapper = new ObjectMapper();
    String JSON = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("Content.json"));
    InputStream stream = IOUtils.toInputStream(JSON);
    Content content = mapper.readValue(stream, Content.class);
    Assert.assertNotNull(content);
    RootResource resource = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    Collection<WorkspaceFeedResource> workspaceFeedResources = resource.getWorkspaceFeeds();
    Iterator<WorkspaceFeedResource> iterator = workspaceFeedResources.iterator();
    WorkspaceFeedResource feedResource = iterator.next();
    feedResource.getContents().createContentResource(content).delete(ClientResponse.Status.OK);
  }

  @Test
  public void testDeleteContentType() throws Exception {
    LOGGER.info(":::::::::::::: DELETE CONTENT_TYPE RESOURCE TEST ::::::::::::::");
    RootResource resource = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    Collection<WorkspaceFeedResource> workspaceFeedResources = resource.getWorkspaceFeeds();
    Iterator<WorkspaceFeedResource> iterator = workspaceFeedResources.iterator();
    WorkspaceFeedResource feedResource = iterator.next();
    ContentTypesResource contentTypesResource = feedResource.getContentTypes();
    Collection<ContentTypeResource> collection = contentTypesResource.getContentTypes();
    Assert.assertEquals(3, collection.size());
    ContentTypeResource contentTypeResource = collection.iterator().next();
    Assert.assertNotNull(contentTypeResource.get());
    contentTypeResource.delete(ClientResponse.Status.OK);
    try {
      contentTypeResource.get();
    }
    catch (UniformInterfaceException exception) {
      Assert.assertEquals(404, exception.getResponse().getStatus());
    }
    contentTypesResource.get();
    Collection<ContentTypeResource> collection2 = contentTypesResource.getContentTypes();
    contentTypeResource = collection2.iterator().next();
    Assert.assertNotNull(contentTypeResource.get());
    contentTypeResource.delete(ClientResponse.Status.OK);
    try {
      contentTypeResource.get();
    }
    catch (UniformInterfaceException exception) {
      Assert.assertEquals(404, exception.getResponse().getStatus());
    }
    contentTypesResource.get();
    Collection<ContentTypeResource> collection1 = contentTypesResource.getContentTypes();
    Assert.assertEquals(1, collection1.size());
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
