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

import com.smartitengineering.cms.client.api.ContentSearcherResource;
import com.smartitengineering.cms.client.api.RootResource;
import com.smartitengineering.cms.client.api.UriTemplateResource;
import com.smartitengineering.cms.client.api.WorkspaceContentResouce;
import com.smartitengineering.cms.client.api.WorkspaceFeedResource;
import com.smartitengineering.cms.ws.common.domains.Workspace;
import com.smartitengineering.cms.ws.common.domains.WorkspaceId;
import com.smartitengineering.cms.ws.common.providers.JacksonJsonProvider;
import com.smartitengineering.cms.ws.common.providers.TextURIListProvider;
import com.smartitengineering.util.bean.PropertiesLocator;
import com.smartitengineering.util.rest.atom.AbstractFeedClientResource;
import com.smartitengineering.util.rest.atom.AtomClientUtil;
import com.smartitengineering.util.rest.client.ApplicationWideClientFactoryImpl;
import com.smartitengineering.util.rest.client.ClientUtil;
import com.smartitengineering.util.rest.client.ConfigProcessor;
import com.smartitengineering.util.rest.client.ConnectionConfig;
import com.smartitengineering.util.rest.client.Resource;
import com.smartitengineering.util.rest.client.ResourceLink;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.atom.abdera.impl.provider.entity.FeedProvider;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class RootResourceImpl extends AbstractFeedClientResource<Resource<? extends Feed>> implements RootResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(RootResourceImpl.class);
  private static final int PORT = 10080;
  public static final String ROOT_URI_STRING = "http://localhost:" + PORT + "/";
  public static final String REL_TEMPLATES = "templates";
  private static final ConnectionConfig SMART_CMS_CONNECTION_CONFIG;
  private static final boolean CONNECTION_CONFIGURED;
  private static final URI SMART_CMS_BASE_URI;
  private static final ConfigProcessor CONFIG_PROCESSOR = new CmsConfigProcessor();

  static {
    if (LOGGER.isInfoEnabled()) {
      System.setProperty("com.smartitengineering.util.rest.client.ApplicationWideClientFactoryImpl.trace", "true");
    }
    SMART_CMS_CONNECTION_CONFIG = new ConnectionConfig();
    String propFileName = "smart-cms-client-config.properties";
    PropertiesLocator locator = new PropertiesLocator();
    locator.setSmartLocations(propFileName);
    final Properties properties = new Properties();
    try {
      locator.loadProperties(properties);
    }
    catch (IOException ex) {
      LOGGER.warn("Exception!", ex);
    }
    if (!properties.isEmpty()) {
      CONNECTION_CONFIGURED = true;
      SMART_CMS_CONNECTION_CONFIG.setBasicUri(properties.getProperty("baseUri", ""));
      SMART_CMS_CONNECTION_CONFIG.setContextPath(properties.getProperty("contextPath", "/"));
      SMART_CMS_CONNECTION_CONFIG.setHost(properties.getProperty("host", "localhost"));
      SMART_CMS_CONNECTION_CONFIG.setPort(NumberUtils.toInt(properties.getProperty("port", ""), 9090));
      SMART_CMS_BASE_URI = UriBuilder.fromUri(SMART_CMS_CONNECTION_CONFIG.getContextPath()).path(SMART_CMS_CONNECTION_CONFIG.
          getBasicUri()).host(SMART_CMS_CONNECTION_CONFIG.getHost()).port(SMART_CMS_CONNECTION_CONFIG.getPort()).
          scheme("http").build();
    }
    else {
      CONNECTION_CONFIGURED = false;
      SMART_CMS_BASE_URI = null;
    }
  }

  private RootResourceImpl(URI uri) throws IllegalArgumentException,
                                           UniformInterfaceException {
    super(null, CONNECTION_CONFIGURED && uri == null ? SMART_CMS_BASE_URI : uri, false,
          CONNECTION_CONFIGURED ? ApplicationWideClientFactoryImpl.getClientFactory(SMART_CMS_CONNECTION_CONFIG,
                                                                                    CONFIG_PROCESSOR) : null);
    if (logger.isDebugEnabled()) {
      logger.debug("Root resource URI for Smart CMS " + uri);
    }
  }

  @Override
  protected void processClientConfig(ClientConfig clientConfig) {
    CONFIG_PROCESSOR.process(clientConfig);
  }

  private static class CmsConfigProcessor implements ConfigProcessor {

    public CmsConfigProcessor() {
    }

    @Override
    public void process(ClientConfig clientConfig) {
      clientConfig.getClasses().add(JacksonJsonProvider.class);
      clientConfig.getClasses().add(TextURIListProvider.class);
      clientConfig.getClasses().add(FeedProvider.class);
    }
  }

  @Override
  protected Resource<? extends Feed> instantiatePageableResource(ResourceLink link) {
    return null;
  }

  @Override
  public Collection<WorkspaceContentResouce> getWorkspaces() {
    final Feed feed = getLastReadStateOfEntity();
    if (feed == null) {
      return Collections.EMPTY_LIST;
    }
    List<Entry> entries = feed.getEntries();
    List<WorkspaceContentResouce> list = new ArrayList<WorkspaceContentResouce>(entries.size());
    for (Entry entry : entries) {
      final List<Link> links = entry.getLinks(WorkspaceContentResouce.WORKSPACE_CONTENT);
      Link link = null;
      for (Link tmp : links) {
        if (MediaType.APPLICATION_JSON.equals(tmp.getMimeType().toString())) {
          link = tmp;
        }
      }
      list.add(new WorkspaceContentResourceImpl(this, AtomClientUtil.convertFromAtomLinkToResourceLink(link)));
    }
    return list;
  }

  public static RootResource getRoot(URI uri) {
    try {
      RootResource resource = new RootResourceImpl(uri);
      return resource;
    }
    catch (RuntimeException ex) {
      LOGGER.error(ex.getMessage(), ex);
      throw ex;
    }
  }

  @Override
  public Workspace createWorkspace(WorkspaceId workspaceId) throws URISyntaxException {
    final MultivaluedMap<String, String> map = new MultivaluedMapImpl();
    map.add("name", workspaceId.getName());
    map.add("namespace", workspaceId.getGlobalNamespace());
    ClientResponse response = post(MediaType.APPLICATION_FORM_URLENCODED, map, ClientResponse.Status.CREATED);
    ResourceLink link = ClientUtil.createResourceLink(WorkspaceContentResouce.WORKSPACE_CONTENT, response.getLocation(),
                                                      MediaType.APPLICATION_JSON);
    WorkspaceContentResouce workspaceContentResouce = new WorkspaceContentResourceImpl(this, link);
    Workspace workspace = workspaceContentResouce.getLastReadStateOfEntity();
    return workspace;
  }

  @Override
  public Collection<WorkspaceFeedResource> getWorkspaceFeeds() {
    try {
      final Feed feed = getLastReadStateOfEntity();
      List<Entry> entries = feed.getEntries();
      List<WorkspaceFeedResource> list = new ArrayList<WorkspaceFeedResource>(entries.size());
      for (Entry entry : entries) {
        final List<Link> links = entry.getLinks(WorkspaceContentResouce.WORKSPACE_CONTENT);
        Link link = null;
        for (Link tmp : links) {
          if (MediaType.APPLICATION_ATOM_XML.equals(tmp.getMimeType().toString())) {
            link = tmp;
          }
        }
        list.add(new WorkspaceFeedResourceImpl(this, AtomClientUtil.convertFromAtomLinkToResourceLink(link)));
      }
      return list;
    }
    catch (UniformInterfaceException exception) {
      if (logger.isDebugEnabled()) {
        logger.debug("Exception while getting..", exception);
      }
      if (exception.getResponse().getStatus() != ClientResponse.Status.NO_CONTENT.getStatusCode()) {
        logger.error("Rethrowing the exception as it was not expected. Turn on Debug to see more.");
        throw exception;
      }
      else {
        return Collections.emptyList();
      }
    }
  }

  @Override
  public ContentSearcherResource searchContent(String query) {
    Link link = get().getLink("search");
    if (StringUtils.isNotBlank(query)) {
      String strLink = link.getHref().toASCIIString();
      strLink = strLink + "?" + query;
      link.setHref(strLink);
    }
    return new ContentSearcherResourceImpl(this, AtomClientUtil.convertFromAtomLinkToResourceLink(link));
  }

  @Override
  public UriTemplateResource getTemplates() {
    try {
      final ResourceLink first = getRelatedResourceUris().getFirst(REL_TEMPLATES);
      if (logger.isDebugEnabled()) {
        logger.debug("Templates URI " + first);
      }
      return new UriTemplateResourceImpl(this, first);
    }
    catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }
}
