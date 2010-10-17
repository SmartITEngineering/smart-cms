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
package com.smartitengineering.cms.spi.impl.content;

import com.google.inject.Inject;
import com.smartitengineering.cms.api.common.TemplateType;
import com.smartitengineering.cms.api.content.Content;
import com.smartitengineering.cms.api.content.Field;
import com.smartitengineering.cms.api.content.Variation;
import com.smartitengineering.cms.api.exception.InvalidTemplateException;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.type.ResourceUri;
import com.smartitengineering.cms.api.type.VariationDef;
import com.smartitengineering.cms.api.workspace.VariationTemplate;
import com.smartitengineering.cms.spi.content.VariationProvider;
import com.smartitengineering.cms.spi.content.template.TypeVariationGenerator;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class VariationProviderImpl implements VariationProvider {

  protected final Logger logger = LoggerFactory.getLogger(getClass());
  @Inject
  private Map<TemplateType, TypeVariationGenerator> generators;

  @Override
  public Variation getVariation(String varName, Content content, Field field) {
    if (StringUtils.isBlank(varName) || content == null || field == null) {
      logger.info("Variation name or content or field is null or blank!");
      return null;
    }
    VariationDef variationDef = field.getFieldDef().getVariations().get(varName);
    if (variationDef == null) {
      logger.info("Representation def is null!");
      return null;
    }
    if (variationDef.getResourceUri().getType().equals(ResourceUri.Type.EXTERNAL)) {
      logger.warn("External resource URI is not yet handled!");
      return null;
    }
    VariationTemplate variationTemplate =
                      SmartContentAPI.getInstance().getWorkspaceApi().getVariationTemplate(content.getContentId().
        getWorkspaceId(), variationDef.getResourceUri().getValue());
    if (variationTemplate == null) {
      logger.info("Representation template is null!");
      return null;
    }
    TypeVariationGenerator generator = generators.get(variationTemplate.getTemplateType());
    if (generator == null) {
      logger.info("Representation generator is null!");
      return null;
    }
    return generator.getVariation(variationTemplate, field);
  }

  @Override
  public boolean isValidTemplate(VariationTemplate variationTemplate) {
    if (variationTemplate == null) {
      logger.info("Representation template is null!");
      return false;
    }
    TypeVariationGenerator generator = generators.get(variationTemplate.getTemplateType());
    if (generator == null) {
      logger.info("Representation generator is null!");
      return false;
    }
    try {
      return generator.getGenerator(variationTemplate) != null;
    }
    catch (InvalidTemplateException ex) {
      logger.debug(ex.getMessage(), ex);
      return false;
    }
  }
}
