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
package com.smartitengineering.cms.spi.impl.workspace;

import com.google.inject.Inject;
import com.smartitengineering.cms.api.workspace.Workspace;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.cms.spi.type.PersistentContentTypeReader;
import com.smartitengineering.cms.spi.workspace.WorkspaceService;
import com.smartitengineering.dao.common.CommonReadDao;
import com.smartitengineering.dao.common.CommonWriteDao;
import com.smartitengineering.dao.common.queryparam.MatchMode;
import com.smartitengineering.dao.common.queryparam.QueryParameter;
import com.smartitengineering.dao.common.queryparam.QueryParameterFactory;
import com.smartitengineering.util.bean.adapter.GenericAdapter;

/**
 *
 * @author imyousuf
 */
abstract class AbstractWorkspaceService implements WorkspaceService {

  @Inject
  protected PersistentContentTypeReader contentTypeReader;
  @Inject
  protected CommonReadDao<PersistentWorkspace, WorkspaceId> commonReadDao;
  @Inject
  protected CommonWriteDao<PersistentWorkspace> commonWriteDao;
  @Inject
  protected GenericAdapter<Workspace, PersistentWorkspace> adapter;

  protected PersistentWorkspace getByIdWorkspaceOnly(WorkspaceId workspaceId) {
    if (workspaceId == null) {
      return null;
    }
    QueryParameter idParam = QueryParameterFactory.getStringLikePropertyParam("id", workspaceId.toString(),
                                                                              MatchMode.EXACT);
    QueryParameter projection = QueryParameterFactory.getPropProjectionParam("workspace");
    return commonReadDao.getSingle(idParam, projection);
  }

  protected PersistentWorkspace getById(WorkspaceId workspaceId) {
    if (workspaceId == null) {
      return null;
    }
    return commonReadDao.getById(workspaceId);
  }
}
