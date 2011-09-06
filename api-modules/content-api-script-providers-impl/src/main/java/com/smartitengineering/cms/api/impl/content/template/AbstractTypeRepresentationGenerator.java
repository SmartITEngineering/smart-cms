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
package com.smartitengineering.cms.api.impl.content.template;

import com.smartitengineering.cms.api.content.Content;
import com.smartitengineering.cms.api.content.MutableRepresentation;
import com.smartitengineering.cms.api.exception.InvalidTemplateException;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.workspace.RepresentationTemplate;
import com.smartitengineering.cms.api.content.template.RepresentationGenerator;
import com.smartitengineering.cms.api.content.template.TypeRepresentationGenerator;
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
public abstract class AbstractTypeRepresentationGenerator implements TypeRepresentationGenerator {

  protected final transient Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public MutableRepresentation getRepresentation(RepresentationTemplate template, Content content,
                                                 String representationName, Map<String, String> params) {
    RepresentationGenerator generator;
    try {
      generator = getGenerator(template);
    }
    catch (InvalidTemplateException ex) {
      logger.warn("Could not get generator!", ex);
      generator = null;
    }
    if (generator == null) {
      if (logger.isInfoEnabled()) {
        logger.info("Generator not available!");
      }
      return null;
    }
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
    final String name = template.getName();
    representation.setName(name);
    representation.setMimeType(content.getContentDefinition().getRepresentationDefs().get(representationName).
        getMIMEType());
    representation.setRepresentation(bytes);
    return representation;
  }
}
