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

import com.smartitengineering.cms.client.api.WorkspaceContentCoProcessorResource;
import com.smartitengineering.cms.client.api.WorkspaceContentCoProcessorsResource;
import com.smartitengineering.cms.client.api.WorkspaceRepresentationResource;
import com.smartitengineering.cms.client.api.WorkspaceRepresentationsResource;
import com.smartitengineering.cms.ws.common.domains.ResourceTemplate;
import com.smartitengineering.util.rest.atom.AbstractFeedClientResource;
import com.smartitengineering.util.rest.atom.AtomClientUtil;
import com.smartitengineering.util.rest.client.Resource;
import com.smartitengineering.util.rest.client.ResourceLink;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.config.ClientConfig;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.core.MediaType;
import org.apache.abdera.model.Entry;

/**
 *
 * @author imyousuf
 */
public class WorkspaceContentCoProcessorsResourceImpl extends AbstractFeedClientResource<WorkspaceRepresentationsResource>
    implements WorkspaceContentCoProcessorsResource {

  public WorkspaceContentCoProcessorsResourceImpl(Resource referrer, ResourceLink link) {
    super(referrer, link);
  }

  @Override
  protected void processClientConfig(ClientConfig clientConfig) {
  }

  @Override
  protected WorkspaceRepresentationsResource instantiatePageableResource(ResourceLink link) {
    return new WorkspaceRepresentationsResourceImpl(this, link);
  }

  @Override
  public Collection<WorkspaceContentCoProcessorResource> getContentCoProcessorResources() {
    if (getLastReadStateOfEntity() == null) {
      return Collections.emptyList();
    }
    final List<Entry> entries = getLastReadStateOfEntity().getEntries();
    if (entries == null || entries.isEmpty()) {
      return Collections.emptyList();
    }
    List<WorkspaceContentCoProcessorResource> resources = new ArrayList<WorkspaceContentCoProcessorResource>(entries.size());
    for (Entry entry : entries) {
      resources.add(new WorkspaceContentCoProcessorResourceImpl(this, AtomClientUtil.convertFromAtomLinkToResourceLink(entry.
          getAlternateLink())));
    }
    return Collections.unmodifiableCollection(resources);
  }

  @Override
  public WorkspaceContentCoProcessorResource createContentCoProcessor(ResourceTemplate resourceTemplate) {
    ClientResponse response = post(MediaType.APPLICATION_JSON, resourceTemplate, Status.CREATED);
    return new WorkspaceContentCoProcessorResourceImpl(this, response.getLocation());
  }
}
