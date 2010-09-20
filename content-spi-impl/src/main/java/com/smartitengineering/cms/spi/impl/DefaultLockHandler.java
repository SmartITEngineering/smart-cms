/*
 *
 * This is a simple Content Management System (CMS)
 * Copyright (C) 2010 Imran M Yousuf (imyousuf@smartitengineering.com)
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

import com.smartitengineering.cms.api.common.Lock;
import com.smartitengineering.cms.spi.lock.Key;
import com.smartitengineering.cms.spi.lock.LockHandler;

/**
 *
 * @author imyousuf
 */
public class DefaultLockHandler implements LockHandler {

  @Override
  public Lock register(Key key) {
    return null;
  }

  @Override
  public void unregister(Key key) {
    return;
  }
}
