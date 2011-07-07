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
import com.smartitengineering.util.rest.atom.AbstractFeedClientResource;
import com.smartitengineering.util.rest.atom.AtomClientUtil;
import com.smartitengineering.util.rest.client.Resource;
import com.smartitengineering.util.rest.client.ResourceLink;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.config.ClientConfig;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.core.MediaType;
import org.apache.abdera.ext.opensearch.OpenSearchConstants;
import org.apache.abdera.ext.opensearch.model.IntegerElement;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;

/**
 *
 * @author imyousuf
 */
public class ContentTypeSearchResultResourceImpl extends AbstractFeedClientResource<ContentTypeSearchResultResource>
    implements ContentTypeSearchResultResource {

  public ContentTypeSearchResultResourceImpl(Resource referrer, ResourceLink resouceLink) throws
      IllegalArgumentException,
      UniformInterfaceException {
    super(referrer, resouceLink);
  }

  @Override
  protected void processClientConfig(ClientConfig clientConfig) {
  }

  @Override
  protected ContentTypeSearchResultResource instantiatePageableResource(ResourceLink link) {
    return new ContentTypeSearchResultResourceImpl(this, link);
  }

  @Override
  public int getTotalItemCount() {
    final Feed feed = getLastReadStateOfEntity();
    if (feed == null) {
      return 0;
    }
    else {
      IntegerElement integerElement = feed.<IntegerElement>getExtension(OpenSearchConstants.TOTAL_RESULTS);
      if (integerElement != null) {
        return integerElement.getValue();
      }
      else {
        return 0;
      }
    }
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
        try {
          List<Link> altLinks = entry.getLinks(Link.REL_ALTERNATE);
          Link alternateLink = null;
          for (Link altLink : altLinks) {
            if (altLink.getMimeType() != null && MediaType.APPLICATION_XML.equals(altLink.getMimeType().toString())) {
              alternateLink = altLink;
              break;
            }
          }
          if (alternateLink != null) {
            list.add(new ContentTypeResourceImpl(this, AtomClientUtil.convertFromAtomLinkToResourceLink(alternateLink)));
          }
        }
        catch (Exception ex) {
          logger.warn("Could not fetch content type", ex);
        }
      }
      return Collections.unmodifiableCollection(list);
    }
  }

  @Override
  public Collection<ContentTypeFeedResource> getContentTypeFeeds() {
    final Feed feed = getLastReadStateOfEntity();
    if (feed == null) {
      return Collections.emptyList();
    }
    else {
      List<Entry> entries = feed.getEntries();
      List<ContentTypeFeedResource> list = new ArrayList<ContentTypeFeedResource>(entries.size());
      for (Entry entry : entries) {
        try {
          List<Link> altLinks = entry.getLinks(Link.REL_ALTERNATE);
          Link alternateLink = null;
          for (Link altLink : altLinks) {
            if (altLink.getMimeType() != null && MediaType.APPLICATION_ATOM_XML.equals(altLink.getMimeType().toString())) {
              alternateLink = altLink;
              break;
            }
          }
          if (alternateLink != null) {
            list.add(new ContentTypeFeedResourceImpl(this, AtomClientUtil.convertFromAtomLinkToResourceLink(
                alternateLink)));
          }
        }
        catch (Exception ex) {
          logger.warn("Could not fetch content type", ex);
        }
      }
      return Collections.unmodifiableCollection(list);
    }
  }
}
