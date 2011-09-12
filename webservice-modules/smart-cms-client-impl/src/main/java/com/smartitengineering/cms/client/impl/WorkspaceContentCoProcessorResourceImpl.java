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
import com.smartitengineering.cms.ws.common.domains.ResourceTemplate;
import com.smartitengineering.util.rest.client.AbstractClientResource;
import com.smartitengineering.util.rest.client.Resource;
import com.smartitengineering.util.rest.client.ResourceLink;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.config.ClientConfig;
import java.net.URI;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author imyousuf
 */
public class WorkspaceContentCoProcessorResourceImpl extends AbstractClientResource<ResourceTemplate, Resource> implements
    WorkspaceContentCoProcessorResource {

  public WorkspaceContentCoProcessorResourceImpl(Resource referrer, URI uri) {
    super(referrer, uri, MediaType.APPLICATION_JSON, ResourceTemplate.class);
  }

  public WorkspaceContentCoProcessorResourceImpl(Resource referrer, ResourceLink link) {
    super(referrer, link);
  }

  @Override
  protected void processClientConfig(ClientConfig clientConfig) {
  }

  @Override
  protected ResourceLink getNextUri() {
    return null;
  }

  @Override
  protected ResourceLink getPreviousUri() {
    return null;
  }

  @Override
  protected Resource instantiatePageableResource(ResourceLink link) {
    return null;
  }

  @Override
  public void update(ResourceTemplate template) {
    put(MediaType.APPLICATION_JSON, template, Status.ACCEPTED);
  }
}
