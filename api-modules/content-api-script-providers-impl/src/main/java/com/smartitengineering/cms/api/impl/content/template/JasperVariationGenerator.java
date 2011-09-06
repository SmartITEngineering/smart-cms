/*
 *
 * This is a simple Content Management System (CMS)
 * Copyright (C) 2011  Imran M Yousuf (imyousuf@smartitengineering.com)
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

import com.smartitengineering.cms.api.content.Field;
import com.smartitengineering.cms.api.exception.InvalidTemplateException;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.workspace.ResourceTemplate;
import com.smartitengineering.cms.api.workspace.VariationTemplate;
import com.smartitengineering.cms.api.content.template.VariationGenerator;
import com.smartitengineering.cms.api.impl.content.template.JasperRepresentationGenerator.AbstractJasperTemplateGenerator;
import java.util.Collections;
import java.util.Map;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

/**
 *
 * @author imyousuf
 */
public class JasperVariationGenerator extends AbstractTypeVariationGenerator {

  public VariationGenerator getGenerator(VariationTemplate template) throws InvalidTemplateException {
    return new JasperTemplateVariationGenerator(template);
  }

  static class JasperTemplateVariationGenerator extends AbstractJasperTemplateGenerator implements
      VariationGenerator {

    public JasperTemplateVariationGenerator(ResourceTemplate template) throws InvalidTemplateException {
      super(template);
    }

    @Override
    public byte[] getVariationForField(Field field, Map<String, String> params) {
      final JRDataSource jRBeanCollectionDataSource = new JRBeanCollectionDataSource(Collections.singleton(field));
      return generateReport(params, jRBeanCollectionDataSource);
    }

    @Override
    protected ResourceTemplate getInternalResourceTemplate(String value) {
      return SmartContentAPI.getInstance().getWorkspaceApi().getVariationTemplate(resourceTemplate.getWorkspaceId(),
                                                                                  value);
    }
  }
}
