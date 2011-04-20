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
package com.smartitengineering.cms.ws.common.utils;

import javax.xml.namespace.QName;

/**
 *
 * @author imyousuf
 */
public final class SimpleFeedExtensions {

  public static final String NAMESPACE_URI_CMS = "http://smartitengineering.com/xml/ns/cms";
  public static final String PREFIX_CMS = "cms";
  public final static QName CONTENT_TYPE_NAME_SPACE = new QName(NAMESPACE_URI_CMS, "contentTypeNamespace", PREFIX_CMS);
  public final static QName CONTENT_TYPE_NAME = new QName(NAMESPACE_URI_CMS, "contentTypeName", PREFIX_CMS);
  public final static QName WORKSPACE_NAME_SPACE = new QName(NAMESPACE_URI_CMS, "workspaceNamespace", PREFIX_CMS);
  public final static QName WORKSPACE_NAME = new QName(NAMESPACE_URI_CMS, "workspaceName", PREFIX_CMS);
  public final static QName CONTENT_ID_IN_WORKSPACAE = new QName(NAMESPACE_URI_CMS, "contentIdInWorkspace", PREFIX_CMS);

  private SimpleFeedExtensions() {
  }
}
