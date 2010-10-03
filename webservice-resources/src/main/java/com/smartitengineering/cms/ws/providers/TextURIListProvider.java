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
package com.smartitengineering.cms.ws.providers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
@Provider
@Consumes(TextURIListProvider.TEXT_URI_LIST)
@Produces(TextURIListProvider.TEXT_URI_LIST)
public class TextURIListProvider implements MessageBodyReader<Collection<URI>>, MessageBodyWriter<Collection<URI>> {

  public static final String TEXT_URI_LIST = "text/uri-list";
  public static final MediaType TEXT_URI_LIST_TYPE = new MediaType("text", "uri-list");
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    if (logger.isDebugEnabled()) {
      logger.debug(new StringBuilder("Type ").append(type.getName()).toString());
      logger.debug(new StringBuilder("Generic Type ").append(genericType).toString());
      logger.debug(new StringBuilder("Media Type ").append(mediaType).toString());
    }
    if (TEXT_URI_LIST_TYPE.isCompatible(mediaType) && Collection.class.isAssignableFrom(type)) {
      return true;
    }
    else {
      return false;
    }
  }

  @Override
  public Collection<URI> readFrom(Class<Collection<URI>> type, Type genericType, Annotation[] annotations,
                                  MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
                                  InputStream entityStream) throws IOException, WebApplicationException {
    if (isReadable(type, genericType, annotations, mediaType)) {
      List<String> asLines = IOUtils.readLines(entityStream, null);
      if (asLines == null || asLines.isEmpty()) {
        return Collections.emptyList();
      }
      List<URI> uris = new ArrayList<URI>();
      for (String line : asLines) {
        try {
          if (logger.isDebugEnabled()) {
            logger.debug(new StringBuilder("Trying add ").append(line).append(" as an URI").toString());
          }
          uris.add(new URI(line));
        }
        catch (URISyntaxException ex) {
          logger.error("URI exception while trying to form an URI", ex);
          throw new IOException(ex);
        }
      }
      return Collections.unmodifiableCollection(uris);
    }
    else {
      return Collections.emptyList();
    }
  }

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return isReadable(type, genericType, annotations, mediaType);
  }

  @Override
  public long getSize(Collection<URI> t,
                      Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(Collection<URI> t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                      MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException,
                                                                                                    WebApplicationException {
    if (isWriteable(type, genericType, annotations, mediaType)) {
      List<String> lines = new ArrayList<String>(t.size());
      for (URI uri : t) {
        if (uri == null) {
          continue;
        }
        lines.add(uri.toASCIIString());
      }
      IOUtils.writeLines(lines, null, entityStream);
    }
  }
}
