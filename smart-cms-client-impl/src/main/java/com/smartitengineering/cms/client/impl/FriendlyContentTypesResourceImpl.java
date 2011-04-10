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

import com.smartitengineering.cms.client.api.FriendlyContentTypeResource;
import com.smartitengineering.cms.client.api.FriendlyContentTypesResource;
import com.smartitengineering.util.rest.atom.AbstractFeedClientResource;
import com.smartitengineering.util.rest.atom.AtomClientUtil;
import com.smartitengineering.util.rest.client.Resource;
import com.smartitengineering.util.rest.client.ResourceLink;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.config.ClientConfig;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;

/**
 *
 * @author imyousuf
 */
public class FriendlyContentTypesResourceImpl extends AbstractFeedClientResource<Resource<? extends Feed>> implements
    FriendlyContentTypesResource {

  public FriendlyContentTypesResourceImpl(Resource referrer, ResourceLink resouceLink) throws IllegalArgumentException,
                                                                                              UniformInterfaceException {
    super(referrer, resouceLink);
  }

  @Override
  protected void processClientConfig(ClientConfig clientConfig) {
  }

  @Override
  protected Resource<? extends Feed> instantiatePageableResource(ResourceLink link) {
    return null;
  }

  @Override
  public Collection<FriendlyContentTypeResource> getFriendlies() {
    List<Entry> entries = getLastReadStateOfEntity().getEntries();
    List<FriendlyContentTypeResource> resources = new ArrayList<FriendlyContentTypeResource>();
    for (Entry entry : entries) {
      resources.add(new FriendlyContentTypeResourceImpl(this, AtomClientUtil.convertFromAtomLinkToResourceLink(entry.
          getLink("friendlyContentType"))));
    }
    return resources;
  }
}
