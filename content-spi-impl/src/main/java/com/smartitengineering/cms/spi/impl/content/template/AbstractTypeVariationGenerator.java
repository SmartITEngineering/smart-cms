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
package com.smartitengineering.cms.spi.impl.content.template;

import com.smartitengineering.cms.api.content.Field;
import com.smartitengineering.cms.api.content.MutableVariation;
import com.smartitengineering.cms.api.content.Variation;
import com.smartitengineering.cms.api.exception.InvalidTemplateException;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.workspace.VariationTemplate;
import com.smartitengineering.cms.spi.content.template.TypeVariationGenerator;
import com.smartitengineering.cms.spi.content.template.VariationGenerator;
import org.apache.commons.codec.binary.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public abstract class AbstractTypeVariationGenerator implements TypeVariationGenerator {

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public Variation getVariation(VariationTemplate template, Field field) {
    VariationGenerator generator;
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
    final String representationForContent = generator.getVariationForField(field);
    MutableVariation variation =
                     SmartContentAPI.getInstance().getContentLoader().createMutableVariation();
    final String name = template.getName();
    variation.setName(name);
    variation.setMimeType(field.getFieldDef().getVariations().get(name).getMIMEType());
    variation.setVariation(StringUtils.getBytesUtf8(representationForContent));
    return variation;
  }
}
