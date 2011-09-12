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

import com.smartitengineering.cms.client.api.ContentSearcherResource;
import com.smartitengineering.cms.client.api.ContentTypesResource;
import com.smartitengineering.cms.client.api.ContentsResource;
import com.smartitengineering.cms.client.api.WorkspaceContentCoProcessorsResource;
import com.smartitengineering.cms.client.api.WorkspaceContentResouce;
import com.smartitengineering.cms.client.api.WorkspaceFeedResource;
import com.smartitengineering.cms.client.api.WorkspaceFriendsResource;
import com.smartitengineering.cms.client.api.WorkspaceRepresentationsResource;
import com.smartitengineering.cms.client.api.WorkspaceValidatorsResource;
import com.smartitengineering.cms.client.api.WorkspaceVariationsResource;
import com.smartitengineering.cms.ws.common.utils.SimpleFeedExtensions;
import com.smartitengineering.util.rest.atom.AbstractFeedClientResource;
import com.smartitengineering.util.rest.atom.AtomClientUtil;
import com.smartitengineering.util.rest.client.Resource;
import com.smartitengineering.util.rest.client.ResourceLink;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.config.ClientConfig;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.commons.lang.StringUtils;

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
    return new WorkspaceFriendsResourceImpl(this,
                                            AtomClientUtil.convertFromAtomLinkToResourceLink(getLastReadStateOfEntity().
        getLink("friendlies")));
  }

  @Override
  public WorkspaceRepresentationsResource getRepresentations() {
    return new WorkspaceRepresentationsResourceImpl(this,
                                                    AtomClientUtil.convertFromAtomLinkToResourceLink(getLastReadStateOfEntity().
        getLink("representations")));
  }

  @Override
  public WorkspaceVariationsResource getVariations() {
    return new WorkspaceVariationsResourceImpl(this,
                                               AtomClientUtil.convertFromAtomLinkToResourceLink(getLastReadStateOfEntity().
        getLink("variations")));
  }

  @Override
  public ContentTypesResource getContentTypes() {
    return new ContentTypesResourceImpl(this,
                                        AtomClientUtil.convertFromAtomLinkToResourceLink(getLastReadStateOfEntity().
        getLink("content-types")));
  }

  @Override
  public ContentsResource getContents() {
    return new ContentsResourceImpl(this, AtomClientUtil.convertFromAtomLinkToResourceLink(getLastReadStateOfEntity().
        getLink("contents")));
  }

  @Override
  public WorkspaceContentResouce getWorkspace() {
    final ResourceLink link =
                       AtomClientUtil.convertFromAtomLinkToResourceLink(getLastReadStateOfEntity().getLink(
        Link.REL_ALTERNATE));
    if (logger.isInfoEnabled()) {
      logger.info("Link Rel: " + link.getRel());
      logger.info("Link URI: " + link.getUri().toASCIIString());
      logger.info("Link MIME Type: " + link.getMimeType());
    }
    return new WorkspaceContentResourceImpl(this, link);
  }

  @Override
  public ContentSearcherResource searchContent(String query) {
    Link link = getLastReadStateOfEntity().getLink("search");
    if (StringUtils.isNotBlank(query)) {
      String strLink = link.getHref().toASCIIString();
      strLink = strLink + "?" + query;
      link.setHref(strLink);
    }
    return new ContentSearcherResourceImpl(this, AtomClientUtil.convertFromAtomLinkToResourceLink(link));
  }

  @Override
  public String getSearchUri() {
    Link link = getLastReadStateOfEntity().getLink("search");
    return link != null ? link.getHref().toASCIIString() : "";
  }

  @Override
  public WorkspaceValidatorsResource getValidators() {
    return new WorkspaceValidatorsResourceImpl(this,
                                               AtomClientUtil.convertFromAtomLinkToResourceLink(getLastReadStateOfEntity().
        getLink("validators")));
  }

  @Override
  public String getWorkspaceNamespace() {
    return getLastReadStateOfEntity().getSimpleExtension(SimpleFeedExtensions.WORKSPACE_NAME_SPACE);
  }

  @Override
  public String getWorkspaceName() {
    return getLastReadStateOfEntity().getSimpleExtension(SimpleFeedExtensions.WORKSPACE_NAME);
  }

  public WorkspaceContentCoProcessorsResource getContentCoProcessors() {
    return new WorkspaceContentCoProcessorsResourceImpl(this,
                                                        AtomClientUtil.convertFromAtomLinkToResourceLink(getLastReadStateOfEntity().
        getLink("contentCoProcessors")));
  }
}
