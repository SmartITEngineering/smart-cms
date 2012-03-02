/*
 *
 * This is a simple Content Management System (CMS)
 * Copyright (C) 2012  Imran M Yousuf (imyousuf@smartitengineering.com)
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

import com.smartitengineering.cms.api.content.template.ContentCoProcessor;
import com.smartitengineering.cms.api.exception.InvalidTemplateException;
import com.smartitengineering.cms.api.workspace.ContentCoProcessorTemplate;
import com.smartitengineering.cms.spi.content.template.ContentCoProcessorGenerator;
import org.apache.commons.codec.binary.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class PythonContentCoProcessorGenerator implements ContentCoProcessorGenerator {

  protected final transient Logger logger = LoggerFactory.getLogger(getClass());

  public ContentCoProcessor getGenerator(ContentCoProcessorTemplate template) throws InvalidTemplateException {
    try {
      return new JythonObjectFactory<ContentCoProcessor>(ContentCoProcessor.class,
                                                         StringUtils.newStringUtf8(template.getTemplate())).createObject();
    }
    catch (Exception ex) {
      logger.warn("Could not create Python based content co processor", ex);
      throw new InvalidTemplateException(ex);
    }
  }
}
