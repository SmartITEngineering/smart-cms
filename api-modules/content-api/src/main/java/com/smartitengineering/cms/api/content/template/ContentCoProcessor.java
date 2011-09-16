/*
 *
 * This is a simple Content Management System (CMS)
 * Copyright (C) 2011  Imran M Yousuf (imyousuf@smartitengineering.com)
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
package com.smartitengineering.cms.api.content.template;

import com.smartitengineering.cms.api.content.MutableContent;
import java.util.Map;

/**
 * This API defines a programmatic API for CMS users to plug custom code to process contents in runtime. By processing
 * it does not limit on what you can do with it, e.g. from changing Content ID, add/remove/modify field(s) etc.
 * @author imyousuf
 * @since 0.1
 */
public interface ContentCoProcessor {

  /**
   * Process the content needed
   * @param content The content to process
   */
  public void processContent(MutableContent content, Map<String, String> params);
}
