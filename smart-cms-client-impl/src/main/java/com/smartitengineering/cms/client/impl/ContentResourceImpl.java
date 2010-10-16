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
import com.smartitengineering.cms.client.api.FieldResource;
import com.smartitengineering.cms.ws.common.domains.Content;
import com.smartitengineering.cms.ws.common.domains.Field;
import com.smartitengineering.util.rest.client.AbstractClientResource;
import com.smartitengineering.util.rest.client.Resource;
import com.smartitengineering.util.rest.client.ResourceLink;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.ClientConfig;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author kaisar
 */
public class ContentResourceImpl extends AbstractClientResource<Content, Resource> implements ContentResource {

  public ContentResourceImpl(Resource referrer, URI uri) {
    super(referrer, uri, MediaType.APPLICATION_JSON, Content.class);
  }

  public ContentResourceImpl(Resource referrer, ResourceLink link) {
    super(referrer, link);
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
  public void update(Content content) {
    put(MediaType.APPLICATION_JSON, content, ClientResponse.Status.ACCEPTED);
  }

  @Override
  public Collection<FieldResource> getFields() {
    Collection<Field> fields = get().getFields();
    if (fields == null) {
      return Collections.emptyList();
    }
    Collection<FieldResource> resources = new ArrayList<FieldResource>(fields.size());
    for (Field field : fields) {
      try {
        resources.add(new FieldResourceImpl(this, new URI(field.getFieldUri())));
      }
      catch (URISyntaxException ex) {
        logger.warn("Could not create URI for field resource!", ex);
      }
    }
    return Collections.unmodifiableCollection(resources);
  }
}
