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
import com.smartitengineering.dao.impl.hbase.spi.FilterConfig;
import com.smartitengineering.dao.impl.hbase.spi.FilterConfigs;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author imyousuf
 */
@Singleton
public class ContentTypeFilterConfigsProvider implements Provider<FilterConfigs<PersistableContentType>> {

  @Override
  public FilterConfigs<PersistableContentType> get() {
    FilterConfigs<PersistableContentType> configs = new FilterConfigs<PersistableContentType>();
    Map<String, FilterConfig> map = new HashMap<String, FilterConfig>();
    configs.setConfigs(map);
    return configs;
  }
}
