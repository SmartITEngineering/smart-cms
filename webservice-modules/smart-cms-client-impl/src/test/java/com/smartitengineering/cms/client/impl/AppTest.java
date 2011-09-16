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
import com.smartitengineering.cms.api.event.Event;
import com.smartitengineering.cms.api.event.Event.EventType;
import com.smartitengineering.cms.api.event.EventListener;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.factory.type.WritableContentType;
import com.smartitengineering.cms.api.impl.type.ContentTypeIdImpl;
import com.smartitengineering.cms.api.type.CollectionDataType;
import com.smartitengineering.cms.api.type.CompositeDataType;
import com.smartitengineering.cms.api.type.ContentCoProcessorDef;
import com.smartitengineering.cms.api.type.ContentStatus;
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.type.ContentType.ContentProcessingPhase;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.api.type.EnumDataType;
import com.smartitengineering.cms.api.type.FieldDef;
import com.smartitengineering.cms.api.type.FieldValueType;
import com.smartitengineering.cms.api.type.RepresentationDef;
import com.smartitengineering.cms.api.type.ValidatorType;
import com.smartitengineering.cms.api.type.VariationDef;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.cms.binder.guice.Initializer;
import com.smartitengineering.cms.client.api.ContainerResource;
import com.smartitengineering.cms.client.api.ContentResource;
import com.smartitengineering.cms.client.api.ContentSearcherResource;
import com.smartitengineering.cms.client.api.ContentTypeFeedResource;
import com.smartitengineering.cms.client.api.ContentTypeResource;
import com.smartitengineering.cms.client.api.ContentTypesResource;
import com.smartitengineering.cms.client.api.ContentsResource;
import com.smartitengineering.cms.client.api.FieldResource;
import com.smartitengineering.cms.client.api.RootResource;
import com.smartitengineering.cms.client.api.UriTemplateResource;
import com.smartitengineering.cms.client.api.WorkspaceContentCoProcessorResource;
import com.smartitengineering.cms.client.api.WorkspaceContentCoProcessorsResource;
import com.smartitengineering.cms.client.api.WorkspaceContentResouce;
import com.smartitengineering.cms.client.api.WorkspaceFeedResource;
import com.smartitengineering.cms.client.api.WorkspaceFriendsResource;
import com.smartitengineering.cms.client.api.WorkspaceRepresentationResource;
import com.smartitengineering.cms.client.api.WorkspaceRepresentationsResource;
import com.smartitengineering.cms.client.api.WorkspaceValidatorResource;
import com.smartitengineering.cms.client.api.WorkspaceValidatorsResource;
import com.smartitengineering.cms.client.api.WorkspaceVariationResource;
import com.smartitengineering.cms.client.api.WorkspaceVariationsResource;
import com.smartitengineering.cms.ws.common.domains.CollectionFieldDef;
import com.smartitengineering.cms.ws.common.domains.CollectionFieldValue;
import com.smartitengineering.cms.ws.common.domains.CompositeFieldDef;
import com.smartitengineering.cms.ws.common.domains.CompositeFieldValue;
import com.smartitengineering.cms.ws.common.domains.Content;
import com.smartitengineering.cms.ws.common.domains.EnumFieldDef;
import com.smartitengineering.cms.ws.common.domains.Field;
import com.smartitengineering.cms.ws.common.domains.FieldImpl;
import com.smartitengineering.cms.ws.common.domains.FieldValue;
import com.smartitengineering.cms.ws.common.domains.FieldValueImpl;
import com.smartitengineering.cms.ws.common.domains.OtherFieldValueImpl;
import com.smartitengineering.cms.ws.common.domains.ResourceTemplateImpl;
import com.smartitengineering.cms.ws.common.domains.Workspace;
import com.smartitengineering.cms.ws.common.domains.WorkspaceImpl.WorkspaceIdImpl;
import com.smartitengineering.cms.ws.common.providers.JacksonJsonProvider;
import com.smartitengineering.cms.ws.common.providers.TextURIListProvider;
import com.smartitengineering.dao.hbase.ddl.HBaseTableGenerator;
import com.smartitengineering.dao.hbase.ddl.config.json.ConfigurationJsonParser;
import com.smartitengineering.util.bean.guice.GuiceUtil;
import com.smartitengineering.util.rest.client.ApplicationWideClientFactoryImpl;
import com.smartitengineering.util.rest.client.ClientUtil;
import com.smartitengineering.util.rest.client.ConnectionConfig;
import com.smartitengineering.util.rest.client.ResourceLink;
import com.smartitengineering.util.rest.client.jersey.cache.CacheableClient;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.atom.abdera.impl.provider.entity.FeedProvider;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.multipart.FormDataMultiPart;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.Feed;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.mutable.MutableInt;
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

  public static final int SLEEP_DURATION = 3000;
  private static final int PORT = 10080;
  public static final String DEFAULT_NS = "com.smartitengineering";
  public static final String ROOT_URI_STRING = "http://localhost:" + PORT + "/cms/";
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
    /*
     * The following is for solr for later, when this is to be used it
     */
    System.setProperty("solr.solr.home", "./target/sample-conf/");
    Handler solr = new WebAppContext("./target/solr/", "/solr");
    handlerList.addHandler(solr);
    final String webapp = "./src/test/webapp/";
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
    friendsResource.addFriend(new URI(ROOT_URI_STRING + "w/com.smartitengineering/test"));
    friendsResource.get();
    Collection<URI> frdUri = friendsResource.getLastReadStateOfEntity();
    Iterator<URI> frdUris = frdUri.iterator();
    Assert.assertEquals(1, frdUri.size());
    Assert.assertEquals(ROOT_URI_STRING + "w/com.smartitengineering/test", frdUris.next().toASCIIString());
    friendsResource.addFriend(URI.create("/cms/w/a%20test%20namespace/this%20is%20a%20test"));
    friendsResource.addFriend(new URI("cms/w/testNS/test"));
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
    Assert.assertEquals(ROOT_URI_STRING + "w/a%20test%20namespace/this%20is%20a%20test", friendWorkspace);
    LOGGER.debug(new StringBuilder("First friend workspace is : ").append(friendWorkspace).toString());
    friendWorkspace = frdUris.next().toASCIIString();
    Assert.assertEquals(ROOT_URI_STRING + "w/com.smartitengineering/test", friendWorkspace);
    LOGGER.debug(new StringBuilder("Second friend workspace is : ").append(friendWorkspace).toString());
    friendWorkspace = frdUris.next().toASCIIString();
    Assert.assertEquals(ROOT_URI_STRING + "w/testNS/test", friendWorkspace);
    LOGGER.debug(new StringBuilder("Third friend workspace is : ").append(friendWorkspace).toString());
  }

  @Test
  public void testDeleteFriend() throws Exception {
    final RootResource rootResource = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    rootResource.get();
    final Iterator<WorkspaceFeedResource> iterator = rootResource.getWorkspaceFeeds().iterator();
    WorkspaceFeedResource feedResource = iterator.next();
    WorkspaceFriendsResource friendsResource = feedResource.getFriends();
    friendsResource.deleteFriend(new URI(ROOT_URI_STRING + "w/com.smartitengineering/test"));
    friendsResource.get();
    Collection<URI> frdUri = friendsResource.getLastReadStateOfEntity();
    Iterator<URI> frdUris = frdUri.iterator();
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(new StringBuilder("Total no of friend workspace after deleting a friend is : ").append(frdUri.size()).
          toString());
    }
