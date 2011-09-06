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
package com.smartitengineering.cms.spi.workspace;

import com.smartitengineering.cms.api.common.TemplateType;
import com.smartitengineering.cms.api.content.ContentId;
import com.smartitengineering.cms.api.workspace.ValidatorTemplate;
import com.smartitengineering.cms.api.workspace.Workspace;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.workspace.RepresentationTemplate;
import com.smartitengineering.cms.api.workspace.VariationTemplate;
import com.smartitengineering.cms.api.factory.workspace.WorkspaceAPI.ResourceSortCriteria;
import com.smartitengineering.cms.api.type.ValidatorType;
import com.smartitengineering.cms.api.workspace.ContentCoProcessorTemplate;
import java.util.Collection;

/**
 *
 * @author imyousuf
 */
public interface WorkspaceService {

  public Workspace create(WorkspaceId workspaceId) throws IllegalArgumentException;

  public Workspace load(WorkspaceId workspaceId);

  public Workspace delete(WorkspaceId workspaceId);

  public Collection<Workspace> getWorkspaces();

  public Collection<ContentType> getContentDefintions(WorkspaceId workspaceId);

  public Collection<WorkspaceId> getFriendlies(WorkspaceId workspaceId);

  public void addFriend(WorkspaceId to, WorkspaceId... workspaceIds);

  public void removeFriend(WorkspaceId from, WorkspaceId workspaceId);

  public RepresentationTemplate putRepresentationTemplate(WorkspaceId workspaceId, String name,
                                                          TemplateType templateType, byte[] data);

  public RepresentationTemplate getRepresentationTemplate(WorkspaceId workspaceId, String name);

  public void deleteRepresentation(RepresentationTemplate template);

  public VariationTemplate putVariationTemplate(WorkspaceId workspaceId, String name, TemplateType templateType,
                                                byte[] data);

  public VariationTemplate getVariationTemplate(WorkspaceId workspaceId, String name);

  public void removeAllVariationTemplates(WorkspaceId workspaceId);

  public Collection<VariationTemplate> getVariationsWithoutData(WorkspaceId id, ResourceSortCriteria criteria);

  public void deleteVariation(VariationTemplate template);

  public void removeAllFriendlies(WorkspaceId workspaceId);

  public void removeAllRepresentationTemplates(WorkspaceId workspaceId);

  public Collection<RepresentationTemplate> getRepresentationsWithoutData(WorkspaceId id, ResourceSortCriteria criteria);

  public Collection<ContentId> getRootContents(WorkspaceId workspaceId);

  public void addRootContent(WorkspaceId to, ContentId... contentIds);

  public void removeRootContent(WorkspaceId from, ContentId contentId);

  public void removeAllRootContents(WorkspaceId workspaceId);

  public ValidatorTemplate getValidationTemplate(WorkspaceId workspaceId, String name);

  public void deleteValidator(ValidatorTemplate template);

  public ValidatorTemplate putValidatorTemplate(WorkspaceId to, String name, ValidatorType templateType,
                                                byte[] template);

  public Collection<ValidatorTemplate> getValidatorsWithoutData(WorkspaceId id,
                                                                ResourceSortCriteria resourceSortCriteria);

  public void removeAllValidatorTemplates(WorkspaceId workspaceId);

  public ContentCoProcessorTemplate putContentCoProcessorTemplate(WorkspaceId workspaceId, String name,
                                                                  TemplateType templateType, byte[] data);

  public ContentCoProcessorTemplate getContentCoProcessorTemplate(WorkspaceId workspaceId, String name);

  public void removeAllContentCoProcessorTemplates(WorkspaceId workspaceId);

  public Collection<ContentCoProcessorTemplate> getContentCoProcessorsWithoutData(WorkspaceId id,
                                                                                  ResourceSortCriteria criteria);

  public void deleteContentCoProcessor(ContentCoProcessorTemplate template);
}
