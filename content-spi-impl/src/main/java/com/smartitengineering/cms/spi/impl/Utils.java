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
package com.smartitengineering.cms.spi.impl;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public final class Utils {

  private static final Logger logger = LoggerFactory.getLogger(Utils.class);
  public static final String UTF_8_ENCODING = "UTF-8";

  private Utils() {
  }

  public static byte[] toBytes(final Date date) {
    if (date == null) {
      return new byte[0];
    }
    try {
      return DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.format(date).getBytes(UTF_8_ENCODING);
    }
    catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public static Date toDate(final byte[] dateBytes) {
    try {
      String dateString = new String(dateBytes, UTF_8_ENCODING);
      return DateUtils.parseDate(dateString, new String[]{DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.getPattern()});
    }
    catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public static void organizeByPrefix(Map<byte[], byte[]> fieldMap, Map<String, Map<byte[], byte[]>> fieldsByName,
                                      char separator) {
    logger.info("Organize by their prefix so that each field cells can be processed at once");
    for (Entry<byte[], byte[]> entry : fieldMap.entrySet()) {
      final String key = Bytes.toString(entry.getKey());
      final int indexOfFirstColon = key.indexOf(separator);
      final String fieldName = key.substring(0, indexOfFirstColon);
      final byte[] fieldNameBytes = Bytes.toBytes(fieldName);
      if (Bytes.startsWith(entry.getKey(), fieldNameBytes)) {
        Map<byte[], byte[]> fieldCells = fieldsByName.get(fieldName);
        if (fieldCells == null) {
          fieldCells = new LinkedHashMap<byte[], byte[]>();
          fieldsByName.put(fieldName, fieldCells);
        }
        fieldCells.put(entry.getKey(), entry.getValue());
      }
    }
  }
}
