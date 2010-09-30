/*
 *
 * This is a simple Content Management System (CMS)
 * Copyright (C) 2009  Imran M Yousuf (imyousuf@smartitengineering.com)
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
package com.smartitengineering.cms.spi.persistence;

import com.smartitengineering.cms.api.common.PersistentWriter;
import com.smartitengineering.cms.api.type.MutableContentType;
import java.util.Map;

/**
 * A registrar for beans that can be persisted. The idea is to register all
 * bean persistence service providers in this registrar.
 * @author imyousuf
 */
public interface PersistentServiceRegistrar {

  /**
   * An operation for retrieving the concrete implementation of persistent
   * service implementaion for the given persistable API bean.
   * @param <T> Should represent the class to be used in concrete SPI
   *					  implementations. For example, {@link MutableContentType}
   * @param writerClass The class to look for in the registrar.
   * @return Service for persisting the bean.
   * @see SmartSPI#getPersistentService(java.lang.Class)
   */
  public <T extends PersistentWriter> PersistentService<T> getPersistentService(Class<T> writerClass);

  /**
   * Retrieve the registry of all persistent service implementations for
   * configured PersistentWriter children.
   * @return An unmodifieable version of the registry.
   */
  public Map<Class, PersistentService> getRegister();
}
