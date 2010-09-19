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
package com.smartitengineering.cms.spi.impl.type;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.smartitengineering.dao.impl.hbase.spi.impl.SchemaInfoProviderBaseConfig;

/**
 *
 * @author imyousuf
 */
@Singleton
public class ContentTypeSchemaBaseConfigProvider implements
    Provider<SchemaInfoProviderBaseConfig<PersistableContentType>> {

  @Override
  public SchemaInfoProviderBaseConfig<PersistableContentType> get() {
    SchemaInfoProviderBaseConfig<PersistableContentType> config = new SchemaInfoProviderBaseConfig();
    return config;
  }
}
