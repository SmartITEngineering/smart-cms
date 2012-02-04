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
import com.smartitengineering.cms.api.exception.InvalidTemplateException;
import com.smartitengineering.cms.api.workspace.VariationTemplate;
import com.smartitengineering.cms.api.content.template.VariationGenerator;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.Semaphore;
import org.apache.commons.io.IOUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class VelocityVariationGenerator extends AbstractTypeVariationGenerator {

  private static final Semaphore MUTEX = new Semaphore(1);

  @Override
  public VariationGenerator getGenerator(VariationTemplate template) throws InvalidTemplateException {
    return new VelocityTemplateVariationGenerator(template.getTemplate());
  }

  static class VelocityTemplateVariationGenerator implements VariationGenerator {

    private final String templateData;
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    public VelocityTemplateVariationGenerator(byte[] templateData) throws InvalidTemplateException {
      try {
        final InputStreamReader inputStreamReader = new InputStreamReader(new ByteArrayInputStream(templateData));
        this.templateData = IOUtils.toString(inputStreamReader);
        IOUtils.closeQuietly(inputStreamReader);
        if (logger.isDebugEnabled()) {
          logger.debug("Template data " + this.templateData);
        }
      }
      catch (Exception ex) {
        logger.warn(ex.getMessage(), ex);
        throw new InvalidTemplateException(ex);
      }
    }

    @Override
    public String getVariationForField(Field field, Map<String, String> params) {
      final VelocityContext ctx = new VelocityContext();
      StringWriter writer = new StringWriter();
      ctx.put("field", field);
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
        VelocityEngine engine = new VelocityEngine();
        final Reader templateReader = new StringReader(this.templateData);
        if (!engine.evaluate(ctx, writer, "some.vm", templateReader)) {
          throw new IllegalStateException("Invalid template!", new InvalidTemplateException());
        }
        IOUtils.closeQuietly(writer);
      }
      catch (Exception ex) {
        throw new RuntimeException(ex);
      }
      finally {
        try {
          MUTEX.release();
        }
        catch (Exception ex) {
          throw new RuntimeException(ex);
        }
      }
      return writer.toString();
    }
  }
}
