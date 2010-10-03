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

import com.smartitengineering.cms.client.api.RootResource;
import com.smartitengineering.cms.client.api.WorkspaceContentResouce;
import com.smartitengineering.cms.client.api.domains.Workspace;
import com.smartitengineering.cms.client.api.domains.WorkspaceId;
import com.smartitengineering.util.rest.atom.AbstractFeedClientResource;
import com.smartitengineering.util.rest.atom.AtomClientUtil;
import com.smartitengineering.util.rest.client.ClientUtil;
import com.smartitengineering.util.rest.client.Resource;
import com.smartitengineering.util.rest.client.ResourceLink;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.atom.abdera.impl.provider.entity.FeedProvider;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
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

  private RootResourceImpl(URI uri) throws IllegalArgumentException,
                                           UniformInterfaceException {
    super(null, uri, false, null);
    if (logger.isDebugEnabled()) {
      logger.debug("Root resource URI for Smart CMS " + uri.toString());
    }
  }

  @Override
  protected void processClientConfig(ClientConfig clientConfig) {
    clientConfig.getClasses().add(JacksonJsonProvider.class);
    clientConfig.getClasses().add(FeedProvider.class);
  }

  @Override
  protected Resource<? extends Feed> instantiatePageableResource(ResourceLink link) {
    return null;
  }

  @Override
  public Collection<WorkspaceContentResouce> getWorkspaces() {
    try {
      final Feed feed = getLastReadStateOfEntity();
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
    RootResource resource = RootResourceImpl.getRoot(new URI(ROOT_URI_STRING));
    final MultivaluedMap<String, String> map = new MultivaluedMapImpl();
    map.add("name", workspaceId.getName());
    map.add("namespace", workspaceId.getGlobalNamespace());
    ClientResponse response = resource.post(MediaType.APPLICATION_FORM_URLENCODED, map, ClientResponse.Status.CREATED);
    ResourceLink link = ClientUtil.createResourceLink(WorkspaceContentResouce.WORKSPACE_CONTENT, response.getLocation(),
                                                      MediaType.APPLICATION_JSON);
    WorkspaceContentResouce workspaceContentResouce = new WorkspaceContentResourceImpl(resource, link);
    Workspace workspace = workspaceContentResouce.getLastReadStateOfEntity();
    return workspace;
  }
}
