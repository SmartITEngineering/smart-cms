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
package com.smartitengineering.cms.spi.impl.content.guice;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.smartitengineering.cms.spi.impl.content.template.persistent.PersistentVariation;
import com.smartitengineering.dao.impl.hbase.spi.FilterConfigs;
import com.smartitengineering.dao.impl.hbase.spi.impl.JsonConfigLoader;
import java.io.IOException;

/**
 *
 * @author imyousuf
 */
@Singleton
public class VariationFilterConfigsProvider implements Provider<FilterConfigs<PersistentVariation>> {

  @Override
  public FilterConfigs<PersistentVariation> get() {
    try {
      return JsonConfigLoader.parseJsonAsFilterConfigMap(getClass().getClassLoader().
          getResourceAsStream("com/smartitengineering/cms/spi/impl/content/VariationFilterConfigs.json"));
    }
    catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }
}
