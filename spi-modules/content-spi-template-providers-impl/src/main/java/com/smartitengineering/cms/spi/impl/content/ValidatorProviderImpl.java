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
import com.smartitengineering.cms.api.content.Content;
import com.smartitengineering.cms.api.content.Field;
import com.smartitengineering.cms.api.exception.InvalidTemplateException;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.type.ResourceUri;
import com.smartitengineering.cms.api.type.ValidatorDef;
import com.smartitengineering.cms.api.type.ValidatorType;
import com.smartitengineering.cms.api.workspace.ValidatorTemplate;
import com.smartitengineering.cms.spi.content.ValidatorProvider;
import com.smartitengineering.cms.api.content.template.TypeFieldValidator;
import java.util.Collection;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class ValidatorProviderImpl implements ValidatorProvider {

  protected final transient Logger logger = LoggerFactory.getLogger(getClass());
  @Inject
  private Map<ValidatorType, TypeFieldValidator> generators;

  @Override
  public boolean isValidField(Content content, Field field) {
    if (content == null || field == null) {
      logger.info("Content or field is null or blank! Returning true");
      return true;
    }
    Collection<ValidatorDef> validatorDefs = field.getFieldDef().getCustomValidators();
    if (logger.isInfoEnabled()) {
      logger.info("Number of validator definitions for " + field.getName() + " is " + validatorDefs.size());
    }
    boolean valid = true;
    for (ValidatorDef validatorDef : validatorDefs) {
      if (validatorDef == null) {
        logger.info("Validator def is null, returning true!");
        continue;
      }
      if (validatorDef.getUri().getType().equals(ResourceUri.Type.EXTERNAL)) {
        logger.warn("External resource URI is not yet handled! Returning true");
        continue;
      }
      ValidatorTemplate validatorTemplate =
                        SmartContentAPI.getInstance().getWorkspaceApi().getValidatorTemplate(content.getContentId().
          getWorkspaceId(), validatorDef.getUri().getValue());
      if (validatorTemplate == null) {
        logger.info("Validator template is null, returning true!");
        continue;
      }
      TypeFieldValidator generator = generators.get(validatorTemplate.getTemplateType());
      if (generator == null) {
        logger.info("Validator generator is null, returning true!");
        continue;
      }
      if (logger.isInfoEnabled()) {
        logger.info("Using custom validators with parameters " + validatorDef.getParameters());
      }
      valid = valid && generator.isValid(validatorTemplate, field, validatorDef.getParameters());
    }
    return valid;
  }

  @Override
  public boolean isValidTemplate(ValidatorTemplate template) {
    if (template == null) {
      logger.info("Validator template is null!");
      return false;
    }
    TypeFieldValidator generator = generators.get(template.getTemplateType());
    if (generator == null) {
      logger.info("Validator generator is null!");
      return false;
    }
    try {
      return generator.getValidator(template) != null;
    }
    catch (InvalidTemplateException ex) {
      logger.debug(ex.getMessage(), ex);
      return false;
    }
  }
}