//    Assert.assertEquals(2, frdUri.size());
//    String friendWorkspace = frdUris.next().toASCIIString();
//    Assert.assertEquals(ROOT_URI_STRING + "w/a%20test%20namespace/this%20is%20a%20test", friendWorkspace);
//    LOGGER.debug(new StringBuilder("First friend workspace is : ").append(friendWorkspace).toString());
//    friendWorkspace = frdUris.next().toASCIIString();
//    Assert.assertEquals(ROOT_URI_STRING + "w/testNS/test", friendWorkspace);
//    LOGGER.debug(new StringBuilder("Second friend workspace is : ").append(friendWorkspace).toString());
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

    uris.add(new URI(ROOT_URI_STRING + "w/atest2/additional"));
    uris.add(new URI(ROOT_URI_STRING + "w/com.smartitengineering/test"));

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
    Assert.assertEquals(ROOT_URI_STRING + "w/atest2/additional", friendWS1);
    LOGGER.debug(new StringBuilder("First friend after replacing is : ").append(friendWS1).toString());
    final String friendWS2 = frdUris.next().toASCIIString();
    Assert.assertEquals(ROOT_URI_STRING + "w/com.smartitengineering/test", friendWS2);
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
  public void testCreateValidator() throws Exception {

    ResourceTemplateImpl template = new ResourceTemplateImpl();
    String temp = "validator";
    template.setName("val");
    final byte[] bytes = temp.getBytes();
    template.setTemplate(bytes);
    template.setTemplateType(TemplateType.JAVASCRIPT.toString());

    RootResource resource = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    Collection<WorkspaceFeedResource> workspaceFeedResources = resource.getWorkspaceFeeds();
    Iterator<WorkspaceFeedResource> iterator = workspaceFeedResources.iterator();
    WorkspaceFeedResource feedResource = iterator.next();
    WorkspaceValidatorsResource representationsResource = feedResource.getValidators();
    WorkspaceValidatorResource validatorResource = representationsResource.createValidator(template);
    Assert.assertEquals("val", validatorResource.get().getName());
    Assert.assertEquals(temp, new String(validatorResource.get().getTemplate()));
    Assert.assertEquals(ValidatorType.JAVASCRIPT.toString(), validatorResource.get().getTemplateType());
  }

  @Test
  public void testUpdateValidator() throws Exception {
    LOGGER.info(":::::::::::::: UPDATE VALIDATOR RESOURCE TEST ::::::::::::::");
    ResourceTemplateImpl template = new ResourceTemplateImpl();
    String temp = "newValidator";
    final byte[] bytes = temp.getBytes();
    template.setTemplate(bytes);
    template.setTemplateType(ValidatorType.RUBY.toString());

    RootResource resource = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    Collection<WorkspaceFeedResource> workspaceFeedResources = resource.getWorkspaceFeeds();
    Iterator<WorkspaceFeedResource> iterator = workspaceFeedResources.iterator();
    WorkspaceFeedResource feedResource = iterator.next();

    Collection<WorkspaceValidatorResource> validatorResources = feedResource.getValidators().getValidatorResources();
    Assert.assertEquals(1, validatorResources.size());
    Iterator<WorkspaceValidatorResource> validatorIterator = validatorResources.iterator();
    WorkspaceValidatorResource validatorRsrc = validatorIterator.next();
    validatorRsrc.update(template);
    Assert.assertEquals("val", validatorRsrc.get().getName());
    Assert.assertEquals(temp, new String(validatorRsrc.get().getTemplate()));
    Assert.assertEquals(ValidatorType.RUBY.toString(), validatorRsrc.get().getTemplateType());
    resource.getWorkspaceFeeds();
    WorkspaceValidatorResource secondValidatorResource = resource.getWorkspaceFeeds().iterator().next().
        getValidators().getValidatorResources().iterator().next();
    template.setTemplateType(ValidatorType.JAVASCRIPT.name());
    secondValidatorResource.update(template);
    Assert.assertEquals(ValidatorType.JAVASCRIPT.name(), secondValidatorResource.get().getTemplateType());
    try {
      validatorRsrc.update(template);
      Assert.fail("Should not have been able to update!");
    }
    catch (UniformInterfaceException ex) {
      //Exception expected
      validatorRsrc.get();
      validatorRsrc.update(template);
    }
  }

  @Test
  public void testDeleteValidator() throws Exception {
    LOGGER.info(":::::::::::::: DELETE VALIDATOR RESOURCE TEST ::::::::::::::");
    ResourceTemplateImpl template = new ResourceTemplateImpl();
    String temp = "Template2";
    template.setName("val2");
    final byte[] bytes = temp.getBytes();
    template.setTemplate(bytes);
    template.setTemplateType(ValidatorType.GROOVY.name());
    RootResource resource = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    Collection<WorkspaceFeedResource> workspaceFeedResources = resource.getWorkspaceFeeds();
    Iterator<WorkspaceFeedResource> iterator = workspaceFeedResources.iterator();
    WorkspaceFeedResource feedResource = iterator.next();
    final WorkspaceValidatorsResource validatorsResource = feedResource.getValidators();

    Collection<WorkspaceValidatorResource> validatorResources = validatorsResource.getValidatorResources();
    Assert.assertEquals(1, validatorResources.size());
    validatorsResource.createValidator(template);
    validatorsResource.get();
    validatorResources = validatorsResource.getValidatorResources();
    Iterator<WorkspaceValidatorResource> validatorIterator = validatorResources.iterator();
    WorkspaceValidatorResource validatorResource = validatorIterator.next();

    validatorResource.delete(ClientResponse.Status.ACCEPTED);
    Collection<WorkspaceValidatorResource> secondValidatorResources = resource.getWorkspaceFeeds().iterator().
        next().getValidators().getValidatorResources();
    Assert.assertEquals(1, secondValidatorResources.size());
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
    Collection<ContentTypeFeedResource> feedCollection = contentTypesResource.getContentTypeFeeds();
    Assert.assertEquals(3, feedCollection.size());
    for (ContentTypeFeedResource res : feedCollection) {
      Assert.assertTrue(res.getFieldDefs().size() > 0);
    }

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
      Assert.assertEquals(servedContentType.getContentTypeID().getWorkspace().getName(),
                          getContentType.getContentTypeID().
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
        Assert.assertEquals(servedContentType.getParent().getWorkspace().getGlobalNamespace(),
                            getContentType.getParent().
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
        if (servedFieldDef.getCustomValidators() != null && !servedFieldDef.getCustomValidators().isEmpty()) {
          if (servedFieldDef.getCustomValidators().iterator().next().getUri() != null) {
            Assert.assertEquals(servedFieldDef.getCustomValidators().iterator().next().getUri().getType().name(),
                                getFieldDef.getCustomValidators().iterator().next().getUri().getType().name());
            Assert.assertEquals(servedFieldDef.getCustomValidators().iterator().next().getUri().getValue(), getFieldDef.
                getCustomValidators().iterator().next().getUri().getValue());
          }
        }

        if (getFieldDef.getSearchDefinition() != null) {
          if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Field Search Def : " + getFieldDef.getSearchDefinition().toString());
          }
          Assert.assertEquals(getFieldDef.getSearchDefinition().toString(), getFieldDef.getSearchDefinition().toString());
        }
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
    Assert.assertEquals(4, collection.size());

    InputStream inputStream = IOUtils.toInputStream(XML);

    String NAME_SPACE = "com.smartitengineering.smart-shopping.content";

    Collection<WritableContentType> contentTypes = SmartContentAPI.getInstance().getContentTypeLoader().
        parseContentTypes(workspaceId, inputStream, com.smartitengineering.cms.api.common.MediaType.APPLICATION_XML);
    String[] name = {"Book", "Publisher", "Author", "Address"};
    Collection<ContentType> HBaseContentTypes = new ArrayList<ContentType>();
    for (int i = 0; i < 4; i++) {

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
      Assert.assertEquals(servedContentType.getContentTypeID().getWorkspace().getName(),
                          getContentType.getContentTypeID().
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
        Assert.assertEquals(servedContentType.getParent().getWorkspace().getGlobalNamespace(),
                            getContentType.getParent().
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
        if (servedFieldDef.getCustomValidators() != null && !servedFieldDef.getCustomValidators().isEmpty()) {
          if (servedFieldDef.getCustomValidators().iterator().next().getUri() != null) {
            Assert.assertEquals(servedFieldDef.getCustomValidators().iterator().next().getUri().getType().name(),
                                getFieldDef.getCustomValidators().iterator().next().getUri().getType().name());
            Assert.assertEquals(servedFieldDef.getCustomValidators().iterator().next().getUri().getValue(), getFieldDef.
                getCustomValidators().iterator().next().getUri().getValue());
          }
        }

        if (getFieldDef.getSearchDefinition() != null) {

          if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Field Search Def : " + getFieldDef.getSearchDefinition().toString());
          }

          Assert.assertEquals(getFieldDef.getSearchDefinition().toString(), getFieldDef.getSearchDefinition().toString());
        }
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
  public void testCreateInvlidContent() throws Exception {
    LOGGER.info(":::::::::::::: CREATE INVALID CONTENT RESOURCE TEST ::::::::::::::");
    ObjectMapper mapper = new ObjectMapper();
    String JSON = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("InvalidContent.json"));
    InputStream stream = IOUtils.toInputStream(JSON);
    Content content = mapper.readValue(stream, Content.class);
    Assert.assertNotNull(content);
    RootResource resource = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    Collection<WorkspaceFeedResource> workspaceFeedResources = resource.getWorkspaceFeeds();
    Iterator<WorkspaceFeedResource> iterator = workspaceFeedResources.iterator();
    WorkspaceFeedResource feedResource = iterator.next();
    try {
      feedResource.getContents().createContentResource(content);
      Assert.fail("Should not be able to create!");
    }
    catch (UniformInterfaceException ex) {
      Assert.assertEquals(400, ex.getResponse().getStatus());
    }
  }

  @Test
  public void testCreateContent() throws Exception {
    LOGGER.info(":::::::::::::: CREATE DUMMY CONTENT RESOURCE TEST ::::::::::::::");
    ObjectMapper mapper1 = new ObjectMapper();
    String JSON1 = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("DummyContent.json"));
    InputStream stream1 = IOUtils.toInputStream(JSON1);
    Content contentTest = mapper1.readValue(stream1, Content.class);
    Assert.assertNotNull(contentTest);
    RootResource resource1 = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    Collection<WorkspaceFeedResource> workspaceFeedResources1 = resource1.getWorkspaceFeeds();
    Iterator<WorkspaceFeedResource> iteratorTest = workspaceFeedResources1.iterator();
    WorkspaceFeedResource feedResourceTest = iteratorTest.next();
    ContentResource contentResourceTest = feedResourceTest.getContents().createContentResource(contentTest);
    FieldValueImpl value = new FieldValueImpl();
    value.setType("content");
    value.setValue(contentResourceTest.getUri().toASCIIString());
    FieldImpl authorField = new FieldImpl();
    authorField.setName("Authors");
    authorField.setValue(value);

    String valueString = "otherValue";
    byte[] otherValue = valueString.getBytes();
    OtherFieldValueImpl otherFieldValueImpl = new OtherFieldValueImpl();
    otherFieldValueImpl.setMimeType("jpeg/image");
    otherFieldValueImpl.setType("other");
    otherFieldValueImpl.setValue(Base64.encodeBase64String(otherValue));

    FieldImpl valueImpl = new FieldImpl();
    valueImpl.setName("b");
    valueImpl.setValue(otherFieldValueImpl);

    Thread.sleep(SLEEP_DURATION);

    LOGGER.info(":::::::::::::: CREATE CONTENT RESOURCE TEST ::::::::::::::");
    ObjectMapper mapper = new ObjectMapper();
    String JSON = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("Content.json"));
    InputStream stream = IOUtils.toInputStream(JSON);
    Content content = mapper.readValue(stream, Content.class);

    content.getFields().add(valueImpl);
    content.getFields().add(authorField);
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
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Parent Container Uri : " + content1.getParentContentUri());
      LOGGER.debug("Status : " + content1.getStatus());
      LOGGER.debug("Number of Fields : " + content1.getFields().size());
    }

    Collection<Field> fields = content.getFields();
    Collections.reverse((List<Field>) fields);
    Iterator<Field> iterator1 = fields.iterator();
    Field field = iterator1.next();
    final Iterator<Field> iterator2 = content1.getFields().iterator();
    Field field1 = iterator2.next();

    Assert.assertEquals(field.getName(), field1.getName());
    Assert.assertNotNull(field1.getFieldRawContentUri());
    Assert.assertNotNull(field1.getFieldUri());
    Assert.assertEquals(field.getValue().getType().toUpperCase(), field1.getValue().getType());
    Assert.assertTrue(field.getValue().getValue().endsWith(field1.getValue().getValue()));

    field = iterator1.next();
    field1 = iterator2.next();
    Assert.assertEquals(field.getName(), field1.getName());
    Assert.assertEquals(field.getValue().getType().toUpperCase(), field1.getValue().getType());
    Assert.assertEquals(field.getValue().getValue(), field1.getValue().getValue());

    field = iterator1.next();
    field1 = iterator2.next();
    Assert.assertEquals(field.getName(), field1.getName());
    Assert.assertEquals(field.getValue().getType().toUpperCase(), field1.getValue().getType());
    Assert.assertEquals(field.getValue().getValue(), field1.getValue().getValue());

    CollectionFieldValue collectionFieldValue = (CollectionFieldValue) field.getValue();
    CollectionFieldValue collectionFieldValue1 = (CollectionFieldValue) field1.getValue();
    Assert.assertEquals(collectionFieldValue.getType().toUpperCase(), collectionFieldValue1.getType());
    Assert.assertEquals(collectionFieldValue.getValues().size(), collectionFieldValue1.getValues().size());
    Iterator<FieldValue> collectionIterator1 = collectionFieldValue.getValues().iterator();
    Iterator<FieldValue> collectionIterator2 = collectionFieldValue1.getValues().iterator();
    while (collectionIterator1.hasNext()) {
      String value1 = collectionIterator1.next().getValue();
      String value2 = collectionIterator2.next().getValue();
      Assert.assertEquals(value1, value2);
    }

    field = iterator1.next();
    field1 = iterator2.next();
    Assert.assertEquals(field.getName(), field1.getName());
    Assert.assertEquals(field.getValue().getType().toUpperCase(), field1.getValue().getType());
    Assert.assertEquals(field.getValue().getValue(), field1.getValue().getValue());

    field = iterator1.next();
    field1 = iterator2.next();
    Assert.assertEquals(field.getName(), field1.getName());
    Assert.assertEquals(field.getValue().getType().toUpperCase(), field1.getValue().getType());
    Assert.assertEquals(field.getValue().getValue(), field1.getValue().getValue());

    field = iterator1.next();
    field1 = iterator2.next();
    Assert.assertEquals(field.getName(), field1.getName());
    Assert.assertEquals(field.getValue().getType().toUpperCase(), field1.getValue().getType());
    Assert.assertEquals(field.getValue().getValue(), field1.getValue().getValue());

    field = iterator1.next();
    field1 = iterator2.next();
    Assert.assertEquals(field.getName(), field1.getName());
    Assert.assertEquals(field.getValue().getType().toUpperCase(), field1.getValue().getType());
    Assert.assertEquals(field.getValue().getValue(), field1.getValue().getValue());

    field = iterator1.next();
    field1 = iterator2.next();
    Assert.assertEquals(field.getName(), field1.getName());
    Assert.assertEquals(field.getValue().getType().toUpperCase(), field1.getValue().getType());
    Assert.assertEquals(field.getValue().getValue(), field1.getValue().getValue());
  }

  @Test
  public void testUriTempaltes() throws URISyntaxException {
    LOGGER.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TEST TEMPLATES %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
    final String workspaceNS, workspaceId, typeNS, typeId;
    RootResource resource = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    WorkspaceFeedResource feedResource = resource.getWorkspaceFeeds().iterator().next();
    workspaceNS = "atest2";
    workspaceId = "additional";
    typeNS = "com.smartitengineering.smart-shopping.content";
    typeId = "Publisher";
    UriTemplateResource templateResource = resource.getTemplates();
    LOGGER.info("Testing getting workspace");
    Assert.assertNotNull(templateResource.getWorkspaceResource(workspaceNS, workspaceId));
    LOGGER.info("Testing getting content type");
    Assert.assertNotNull(templateResource.getContentTypeResource(workspaceNS, workspaceId, typeNS, typeId));
    LOGGER.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% END TEST TEMPLATES %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
  }

  @Test
  public void testContentListener() throws Exception {
    final MutableInt createCount = new MutableInt(0);
    final MutableInt updateCount = new MutableInt(0);
    final MutableInt deleteCount = new MutableInt(0);
    SmartContentAPI.getInstance().getEventRegistrar().addListener(new EventListener() {

      @Override
      public boolean accepts(Event event) {
        return event.getEventSourceType().equals(Event.Type.CONTENT) && event.getEventType().equals(EventType.CREATE);
      }

      @Override
      public void notify(Event event) {
        createCount.add(1);
      }
    });
    SmartContentAPI.getInstance().getEventRegistrar().addListener(new EventListener() {

      @Override
      public boolean accepts(Event event) {
        return event.getEventSourceType().equals(Event.Type.CONTENT) && event.getEventType().equals(EventType.UPDATE);
      }

      @Override
      public void notify(Event event) {
        updateCount.add(1);
      }
    });
    SmartContentAPI.getInstance().getEventRegistrar().addListener(new EventListener() {

      @Override
      public boolean accepts(Event event) {
        return event.getEventSourceType().equals(Event.Type.CONTENT) && event.getEventType().equals(EventType.DELETE);
      }

      @Override
      public void notify(Event event) {
        deleteCount.add(1);
      }
    });
    ObjectMapper mapper1 = new ObjectMapper();
    String JSON1 = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("DummyContent.json"));
    InputStream stream1 = IOUtils.toInputStream(JSON1);
    Content contentTest = mapper1.readValue(stream1, Content.class);
    Assert.assertNotNull(contentTest);
    RootResource resource1 = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    Collection<WorkspaceFeedResource> workspaceFeedResources1 = resource1.getWorkspaceFeeds();
    Iterator<WorkspaceFeedResource> iteratorTest = workspaceFeedResources1.iterator();
    WorkspaceFeedResource feedResourceTest = iteratorTest.next();
    ContentResource authorResource = feedResourceTest.getContents().createContentResource(contentTest);
    FieldValueImpl value = new FieldValueImpl();
    value.setType("content");
    value.setValue(authorResource.getUri().toASCIIString());
    FieldImpl authorField = new FieldImpl();
    authorField.setName("Authors");
    authorField.setValue(value);
    String valueString = "otherValue";
    byte[] otherValue = valueString.getBytes();
    OtherFieldValueImpl otherFieldValueImpl = new OtherFieldValueImpl();
    otherFieldValueImpl.setMimeType("jpeg/image");
    otherFieldValueImpl.setType("other");
    otherFieldValueImpl.setValue(Base64.encodeBase64String(otherValue));

    FieldImpl valueImpl = new FieldImpl();
    valueImpl.setName("b");
    valueImpl.setValue(otherFieldValueImpl);
    ObjectMapper mapper = new ObjectMapper();
    String JSON = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("Content.json"));
    InputStream stream = IOUtils.toInputStream(JSON);
    Content content = mapper.readValue(stream, Content.class);

    content.getFields().add(valueImpl);
    content.getFields().add(authorField);
    Assert.assertNotNull(content);

    RootResource resource = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    Collection<WorkspaceFeedResource> workspaceFeedResources = resource.getWorkspaceFeeds();
    Iterator<WorkspaceFeedResource> iterator = workspaceFeedResources.iterator();
    WorkspaceFeedResource feedResource = iterator.next();
    Thread.sleep(SLEEP_DURATION);
    ContentResource contentResource = feedResource.getContents().createContentResource(content);
    Assert.assertEquals(2, createCount.intValue());
    contentResource.update(content);
    Assert.assertEquals(1, updateCount.intValue());
    authorResource.delete();
    contentResource.get();
    contentResource.delete();
    Assert.assertEquals(2, deleteCount.intValue());
    Thread.sleep(SLEEP_DURATION);
  }

  @Test
  public void testAddContainerContent() throws Exception {
    LOGGER.info(":::::::::::::: CREATE CONTENT IN CONTAINER RESOURCE TEST ::::::::::::::");

    ObjectMapper mapper1 = new ObjectMapper();
    String JSON1 = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("DummyContent.json"));
    InputStream stream1 = IOUtils.toInputStream(JSON1);
    Content contentTest = mapper1.readValue(stream1, Content.class);
    Assert.assertNotNull(contentTest);
    RootResource resource1 = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    Collection<WorkspaceFeedResource> workspaceFeedResources1 = resource1.getWorkspaceFeeds();
    Iterator<WorkspaceFeedResource> iteratorTest = workspaceFeedResources1.iterator();
    WorkspaceFeedResource feedResourceTest = iteratorTest.next();
    ContentResource contentResourceTest = feedResourceTest.getContents().createContentResource(contentTest);
    Thread.sleep(SLEEP_DURATION);

    FieldValueImpl value = new FieldValueImpl();
    value.setType("content");
    value.setValue(contentResourceTest.getUri().toASCIIString());
    FieldImpl authorField = new FieldImpl();
    authorField.setName("Authors");
    authorField.setValue(value);

    String valueString = "otherValue";
    byte[] otherValue = valueString.getBytes();
    OtherFieldValueImpl otherFieldValueImpl = new OtherFieldValueImpl();
    otherFieldValueImpl.setMimeType("jpeg/image");
    otherFieldValueImpl.setType("other");
    otherFieldValueImpl.setValue(Base64.encodeBase64String(otherValue));

    FieldImpl valueImpl = new FieldImpl();
    valueImpl.setName("b");
    valueImpl.setValue(otherFieldValueImpl);

    ObjectMapper mapper = new ObjectMapper();
    String JSON = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("Update-Content.json"));
    InputStream stream = IOUtils.toInputStream(JSON);
    Content content = mapper.readValue(stream, Content.class);

    content.getFields().add(valueImpl);
    content.getFields().add(authorField);

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
    Collection<Field> fields = content.getFields();
    Collections.reverse((List<Field>) fields);
    final Field field = fields.iterator().next();
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

    ObjectMapper mapper1 = new ObjectMapper();
    String JSON1 = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("DummyContent.json"));
    InputStream stream1 = IOUtils.toInputStream(JSON1);
    Content contentTest = mapper1.readValue(stream1, Content.class);
    Assert.assertNotNull(contentTest);
    RootResource resource1 = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    Collection<WorkspaceFeedResource> workspaceFeedResources1 = resource1.getWorkspaceFeeds();
    Iterator<WorkspaceFeedResource> iteratorTest = workspaceFeedResources1.iterator();
    WorkspaceFeedResource feedResourceTest = iteratorTest.next();
    ContentResource contentResourceTest = feedResourceTest.getContents().createContentResource(contentTest);
    Thread.sleep(SLEEP_DURATION);

    FieldValueImpl value = new FieldValueImpl();
    value.setType("content");
    value.setValue(contentResourceTest.getUri().toASCIIString());
    FieldImpl authorField = new FieldImpl();
    authorField.setName("Authors");
    authorField.setValue(value);

    String valueString = "otherValue";
    byte[] otherValue = valueString.getBytes();
    OtherFieldValueImpl otherFieldValueImpl = new OtherFieldValueImpl();
    otherFieldValueImpl.setMimeType("jpeg/image");
    otherFieldValueImpl.setType("other");
    otherFieldValueImpl.setValue(Base64.encodeBase64String(otherValue));

    FieldImpl valueImpl = new FieldImpl();
    valueImpl.setName("b");
    valueImpl.setValue(otherFieldValueImpl);

    Collection<URI> contentUri = new ArrayList<URI>();
    ObjectMapper mapper = new ObjectMapper();
    String JSON = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("Content.json"));
    InputStream stream = IOUtils.toInputStream(JSON);
    Content content = mapper.readValue(stream, Content.class);
    content.getFields().add(valueImpl);
    content.getFields().add(authorField);
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

    ObjectMapper mapper1 = new ObjectMapper();
    String JSON1 = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("DummyContent.json"));
    InputStream stream1 = IOUtils.toInputStream(JSON1);
    Content contentTest = mapper1.readValue(stream1, Content.class);
    Assert.assertNotNull(contentTest);
    RootResource resource1 = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    Collection<WorkspaceFeedResource> workspaceFeedResources1 = resource1.getWorkspaceFeeds();
    Iterator<WorkspaceFeedResource> iteratorTest = workspaceFeedResources1.iterator();
    WorkspaceFeedResource feedResourceTest = iteratorTest.next();
    ContentResource contentResourceTest = feedResourceTest.getContents().createContentResource(contentTest);

    Thread.sleep(SLEEP_DURATION);

    FieldValueImpl value = new FieldValueImpl();
    value.setType("content");
    value.setValue(contentResourceTest.getUri().toASCIIString());
    FieldImpl authorField = new FieldImpl();
    authorField.setName("Authors");
    authorField.setValue(value);

    String valueString = "otherValue";
    byte[] otherValue = valueString.getBytes();
    OtherFieldValueImpl otherFieldValueImpl = new OtherFieldValueImpl();
    otherFieldValueImpl.setMimeType("jpeg/image");
    otherFieldValueImpl.setType("other");
    otherFieldValueImpl.setValue(Base64.encodeBase64String(otherValue));

    FieldImpl valueImpl = new FieldImpl();
    valueImpl.setName("b");
    valueImpl.setValue(otherFieldValueImpl);

    ObjectMapper mapper = new ObjectMapper();
    String JSON = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("Content.json"));
    InputStream stream = IOUtils.toInputStream(JSON);
    Content content = mapper.readValue(stream, Content.class);

    content.getFields().add(valueImpl);
    content.getFields().add(authorField);
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

    LOGGER.info("::: TEST SEARCHING FROM WORKSPACE RESOURCE");
    ContentSearcherResource contentSearcherResource = feedResource.searchContent("count=3");
    Assert.assertEquals(3, contentSearcherResource.get().getEntries().size());

    LOGGER.info("::: TEST SEARCHING FROM CONTENT RESOURCE");
    ContentsResource contentsResource = feedResource.getContents();
    ContentSearcherResource contentSearcherResource1 = contentsResource.searchContent("count=6");
    Assert.assertEquals(6, contentSearcherResource1.get().getEntries().size());

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Parent Container Uri : " + content1.getParentContentUri());
      LOGGER.debug("Status : " + content1.getStatus());
      LOGGER.debug("Number of Fields : " + content1.getFields().size());
    }

    Collection<Field> fields = content.getFields();
    Collections.reverse((List<Field>) fields);
    Iterator<Field> iterator1 = fields.iterator();
    Field field = iterator1.next();
    final Iterator<Field> iterator2 = content1.getFields().iterator();
    Field field1 = iterator2.next();

    Assert.assertEquals(field.getName(), field1.getName());
    Assert.assertNotNull(field1.getFieldRawContentUri());
    Assert.assertNotNull(field1.getFieldUri());
    Assert.assertEquals(field.getValue().getType().toUpperCase(), field1.getValue().getType());
    Assert.assertTrue(field.getValue().getValue().endsWith(field1.getValue().getValue()));

    field = iterator1.next();
    field1 = iterator2.next();
    Assert.assertEquals(field.getName(), field1.getName());
    Assert.assertEquals(field.getValue().getType().toUpperCase(), field1.getValue().getType());
    Assert.assertEquals(field.getValue().getValue(), field1.getValue().getValue());

    field = iterator1.next();
    field1 = iterator2.next();
    Assert.assertEquals(field.getName(), field1.getName());
    Assert.assertEquals(field.getValue().getType().toUpperCase(), field1.getValue().getType());
    Assert.assertEquals(field.getValue().getValue(), field1.getValue().getValue());

    CollectionFieldValue collectionFieldValue = (CollectionFieldValue) field.getValue();
    CollectionFieldValue collectionFieldValue1 = (CollectionFieldValue) field1.getValue();
    Assert.assertEquals(collectionFieldValue.getType().toUpperCase(), collectionFieldValue1.getType());
    Assert.assertEquals(collectionFieldValue.getValues().size(), collectionFieldValue1.getValues().size());
    Iterator<FieldValue> collectionIterator1 = collectionFieldValue.getValues().iterator();
    Iterator<FieldValue> collectionIterator2 = collectionFieldValue1.getValues().iterator();
    while (collectionIterator1.hasNext()) {
      String value1 = collectionIterator1.next().getValue();
      String value2 = collectionIterator2.next().getValue();
      Assert.assertEquals(value1, value2);
    }

    field = iterator1.next();
    field1 = iterator2.next();
    Assert.assertEquals(field.getName(), field1.getName());
    Assert.assertEquals(field.getValue().getType().toUpperCase(), field1.getValue().getType());
    Assert.assertEquals(field.getValue().getValue(), field1.getValue().getValue());

    field = iterator1.next();
    field1 = iterator2.next();
    Assert.assertEquals(field.getName(), field1.getName());
    Assert.assertEquals(field.getValue().getType().toUpperCase(), field1.getValue().getType());
    Assert.assertEquals(field.getValue().getValue(), field1.getValue().getValue());

    field = iterator1.next();
    field1 = iterator2.next();
    Assert.assertEquals(field.getName(), field1.getName());
    Assert.assertEquals(field.getValue().getType().toUpperCase(), field1.getValue().getType());
    Assert.assertEquals(field.getValue().getValue(), field1.getValue().getValue());

    field = iterator1.next();
    field1 = iterator2.next();
    Assert.assertEquals(field.getName(), field1.getName());
    Assert.assertEquals(field.getValue().getType().toUpperCase(), field1.getValue().getType());
    Assert.assertEquals(field.getValue().getValue(), field1.getValue().getValue());

    field = iterator1.next();
    field1 = iterator2.next();
    Assert.assertEquals(field.getName(), field1.getName());
    Assert.assertEquals(field.getValue().getType().toUpperCase(), field1.getValue().getType());
    Assert.assertEquals(field.getValue().getValue(), field1.getValue().getValue());

    LOGGER.info(":::::::::::::: Updating Content Resource ::::::::::::::");

    ObjectMapper updateMapper = new ObjectMapper();
    String updateJSON = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("Update-Content.json"));
    InputStream updateStream = IOUtils.toInputStream(updateJSON);
    Content updateContent = updateMapper.readValue(updateStream, Content.class);

    updateContent.getFields().add(valueImpl);
    updateContent.getFields().add(authorField);
    Assert.assertNotNull(updateContent);

    RootResource updateResource = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    Collection<WorkspaceFeedResource> updateWorkspaceFeedResources = updateResource.getWorkspaceFeeds();
    Iterator<WorkspaceFeedResource> updateIterator = updateWorkspaceFeedResources.iterator();
    WorkspaceFeedResource updateFeedResource = updateIterator.next();

    ContentResource updateContentResource = updateFeedResource.getContents().createContentResource(updateContent);
    Content updateContent1 = updateContentResource.get();
    Assert.assertNotNull(updateContent1);
    Assert.assertEquals(updateContent.getParentContentUri(), updateContent1.getParentContentUri());
    Assert.assertEquals(updateContent.getStatus(), updateContent1.getStatus());
    Assert.assertEquals(updateContent.getFields().size(), updateContent1.getFields().size());

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Parent Container Uri : " + updateContent1.getParentContentUri());
      LOGGER.debug("Status : " + updateContent1.getStatus());
      LOGGER.debug("Number of Fields : " + updateContent1.getFields().size());
    }

    Collection<Field> updateFields = updateContent.getFields();
    Collections.reverse((List<Field>) updateFields);
    Iterator<Field> updateIterator1 = updateFields.iterator();
    Field updateField = updateIterator1.next();
    final Iterator<Field> updateIterator2 = updateContent1.getFields().iterator();
    Field updateField1 = updateIterator2.next();

    Assert.assertEquals(updateField.getName(), updateField1.getName());
    Assert.assertNotNull(updateField1.getFieldRawContentUri());
    Assert.assertNotNull(updateField1.getFieldUri());
    Assert.assertEquals(updateField.getValue().getType().toUpperCase(), updateField1.getValue().getType());
    Assert.assertTrue(updateField.getValue().getValue().endsWith(updateField1.getValue().getValue()));

    updateField = updateIterator1.next();
    updateField1 = updateIterator2.next();
    Assert.assertEquals(updateField.getName(), updateField1.getName());
    Assert.assertEquals(updateField.getValue().getType().toUpperCase(), updateField1.getValue().getType());
    Assert.assertEquals(updateField.getValue().getValue(), updateField1.getValue().getValue());

    updateField = updateIterator1.next();
    updateField1 = updateIterator2.next();
    Assert.assertEquals(updateField.getName(), updateField1.getName());
    Assert.assertEquals(updateField.getValue().getType().toUpperCase(), updateField1.getValue().getType());
    Assert.assertEquals(updateField.getValue().getValue(), updateField1.getValue().getValue());

    CollectionFieldValue updateCollectionFieldValue = (CollectionFieldValue) updateField.getValue();
    CollectionFieldValue updateCollectionFieldValue1 = (CollectionFieldValue) updateField1.getValue();
    Assert.assertEquals(updateCollectionFieldValue.getType().toUpperCase(), updateCollectionFieldValue1.getType());
    Assert.assertEquals(updateCollectionFieldValue.getValues().size(), updateCollectionFieldValue1.getValues().size());
    Iterator<FieldValue> updateCollectionIterator1 = updateCollectionFieldValue.getValues().iterator();
    Iterator<FieldValue> updateCollectionIterator2 = updateCollectionFieldValue1.getValues().iterator();
    while (updateCollectionIterator1.hasNext()) {
      String value1 = updateCollectionIterator1.next().getValue();
      String value2 = updateCollectionIterator2.next().getValue();
      Assert.assertEquals(value1, value2);
    }

    updateField = updateIterator1.next();
    updateField1 = updateIterator2.next();
    Assert.assertEquals(updateField.getName(), updateField1.getName());
    Assert.assertEquals(updateField.getValue().getType().toUpperCase(), updateField1.getValue().getType());
    Assert.assertEquals(updateField.getValue().getValue(), updateField1.getValue().getValue());

    updateField = updateIterator1.next();
    updateField1 = updateIterator2.next();
    Assert.assertEquals(updateField.getName(), updateField1.getName());
    Assert.assertEquals(updateField.getValue().getType().toUpperCase(), updateField1.getValue().getType());
    Assert.assertEquals(updateField.getValue().getValue(), updateField1.getValue().getValue());

    updateField = updateIterator1.next();
    updateField1 = updateIterator2.next();
    Assert.assertEquals(updateField.getName(), updateField1.getName());
    Assert.assertEquals(updateField.getValue().getType().toUpperCase(), updateField1.getValue().getType());
    Assert.assertEquals(updateField.getValue().getValue(), updateField1.getValue().getValue());

    updateField = updateIterator1.next();
    updateField1 = updateIterator2.next();
    Assert.assertEquals(updateField.getName(), updateField1.getName());
    Assert.assertEquals(updateField.getValue().getType().toUpperCase(), updateField1.getValue().getType());
    Assert.assertEquals(updateField.getValue().getValue(), updateField1.getValue().getValue());

    updateField = updateIterator1.next();
    updateField1 = updateIterator2.next();
    Assert.assertEquals(updateField.getName(), updateField1.getName());
    Assert.assertEquals(updateField.getValue().getType().toUpperCase(), updateField1.getValue().getType());
    Assert.assertEquals(updateField.getValue().getValue(), updateField1.getValue().getValue());
  }

  @Test
  public void testContentRepresentation() throws Exception {
    LOGGER.info(":::::::::::::: CONTENT REPRESENTATION RESOURCE TEST ::::::::::::::");

    ObjectMapper mapper1 = new ObjectMapper();
    String JSON1 = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("DummyContent.json"));
    InputStream stream1 = IOUtils.toInputStream(JSON1);
    Content contentTest = mapper1.readValue(stream1, Content.class);
    Assert.assertNotNull(contentTest);
    RootResource resource1 = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    Collection<WorkspaceFeedResource> workspaceFeedResources1 = resource1.getWorkspaceFeeds();
    Iterator<WorkspaceFeedResource> iteratorTest = workspaceFeedResources1.iterator();
    WorkspaceFeedResource feedResourceTest = iteratorTest.next();
    ContentResource contentResourceTest = feedResourceTest.getContents().createContentResource(contentTest);
    LOGGER.info("Author for representation created!");
    Thread.sleep(SLEEP_DURATION);

    FieldValueImpl value = new FieldValueImpl();
    value.setType("content");
    value.setValue(contentResourceTest.getUri().toASCIIString());
    FieldImpl authorField = new FieldImpl();
    authorField.setName("Authors");
    authorField.setValue(value);

    String valueString = "otherValue";
    byte[] otherValue = valueString.getBytes();
    OtherFieldValueImpl otherFieldValueImpl = new OtherFieldValueImpl();
    otherFieldValueImpl.setMimeType("jpeg/image");
    otherFieldValueImpl.setType("other");
    otherFieldValueImpl.setValue(Base64.encodeBase64String(otherValue));

    FieldImpl valueImpl = new FieldImpl();
    valueImpl.setName("b");
    valueImpl.setValue(otherFieldValueImpl);

    ObjectMapper mapper = new ObjectMapper();
    String JSON = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("Content.json"));
    InputStream stream = IOUtils.toInputStream(JSON);
    Content content = mapper.readValue(stream, Content.class);

    content.getFields().add(valueImpl);
    content.getFields().add(authorField);
    Assert.assertNotNull(content);

    RootResource resource = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    Collection<WorkspaceFeedResource> workspaceFeedResources = resource.getWorkspaceFeeds();
    Iterator<WorkspaceFeedResource> iterator = workspaceFeedResources.iterator();
    WorkspaceFeedResource feedResource = iterator.next();

    ContentResource contentResource = feedResource.getContents().createContentResource(content);

    Collection<String> urls = contentResource.getRepresentationUrls();
    Iterator<String> iterator1 = urls.iterator();
    while (iterator1.hasNext()) {
      String next = iterator1.next();
      String type = contentResource.getRepresentation(next);
      Assert.assertEquals("some/type", type);
    }
  }

  @Test
  public void testFieldVariation() throws Exception {
    ObjectMapper mapper1 = new ObjectMapper();
    String JSON1 = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("DummyContent.json"));
    InputStream stream1 = IOUtils.toInputStream(JSON1);
    Content contentTest = mapper1.readValue(stream1, Content.class);
    Assert.assertNotNull(contentTest);
    RootResource resource1 = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    Collection<WorkspaceFeedResource> workspaceFeedResources1 = resource1.getWorkspaceFeeds();
    Iterator<WorkspaceFeedResource> iteratorTest = workspaceFeedResources1.iterator();
    WorkspaceFeedResource feedResourceTest = iteratorTest.next();
    ContentResource contentResourceTest = feedResourceTest.getContents().createContentResource(contentTest);
    Thread.sleep(SLEEP_DURATION);

    FieldValueImpl value = new FieldValueImpl();
    value.setType("content");
    value.setValue(contentResourceTest.getUri().toASCIIString());
    FieldImpl authorField = new FieldImpl();
    authorField.setName("Authors");
    authorField.setValue(value);

    String valueString = "otherValue";
    byte[] otherValue = valueString.getBytes();
    OtherFieldValueImpl otherFieldValueImpl = new OtherFieldValueImpl();
    otherFieldValueImpl.setMimeType("jpeg/image");
    otherFieldValueImpl.setType("other");
    otherFieldValueImpl.setValue(Base64.encodeBase64String(otherValue));

    FieldImpl valueImpl = new FieldImpl();
    valueImpl.setName("b");
    valueImpl.setValue(otherFieldValueImpl);

    ObjectMapper mapper = new ObjectMapper();
    String JSON = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("Content.json"));
    InputStream stream = IOUtils.toInputStream(JSON);
    Content content = mapper.readValue(stream, Content.class);

    content.getFields().add(valueImpl);
    content.getFields().add(authorField);
    Assert.assertNotNull(content);

    RootResource resource = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    Collection<WorkspaceFeedResource> workspaceFeedResources = resource.getWorkspaceFeeds();
    Iterator<WorkspaceFeedResource> iterator = workspaceFeedResources.iterator();
    WorkspaceFeedResource feedResource = iterator.next();

    ContentResource contentResource = feedResource.getContents().createContentResource(content);
    Iterator<FieldResource> iterator2 = contentResource.getFields().iterator();
    while (iterator2.hasNext()) {
      FieldResource next = iterator2.next();
      Collection<String> variations = next.getVariationUrls();
      Iterator<String> iterator1 = variations.iterator();
      while (iterator1.hasNext()) {
        String url = iterator1.next();
        String variation = next.getVariation(url);
        Assert.assertEquals("some/type", variation);
      }
    }
  }

  @Test
  public void testSearch() throws Exception {
    LOGGER.info(":::::::::::::: SEARCH CONTENT RESOURCE TEST ::::::::::::::");
    RootResource resource = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    Thread.sleep(SLEEP_DURATION);
    String query =
           "typeId=atest2:additional:com.smartitengineering.smart-shopping.content:Author&status=published&status=draft&disjunction=true";
    ContentSearcherResource searchContent = resource.searchContent(query);
    Assert.assertEquals(5, searchContent.get().getEntries().size());
    IRI href = searchContent.get().getLink("next").getHref();
    query =
    "typeId=atest2:additional:com.smartitengineering.smart-shopping.content:Author&status=published&status=draft&disjunction=true&count=10";
    searchContent = resource.searchContent(query);
    Assert.assertEquals(10, searchContent.get().getEntries().size());

  }

  @Test
  public void testDeleteContent() throws Exception {
    LOGGER.info(":::::::::::::: DELETE CONTENT RESOURCE TEST ::::::::::::::");

    ObjectMapper mapper1 = new ObjectMapper();
    String JSON1 = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("DummyContent.json"));
    InputStream stream1 = IOUtils.toInputStream(JSON1);
    Content contentTest = mapper1.readValue(stream1, Content.class);
    Assert.assertNotNull(contentTest);
    RootResource resource1 = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    Collection<WorkspaceFeedResource> workspaceFeedResources1 = resource1.getWorkspaceFeeds();
    Iterator<WorkspaceFeedResource> iteratorTest = workspaceFeedResources1.iterator();
    WorkspaceFeedResource feedResourceTest = iteratorTest.next();
    ContentResource contentResourceTest = feedResourceTest.getContents().createContentResource(contentTest);
    Thread.sleep(SLEEP_DURATION);

    FieldValueImpl value = new FieldValueImpl();
    value.setType("content");
    value.setValue(contentResourceTest.getUri().toASCIIString());
    FieldImpl authorField = new FieldImpl();
    authorField.setName("Authors");
    authorField.setValue(value);

    String valueString = "otherValue";
    byte[] otherValue = valueString.getBytes();
    OtherFieldValueImpl otherFieldValueImpl = new OtherFieldValueImpl();
    otherFieldValueImpl.setMimeType("jpeg/image");
    otherFieldValueImpl.setType("other");
    otherFieldValueImpl.setValue(Base64.encodeBase64String(otherValue));

    FieldImpl valueImpl = new FieldImpl();
    valueImpl.setName("b");
    valueImpl.setValue(otherFieldValueImpl);

    ObjectMapper mapper = new ObjectMapper();
    String JSON = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("Content.json"));
    InputStream stream = IOUtils.toInputStream(JSON);
    Content content = mapper.readValue(stream, Content.class);
    content.getFields().add(valueImpl);
    content.getFields().add(authorField);
    Assert.assertNotNull(content);
    RootResource resource = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    Collection<WorkspaceFeedResource> workspaceFeedResources = resource.getWorkspaceFeeds();
    Iterator<WorkspaceFeedResource> iterator = workspaceFeedResources.iterator();
    WorkspaceFeedResource feedResource = iterator.next();
    feedResource.getContents().createContentResource(content).delete(ClientResponse.Status.OK);
    feedResource.get();
    Assert.assertEquals(0, feedResource.getContents().getContentResources().size());
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
    Assert.assertEquals(4, collection.size());
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
    Assert.assertEquals(2, collection1.size());
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

  @Test
  public void testMultiValidatorWithParams() throws Exception {
    WorkspaceFeedResource feedResource = setupMultiValidatorAndParamTest();
    ObjectMapper mapper1 = new ObjectMapper();
    Content contentTest = mapper1.readValue(
        getClass().getClassLoader().getResourceAsStream("testtemplates/Content.json"), Content.class);
    ContentResource contentResource = feedResource.getContents().createContentResource(contentTest);
    try {
      contentTest = mapper1.readValue(getClass().getClassLoader().getResourceAsStream(
          "testtemplates/Content_invalid_max.json"), Content.class);
      feedResource.getContents().createContentResource(contentTest);
      Assert.fail("Should not be able to create content!");
    }
    catch (Exception ex) {
    }
    try {
      contentTest = mapper1.readValue(getClass().getClassLoader().getResourceAsStream(
          "testtemplates/Content_invalid_min.json"), Content.class);
      feedResource.getContents().createContentResource(contentTest);
      Assert.fail("Should not be able to create content!");
    }
    catch (Exception ex) {
    }
    contentResource.delete(ClientResponse.Status.ACCEPTED, ClientResponse.Status.OK);
  }

  @Test
  public void testVariationWithParams() throws Exception {
    WorkspaceFeedResource feedResource = setupMultiValidatorAndParamTest();
    ObjectMapper mapper1 = new ObjectMapper();
    Content contentTest = mapper1.readValue(
        getClass().getClassLoader().getResourceAsStream("testtemplates/Content.json"), Content.class);
    ContentResource contentResource = feedResource.getContents().createContentResource(contentTest);
    String variationUri = contentResource.getLastReadStateOfEntity().getFieldsMap().get("fieldA").getVariationsByNames().
        get("avar");
    com.smartitengineering.util.rest.client.HttpClient client = contentResource.getClientFactory().getHttpClient();
    WebResource varResource = client.getWebResource(URI.create(variationUri));
    String variation = varResource.get(String.class);
    Assert.assertEquals("Nothing ", variation);
    contentResource.delete(ClientResponse.Status.ACCEPTED, ClientResponse.Status.OK);
  }

  @Test
  public void testRepresentationWithParams() throws Exception {
    WorkspaceFeedResource feedResource = setupMultiValidatorAndParamTest();
    ObjectMapper mapper1 = new ObjectMapper();
    Content contentTest = mapper1.readValue(
        getClass().getClassLoader().getResourceAsStream("testtemplates/Content.json"), Content.class);
    ContentResource contentResource = feedResource.getContents().createContentResource(contentTest);
    String representationUri = contentResource.getLastReadStateOfEntity().getRepresentationsByName().get("arep");
    com.smartitengineering.util.rest.client.HttpClient client = contentResource.getClientFactory().getHttpClient();
    WebResource repResource = client.getWebResource(URI.create(representationUri));
    String representation = repResource.get(String.class);
    Assert.assertEquals("Nothing", representation);
    representationUri = contentResource.getLastReadStateOfEntity().getRepresentationsByName().get("arep2");
    repResource = client.getWebResource(URI.create(representationUri));
    representation = repResource.get(String.class);
    Assert.assertEquals("Nothing I", representation);
    contentResource.delete(ClientResponse.Status.ACCEPTED, ClientResponse.Status.OK);
  }

  @Test
  public void testContentTypeExtension() throws Exception {
    WorkspaceFeedResource feedResource = setupMultiValidatorAndParamTest();
    String contentTypeXml = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(
        "testtemplates/content-type-extension.xml"));
    feedResource.getContentTypes().createContentType(contentTypeXml);
    com.smartitengineering.cms.api.impl.workspace.WorkspaceIdImpl id =
                                                                  new com.smartitengineering.cms.api.impl.workspace.WorkspaceIdImpl();
    id.setGlobalNamespace("test");
    id.setName("templates");
    ContentTypeIdImpl idImpl = new ContentTypeIdImpl();
    idImpl.setWorkspace(id);
    idImpl.setNamespace("test");
    idImpl.setName("TypeB");
    ContentType type = SmartContentAPI.getInstance().getContentTypeLoader().loadContentType(idImpl);
    Assert.assertFalse(type.getStatuses().isEmpty());
    Assert.assertFalse(type.getRepresentationDefs().isEmpty());
    Assert.assertEquals(1, type.getStatuses().size());
    Assert.assertEquals(2, type.getRepresentationDefs().size());
    idImpl = new ContentTypeIdImpl();
    idImpl.setWorkspace(id);
    idImpl.setNamespace("test");
    idImpl.setName("TypeC");
    type = SmartContentAPI.getInstance().getContentTypeLoader().loadContentType(idImpl);
    Assert.assertFalse(type.getStatuses().isEmpty());
    Assert.assertFalse(type.getRepresentationDefs().isEmpty());
    Assert.assertEquals(1, type.getStatuses().size());
    Assert.assertEquals(2, type.getRepresentationDefs().size());
    idImpl = new ContentTypeIdImpl();
    idImpl.setWorkspace(id);
    idImpl.setNamespace("test");
    idImpl.setName("TypeD");
    type = SmartContentAPI.getInstance().getContentTypeLoader().loadContentType(idImpl);
    Assert.assertFalse(type.getStatuses().isEmpty());
    Assert.assertFalse(type.getRepresentationDefs().isEmpty());
    Assert.assertEquals(1, type.getStatuses().size());
    Assert.assertEquals(3, type.getRepresentationDefs().size());
    idImpl = new ContentTypeIdImpl();
    idImpl.setWorkspace(id);
    idImpl.setNamespace("test");
    idImpl.setName("TypeE");
    type = SmartContentAPI.getInstance().getContentTypeLoader().loadContentType(idImpl);
    Assert.assertFalse(type.getStatuses().isEmpty());
    Assert.assertFalse(type.getRepresentationDefs().isEmpty());
    Assert.assertEquals(2, type.getStatuses().size());
    Assert.assertEquals(2, type.getRepresentationDefs().size());
    idImpl = new ContentTypeIdImpl();
    idImpl.setWorkspace(id);
    idImpl.setNamespace("test");
    idImpl.setName("TypeF");
    type = SmartContentAPI.getInstance().getContentTypeLoader().loadContentType(idImpl);
    Assert.assertFalse(type.getStatuses().isEmpty());
    Assert.assertTrue(type.getRepresentationDefs().isEmpty());
    Assert.assertEquals(1, type.getStatuses().size());
    Assert.assertEquals(0, type.getRepresentationDefs().size());
  }

  private WorkspaceFeedResource setupMultiValidatorAndParamTest() {
    RootResource resource = RootResourceImpl.getRoot(URI.create(ROOT_URI_STRING));
    resource.get();
    WorkspaceFeedResource feedResource;
    try {
      feedResource = resource.getTemplates().getWorkspaceResource("test", "templates");
    }
    catch (Exception ex) {
      feedResource = null;
      LOGGER.info("Exception getting feed resoruce", ex);
    }
    boolean valid = false;
    if (feedResource == null) {
      try {
        Workspace workspace = resource.createWorkspace(new WorkspaceIdImpl("test", "templates"));
        feedResource = resource.getTemplates().getWorkspaceResource(workspace.getId().getGlobalNamespace(), workspace.
            getId().getName());
        String contentTypeXml = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(
            "testtemplates/content-type-templates.xml"));
        feedResource.getContentTypes().createContentType(contentTypeXml);
        ResourceTemplateImpl template = new ResourceTemplateImpl();
        template.setName("internalval");
        template.setTemplate(IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream(
            "testtemplates/validator.groovy")));
        template.setTemplateType("GROOVY");
        template.setWorkspaceId(workspace.getId());
        feedResource.getValidators().createValidator(template);
        template = new ResourceTemplateImpl();
        template.setName("internalvar");
        template.setTemplate(IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream(
            "testtemplates/variation.groovy")));
        template.setTemplateType("GROOVY");
        template.setWorkspaceId(workspace.getId());
        feedResource.getVariations().createVariation(template);
        template = new ResourceTemplateImpl();
        template.setName("internalrep");
        template.setTemplate(IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream(
            "testtemplates/representation.groovy")));
        template.setTemplateType("GROOVY");
        template.setWorkspaceId(workspace.getId());
        feedResource.getRepresentations().createRepresentations(template);
        template = new ResourceTemplateImpl();
        template.setName("vmrep");
        template.setTemplate(IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream(
            "testtemplates/anotherrep.vm")));
        template.setTemplateType("VELOCITY");
        template.setWorkspaceId(workspace.getId());
        feedResource.getRepresentations().createRepresentations(template);
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
    return feedResource;
  }

  private WorkspaceFeedResource setupCompositeWorkspace() {
    RootResource resource = RootResourceImpl.getRoot(URI.create(ROOT_URI_STRING));
    resource.get();
    WorkspaceFeedResource feedResource;
    try {
      feedResource = resource.getTemplates().getWorkspaceResource("test", "composites");
    }
    catch (Exception ex) {
      feedResource = null;
      LOGGER.info("Exception getting feed resoruce", ex);
    }
    boolean valid = false;
    if (feedResource == null) {
      try {
        Workspace workspace = resource.createWorkspace(new WorkspaceIdImpl("test", "composites"));
        feedResource = resource.getTemplates().getWorkspaceResource(workspace.getId().getGlobalNamespace(), workspace.
            getId().getName());
        ResourceTemplateImpl template = new ResourceTemplateImpl();
        template.setName("internalval");
        template.setTemplate(IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream(
            "testtemplates/validator.groovy")));
        template.setTemplateType("GROOVY");
        template.setWorkspaceId(workspace.getId());
        feedResource.getValidators().createValidator(template);
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
    return feedResource;
  }

  @Test
  public void testCreateCompositeContentType() throws Exception {
    LOGGER.info("~~~~~~~~~~~~~~~~~~~~~~~~~~ COMPOSITE CONTENT TYPE CREATION ~~~~~~~~~~~~~~~~~~~~~~~~~");
    WorkspaceFeedResource feedResource = setupCompositeWorkspace();
    String contentTypeXml = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(
        "composite/content-type-def-with-composition.xml"));
    feedResource.getContentTypes().createContentType(contentTypeXml);
    com.smartitengineering.cms.api.impl.workspace.WorkspaceIdImpl id =
                                                                  new com.smartitengineering.cms.api.impl.workspace.WorkspaceIdImpl();
    id.setGlobalNamespace("test");
    id.setName("composites");
    ContentTypeIdImpl idImpl = new ContentTypeIdImpl();
    idImpl.setWorkspace(id);
    idImpl.setNamespace("test");
    idImpl.setName("Order");
    ContentType type = SmartContentAPI.getInstance().getContentTypeLoader().loadContentType(idImpl);
    Assert.assertNotNull(type);
    Collection<FieldDef> compositeFieldDefs = type.getFieldDefs().values();
    Assert.assertEquals(3, compositeFieldDefs.size());
    final Iterator<FieldDef> mainIterator = compositeFieldDefs.iterator();
    FieldDef field = mainIterator.next();
    Assert.assertEquals(FieldValueType.COMPOSITE, field.getValueDef().getType());
    CompositeDataType compositeDataType = (CompositeDataType) field.getValueDef();
    Assert.assertNotNull(compositeDataType.getEmbeddedContentType());
    Assert.assertEquals(2, compositeDataType.getOwnComposition().size());
    Assert.assertEquals(9, compositeDataType.getComposition().size());
    Map<String, String> parameters = compositeDataType.getComposedFieldDefs().get("astreet1").getParameters();
    Assert.assertEquals(1, parameters.size());
    Assert.assertEquals("fieldVal", parameters.get("fieldParam"));
    parameters = compositeDataType.getComposedFieldDefs().get("astreet2").getParameterizedDisplayNames();
    Assert.assertEquals(1, parameters.size());
    Assert.assertEquals("Street (Line 2) (en-US)", parameters.get("en-US"));
    Iterator<FieldDef> compositionIterator = compositeDataType.getOwnComposition().iterator();
    compositionIterator.next();
    FieldDef collectionFieldDef = compositionIterator.next();
    Assert.assertEquals(FieldValueType.COLLECTION, collectionFieldDef.getValueDef().getType());
    CollectionDataType collectionDataType = (CollectionDataType) collectionFieldDef.getValueDef();
    Assert.assertEquals(FieldValueType.COMPOSITE, collectionDataType.getItemDataType().getType());
    compositeDataType = (CompositeDataType) collectionDataType.getItemDataType();
    Assert.assertEquals(2, compositeDataType.getOwnComposition().size());
    final Iterator<FieldDef> ownComposition = compositeDataType.getOwnComposition().iterator();
    Assert.assertEquals("anumber", ownComposition.next().getName());
    Assert.assertEquals("availability", ownComposition.next().getName());
    field = mainIterator.next();
    Assert.assertEquals(FieldValueType.COMPOSITE, field.getValueDef().getType());
    compositeDataType = (CompositeDataType) field.getValueDef();
    Assert.assertNotNull(compositeDataType.getEmbeddedContentType());
    Assert.assertEquals(0, compositeDataType.getOwnComposition().size());
    field = mainIterator.next();
    Assert.assertEquals(FieldValueType.COLLECTION, field.getValueDef().getType());
    collectionDataType = (CollectionDataType) field.getValueDef();
    Assert.assertEquals(FieldValueType.COMPOSITE, collectionDataType.getItemDataType().getType());
    compositeDataType = (CompositeDataType) collectionDataType.getItemDataType();
    Assert.assertNull(compositeDataType.getEmbeddedContentType());
    Assert.assertEquals(2, compositeDataType.getOwnComposition().size());
    parameters = compositeDataType.getComposedFieldDefs().get("By").getParameters();
    Assert.assertEquals(1, parameters.size());
    Assert.assertEquals("fieldVal", parameters.get("fieldParam"));
    idImpl.setName("Address");
    type = SmartContentAPI.getInstance().getContentTypeLoader().loadContentType(idImpl);
    parameters = type.getParameterizedDisplayNames();
    Assert.assertEquals(1, parameters.size());
    Assert.assertEquals("Address (en-US)", parameters.get("en-US"));
  }

  @Test
  public void testCreateContentWithCompositeField() throws Exception {
    LOGGER.info("~~~~~~~~~~~~~~~~~~~~~~~~~~ COMPOSITE CONTENT CREATION ~~~~~~~~~~~~~~~~~~~~~~~~~");
    WorkspaceFeedResource feedResource = setupCompositeWorkspace();
    final Properties properties = new Properties();
    properties.load(getClass().getClassLoader().getResourceAsStream(
        "composite/form-value.properties"));
    Set<Object> formKeys = properties.keySet();
    FormDataMultiPart multiPart = new FormDataMultiPart();
    for (Object key : formKeys) {
      multiPart.field(key.toString(), properties.getProperty(key.toString()));
    }
    ClientResponse response = feedResource.getContents().post(MediaType.MULTIPART_FORM_DATA, multiPart,
                                                              ClientResponse.Status.CREATED,
                                                              ClientResponse.Status.ACCEPTED, ClientResponse.Status.OK);
    URI uri = response.getLocation();
    ContentResourceImpl resourceImpl = new ContentResourceImpl(feedResource, uri);
    final Content lastReadStateOfEntity = resourceImpl.getLastReadStateOfEntity();
    Assert.assertNotNull(lastReadStateOfEntity);
    Assert.assertEquals("i", ((CompositeFieldValue) lastReadStateOfEntity.getFieldsMap().get("billingAddress").getValue()).
        getValues().get("astreet1").getValue().getValue());
    ContentResource resource = feedResource.getContents().createContentResource(lastReadStateOfEntity);
    Assert.assertNotNull(resource.getLastReadStateOfEntity());
    resource.update(lastReadStateOfEntity);
    resource.get();
    Assert.assertNotNull(resource.getLastReadStateOfEntity());
  }

  @Test
  public void testRequiredFieldValidationForCompositeField() throws Exception {
    LOGGER.info("~~~~~~~~~~~~~~~~~~~~~~~~~~ MANDATORY COMPOSITE FIELD VALIDATION ~~~~~~~~~~~~~~~~~~~~~~~~~");
    WorkspaceFeedResource feedResource = setupCompositeWorkspace();
    final String[] resources = new String[]{"composite/form-value-invalid-collection.properties",
      "composite/form-value-invalid-complex.properties", "composite/form-value-invalid-composition.properties",
      "composite/form-value-invalid-content.properties"};
    for (String resource : resources) {
      final Properties properties = new Properties();
      properties.load(getClass().getClassLoader().getResourceAsStream(
          resource));
      Set<Object> formKeys = properties.keySet();
      FormDataMultiPart multiPart = new FormDataMultiPart();
      for (Object key : formKeys) {
        multiPart.field(key.toString(), properties.getProperty(key.toString()));
      }
      try {
        feedResource.getContents().post(MediaType.MULTIPART_FORM_DATA, multiPart, ClientResponse.Status.CREATED,
                                        ClientResponse.Status.ACCEPTED, ClientResponse.Status.OK);
        Assert.fail("Invalid content should have failed for " + resource);
      }
      catch (Exception ex) {
        //Expected that creation fails
      }
    }
  }

  @Test
  public void testCustomFieldValidationForCompositeField() throws Exception {
    LOGGER.info("~~~~~~~~~~~~~~~~~~~~~~~~~~ CUSTOM COMPOSITE FIELD VALIDATION ~~~~~~~~~~~~~~~~~~~~~~~~~");
    WorkspaceFeedResource feedResource = setupCompositeWorkspace();
    final String[] invalidResources = new String[]{"composite/form-value-invalid-custom_validation.properties"};
    for (String resource : invalidResources) {
      final Properties properties = new Properties();
      properties.load(getClass().getClassLoader().getResourceAsStream(
          resource));
      Set<Object> formKeys = properties.keySet();
      FormDataMultiPart multiPart = new FormDataMultiPart();
      for (Object key : formKeys) {
        multiPart.field(key.toString(), properties.getProperty(key.toString()));
      }
      try {
        feedResource.getContents().post(MediaType.MULTIPART_FORM_DATA, multiPart, ClientResponse.Status.CREATED,
                                        ClientResponse.Status.ACCEPTED, ClientResponse.Status.OK);
        Assert.fail("Invalid content should have failed for " + resource);
      }
      catch (Exception ex) {
        //Expected that creation fails
      }
    }
    final String[] validResources = new String[]{"composite/form-value-valid-custom_validation.properties"};
    for (String resource : validResources) {
      final Properties properties = new Properties();
      properties.load(getClass().getClassLoader().getResourceAsStream(
          resource));
      Set<Object> formKeys = properties.keySet();
      FormDataMultiPart multiPart = new FormDataMultiPart();
      for (Object key : formKeys) {
        multiPart.field(key.toString(), properties.getProperty(key.toString()));
      }
      try {
        ClientResponse response = feedResource.getContents().post(MediaType.MULTIPART_FORM_DATA, multiPart,
                                                                  ClientResponse.Status.CREATED,
                                                                  ClientResponse.Status.ACCEPTED,
                                                                  ClientResponse.Status.OK);
        URI uri = response.getLocation();
        ContentResourceImpl resourceImpl = new ContentResourceImpl(feedResource, uri);
        final Content lastReadStateOfEntity = resourceImpl.getLastReadStateOfEntity();
        Assert.assertNotNull(lastReadStateOfEntity);
      }
      catch (Exception ex) {
        LOGGER.error(ex.getMessage(), ex);
        Assert.fail("Valid content should not have failed for " + resource);
      }
    }
  }

  @Test
  public void testDefTypePersistence() throws Exception {
    com.smartitengineering.cms.api.impl.workspace.WorkspaceIdImpl id =
                                                                  new com.smartitengineering.cms.api.impl.workspace.WorkspaceIdImpl();
    id.setGlobalNamespace("test");
    id.setName("composites");
    ContentTypeIdImpl idImpl = new ContentTypeIdImpl();
    idImpl.setWorkspace(id);
    idImpl.setNamespace("test");
    idImpl.setName("Address");
    ContentType type = SmartContentAPI.getInstance().getContentTypeLoader().loadContentType(idImpl);
    Assert.assertEquals(ContentType.DefinitionType.CONCRETE_COMPONENT, type.getDefinitionType());
  }

  @Test
  public void testNonConcreteTypeCreate() throws Exception {
    LOGGER.info("~~~~~~~~~~~~~~~~~~~~~~~~~~ CREATE NON CONCRETE TYPE CONTENT ~~~~~~~~~~~~~~~~~~~~~~~~~");
    WorkspaceFeedResource feedResource = setupCompositeWorkspace();
    final String[] invalidResources = new String[]{"composite/address-form.properties"};
    for (String resource : invalidResources) {
      final Properties properties = new Properties();
      properties.load(getClass().getClassLoader().getResourceAsStream(
          resource));
      Set<Object> formKeys = properties.keySet();
      FormDataMultiPart multiPart = new FormDataMultiPart();
      for (Object key : formKeys) {
        multiPart.field(key.toString(), properties.getProperty(key.toString()));
      }
      try {
        feedResource.getContents().post(MediaType.MULTIPART_FORM_DATA, multiPart, ClientResponse.Status.CREATED,
                                        ClientResponse.Status.ACCEPTED, ClientResponse.Status.OK);
        Assert.fail("Invalid content should have failed for " + resource);
      }
      catch (Exception ex) {
        //Expected that creation fails
      }
    }
  }

  @Test
  public void testContentStatusesResource() throws Exception {
    LOGGER.info("~~~~~~~~~~~~~~~~~~~~~~~~~~ Content TYPE STATUSES ~~~~~~~~~~~~~~~~~~~~~~~~~");
    WorkspaceFeedResource feedResource = setupCompositeWorkspace();
    RootResource resource = RootResourceImpl.getRoot(URI.create(ROOT_URI_STRING));
    resource.get();
    ContentTypeFeedResource typeFeedResource = resource.getTemplates().getContentTypeResource(feedResource.
        getWorkspaceNamespace(), feedResource.getWorkspaceName(), "test", "Address");
    Assert.assertNotNull(typeFeedResource);
    Assert.assertNotNull(typeFeedResource.getStatuses());
    Assert.assertFalse(typeFeedResource.getStatuses().isEmpty());
  }

  private WorkspaceFeedResource setupEnumWorkspace() {
    RootResource resource = RootResourceImpl.getRoot(URI.create(ROOT_URI_STRING));
    resource.get();
    WorkspaceFeedResource feedResource;
    try {
      feedResource = resource.getTemplates().getWorkspaceResource("test", "enums");
    }
    catch (Exception ex) {
      feedResource = null;
      LOGGER.info("Exception getting feed resoruce", ex);
    }
    boolean valid = false;
    if (feedResource == null) {
      try {
        Workspace workspace = resource.createWorkspace(new WorkspaceIdImpl("test", "enums"));
        feedResource = resource.getTemplates().getWorkspaceResource(workspace.getId().getGlobalNamespace(), workspace.
            getId().getName());
        String contentTypeXml = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(
            "enum/content-type-def-with-enum.xml"));
        feedResource.getContentTypes().createContentType(contentTypeXml);
        contentTypeXml = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(
            "contentcoprocessors/content-type-def-with-enum.xml"));
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
    return feedResource;
  }

  @Test
  public void testCreateEnumContentType() throws Exception {
    LOGGER.info("~~~~~~~~~~~~~~~~~~~~~~~~~~ ENUM CONTENT TYPE CREATION ~~~~~~~~~~~~~~~~~~~~~~~~~");
    WorkspaceFeedResource feedResource = setupEnumWorkspace();
    com.smartitengineering.cms.api.impl.workspace.WorkspaceIdImpl id =
                                                                  new com.smartitengineering.cms.api.impl.workspace.WorkspaceIdImpl();
    id.setGlobalNamespace(feedResource.getWorkspaceNamespace());
    id.setName(feedResource.getWorkspaceName());
    ContentTypeIdImpl idImpl = new ContentTypeIdImpl();
    idImpl.setWorkspace(id);
    idImpl.setNamespace("enum");
    idImpl.setName("EnumTest");
    ContentType type = SmartContentAPI.getInstance().getContentTypeLoader().loadContentType(idImpl);
    Assert.assertNotNull(type);
    Map<String, FieldDef> enumFieldDefs = type.getFieldDefs();
    Assert.assertEquals(5, enumFieldDefs.size());
    FieldDef directFieldDef = enumFieldDefs.get("directEnumField");
    Assert.assertNotNull(directFieldDef);
    LOGGER.debug("+++++++++++++++++++++ Direct Enum CHOICES " +
        ((EnumDataType) directFieldDef.getValueDef()).getChoices());
    Assert.assertTrue(((EnumDataType) directFieldDef.getValueDef()).getChoices().contains("1"));
    Assert.assertTrue(((EnumDataType) directFieldDef.getValueDef()).getChoices().contains("2"));
    FieldDef collectiveFieldDef = enumFieldDefs.get("collectiveEnumField");
    Assert.assertNotNull(collectiveFieldDef);
    Assert.assertTrue(((EnumDataType) ((CollectionDataType) collectiveFieldDef.getValueDef()).getItemDataType()).
        getChoices().contains("3"));
    Assert.assertTrue(((EnumDataType) ((CollectionDataType) collectiveFieldDef.getValueDef()).getItemDataType()).
        getChoices().contains("4"));
    FieldDef compositedFieldDef = enumFieldDefs.get("compositedEnumField");
    Assert.assertNotNull(compositedFieldDef);
    Assert.assertTrue(((EnumDataType) ((CompositeDataType) compositedFieldDef.getValueDef()).getComposedFieldDefs().get(
                       "enumField").getValueDef()).getChoices().contains("5"));
    Assert.assertTrue(((EnumDataType) ((CompositeDataType) compositedFieldDef.getValueDef()).getComposedFieldDefs().get(
                       "enumField").getValueDef()).getChoices().contains("6"));
    CompositeDataType compositedDataType = (CompositeDataType) ((CollectionDataType) enumFieldDefs.get(
                                                                "collectiveCompositedEnumField").getValueDef()).
        getItemDataType();
    Assert.assertNotNull(compositedFieldDef);
    Assert.assertTrue(((EnumDataType) compositedDataType.getComposedFieldDefs().get(
                       "enumField").getValueDef()).getChoices().contains("7"));
    Assert.assertTrue(((EnumDataType) compositedDataType.getComposedFieldDefs().get(
                       "enumField").getValueDef()).getChoices().contains("8"));
    Collection<ContentTypeFeedResource> typeFeeds = feedResource.getContentTypes().getContentTypeFeeds();
    Assert.assertNotNull(typeFeeds);
    Assert.assertEquals(2, typeFeeds.size());
    ContentTypeFeedResource feed = typeFeeds.iterator().next();
    Collection<com.smartitengineering.cms.ws.common.domains.FieldDef> defs = feed.getFieldDefs();
    Assert.assertEquals(5, defs.size());
    Map<String, com.smartitengineering.cms.ws.common.domains.FieldDef> wDefs =
                                                                       new HashMap<String, com.smartitengineering.cms.ws.common.domains.FieldDef>();
    for (com.smartitengineering.cms.ws.common.domains.FieldDef def : defs) {
      wDefs.put(def.getName(), def);
    }
    com.smartitengineering.cms.ws.common.domains.FieldDef wDirectFieldDef = wDefs.get("directEnumField");
    Assert.assertNotNull(wDirectFieldDef);
    Assert.assertTrue(wDirectFieldDef instanceof EnumFieldDef);
    Assert.assertTrue(((EnumFieldDef) wDirectFieldDef).getChoices().contains("1"));
    Assert.assertTrue(((EnumFieldDef) wDirectFieldDef).getChoices().contains("2"));
    com.smartitengineering.cms.ws.common.domains.FieldDef wCollectiveFieldDef = wDefs.get("collectiveEnumField");
    Assert.assertNotNull(wCollectiveFieldDef);
    Assert.assertNotNull(((CollectionFieldDef) wCollectiveFieldDef).getItemDef());
    Assert.assertTrue(((CollectionFieldDef) wCollectiveFieldDef).getItemDef() instanceof EnumFieldDef);
    Assert.assertTrue(
        ((EnumFieldDef) ((CollectionFieldDef) wCollectiveFieldDef).getItemDef()).getChoices().contains("3"));
    Assert.assertTrue(
        ((EnumFieldDef) ((CollectionFieldDef) wCollectiveFieldDef).getItemDef()).getChoices().contains("4"));
    com.smartitengineering.cms.ws.common.domains.FieldDef wCompositedFieldDef = wDefs.get("compositedEnumField");
    Assert.assertNotNull(wCompositedFieldDef);
    Assert.assertNotNull(((CompositeFieldDef) wCompositedFieldDef).getComposedFields().get("enumField"));
    Assert.assertTrue(
        ((CompositeFieldDef) wCompositedFieldDef).getComposedFields().get("enumField") instanceof EnumFieldDef);
    Assert.assertTrue(((EnumFieldDef) ((CompositeFieldDef) wCompositedFieldDef).getComposedFields().get("enumField")).
        getChoices().contains("5"));
    Assert.assertTrue(((EnumFieldDef) ((CompositeFieldDef) wCompositedFieldDef).getComposedFields().get("enumField")).
        getChoices().contains("6"));
    wCompositedFieldDef = ((CollectionFieldDef) wDefs.get("collectiveCompositedEnumField")).getItemDef();
    Assert.assertNotNull(wCompositedFieldDef);
    Assert.assertNotNull(((CompositeFieldDef) wCompositedFieldDef).getComposedFields().get("enumField"));
    Assert.assertTrue(
        ((CompositeFieldDef) wCompositedFieldDef).getComposedFields().get("enumField") instanceof EnumFieldDef);
    Assert.assertTrue(((EnumFieldDef) ((CompositeFieldDef) wCompositedFieldDef).getComposedFields().get("enumField")).
        getChoices().contains("7"));
    Assert.assertTrue(((EnumFieldDef) ((CompositeFieldDef) wCompositedFieldDef).getComposedFields().get("enumField")).
        getChoices().contains("8"));
  }

  @Test
  public void testCreateContentWithEnumField() throws Exception {
    LOGGER.info("~~~~~~~~~~~~~~~~~~~~~~~~~~ ENUM CONTENT CREATION ~~~~~~~~~~~~~~~~~~~~~~~~~");
    WorkspaceFeedResource feedResource = setupEnumWorkspace();
    Properties properties = new Properties();
    properties.load(getClass().getClassLoader().getResourceAsStream(
        "enum/form-value.properties"));
    Set<Object> formKeys = properties.keySet();
    FormDataMultiPart multiPart = new FormDataMultiPart();
    for (Object key : formKeys) {
      multiPart.field(key.toString(), properties.getProperty(key.toString()));
    }
    ClientResponse response = feedResource.getContents().post(MediaType.MULTIPART_FORM_DATA, multiPart,
                                                              ClientResponse.Status.CREATED,
                                                              ClientResponse.Status.ACCEPTED, ClientResponse.Status.OK);
    URI uri = response.getLocation();
    ContentResourceImpl resourceImpl = new ContentResourceImpl(feedResource, uri);
    final Content lastReadStateOfEntity = resourceImpl.getLastReadStateOfEntity();
    Assert.assertNotNull(lastReadStateOfEntity);
    Field field = lastReadStateOfEntity.getFieldsMap().get("directEnumField");
    String val = field.getValue().getValue();
    Assert.assertEquals("1", val);
    field = ((CompositeFieldValue) lastReadStateOfEntity.getFieldsMap().get("compositedEnumField").getValue()).getValues().
        get("enumField");
    val = field.getValue().getValue();
    Assert.assertEquals("5", val);
    val = ((CollectionFieldValue) lastReadStateOfEntity.getFieldsMap().get("collectiveEnumField").getValue()).getValues().
        iterator().next().getValue();
    Assert.assertEquals("4", val);
    Thread.sleep(SLEEP_DURATION);
    for (int i = 1; i < 5; ++i) {
      try {
        properties = new Properties();
        properties.load(getClass().getClassLoader().getResourceAsStream(
            "enum/form-invalid-value-" + i + ".properties"));
        formKeys = properties.keySet();
        multiPart = new FormDataMultiPart();
        for (Object key : formKeys) {
          multiPart.field(key.toString(), properties.getProperty(key.toString()));
        }
        feedResource.getContents().post(MediaType.MULTIPART_FORM_DATA, multiPart, ClientResponse.Status.CREATED,
                                        ClientResponse.Status.ACCEPTED, ClientResponse.Status.OK);
        Assert.fail("Invalid content should have failed " + i);
      }
      catch (Exception ex) {
        //Expected that creation fails
      }
    }
  }

  @Test
  public void testCreateContentCoProcessor() throws Exception {

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
    WorkspaceContentCoProcessorsResource rsrcs = feedResource.getContentCoProcessors();
    WorkspaceContentCoProcessorResource rsrc = rsrcs.createContentCoProcessor(
        template);
    Assert.assertEquals("rep", rsrc.get().getName());
    Assert.assertEquals(temp, new String(rsrc.get().getTemplate()));
    Assert.assertEquals(TemplateType.JAVASCRIPT.toString(), rsrc.get().getTemplateType());
  }

  @Test
  public void testUpdateContentCoProcessor() throws Exception {
    LOGGER.info(":::::::::::::: UPDATE CONTENT CO PROCESSOR RESOURCE TEST ::::::::::::::");
    ResourceTemplateImpl template = new ResourceTemplateImpl();
    String temp = "newTemplate";
    final byte[] bytes = temp.getBytes();
    template.setTemplate(bytes);
    template.setTemplateType(TemplateType.RUBY.toString());

    RootResource resource = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    Collection<WorkspaceFeedResource> workspaceFeedResources = resource.getWorkspaceFeeds();
    Iterator<WorkspaceFeedResource> iterator = workspaceFeedResources.iterator();
    WorkspaceFeedResource feedResource = iterator.next();

    Collection<WorkspaceContentCoProcessorResource> representationResources = feedResource.getContentCoProcessors().
        getContentCoProcessorResources();
    Assert.assertEquals(1, representationResources.size());
    Iterator<WorkspaceContentCoProcessorResource> representationIterator = representationResources.iterator();
    WorkspaceContentCoProcessorResource rsrc = representationIterator.next();

    rsrc.update(template);
    Assert.assertEquals("rep", rsrc.get().getName());
    Assert.assertEquals(temp, new String(rsrc.get().getTemplate()));
    Assert.assertEquals(TemplateType.RUBY.toString(), rsrc.get().getTemplateType());
    resource.getWorkspaceFeeds();
    WorkspaceContentCoProcessorResource secondRepresentationResource = resource.getWorkspaceFeeds().iterator().next().
        getContentCoProcessors().getContentCoProcessorResources().iterator().next();
    template.setTemplateType(TemplateType.JAVASCRIPT.name());
    secondRepresentationResource.update(template);
    Assert.assertEquals(TemplateType.JAVASCRIPT.name(), secondRepresentationResource.get().getTemplateType());
    try {
      rsrc.update(template);
      Assert.fail("Should not have been able to update!");
    }
    catch (UniformInterfaceException ex) {
      //Exception expected
      rsrc.get();
      rsrc.update(template);
    }
  }

  @Test
  public void testDeleteContentCoProcessor() throws Exception {
    LOGGER.info(":::::::::::::: DELETE CONTENT CO PROCESSOR RESOURCE TEST ::::::::::::::");
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
    final WorkspaceContentCoProcessorsResource procsResource = feedResource.getContentCoProcessors();

    Collection<WorkspaceContentCoProcessorResource> rsrcs = procsResource.getContentCoProcessorResources();
    Assert.assertEquals(1, rsrcs.size());
    procsResource.createContentCoProcessor(template);
    Iterator<WorkspaceContentCoProcessorResource> representationIterator = rsrcs.iterator();
    WorkspaceContentCoProcessorResource rsrc = representationIterator.next();

    rsrc.delete(ClientResponse.Status.ACCEPTED);
    Collection<WorkspaceContentCoProcessorResource> secRsrcs = resource.getWorkspaceFeeds().iterator().
        next().getContentCoProcessors().getContentCoProcessorResources();
    Assert.assertEquals(1, secRsrcs.size());
  }

  @Test
  public void testCreateContentTypeWithContentCoProcessors() {
    WorkspaceFeedResource feedResource = setupEnumWorkspace();
    com.smartitengineering.cms.api.impl.workspace.WorkspaceIdImpl id =
                                                                  new com.smartitengineering.cms.api.impl.workspace.WorkspaceIdImpl();
    id.setGlobalNamespace(feedResource.getWorkspaceNamespace());
    id.setName(feedResource.getWorkspaceName());
    ContentTypeIdImpl idImpl = new ContentTypeIdImpl();
    idImpl.setWorkspace(id);
    idImpl.setNamespace("enum");
    idImpl.setName("ContentCoProcessorTest");
    ContentType type = SmartContentAPI.getInstance().getContentTypeLoader().loadContentType(idImpl);
    final Map<ContentProcessingPhase, Collection<ContentCoProcessorDef>> contentCoProcessorDefs =
                                                                         type.getContentCoProcessorDefs();
    Assert.assertNotNull(contentCoProcessorDefs);
    Assert.assertFalse(contentCoProcessorDefs.isEmpty());
    Assert.assertNotNull(contentCoProcessorDefs.get(ContentProcessingPhase.READ));
    Assert.assertNotNull(contentCoProcessorDefs.get(ContentProcessingPhase.WRITE));
    Assert.assertFalse(contentCoProcessorDefs.get(ContentProcessingPhase.READ).isEmpty());
    Assert.assertFalse(contentCoProcessorDefs.get(ContentProcessingPhase.WRITE).isEmpty());
    Assert.assertEquals(2, contentCoProcessorDefs.get(ContentProcessingPhase.READ).size());
    Assert.assertEquals(1, contentCoProcessorDefs.get(ContentProcessingPhase.WRITE).size());
    final Iterator<ContentCoProcessorDef> readItr =
                                          contentCoProcessorDefs.get(ContentProcessingPhase.READ).iterator();
    ContentCoProcessorDef def = readItr.next();
    Assert.assertEquals("testr", def.getName());
    Assert.assertNull(def.getMIMEType());
    Assert.assertEquals(0, def.getPriority());
    Assert.assertEquals(1, def.getParameters().size());
    Assert.assertEquals("v", def.getParameters().get("k"));
    Assert.assertEquals("test", def.getResourceUri().getValue());
    def = readItr.next();
    Assert.assertEquals("testr1", def.getName());
    Assert.assertNull(def.getMIMEType());
    Assert.assertEquals(1, def.getPriority());
    Assert.assertEquals(1, def.getParameters().size());
    Assert.assertEquals("v1", def.getParameters().get("k1"));
    Assert.assertEquals("test1", def.getResourceUri().getValue());
    def = contentCoProcessorDefs.get(ContentProcessingPhase.WRITE).iterator().next();
    Assert.assertEquals("testw", def.getName());
    Assert.assertNull(def.getMIMEType());
    Assert.assertEquals(0, def.getPriority());
    Assert.assertEquals(1, def.getParameters().size());
    Assert.assertEquals("v2", def.getParameters().get("k2"));
    Assert.assertEquals("test2", def.getResourceUri().getValue());
  }

  private WorkspaceFeedResource setupContentCoProcessorExecWorkspace() {
    RootResource resource = RootResourceImpl.getRoot(URI.create(ROOT_URI_STRING));
    resource.get();
    WorkspaceFeedResource feedResource;
    try {
      feedResource = resource.getTemplates().getWorkspaceResource("test", "enums");
    }
    catch (Exception ex) {
      feedResource = null;
      LOGGER.info("Exception getting feed resoruce", ex);
    }
    boolean valid = false;
    {
      try {
        final WorkspaceIdImpl workspaceId = new WorkspaceIdImpl("test", "enums");
        if (feedResource == null) {

          Workspace workspace = resource.createWorkspace(workspaceId);
          feedResource = resource.getTemplates().getWorkspaceResource(workspace.getId().getGlobalNamespace(), workspace.
              getId().getName());
        }
        String contentTypeXml = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(
            "contentcoprocessors/content-type-def-with-enum-ext.xml"));
        feedResource.getContentTypes().createContentType(contentTypeXml);
        ResourceTemplateImpl template = new ResourceTemplateImpl();
        template.setName("test");
        template.setTemplate(IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream(
            "contentcoprocessors/coprocessor.groovy")));
        template.setTemplateType("GROOVY");
        template.setWorkspaceId(workspaceId);
        feedResource.getContentCoProcessors().createContentCoProcessor(template);
        valid = true;
      }
      catch (Exception ex) {
        LOGGER.error("Error creating test workspace for templates", ex);
      }
    }
    Assert.assertTrue(valid);
    return feedResource;
  }

  @Test
  public void testCreateContentWithContentCoProcessors() throws Exception {
    WorkspaceFeedResource feedResource = setupContentCoProcessorExecWorkspace();
    ClientConfig config = new DefaultClientConfig();
    config.getClasses().add(JacksonJsonProvider.class);
    config.getClasses().add(TextURIListProvider.class);
    config.getClasses().add(FeedProvider.class);
    Client client = Client.create(config);
    {
      final Properties properties = new Properties();
      properties.load(getClass().getClassLoader().getResourceAsStream(
          "contentcoprocessors/form-value-write.properties"));
      Set<Object> formKeys = properties.keySet();
      FormDataMultiPart multiPart = new FormDataMultiPart();
      for (Object key : formKeys) {
        multiPart.field(key.toString(), properties.getProperty(key.toString()));
      }
      ClientResponse response =
                     feedResource.getContents().post(MediaType.MULTIPART_FORM_DATA, multiPart,
                                                     ClientResponse.Status.CREATED,
                                                     ClientResponse.Status.ACCEPTED, ClientResponse.Status.OK);
      URI uri = response.getLocation();
      ContentResourceImpl resourceImpl = new ContentResourceImpl(feedResource, uri);
      final Content lastReadStateOfEntity = resourceImpl.getLastReadStateOfEntity();
      Assert.assertNotNull(lastReadStateOfEntity);
      Assert.assertNotNull(lastReadStateOfEntity.getFieldsMap().get("directEnumFieldCopy"));
      Assert.assertNotNull(lastReadStateOfEntity.getFieldsMap().get("dynaField"));
      Assert.assertEquals(lastReadStateOfEntity.getFieldsMap().get("directEnumField").getValue().getValue(),
                          lastReadStateOfEntity.getFieldsMap().get("directEnumFieldCopy").getValue().getValue());
      String dynaField = lastReadStateOfEntity.getFieldsMap().get("dynaField").getValue().getValue();
      Thread.sleep(SLEEP_DURATION);
      final Content reReadStateOfEntity = client.resource(resourceImpl.getUri()).accept(MediaType.APPLICATION_JSON).
          header("Pragma", "no-cache").get(Content.class);
      Assert.assertEquals(dynaField, reReadStateOfEntity.getFieldsMap().get("dynaField").getValue().getValue());
    }
    {
      final Properties properties = new Properties();
      properties.load(getClass().getClassLoader().getResourceAsStream(
          "contentcoprocessors/form-value-read.properties"));
      Set<Object> formKeys = properties.keySet();
      FormDataMultiPart multiPart = new FormDataMultiPart();
      for (Object key : formKeys) {
        multiPart.field(key.toString(), properties.getProperty(key.toString()));
      }
      ClientResponse response =
                     feedResource.getContents().post(MediaType.MULTIPART_FORM_DATA, multiPart,
                                                     ClientResponse.Status.CREATED,
                                                     ClientResponse.Status.ACCEPTED, ClientResponse.Status.OK);
      URI uri = response.getLocation();
      ContentResourceImpl resourceImpl = new ContentResourceImpl(feedResource, uri);
      final Content lastReadStateOfEntity = resourceImpl.getLastReadStateOfEntity();
      Assert.assertNotNull(lastReadStateOfEntity);
      Assert.assertNotNull(lastReadStateOfEntity.getFieldsMap().get("directEnumFieldCopy"));
      Assert.assertNotNull(lastReadStateOfEntity.getFieldsMap().get("dynaField"));
      Assert.assertEquals(lastReadStateOfEntity.getFieldsMap().get("directEnumField").getValue().getValue(),
                          lastReadStateOfEntity.getFieldsMap().get("directEnumFieldCopy").getValue().getValue());
      String dynaField = lastReadStateOfEntity.getFieldsMap().get("dynaField").getValue().getValue();
      Thread.sleep(SLEEP_DURATION);
      final Content reReadStateOfEntity = client.resource(resourceImpl.getUri()).accept(MediaType.APPLICATION_JSON).
          header("Pragma", "no-cache").get(Content.class);
      Assert.assertFalse(dynaField.equals(reReadStateOfEntity.getFieldsMap().get("dynaField").getValue().getValue()));
    }
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
