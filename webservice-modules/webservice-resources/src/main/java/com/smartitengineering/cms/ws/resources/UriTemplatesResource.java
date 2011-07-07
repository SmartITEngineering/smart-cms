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
package com.smartitengineering.cms.ws.resources;

import com.smartitengineering.cms.ws.resources.content.ContentsResource;
import com.smartitengineering.cms.ws.resources.type.ContentTypesResource;
import com.smartitengineering.cms.ws.resources.workspace.WorkspaceResource;
import com.smartitengineering.util.opensearch.api.OpenSearchDescriptor;
import com.smartitengineering.util.opensearch.impl.OpenSearchDescriptorBuilder;
import com.smartitengineering.util.opensearch.impl.UrlBuilder;
import com.smartitengineering.util.opensearch.jaxrs.MediaType;
import com.smartitengineering.util.rest.server.AbstractResource;
import java.net.URI;
import java.net.URLDecoder;
import java.util.concurrent.locks.ReentrantLock;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriBuilderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class UriTemplatesResource extends AbstractResource {

  protected final transient Logger logger = LoggerFactory.getLogger(getClass());
  private static String workspaceResourceUriTemplate;
  private static String contentTypeResourceUriTemplate;
  private static String friendlyContentTypeResourceUriTemplate;
  private static String contentResourceUriTemplate;
  private final static String REL_WORKSPACE = "workspace";
  private final static String REL_CONTENT_TYPE = "contenttype";
  private final static String REL_CONTENT = "content";
  private final static String REL_FRIENDLY_CONTENT_TYPE = "friendlyContentType";
  private static OpenSearchDescriptor descriptor;
  private static final ReentrantLock lock = new ReentrantLock();

  public UriTemplatesResource() {
  }

  @GET
  @Produces(MediaType.APPLICATION_OPENSEARCHDESCRIPTION_XML)
  public Response getSpec() {
    if (descriptor == null) {
      lock.lock();
      try {
        if (descriptor == null) {
          OpenSearchDescriptorBuilder descBuilder = OpenSearchDescriptorBuilder.getBuilder();
          descBuilder.shortName("UrlTemplates");
          descBuilder.description("Search for common entities");
          String templateBuilder = getWorkspaceUri();
          final String orgUrlTemplate = templateBuilder.toString();
          UrlBuilder workspaceBuilder = UrlBuilder.getBuilder().rel(REL_WORKSPACE).template(orgUrlTemplate).type(
              javax.ws.rs.core.MediaType.APPLICATION_ATOM_XML);
          templateBuilder = getContentTypeUri();
          final String typeUrlTemplate = templateBuilder.toString();
          UrlBuilder contentTypeBuilder = UrlBuilder.getBuilder().rel(REL_CONTENT_TYPE).template(typeUrlTemplate).
              type(javax.ws.rs.core.MediaType.APPLICATION_ATOM_XML);
          templateBuilder = getContentUri();
          final String contentUrlTemplate = templateBuilder.toString();
          UrlBuilder contentBuilder = UrlBuilder.getBuilder().rel(REL_CONTENT).template(contentUrlTemplate).
              type(javax.ws.rs.core.MediaType.APPLICATION_ATOM_XML);
          templateBuilder = getFriendlyContentTypeUri();
          final String friendlyContentTypeUrlTemplate = templateBuilder.toString();
          UrlBuilder friendlyContentTypeBuilder = UrlBuilder.getBuilder().rel(REL_FRIENDLY_CONTENT_TYPE).template(
              friendlyContentTypeUrlTemplate).type(javax.ws.rs.core.MediaType.APPLICATION_ATOM_XML);
          if (logger.isInfoEnabled()) {
            logger.info("Workspace Template URL: " + orgUrlTemplate);
            logger.info("Content type Template URL: " + typeUrlTemplate);
            logger.info("Content Template URL: " + contentUrlTemplate);
            logger.info("Friendly Content Type Template URL: " + friendlyContentTypeUrlTemplate);
          }
          descBuilder.urls(workspaceBuilder.build(), contentTypeBuilder.build(), contentBuilder.build(), friendlyContentTypeBuilder.
              build());
          descriptor = descBuilder.build();
        }
      }
      finally {
        lock.unlock();
      }
    }
    ResponseBuilder builder = Response.ok(descriptor);
    return builder.build();
  }

  protected String getResourceClassUri(final URI path) throws IllegalArgumentException,
                                                              UriBuilderException,
                                                              RuntimeException {
    StringBuilder builder = new StringBuilder();
    final URI build = path;
    try {
      builder.append(URLDecoder.decode(build.toString(), "UTF-8"));
    }
    catch (Exception ex) {
      throw new RuntimeException(ex);
    }
    return builder.toString();
  }

  private String getWorkspaceUri() {
    if (workspaceResourceUriTemplate == null) {
      lock.lock();
      try {
        if (workspaceResourceUriTemplate == null) {
          workspaceResourceUriTemplate = getResourceClassUri(
              getRelativeURIBuilder().path(WorkspaceResource.class).build());
        }
      }
      finally {
        lock.unlock();
      }
    }
    return workspaceResourceUriTemplate;
  }

  private String getContentTypeUri() {
    if (contentTypeResourceUriTemplate == null) {
      lock.lock();
      try {
        if (contentTypeResourceUriTemplate == null) {
          contentTypeResourceUriTemplate = getResourceClassUri(getRelativeURIBuilder().path(ContentTypesResource.class).
              path(
              ContentTypesResource.PATH_TO_CONTENT_TYPE).build());
        }
      }
      finally {
        lock.unlock();
      }
    }
    return contentTypeResourceUriTemplate;
  }

  private String getFriendlyContentTypeUri() {
    if (friendlyContentTypeResourceUriTemplate == null) {
      lock.lock();
      try {
        if (friendlyContentTypeResourceUriTemplate == null) {
          friendlyContentTypeResourceUriTemplate = getResourceClassUri(getRelativeURIBuilder().path(
              ContentTypesResource.class).
              path(
              ContentTypesResource.PATH_TO_FRIENDLY_CONTENT_TYPE).build());
        }
      }
      finally {
        lock.unlock();
      }
    }
    return friendlyContentTypeResourceUriTemplate;
  }

  private String getContentUri() {
    if (contentResourceUriTemplate == null) {
      lock.lock();
      try {
        if (contentResourceUriTemplate == null) {
          contentResourceUriTemplate = getResourceClassUri(getRelativeURIBuilder().path(ContentsResource.class).path(
              ContentsResource.PATH_TO_CONTENT).build());
        }
      }
      finally {
        lock.unlock();
      }
    }
    return contentResourceUriTemplate;
  }
}
