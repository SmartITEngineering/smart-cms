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
import com.smartitengineering.cms.api.content.Field;
import com.smartitengineering.cms.api.content.MutableVariation;
import com.smartitengineering.cms.api.content.template.VariationGenerator;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.type.FieldDef;
import com.smartitengineering.cms.api.type.VariationDef;
import java.io.InputStream;
import java.io.Reader;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class AbstractVariationProvider {

  protected final transient Logger logger = LoggerFactory.getLogger(getClass());

  protected MutableVariation getVariation(Content content, Field field, String variationName) {
    if (logger.isInfoEnabled()) {
      logger.info("Parameters: " + content + " " + field + " " + variationName);
    }
    if (field == null) {
      return null;
    }
    final FieldDef fieldDef = field.getFieldDef();
    if (fieldDef == null) {
      return null;
    }
    final VariationDef varDef = fieldDef.getVariations().get(variationName);
    if (varDef == null) {
      return null;
    }
    VariationGenerator generator;
    generator = SmartContentAPI.getInstance().getWorkspaceApi().getVariationGenerator(content.getContentId().
        getWorkspaceId(), varDef.getResourceUri().getValue());
    if (generator == null) {
      if (logger.isInfoEnabled()) {
        logger.info("Generator not available!");
      }
      return null;
    }
    final Object variationForField = generator.getVariationForField(field, varDef.getParameters());
    final byte[] bytes;
    if (variationForField instanceof String) {
      bytes = StringUtils.getBytesUtf8((String) variationForField);
    }
    else if (variationForField instanceof byte[]) {
      bytes = (byte[]) variationForField;
    }
    else if (variationForField instanceof InputStream) {
      try {
        bytes = IOUtils.toByteArray((InputStream) variationForField);
      }
      catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    }
    else if (variationForField instanceof Reader) {
      try {
        bytes = IOUtils.toByteArray((Reader) variationForField);
      }
      catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    }
    else {
      bytes = StringUtils.getBytesUtf8(variationForField.toString());
    }
    MutableVariation variation = SmartContentAPI.getInstance().getContentLoader().createMutableVariation(content.
        getContentId(), fieldDef);
    variation.setName(variationName);
    variation.setMimeType(varDef.getMIMEType());
    variation.setVariation(bytes);
    return variation;
  }
}
