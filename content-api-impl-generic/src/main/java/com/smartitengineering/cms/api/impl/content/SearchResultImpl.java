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
package com.smartitengineering.cms.api.impl.content;

import com.smartitengineering.cms.api.content.Content;
import com.smartitengineering.cms.api.content.SearchResult;
import java.util.Collection;

/**
 *
 * @author imyousuf
 */
public class SearchResultImpl implements SearchResult {

  private long totalResultsCount;
  private Collection<Content> result;

  @Override
  public long getTotalResultsCount() {
    return totalResultsCount;
  }

  @Override
  public Collection<Content> getResult() {
    return result;
  }

  public void setResult(Collection<Content> result) {
    this.result = result;
  }

  public void setTotalResultsCount(long totalResultsCount) {
    this.totalResultsCount = totalResultsCount;
  }
}
