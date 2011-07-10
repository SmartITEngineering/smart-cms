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
package com.smartitengineering.cms.spi.impl.hbase;

import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.codec.binary.StringUtils;
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

  private static final transient Logger logger = LoggerFactory.getLogger(Utils.class);

  private Utils() {
  }

  public static byte[] toBytes(final Date date) {
    if (date == null) {
      return new byte[0];
    }
    try {
      return StringUtils.getBytesUtf8(DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.format(date));
    }
    catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public static Date toDate(final byte[] dateBytes) {
    try {
      String dateString = StringUtils.newStringUtf8(dateBytes);
      return DateUtils.parseDate(dateString, new String[]{DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.getPattern()});
    }
    catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public static void organizeByPrefix(Map<byte[], byte[]> fieldMap, Map<String, Map<String, byte[]>> fieldsByName,
                                      char separator) {
    logger.info("Organize by their prefix so that each field cells can be processed at once");
    for (Entry<byte[], byte[]> entry : fieldMap.entrySet()) {
      final String key = Bytes.toString(entry.getKey());
      final int indexOfFirstColon = key.indexOf(separator);
      if (indexOfFirstColon > -1) {
        final String fieldName = key.substring(0, indexOfFirstColon);
        final byte[] fieldNameBytes = Bytes.toBytes(fieldName);
        if (Bytes.startsWith(entry.getKey(), fieldNameBytes)) {
          Map<String, byte[]> fieldCells = fieldsByName.get(fieldName);
          if (fieldCells == null) {
            fieldCells = new LinkedHashMap<String, byte[]>();
            fieldsByName.put(fieldName, fieldCells);
          }
          fieldCells.put(Bytes.toString(entry.getKey()), entry.getValue());
        }
      }
      else {
        fieldsByName.put(key, Collections.singletonMap(Bytes.toString(entry.getKey()), entry.getValue()));
      }
    }
  }

  public static void organizeByPrefixOnString(Map<String, byte[]> fieldMap,
                                              Map<String, Map<String, byte[]>> fieldsByName, char separator) {
    logger.info("Organize by their prefix so that each field cells can be processed at once");
    for (Entry<String, byte[]> entry : fieldMap.entrySet()) {
      final String key = entry.getKey();
      final int indexOfFirstColon = key.indexOf(separator);
      if (indexOfFirstColon > -1) {
        final String fieldName = key.substring(0, indexOfFirstColon);
        Map<String, byte[]> fieldCells = fieldsByName.get(fieldName);
        if (fieldCells == null) {
          fieldCells = new LinkedHashMap<String, byte[]>();
          fieldsByName.put(fieldName, fieldCells);
        }
        fieldCells.put(entry.getKey(), entry.getValue());
      }
      else {
        fieldsByName.put(key, Collections.singletonMap(entry.getKey(), entry.getValue()));
      }
    }
  }

  public static String readStringInUTF8(DataInput in) throws IOException, UnsupportedEncodingException {
    int allocationBlockSize = 2000;
    int capacity = allocationBlockSize;
    int length = 0;
    ByteBuffer buffer = ByteBuffer.allocate(allocationBlockSize);
    boolean notEof = true;
    while (notEof) {
      try {
        buffer.put(in.readByte());
        if (++length >= capacity) {
          capacity += allocationBlockSize;
          buffer.limit(capacity);
        }
      }
      catch (EOFException ex) {
        notEof = false;
      }
    }
    String string = StringUtils.newStringUtf8(Arrays.copyOf(buffer.array(), length));
    return string;
  }
}
