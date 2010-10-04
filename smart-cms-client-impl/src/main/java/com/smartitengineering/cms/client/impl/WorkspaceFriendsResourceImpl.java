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

import com.smartitengineering.cms.client.api.WorkspaceFriendsResource;
import com.smartitengineering.cms.ws.common.providers.TextURIListProvider;
import com.smartitengineering.util.rest.client.AbstractClientResource;
import com.smartitengineering.util.rest.client.Resource;
import com.smartitengineering.util.rest.client.ResourceLink;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.net.URI;
import java.util.Collection;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

/**
 *
 * @author kaisar
 */
public class WorkspaceFriendsResourceImpl extends AbstractClientResource<Collection<URI>, Resource> implements
    WorkspaceFriendsResource {

  public WorkspaceFriendsResourceImpl(Resource referrer, ResourceLink resouceLink) throws IllegalArgumentException,
                                                                                          UniformInterfaceException {
    super(referrer, resouceLink);
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
  public void deleteAllFriends() {
    delete(ClientResponse.Status.ACCEPTED);
  }

  @Override
  public void replaceAllFriends(Collection<URI> workspaces) {
    put(TextURIListProvider.TEXT_URI_LIST, workspaces, ClientResponse.Status.OK);
  }

  @Override
  public void deleteFriend(URI friend) {
    UriBuilder builder = getCurrentUriBuilder().clone();
    builder.queryParam("workspaceUri", friend.toASCIIString());
    ClientResponse response = getHttpClient().getWebResource(builder.build()).delete(ClientResponse.class);
    if (response.getStatus() != ClientResponse.Status.ACCEPTED.getStatusCode()) {
      throw new UniformInterfaceException(response);
    }
  }

  @Override
  public void addFriend(URI friend) {
    final MultivaluedMap<String, String> map = new MultivaluedMapImpl();
    map.add("workspaceUri", friend.toASCIIString());
    post(MediaType.APPLICATION_FORM_URLENCODED, map, ClientResponse.Status.ACCEPTED);
  }
}
