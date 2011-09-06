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
import groovy.lang.GroovyClassLoader;
import org.apache.commons.codec.binary.StringUtils;

/**
 *
 * @author imyousuf
 */
public class GroovyObjectFactory {

  private final GroovyClassLoader groovyClassLoader = new GroovyClassLoader(getClass().getClassLoader());
  private final static GroovyObjectFactory OBJECT_FACTORY = new GroovyObjectFactory();

  public static GroovyObjectFactory getInstance() {
    return OBJECT_FACTORY;
  }

  public synchronized <T> T getObjectFromScript(byte[] script, Class<? extends T> clazz) throws InvalidTemplateException {
    try {
      clazz = groovyClassLoader.parseClass(StringUtils.newStringUtf8(script));
    }
    catch (Exception ex) {
      throw new InvalidTemplateException(ex);
    }
    try {
      return clazz.newInstance();
    }
    catch (Exception ex) {
      throw new InvalidTemplateException(ex);
    }

  }
}
