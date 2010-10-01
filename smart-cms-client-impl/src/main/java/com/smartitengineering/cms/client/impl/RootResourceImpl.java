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
import com.smartitengineering.util.rest.atom.AbstractFeedClientResource;
import com.smartitengineering.util.rest.atom.AtomClientUtil;
import com.smartitengineering.util.rest.client.ClientUtil;
import com.smartitengineering.util.rest.client.Resource;
import com.smartitengineering.util.rest.client.ResourceLink;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.atom.abdera.impl.provider.entity.FeedProvider;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class RootResourceImpl extends AbstractFeedClientResource<Resource<? extends Feed>> implements RootResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(RootResourceImpl.class);

  private RootResourceImpl(URI uri) throws IllegalArgumentException,
                                           UniformInterfaceException {
    super(null, uri);
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
    List<Entry> entries = getLastReadStateOfEntity().getEntries();
    List<WorkspaceContentResouce> list = new ArrayList<WorkspaceContentResouce>(entries.size());
    for (Entry entry : entries) {
      list.add(new WorkspaceContentResourceImpl(this, AtomClientUtil.convertFromAtomLinkToResourceLink(entry.getLink(
          WorkspaceContentResouce.WORKSPACE_CONTENT))));
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
}
