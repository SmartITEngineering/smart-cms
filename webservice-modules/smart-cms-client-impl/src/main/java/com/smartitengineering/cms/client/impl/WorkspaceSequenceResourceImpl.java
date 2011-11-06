/*
 *
 * This is a simple Content Management System (CMS)
 * Copyright (C) 2011  Imran M Yousuf (imyousuf@smartitengineering.com)
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

import com.smartitengineering.cms.client.api.WorkspaceSequenceResource;
import com.smartitengineering.util.rest.client.AbstractClientResource;
import com.smartitengineering.util.rest.client.Resource;
import com.smartitengineering.util.rest.client.ResourceLink;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.commons.lang.math.NumberUtils;

/**
 *
 * @author imyousuf
 */
public class WorkspaceSequenceResourceImpl extends AbstractClientResource<Map<String, String>, Resource> implements
    WorkspaceSequenceResource {

  private final String name;

  public WorkspaceSequenceResourceImpl(String name, Resource ref, ResourceLink link) throws IllegalArgumentException,
                                                                                            UniformInterfaceException {
    super(ref, link);
    this.name = name;
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

  public String getName() {
    return name;
  }

  public long getCurrentValue() {
    return NumberUtils.toLong(getLastReadStateOfEntity().get("value"));
  }

  public long update(long delta) {
    MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
    formData.putSingle("delta", Long.toString(delta));
    ClientResponse response = post(MediaType.APPLICATION_FORM_URLENCODED, formData, ClientResponse.Status.OK);
    response.bufferEntity();
    response.close();
    return NumberUtils.toLong(response.getEntity(new GenericType<Map<String, String>>() {
    }).get("value"));
  }
}
