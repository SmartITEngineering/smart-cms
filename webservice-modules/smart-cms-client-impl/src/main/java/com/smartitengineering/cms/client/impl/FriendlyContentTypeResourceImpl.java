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

import com.smartitengineering.cms.client.api.ContentTypeFeedResource;
import com.smartitengineering.cms.client.api.ContentTypeResource;
import com.smartitengineering.cms.client.api.ContentTypeSearchResultResource;
import com.smartitengineering.cms.client.api.FriendlyContentTypeResource;
import com.smartitengineering.util.rest.atom.AbstractFeedClientResource;
import com.smartitengineering.util.rest.atom.AtomClientUtil;
import com.smartitengineering.util.rest.client.Resource;
import com.smartitengineering.util.rest.client.ResourceLink;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.config.ClientConfig;
import org.apache.abdera.model.Feed;

/**
 *
 * @author imyousuf
 */
public class FriendlyContentTypeResourceImpl extends AbstractFeedClientResource<Resource<? extends Feed>> implements
    FriendlyContentTypeResource {

  public FriendlyContentTypeResourceImpl(Resource referrer, ResourceLink resouceLink) throws IllegalArgumentException,
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
  public ContentTypeResource getContentTypeResource() {
    return new ContentTypeResourceImpl(this, AtomClientUtil.convertFromAtomLinkToResourceLink(getLastReadStateOfEntity().
        getLink("contentType")));
  }

  @Override
  public ContentTypeFeedResource getContentTypeFeedResource() {
    return new ContentTypeFeedResourceImpl(this, AtomClientUtil.convertFromAtomLinkToResourceLink(getLastReadStateOfEntity().
        getLink("contentTypeFeed")));
  }

  @Override
  public ContentTypeSearchResultResource getChildren() {
    return new ContentTypeSearchResultResourceImpl(this, AtomClientUtil.convertFromAtomLinkToResourceLink(getLastReadStateOfEntity().
        getLink("children")));
  }

  @Override
  public ContentTypeSearchResultResource getInstances() {
    return new ContentTypeSearchResultResourceImpl(this, AtomClientUtil.convertFromAtomLinkToResourceLink(getLastReadStateOfEntity().
        getLink("instances")));
  }
}
