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

import com.smartitengineering.cms.spi.impl.AbstractVariationProvider;
import com.smartitengineering.cms.api.content.Content;
import com.smartitengineering.cms.api.content.Field;
import com.smartitengineering.cms.api.content.MutableVariation;
import com.smartitengineering.cms.api.content.Variation;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.workspace.VariationTemplate;
import com.smartitengineering.cms.spi.content.VariationProvider;
import java.util.Date;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author imyousuf
 */
public class VariationProviderImpl extends AbstractVariationProvider implements VariationProvider {

  @Override
  public Variation getVariation(String varName, Content content, Field field) {
    if (StringUtils.isBlank(varName) || content == null || field == null) {
      logger.info("Variation name or content or field is null or blank!");
      return null;
    }
    final MutableVariation variation = getVariation(content, field, varName);
    variation.setLastModifiedDate(new Date());
    return variation;
  }

  @Override
  public boolean isValidTemplate(VariationTemplate variationTemplate) {
    if (variationTemplate == null) {
      logger.info("Representation template is null!");
      return false;
    }
    return SmartContentAPI.getInstance().getWorkspaceApi().getVariationGenerator(variationTemplate) != null;
  }
}
