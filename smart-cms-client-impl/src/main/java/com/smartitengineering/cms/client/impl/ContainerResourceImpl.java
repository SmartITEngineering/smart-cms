/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smartitengineering.cms.client.impl;

import com.smartitengineering.cms.client.api.ContainerResource;
import com.smartitengineering.cms.client.api.ContentResource;
import com.smartitengineering.cms.ws.common.domains.Content;
import com.smartitengineering.cms.ws.common.domains.ContentImpl;
import com.smartitengineering.cms.ws.common.providers.TextURIListProvider;
import com.smartitengineering.util.rest.atom.AtomClientUtil;
import com.smartitengineering.util.rest.client.AbstractClientResource;
import com.smartitengineering.util.rest.client.Resource;
import com.smartitengineering.util.rest.client.ResourceLink;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;

/**
 *
 * @author kaisar
 */
public class ContainerResourceImpl extends AbstractClientResource<Feed, Resource> implements ContainerResource {

  public ContainerResourceImpl(Resource referrer, ResourceLink link) {
    super(referrer, link);
  }

  public ContainerResourceImpl(Resource referrer, URI uri) {
    super(referrer, uri, MediaType.APPLICATION_JSON, Feed.class);
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
  public void createContainer(URI contentUri) {
    MultivaluedMap<String, String> map = new MultivaluedMapImpl();
    map.add("contentUri", contentUri.toASCIIString());
    post(MediaType.APPLICATION_FORM_URLENCODED, map, ClientResponse.Status.ACCEPTED);
  }

  @Override
  public void updateContainer(Collection<URI> contentUri) {
    put(TextURIListProvider.TEXT_URI_LIST, contentUri, ClientResponse.Status.OK);
  }

  @Override
  public Collection<ContentResource> getContainerContents() {
    List<Entry> entries = get().getEntries();
    if (entries == null || entries.isEmpty()) {
      return Collections.emptyList();
    }
    List<ContentResource> containerContents = new ArrayList<ContentResource>(entries.size());
    for (Entry entry : entries) {
      containerContents.add(new ContentResourceImpl(this, AtomClientUtil.convertFromAtomLinkToResourceLink(entry.
          getAlternateLink())));
    }
    return Collections.unmodifiableCollection(containerContents);
  }
}
