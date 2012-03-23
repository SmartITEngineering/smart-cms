/*
 *
 * This is a simple Content Management System (CMS)
 * Copyright (C) 2012  Imran M Yousuf (imyousuf@smartitengineering.com)
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
package com.smartitengineering.cms.ws.resources;

import com.smartitengineering.util.bean.PropertiesLocator;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Semaphore;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public final class ResourcesConfig {

  private final Properties properties = new Properties();

  private ResourcesConfig() throws IOException {
    PropertiesLocator locator = new PropertiesLocator();
    locator.setSmartLocations("com/smartitengineering/cms/ws/resources/web-service-config.properties");
    locator.loadProperties(properties);
  }
  private static final Semaphore MUTEX = new Semaphore(1);
  private static final Logger LOGGER = LoggerFactory.getLogger(ResourcesConfig.class);
  private static ResourcesConfig config;

  public static ResourcesConfig getInstance() {
    if (config == null) {
      try {
        MUTEX.acquire();
      }
      catch (Exception ex) {
        LOGGER.warn("Could not attain Config creation lock", ex);
        return null;
      }
      try {
        if (config == null) {
          config = new ResourcesConfig();
        }
      }
      catch (Exception ex) {
        LOGGER.warn("Could create Config", ex);
      }
      finally {
        MUTEX.release();
      }
    }
    return config;
  }

  public Map<String, String> getAllProperties() {
    return Collections.unmodifiableMap(new LinkedHashMap(properties));
  }

  public int getContentHttpCacheControlMaxAge() {
    return NumberUtils.toInt(properties.getProperty("com.smartitengineering.cms.content.http.cache.maxAge"), 300);
  }

  public int getFieldHttpCacheControlMaxAge() {
    return NumberUtils.toInt(properties.getProperty("com.smartitengineering.cms.field.http.cache.maxAge"), 300);
  }

  public int getRepresentationHttpCacheControlMaxAge() {
    return NumberUtils.toInt(properties.getProperty("com.smartitengineering.cms.representation.http.cache.maxAge"), 900);
  }

  public int getVariationHttpCacheControlMaxAge() {
    return NumberUtils.toInt(properties.getProperty("com.smartitengineering.cms.variation.http.cache.maxAge"), 900);
  }
}
