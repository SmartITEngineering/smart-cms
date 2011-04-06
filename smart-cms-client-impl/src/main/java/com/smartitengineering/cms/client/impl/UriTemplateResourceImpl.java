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

import com.smartitengineering.cms.client.api.ContentResource;
import com.smartitengineering.cms.client.api.ContentTypeResource;
import com.smartitengineering.cms.client.api.UriTemplateResource;
import com.smartitengineering.cms.client.api.WorkspaceFeedResource;
import com.smartitengineering.util.opensearch.api.OpenSearchDescriptor;
import com.smartitengineering.util.opensearch.api.Url;
import com.smartitengineering.util.opensearch.api.Url.Rel;
import com.smartitengineering.util.rest.client.AbstractClientResource;
import com.smartitengineering.util.rest.client.ClientUtil;
import com.smartitengineering.util.rest.client.Resource;
import com.smartitengineering.util.rest.client.ResourceLink;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.config.ClientConfig;
import java.net.URI;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;

/**
 *
 * @author imyousuf
 */
public class UriTemplateResourceImpl extends AbstractClientResource<OpenSearchDescriptor, Resource> implements
    UriTemplateResource {

  private static final String WORKSPACE_NAME = "{wsName}";
  private static final String WORKSPACE_NS = "{ns}";
  private static final String TYPE_NAME = "{typeName}";
  private static final String TYPE_NS = "{typeNS}";
  private static final String CONTENT_ID = "{contentId}";

  public UriTemplateResourceImpl(Resource referrer, ResourceLink resouceLink) throws IllegalArgumentException,
                                                                                     UniformInterfaceException {
    super(referrer, resouceLink);
  }

  @Override
  public WorkspaceFeedResource getWorkspaceResource(String workspaceNS, String workspaceId) {
    ResourceLink link = getResourceLink("workspace",
                                        new SimpleImmutableEntry<String, String>(WORKSPACE_NS, workspaceNS),
                                        new SimpleImmutableEntry<String, String>(WORKSPACE_NAME, workspaceId));
    if (link != null) {
      return new WorkspaceFeedResourceImpl(this, link);
    }
    return null;
  }

  @Override
  public ContentTypeResource getContentTypeResource(String workspaceNS, String workspaceId, String typeNS, String typeId) {
    ResourceLink link = getResourceLink("contentType",
                                        new SimpleImmutableEntry<String, String>(WORKSPACE_NS, workspaceNS),
                                        new SimpleImmutableEntry<String, String>(WORKSPACE_NAME, workspaceId),
                                        new SimpleImmutableEntry<String, String>(TYPE_NS, typeNS),
                                        new SimpleImmutableEntry<String, String>(TYPE_NAME, typeId));
    if (link != null) {
      return new ContentTypeResourceImpl(this, link);
    }
    return null;
  }

  @Override
  public ContentResource getContentResource(String workspaceNS, String workspaceId, String contentId) {
    ResourceLink link = getResourceLink("content",
                                        new SimpleImmutableEntry<String, String>(WORKSPACE_NS, workspaceNS),
                                        new SimpleImmutableEntry<String, String>(WORKSPACE_NAME, workspaceId),
                                        new SimpleImmutableEntry<String, String>(CONTENT_ID, contentId));
    if (link != null) {
      return new ContentResourceImpl(this, link);
    }
    return null;
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

  public ResourceLink getResourceLink(String relValue, Entry<String, String>... templateVars) {
    OpenSearchDescriptor descriptor = getLastReadStateOfEntity();
    for (Url url : descriptor.getUrls()) {
      for (Rel rel : url.getRels()) {
        if (relValue.equals(rel.getValue())) {
          String urlStr = url.getTemplate();
          for (Entry<String, String> templateVar : templateVars) {
            urlStr = urlStr.replace(templateVar.getKey(), templateVar.getValue());
          }
          ResourceLink link = ClientUtil.createResourceLink(relValue, URI.create(urlStr), url.getType());
          return link;
        }
      }
    }
    return null;
  }
}
