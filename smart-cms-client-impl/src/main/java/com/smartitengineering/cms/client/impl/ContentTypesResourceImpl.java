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
import com.smartitengineering.cms.client.api.ContentTypesResource;
import com.smartitengineering.util.rest.atom.AbstractFeedClientResource;
import com.smartitengineering.util.rest.atom.AtomClientUtil;
import com.smartitengineering.util.rest.client.Resource;
import com.smartitengineering.util.rest.client.ResourceLink;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.config.ClientConfig;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.core.MediaType;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;

/**
 *
 * @author kaisar
 */
public class ContentTypesResourceImpl extends AbstractFeedClientResource<ContentTypesResource> implements
    ContentTypesResource {

  public ContentTypesResourceImpl(Resource referrer, ResourceLink resouceLink) throws IllegalArgumentException,
                                                                                      UniformInterfaceException {
    super(referrer, resouceLink);
  }

  @Override
  protected void processClientConfig(ClientConfig clientConfig) {
  }

  @Override
  protected ContentTypesResource instantiatePageableResource(ResourceLink link) {
    return null;
  }

  @Override
  public Collection<ContentTypeResource> getContentTypes() {
    final Feed feed = getLastReadStateOfEntity();
    if (feed == null) {
      return Collections.emptyList();
    }
    else {
      List<Entry> entries = feed.getEntries();
      List<ContentTypeResource> list = new ArrayList<ContentTypeResource>(entries.size());
      for (Entry entry : entries) {
        list.add(new ContentTypeResourceImpl(this, AtomClientUtil.convertFromAtomLinkToResourceLink(entry.
            getAlternateLink())));
      }
      return Collections.unmodifiableCollection(list);
    }
  }

  @Override
  public void createContentType(String contentType) {
    post(MediaType.APPLICATION_XML, contentType, ClientResponse.Status.ACCEPTED);
  }
}
