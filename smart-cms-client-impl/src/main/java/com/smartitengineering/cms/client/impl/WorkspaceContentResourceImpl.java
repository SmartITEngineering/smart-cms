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

import com.smartitengineering.cms.client.api.WorkspaceContentResouce;
import com.smartitengineering.cms.ws.common.domains.Workspace;
import com.smartitengineering.util.rest.client.AbstractClientResource;
import com.smartitengineering.util.rest.client.Resource;
import com.smartitengineering.util.rest.client.ResourceLink;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.config.ClientConfig;

/**
 *
 * @author imyousuf
 */
public class WorkspaceContentResourceImpl extends AbstractClientResource<Workspace, WorkspaceContentResouce> implements
    WorkspaceContentResouce {

  public WorkspaceContentResourceImpl(Resource referrer, ResourceLink resouceLink) throws IllegalArgumentException,
                                                                                          UniformInterfaceException {
    super(referrer, resouceLink);
  }

  @Override
  protected void processClientConfig(ClientConfig clientConfig) {
  }

  @Override
  protected WorkspaceContentResouce instantiatePageableResource(ResourceLink link) {
    return null;
  }

  @Override
  protected ResourceLink getNextUri() {
    return null;
  }

  @Override
  protected ResourceLink getPreviousUri() {
    return null;
  }
}
