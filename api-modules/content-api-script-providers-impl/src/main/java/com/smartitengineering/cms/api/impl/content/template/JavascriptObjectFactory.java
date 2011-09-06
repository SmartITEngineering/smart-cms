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
import org.apache.commons.codec.binary.StringUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;

/**
 *
 * @author imyousuf
 */
public class JavascriptObjectFactory {

  private final Context context = Context.enter();
  private final Scriptable scriptable = context.initStandardObjects();

  private JavascriptObjectFactory() {
  }
  private static final JavascriptObjectFactory OBJECT_FACTORY = new JavascriptObjectFactory();

  public static JavascriptObjectFactory getInstance() {
    return OBJECT_FACTORY;
  }

  public synchronized <T> T getObjectFromScript(byte[] data, Class<? extends T> clazz) throws InvalidTemplateException {
    try {
      String script = StringUtils.newStringUtf8(data);
      Script myScript = context.compileString(script, "source", 0, null);
      Object object = myScript.exec(context, scriptable);
      T generator = (T) Context.jsToJava(object, clazz);
      return generator;
    }
    catch (Exception ex) {
      throw new InvalidTemplateException(ex);
    }
  }
}
