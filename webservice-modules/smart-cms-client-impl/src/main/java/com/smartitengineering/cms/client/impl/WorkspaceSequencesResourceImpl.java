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

import com.smartitengineering.cms.client.api.WorkspaceSequenceResource;
import com.smartitengineering.cms.client.api.WorkspaceSequencesResource;
import com.smartitengineering.util.rest.atom.AbstractFeedClientResource;
import com.smartitengineering.util.rest.atom.AtomClientUtil;
import com.smartitengineering.util.rest.client.Resource;
import com.smartitengineering.util.rest.client.ResourceLink;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;

/**
 *
 * @author imyousuf
 */
public class WorkspaceSequencesResourceImpl extends AbstractFeedClientResource<Resource<? extends Feed>> implements
    WorkspaceSequencesResource {

  public WorkspaceSequencesResourceImpl(Resource referrer, ResourceLink resouceLink) throws IllegalArgumentException,
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

  public Collection<WorkspaceSequenceResource> getSequences() {
    List<WorkspaceSequenceResource> seqs = new ArrayList<WorkspaceSequenceResource>();
    if (getLastReadStateOfEntity().getEntries() != null) {
      for (Entry entry : getLastReadStateOfEntity().getEntries()) {
        seqs.add(getSequenceResource(entry));
      }
    }
    return seqs;
  }

  public WorkspaceSequenceResource getSequenceByName(String name) {
    final Entry entry = getLastReadStateOfEntity().getEntry(name);
    return getSequenceResource(entry);
  }

  protected WorkspaceSequenceResource getSequenceResource(final Entry entry) throws UniformInterfaceException,
                                                                                    IllegalArgumentException {
    if (entry == null) {
      return null;
    }
    else {
      return new WorkspaceSequenceResourceImpl(entry.getTitle(), this, AtomClientUtil.convertFromAtomLinkToResourceLink(
          entry.getSelfLink()));
    }
  }

  public void createSequence(String name, long initialValue) {
    MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
    formData.putSingle("sequenceName", name);
    formData.putSingle("initialValue", Long.toString(initialValue));
    post(MediaType.APPLICATION_FORM_URLENCODED, formData, ClientResponse.Status.CREATED);
  }
}
