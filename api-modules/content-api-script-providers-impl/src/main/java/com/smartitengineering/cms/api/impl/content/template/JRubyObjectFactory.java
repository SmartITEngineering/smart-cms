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

import com.smartitengineering.cms.api.exception.InvalidTemplateException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.jruby.embed.ScriptingContainer;

/**
 *
 * @author imyousuf
 */
public class JRubyObjectFactory {

  private final ScriptingContainer scriptingContainer = new ScriptingContainer();
  private final static JRubyObjectFactory OBJECT_FACTORY = new JRubyObjectFactory();

  public static JRubyObjectFactory getInstance() {
    return OBJECT_FACTORY;
  }

  private JRubyObjectFactory() {
  }

  public synchronized <T> T getObjectFromScript(byte[] script, Class<? extends T> clazz) throws InvalidTemplateException {
    ByteArrayInputStream inputStream = new ByteArrayInputStream(script);
    final Object receiver;
    try {
      receiver = scriptingContainer.runScriptlet(IOUtils.toString(inputStream));
    }
    catch (IOException ex) {
      throw new InvalidTemplateException(ex);
    }
    final T generator = scriptingContainer.getInstance(receiver, clazz);
    return generator;
  }
}
