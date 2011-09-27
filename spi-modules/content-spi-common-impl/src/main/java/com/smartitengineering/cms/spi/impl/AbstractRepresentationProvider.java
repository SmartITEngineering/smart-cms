/*
 *
 * This is a simple Content Management System (CMS)
 * Copyright (C) 2009  Imran M Yousuf (imyousuf@smartitengineering.com)
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
package com.smartitengineering.cms.spi.impl;

import com.smartitengineering.cms.api.content.Content;
import com.smartitengineering.cms.api.content.MutableRepresentation;
import com.smartitengineering.cms.api.content.template.RepresentationGenerator;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.type.RepresentationDef;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class AbstractRepresentationProvider {

  protected final transient Logger logger = LoggerFactory.getLogger(getClass());

  protected MutableRepresentation getMutableRepresentation(RepresentationGenerator generator, Content content,
                                                           Map<String, String> params, String representationName) throws
      RuntimeException {
    final Object representationForContent = generator.getRepresentationForContent(content, params);
    final byte[] bytes;
    if (representationForContent instanceof String) {
      bytes = StringUtils.getBytesUtf8((String) representationForContent);
    }
    else if (representationForContent instanceof byte[]) {
      bytes = (byte[]) representationForContent;
    }
    else if (representationForContent instanceof InputStream) {
      try {
        bytes = IOUtils.toByteArray((InputStream) representationForContent);
      }
      catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    }
    else if (representationForContent instanceof Reader) {
      try {
        bytes = IOUtils.toByteArray((Reader) representationForContent);
      }
      catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    }
    else {
      bytes = StringUtils.getBytesUtf8(representationForContent.toString());
    }
    MutableRepresentation representation =
                          SmartContentAPI.getInstance().getContentLoader().createMutableRepresentation(content.
        getContentId());
    final RepresentationDef def = content.getContentDefinition().getRepresentationDefs().get(representationName);
    representation.setName(representationName);
    representation.setMimeType(def.getMIMEType());
    representation.setRepresentation(bytes);
    return representation;
  }
}
