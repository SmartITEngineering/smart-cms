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
import com.smartitengineering.cms.api.exception.InvalidTemplateException;
import com.smartitengineering.cms.api.workspace.RepresentationTemplate;
import com.smartitengineering.cms.api.content.template.RepresentationGenerator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Map;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class VelocityRepresentationGenerator extends AbstractTypeRepresentationGenerator {

  @Override
  public RepresentationGenerator getGenerator(RepresentationTemplate template) throws InvalidTemplateException {
    return new VelocityTemplateRepresentationGenerator(template.getTemplate());
  }

  static class VelocityTemplateRepresentationGenerator implements RepresentationGenerator {

    private final SimpleNode simpleNode;
    private final VelocityContext ctx = new VelocityContext();
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    public VelocityTemplateRepresentationGenerator(byte[] templateData) throws InvalidTemplateException {
      try {
        this.simpleNode = RuntimeSingleton.parse(new InputStreamReader(new ByteArrayInputStream(templateData)),
                                                 "some.vm");
      }
      catch (Exception ex) {
        logger.warn(ex.getMessage(), ex);
        throw new InvalidTemplateException(ex);
      }
      if (simpleNode == null) {
        throw new InvalidTemplateException();
      }
    }

    @Override
    public String getRepresentationForContent(Content content, Map<String, String> params) {
      StringWriter writer = new StringWriter();
      ctx.put("content", content);
      ctx.put("params", params);
      try {
        RuntimeSingleton.getRuntimeInstance().render(ctx, writer, "some.vm", simpleNode);
      }
      catch (IOException ex) {
        throw new RuntimeException(ex);
      }
      return writer.toString();
    }
  }
}
