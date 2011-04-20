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

import com.smartitengineering.cms.client.api.ContentTypeFeedResource;
import com.smartitengineering.cms.client.api.ContentTypeResource;
import com.smartitengineering.cms.client.api.ContentTypeSearchResultResource;
import com.smartitengineering.cms.ws.common.domains.FieldDef;
import com.smartitengineering.cms.ws.common.utils.SimpleFeedExtensions;
import com.smartitengineering.util.rest.atom.AbstractFeedClientResource;
import com.smartitengineering.util.rest.atom.AtomClientUtil;
import com.smartitengineering.util.rest.client.Resource;
import com.smartitengineering.util.rest.client.ResourceLink;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.config.ClientConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author imyousuf
 */
public class ContentTypeFeedResourceImpl extends AbstractFeedClientResource<Resource<? extends Feed>> implements
    ContentTypeFeedResource {

  public ContentTypeFeedResourceImpl(Resource referrer, ResourceLink resouceLink) throws IllegalArgumentException,
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
    return new ContentTypeResourceImpl(this, getRelatedResourceUris().getFirst(Link.REL_ALTERNATE));
  }

  @Override
  public List<FieldDef> getFieldDefs() {
    if (getLastReadStateOfEntity() == null || getLastReadStateOfEntity().getEntries() == null || getLastReadStateOfEntity().
        getEntries().isEmpty()) {
      return Collections.emptyList();
    }
    List<FieldDef> defs = new ArrayList<FieldDef>(getLastReadStateOfEntity().getEntries().size());
    ObjectMapper mapper = new ObjectMapper();
    for (Entry entry : getLastReadStateOfEntity().getEntries()) {
      if (logger.isInfoEnabled()) {
        logger.info("FiedDef JSON Content: " + entry.getContent());
      }
      try {
        defs.add(mapper.readValue(entry.getContent(), FieldDef.class));
      }
      catch (Exception ex) {
        logger.error("Could not parse Field Def JSON", ex);
      }
    }
    return defs;
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

  @Override
  public String getDisplayName() {
    return getLastReadStateOfEntity().getTitle();
  }

  @Override
  public String getWorkspaceNamespace() {
    return getLastReadStateOfEntity().getSimpleExtension(SimpleFeedExtensions.WORKSPACE_NAME_SPACE);
  }

  @Override
  public String getWorkspaceName() {
    return getLastReadStateOfEntity().getSimpleExtension(SimpleFeedExtensions.WORKSPACE_NAME);
  }

  @Override
  public String getContentTypeName() {
    return getLastReadStateOfEntity().getSimpleExtension(SimpleFeedExtensions.CONTENT_TYPE_NAME);
  }

  @Override
  public String getContentTypeNamespace() {
    return getLastReadStateOfEntity().getSimpleExtension(SimpleFeedExtensions.CONTENT_TYPE_NAME_SPACE);
  }
}
