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

import com.smartitengineering.cms.api.content.Content;
import com.smartitengineering.cms.api.content.Field;
import com.smartitengineering.cms.api.content.template.FieldValidator;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.type.ResourceUri;
import com.smartitengineering.cms.api.type.ValidatorDef;
import com.smartitengineering.cms.api.workspace.ValidatorTemplate;
import com.smartitengineering.cms.spi.content.ValidatorProvider;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class ValidatorProviderImpl implements ValidatorProvider {

  protected final transient Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public boolean isValidField(Content content, Field field) {
    if (content == null || field == null) {
      logger.info("Content or field is null or blank! Returning true");
      return true;
    }
    Collection<ValidatorDef> validatorDefs = field.getFieldDef().getCustomValidators();
    if (logger.isDebugEnabled()) {
      logger.debug("Number of validator definitions for " + field.getName() + " is " + validatorDefs.size());
    }
    boolean valid = true;
    for (ValidatorDef validatorDef : validatorDefs) {
      if (validatorDef == null) {
        logger.info("Validator def is null, returning true!");
        continue;
      }
      final ResourceUri uri = validatorDef.getUri();
      if (uri.getType().equals(ResourceUri.Type.EXTERNAL)) {
        logger.warn("External resource URI is not yet handled! Returning true");
        continue;
      }
      FieldValidator validator =
                     SmartContentAPI.getInstance().getWorkspaceApi().getFieldValidator(content.getContentId().
          getWorkspaceId(), uri.getValue());
      if (validator == null) {
        logger.info("Validator template is null, returning true!");
        continue;
      }
      valid = valid && validator.isValidFieldValue(field, validatorDef.getParameters());
    }
    return valid;
  }

  @Override
  public boolean isValidTemplate(ValidatorTemplate template) {
    if (template == null) {
      logger.info("Validator template is null!");
      return false;
    }
    return SmartContentAPI.getInstance().getWorkspaceApi().getFieldValidator(template) != null;
  }
}
