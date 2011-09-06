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

import org.apache.commons.io.IOUtils;
import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PySystemState;
import org.python.core.util.FileUtil;
import org.python.util.PythonInterpreter;

/**
 *
 * @author imyousuf
 */
public class JythonObjectFactory<T> {

  private final Class interfaceType;
  private final PyObject klass;
  private final PythonInterpreter pythonInterpreter;

  // likely want to reuse PySystemState in some clever fashion since expensive to setup...
  public JythonObjectFactory(PythonInterpreter interpreter, Class<? extends T> interfaceType, String script) {
    this.interfaceType = interfaceType;
    pythonInterpreter = interpreter;
    pythonInterpreter.exec(FileUtil.wrap(IOUtils.toInputStream(script)));
    klass = pythonInterpreter.get("rep");
  }

  public T createObject() {
    return (T) klass.__call__().__tojava__(interfaceType);
  }

  public T createObject(Object arg1) {
    return (T) klass.__call__(Py.java2py(arg1)).__tojava__(interfaceType);
  }

  public T createObject(Object arg1, Object arg2) {
    return (T) klass.__call__(Py.java2py(arg1), Py.java2py(arg2)).__tojava__(interfaceType);
  }

  public T createObject(Object arg1, Object arg2, Object arg3) {
    return (T) klass.__call__(Py.java2py(arg1), Py.java2py(arg2), Py.java2py(arg3)).__tojava__(interfaceType);
  }

  public T createObject(Object args[], String keywords[]) {
    PyObject convertedArgs[] = new PyObject[args.length];
    for (int i = 0; i < args.length; i++) {
      convertedArgs[i] = Py.java2py(args[i]);
    }
    return (T) klass.__call__(convertedArgs, keywords).__tojava__(interfaceType);
  }

  public T createObject(Object... args) {
    return createObject(args, Py.NoKeywords);
  }
}
