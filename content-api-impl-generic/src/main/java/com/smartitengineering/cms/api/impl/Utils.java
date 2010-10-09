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
package com.smartitengineering.cms.api.impl;

import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 *
 * @author imyousuf
 */
public final class Utils {

  private Utils() {
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
    String string = new String(Arrays.copyOf(buffer.array(), length), "UTF-8");
    return string;
  }
}
