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
package com.smartitengineering.cms.spi.util;

import com.smartitengineering.cms.api.content.ContentId;
import com.smartitengineering.cms.spi.content.PersistableContent;
import com.smartitengineering.cms.spi.lock.Key;

/**
 *
 * @author imyousuf
 */
public final class LockUtil {

  private LockUtil() {
  }

  public static <T> String getLockKeyRepresentation(String prefix, T keySpecimen) {
    if (prefix == null || keySpecimen == null) {
      throw new IllegalArgumentException("Prefix and/or key specimen is null");
    }
    StringBuilder builder = new StringBuilder();
    builder.append(prefix).append(keySpecimen.toString());
    return builder.toString();
  }

  public static <T> Key getCommonLockKey(String prefix, T keySpeciment) {
    return new CommonLockKey(prefix, keySpeciment);
  }

  public static Key getContentLockKey(ContentId contentId) {
    return getCommonLockKey(PersistableContent.LOCK_KEY_PREFIX, contentId);
  }
}
