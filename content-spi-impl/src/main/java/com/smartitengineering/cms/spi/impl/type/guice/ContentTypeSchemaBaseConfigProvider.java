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
package com.smartitengineering.cms.spi.impl.type.guice;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.smartitengineering.cms.spi.impl.type.PersistentContentType;
import com.smartitengineering.dao.impl.hbase.spi.impl.JsonConfigLoader;
import com.smartitengineering.dao.impl.hbase.spi.impl.SchemaInfoProviderBaseConfig;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author imyousuf
 */
@Singleton
public class ContentTypeSchemaBaseConfigProvider implements
    Provider<SchemaInfoProviderBaseConfig<PersistentContentType>> {

  @Override
  public SchemaInfoProviderBaseConfig<PersistentContentType> get() {
    try {
      final InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(
          "com/smartitengineering/cms/spi/impl/type/ContentTypeSchemaBaseConfig.json");
      return JsonConfigLoader.parseJsonAsBaseConfig(resourceAsStream);
    }
    catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }
}
