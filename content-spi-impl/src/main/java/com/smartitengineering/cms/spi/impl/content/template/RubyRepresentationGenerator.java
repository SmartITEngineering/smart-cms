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

import com.smartitengineering.cms.api.exception.InvalidTemplateException;
import com.smartitengineering.cms.api.workspace.RepresentationTemplate;
import com.smartitengineering.cms.spi.content.template.RepresentationGenerator;
import com.smartitengineering.cms.spi.content.template.TypeRepresentationGenerator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.jruby.embed.ScriptingContainer;

/**
 *
 * @author imyousuf
 */
public class RubyRepresentationGenerator extends AbstractTypeRepresentationGenerator implements
    TypeRepresentationGenerator {

  private final ScriptingContainer scriptingContainer = new ScriptingContainer();

  @Override
  public RepresentationGenerator getGenerator(RepresentationTemplate template) throws InvalidTemplateException {
    ByteArrayInputStream inputStream = new ByteArrayInputStream(template.getTemplate());
    final Object receiver;
    try {
      receiver = scriptingContainer.runScriptlet(IOUtils.toString(inputStream));
    }
    catch (IOException ex) {
      throw new InvalidTemplateException(ex);
    }
    final RepresentationGenerator generator = scriptingContainer.getInstance(receiver, RepresentationGenerator.class);
    return generator;
  }
}
