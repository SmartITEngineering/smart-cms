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
package com.smartitengineering.cms.api.impl.type;

import com.smartitengineering.cms.api.type.MutableSearchDef;
import com.smartitengineering.cms.api.type.SearchDef;

/**
 *
 * @author kaisar
 */
public class SearchDefImpl implements MutableSearchDef, SearchDef {

  private boolean indexed;
  private boolean stored;
  private String boostConfig;

  @Override
  public void setIndexed(boolean indexed) {
    this.indexed = indexed;
  }

  @Override
  public void setStored(boolean stored) {
    this.stored = stored;
  }

  @Override
  public void setBoostConfig(String boostConfig) {
    this.boostConfig = boostConfig;
  }

  @Override
  public boolean isIndexed() {
    return this.indexed;
  }

  @Override
  public boolean isStored() {
    return this.stored;
  }

  @Override
  public String getBoostConfig() {
    return this.boostConfig;
  }
}
