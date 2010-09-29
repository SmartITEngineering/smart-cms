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

import com.smartitengineering.cms.api.common.TemplateType;
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.workspace.RepresentationTemplate;
import com.smartitengineering.cms.api.workspace.VariationTemplate;
import com.smartitengineering.cms.api.workspace.Workspace;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.cms.spi.SmartContentSPI;
import com.smartitengineering.cms.spi.type.PersistentContentTypeReader;
import com.smartitengineering.cms.spi.workspace.PersistableWorkspace;
import com.smartitengineering.cms.spi.workspace.WorkspaceService;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 *
 * @author imyousuf
 */
public class WorkspaceServiceImpl extends AbstractWorkspaceService implements WorkspaceService {

  public PersistentContentTypeReader getContentTypeReader() {
    return contentTypeReader;
  }

  @Override
  public Workspace create(WorkspaceId workspaceId) throws IllegalArgumentException {
    PersistableWorkspace workspace = SmartContentSPI.getInstance().getPersistableDomainFactory().
        createPersistentWorkspace();
    workspace.setCreationDate(new Date());
    workspace.setId(workspaceId);
    commonWriteDao.save(adapter.convert(workspace));
    return workspace;
  }

  @Override
  public Workspace load(WorkspaceId workspaceId) {
    return adapter.convertInversely(getByIdWorkspaceOnly(workspaceId));
  }

  @Override
  public Workspace delete(WorkspaceId workspaceId) {
    Workspace workspace = load(workspaceId);
    if (workspace == null) {
      throw new IllegalArgumentException("No workspace found with workspaceId " + workspaceId);
    }
    commonWriteDao.delete(adapter.convert(workspace));
    return workspace;
  }

  @Override
  public Collection<Workspace> getWorkspaces() {
    final List<PersistentWorkspace> list = commonReadDao.getList();
    if (list == null || list.isEmpty()) {
      return Collections.emptyList();
    }
    return Collections.unmodifiableCollection(adapter.convertInversely(
        list.toArray(new PersistentWorkspace[list.size()])));
  }

  @Override
  public Collection<ContentType> getContentDefintions(WorkspaceId workspaceId) {
    return Collections.unmodifiableCollection(getContentTypeReader().getByWorkspace(workspaceId));
  }

  @Override
  public Collection<WorkspaceId> getFriendlies(WorkspaceId workspaceId) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void addFriend(WorkspaceId to, WorkspaceId workspaceId) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void removeFriend(WorkspaceId from, WorkspaceId workspaceId) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public RepresentationTemplate putRepresentationTemplate(WorkspaceId workspaceId, String name,
                                                          TemplateType templateType, byte[] data) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public RepresentationTemplate getRepresentationTemplate(WorkspaceId workspaceId, String name) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public VariationTemplate putVariationTemplate(WorkspaceId workspaceId, String name, TemplateType templateType,
                                                byte[] data) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public VariationTemplate getVariationTemplate(WorkspaceId workspaceId, String name) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void deleteRepresentation(RepresentationTemplate template) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void deleteVariation(VariationTemplate template) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
