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

import com.smartitengineering.cms.client.api.FieldResource;
import com.smartitengineering.cms.client.api.VariationResource;
import com.smartitengineering.cms.ws.common.domains.Field;
import com.smartitengineering.util.rest.client.AbstractClientResource;
import com.smartitengineering.util.rest.client.Resource;
import com.smartitengineering.util.rest.client.ResourceLink;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.ClientConfig;
import java.net.URI;
import java.util.Collection;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author kaisar
 */
public class FieldResourceImpl extends AbstractClientResource<Field, Resource> implements FieldResource {

  public FieldResourceImpl(Resource referrer, URI uri) {
    super(referrer, uri, MediaType.APPLICATION_JSON, Field.class);
  }

  public FieldResourceImpl(Resource referrer, ResourceLink link) {
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
  public void update(Field field) {
    put(MediaType.APPLICATION_JSON, field, ClientResponse.Status.ACCEPTED);
  }

  @Override
  public Collection<VariationResource> getVariations() {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
