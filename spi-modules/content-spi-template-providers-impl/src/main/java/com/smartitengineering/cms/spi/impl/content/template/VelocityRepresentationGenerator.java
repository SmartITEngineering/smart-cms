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

import com.smartitengineering.cms.api.content.Content;
import com.smartitengineering.cms.api.exception.InvalidTemplateException;
import com.smartitengineering.cms.api.workspace.RepresentationTemplate;
import com.smartitengineering.cms.api.content.template.RepresentationGenerator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.Semaphore;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class VelocityRepresentationGenerator extends AbstractTypeRepresentationGenerator {

  private static final Semaphore MUTEX = new Semaphore(1);

  @Override
  public RepresentationGenerator getGenerator(RepresentationTemplate template) throws InvalidTemplateException {
    return new VelocityTemplateRepresentationGenerator(template.getTemplate());
  }

  static class VelocityTemplateRepresentationGenerator implements RepresentationGenerator {

    private final InputStreamReader inputStreamReader;
    private final VelocityContext ctx = new VelocityContext();
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    public VelocityTemplateRepresentationGenerator(byte[] templateData) throws InvalidTemplateException {
      try {
        inputStreamReader = new InputStreamReader(new ByteArrayInputStream(templateData));
      }
      catch (Exception ex) {
        logger.warn(ex.getMessage(), ex);
        throw new InvalidTemplateException(ex);
      }
    }

    @Override
    public String getRepresentationForContent(Content content, Map<String, String> params) {
      StringWriter writer = new StringWriter();
      ctx.put("content", content);
      if (params != null) {
        ctx.put("params", params);
      }
      try {
        MUTEX.acquire();
      }
      catch (Exception ex) {
        throw new RuntimeException(ex);
      }
      try {
        if (!RuntimeSingleton.getRuntimeServices().evaluate(ctx, writer, "some.vm", inputStreamReader)) {
          throw new IllegalStateException("Invalid template!", new InvalidTemplateException());
        }
      }
      catch (IOException ex) {
        throw new RuntimeException(ex);
      }
      finally {
        try {
          MUTEX.release();
        }
        catch (Exception ex) {
          throw new RuntimeException(ex);
        }
        return writer.toString();
      }
    }
  }
}