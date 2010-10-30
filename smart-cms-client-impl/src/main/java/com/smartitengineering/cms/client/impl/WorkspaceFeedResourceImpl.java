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

import com.smartitengineering.cms.client.api.ContentTypesResource;
import com.smartitengineering.cms.client.api.ContentsResource;
import com.smartitengineering.cms.client.api.WorkspaceFeedResource;
import com.smartitengineering.cms.client.api.WorkspaceFriendsResource;
import com.smartitengineering.cms.client.api.WorkspaceRepresentationsResource;
import com.smartitengineering.cms.client.api.WorkspaceVariationsResource;
import com.smartitengineering.util.rest.atom.AbstractFeedClientResource;
import com.smartitengineering.util.rest.atom.AtomClientUtil;
import com.smartitengineering.util.rest.client.Resource;
import com.smartitengineering.util.rest.client.ResourceLink;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.config.ClientConfig;
import org.apache.abdera.model.Feed;

/**
 *
 * @author kaisar
 */
public class WorkspaceFeedResourceImpl extends AbstractFeedClientResource<Resource<? extends Feed>> implements
    WorkspaceFeedResource {

  public WorkspaceFeedResourceImpl(Resource referrer, ResourceLink uri) throws IllegalArgumentException,
                                                                               UniformInterfaceException {
    super(referrer, uri);
  }

  @Override
  protected void processClientConfig(ClientConfig clientConfig) {
  }

  @Override
  protected Resource<? extends Feed> instantiatePageableResource(ResourceLink link) {
    return null;
  }

  @Override
  public WorkspaceFriendsResource getFriends() {
    return new WorkspaceFriendsResourceImpl(this, AtomClientUtil.convertFromAtomLinkToResourceLink(getLastReadStateOfEntity().
        getLink("friendlies")));
  }

  @Override
  public WorkspaceRepresentationsResource getRepresentations() {
    return new WorkspaceRepresentationsResourceImpl(this, AtomClientUtil.convertFromAtomLinkToResourceLink(getLastReadStateOfEntity().
        getLink("representations")));
  }

  @Override
  public WorkspaceVariationsResource getVariations() {
    return new WorkspaceVariationsResourceImpl(this, AtomClientUtil.convertFromAtomLinkToResourceLink(getLastReadStateOfEntity().
        getLink("variations")));
  }

  @Override
  public ContentTypesResource getContentTypes() {
    return new ContentTypesResourceImpl(this, AtomClientUtil.convertFromAtomLinkToResourceLink(getLastReadStateOfEntity().
        getLink("content-types")));
  }

  @Override
  public ContentsResource getContents() {
    return new ContentsResourceImpl(this, AtomClientUtil.convertFromAtomLinkToResourceLink(getLastReadStateOfEntity().
        getLink("contents")));
  }
}
