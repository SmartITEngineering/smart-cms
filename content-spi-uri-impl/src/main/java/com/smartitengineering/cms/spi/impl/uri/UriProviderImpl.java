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
package com.smartitengineering.cms.spi.impl.uri;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.smartitengineering.cms.api.content.ContentId;
import com.smartitengineering.cms.api.type.FieldDef;
import com.smartitengineering.cms.spi.content.UriProvider;
import com.smartitengineering.cms.ws.resources.content.ContentResource;
import com.smartitengineering.cms.ws.resources.content.FieldResource;
import com.smartitengineering.cms.ws.resources.content.RepresentationResource;
import com.smartitengineering.cms.ws.resources.content.VariationResource;
import java.net.URI;
import javax.ws.rs.core.UriBuilder;

/**
 *
 * @author imyousuf
 */
public class UriProviderImpl implements UriProvider {

  @Inject
  @Named("cmsBaseUri")
  private URI baseUri;

  @Override
  public URI getContentUri(ContentId contentId) {
    return ContentResource.getContentUri(UriBuilder.fromUri(baseUri), contentId);
  }

  @Override
  public URI getFieldUri(ContentId contentId, FieldDef fieldDef) {
    return FieldResource.getFieldURI(UriBuilder.fromUri(baseUri), contentId, fieldDef);
  }

  @Override
  public URI getRawFieldContentUri(ContentId contentId, FieldDef fieldDef) {
    return FieldResource.getFieldRawURI(UriBuilder.fromUri(baseUri), contentId, fieldDef);
  }

  @Override
  public URI getAbsRawFieldContentUri(ContentId contentId, FieldDef fieldDef) {
    return FieldResource.getFieldAbsRawURI(UriBuilder.fromUri(baseUri), contentId, fieldDef);
  }

  @Override
  public URI getRepresentationUri(ContentId contentId, String name) {
    return RepresentationResource.getUri(UriBuilder.fromUri(baseUri), contentId, name);
  }

  @Override
  public URI getVariationUri(ContentId contentId, FieldDef fieldDef, String name) {
    return VariationResource.getUri(UriBuilder.fromUri(baseUri), contentId, fieldDef, name);
  }
}
