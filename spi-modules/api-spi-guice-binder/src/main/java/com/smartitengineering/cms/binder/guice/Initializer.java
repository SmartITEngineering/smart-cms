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
package com.smartitengineering.cms.binder.guice;

import com.smartitengineering.util.bean.guice.GuiceUtil;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author imyousuf
 */
public final class Initializer {

  public static final String DEFAULT_CONF = "com/smartitengineering/cms/binder/guice/spi-modules.properties";
  public static final String DEFAULT_CONF_PROP = "com.smartitengineering.cms.conf";

  private Initializer() {
  }

  public static void init() {
    init(null);
  }

  public static void init(String confSmartLocation) {
    if (StringUtils.isNotBlank(confSmartLocation)) {
      GuiceUtil.getInstance(confSmartLocation).register();
    }
    else if (StringUtils.isNotBlank(System.getProperty(DEFAULT_CONF_PROP))) {
      GuiceUtil.getInstance(System.getProperty(DEFAULT_CONF_PROP)).register();
    }
    else {
      GuiceUtil.getInstance(DEFAULT_CONF).register();
    }
  }
}
