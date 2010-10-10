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

import com.smartitengineering.cms.client.api.ContentTypeResource;
import com.smartitengineering.cms.ws.common.domains.ResourceTemplate;
import com.smartitengineering.util.rest.client.AbstractClientResource;
import com.smartitengineering.util.rest.client.Resource;
import com.smartitengineering.util.rest.client.ResourceLink;
import com.sun.jersey.api.client.config.ClientConfig;
import java.net.URI;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author kaisar
 */
public class ContentTypeResourceImpl extends AbstractClientResource<ResourceTemplate, Resource> implements
    ContentTypeResource {

  public ContentTypeResourceImpl(Resource referer, ResourceLink link) {
    super(referer, link);
  }

  public ContentTypeResourceImpl(Resource referer, URI uri) {
    super(referer, uri, MediaType.APPLICATION_JSON, ResourceTemplate.class);
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
  public String getContentType() {
    final String contentType = getResourceRepresentationType();
    return contentType;
  }
}
