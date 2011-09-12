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
package com.smartitengineering.cms.api.factory.workspace;

import com.smartitengineering.cms.api.common.TemplateType;
import com.smartitengineering.cms.api.content.ContentId;
import com.smartitengineering.cms.api.content.template.ContentCoProcessor;
import com.smartitengineering.cms.api.type.ValidatorType;
import com.smartitengineering.cms.api.workspace.ContentCoProcessorTemplate;
import com.smartitengineering.cms.api.workspace.RepresentationTemplate;
import com.smartitengineering.cms.api.workspace.ResourceTemplate;
import com.smartitengineering.cms.api.workspace.ValidatorTemplate;
import com.smartitengineering.cms.api.workspace.VariationTemplate;
import com.smartitengineering.cms.api.workspace.Workspace;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

/**
 *
 * @author imyousuf
 */
public interface WorkspaceAPI {

  String getGlobalNamespace();

  WorkspaceId createWorkspaceId(String globalNamespace, String name);

  WorkspaceId createWorkspaceId(String name);

  WorkspaceId createWorkspace(String name);

  WorkspaceId createWorkspace(String globalNamespace, String name);

  WorkspaceId createWorkspace(WorkspaceId workspaceId);

  RepresentationTemplate putRepresentationTemplate(WorkspaceId to, String name, TemplateType templateType,
                                                   InputStream stream) throws
      IOException;

  RepresentationTemplate putRepresentationTemplate(WorkspaceId to, String name, TemplateType templateType, byte[] data);

  RepresentationTemplate getRepresentationTemplate(WorkspaceId id, String name);

  void delete(RepresentationTemplate template);

  void delete(VariationTemplate template);

  VariationTemplate putVariationTemplate(WorkspaceId to, String name, TemplateType templateType, InputStream stream)
      throws IOException;

  VariationTemplate putVariationTemplate(WorkspaceId to, String name, TemplateType templateType, byte[] data);

  VariationTemplate getVariationTemplate(WorkspaceId id, String name);

  void delete(ContentCoProcessorTemplate template);

  ContentCoProcessorTemplate putContentCoProcessorTemplate(WorkspaceId to, String name, TemplateType templateType,
                                                           InputStream stream) throws IOException;

  ContentCoProcessorTemplate putContentCoProcessorTemplate(WorkspaceId to, String name, TemplateType templateType,
                                                           byte[] data);

  ContentCoProcessorTemplate getContentCoProcessorTemplate(WorkspaceId id, String name);

  ContentCoProcessor getContentCoProcessor(WorkspaceId id, String name);

  WorkspaceId getWorkspaceIdIfExists(String name);

  WorkspaceId getWorkspaceIdIfExists(WorkspaceId workspaceId);

  Workspace getWorkspace(WorkspaceId workspaceId);

  Collection<Workspace> getWorkspaces();

  public Collection<WorkspaceId> getFriendlies(WorkspaceId workspaceId);

  public void addFriend(WorkspaceId to, WorkspaceId... workspaceIds);

  public void removeFriend(WorkspaceId from, WorkspaceId workspaceId);

  public void removeAllFriendlies(WorkspaceId workspaceId);

  public void removeAllRepresentationTemplates(WorkspaceId workspaceId);

  public void removeAllVariationTemplates(WorkspaceId workspaceId);

  public void removeAllContentCoProcessorTemplates(WorkspaceId workspaceId);

  enum ResourceSortCriteria {

    BY_NAME,
    BY_DATE,}

  public Collection<String> getRepresentationNames(WorkspaceId id);

  public Collection<String> getVariationNames(WorkspaceId id);

  public Collection<String> getRepresentationNames(WorkspaceId id, String startPoint, int count);

  public Collection<String> getVariationNames(WorkspaceId id, String startPoint, int count);

  public Collection<String> getRepresentationNames(WorkspaceId id, ResourceSortCriteria criteria);

  public Collection<String> getVariationNames(WorkspaceId id, ResourceSortCriteria criteria);

  public Collection<String> getContentCoProcessorNames(WorkspaceId id);

  public Collection<String> getContentCoProcessorNames(WorkspaceId id, String startPoint, int count);

  public Collection<String> getContentCoProcessorNames(WorkspaceId id, ResourceSortCriteria criteria);

  public Collection<String> getContentCoProcessorNames(WorkspaceId id, ResourceSortCriteria criteria, String startPoint,
                                                       int count);

  public Collection<String> getRepresentationNames(WorkspaceId id, ResourceSortCriteria criteria, String startPoint,
                                                   int count);

  public Collection<String> getVariationNames(WorkspaceId id, ResourceSortCriteria criteria, String startPoint,
                                              int count);

  public String getEntityTagValueForResourceTemplate(ResourceTemplate resourceTemplate);

  public Collection<ContentId> getRootContents(WorkspaceId workspaceId);

  public void addRootContent(WorkspaceId to, ContentId... contentIds);

  public void removeRootContent(WorkspaceId from, ContentId contentId);

  public void removeAllRootContents(WorkspaceId workspaceId);

  ValidatorTemplate putValidatorTemplate(WorkspaceId to, String name, ValidatorType templateType, InputStream stream)
      throws IOException;

  ValidatorTemplate putValidatorTemplate(WorkspaceId to, String name, ValidatorType templateType, byte[] data);

  void delete(ValidatorTemplate template);

  public ValidatorTemplate getValidatorTemplate(WorkspaceId workspaceId, String name);

  public void removeAllValidatorTemplates(WorkspaceId workspaceId);

  public Collection<String> getValidatorNames(WorkspaceId id);

  public Collection<String> getValidatorNames(WorkspaceId id, String startPoint, int count);

  public Collection<String> getValidatorNames(WorkspaceId id, ResourceSortCriteria criteria);

  public Collection<String> getValidatorNames(WorkspaceId id, ResourceSortCriteria criteria, String startPoint,
                                              int count);

  public String getEntityTagValueForValidatorTemplate(ValidatorTemplate validatorTemplate);
}
