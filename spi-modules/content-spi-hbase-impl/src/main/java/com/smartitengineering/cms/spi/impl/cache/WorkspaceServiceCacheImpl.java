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
package com.smartitengineering.cms.spi.impl.cache;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.smartitengineering.cms.api.common.TemplateType;
import com.smartitengineering.cms.api.content.ContentId;
import com.smartitengineering.cms.api.factory.workspace.WorkspaceAPI.ResourceSortCriteria;
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.type.ValidatorType;
import com.smartitengineering.cms.api.workspace.ContentCoProcessorTemplate;
import com.smartitengineering.cms.api.workspace.RepresentationTemplate;
import com.smartitengineering.cms.api.workspace.ValidatorTemplate;
import com.smartitengineering.cms.api.workspace.VariationTemplate;
import com.smartitengineering.cms.api.workspace.Workspace;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.cms.spi.workspace.WorkspaceService;
import com.smartitengineering.dao.common.cache.CacheServiceProvider;
import com.smartitengineering.dao.common.cache.Lock;
import com.smartitengineering.dao.common.cache.Mutex;
import com.smartitengineering.dao.common.cache.impl.CacheAPIFactory;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class WorkspaceServiceCacheImpl implements WorkspaceService {

  @Inject
  @Named("primary")
  private WorkspaceService primaryWorkspaceService;
  @Inject
  private CacheServiceProvider<String, Workspace> cacheProvider;
  protected final Mutex<String> mutex = CacheAPIFactory.<String>getMutex();
  protected transient final Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public void removeRootContent(WorkspaceId from, ContentId contentId) {
    primaryWorkspaceService.removeRootContent(from, contentId);
  }

  @Override
  public void removeFriend(WorkspaceId from, WorkspaceId workspaceId) {
    primaryWorkspaceService.removeFriend(from, workspaceId);
  }

  @Override
  public void removeAllVariationTemplates(WorkspaceId workspaceId) {
    primaryWorkspaceService.removeAllVariationTemplates(workspaceId);
  }

  @Override
  public void removeAllRootContents(WorkspaceId workspaceId) {
    primaryWorkspaceService.removeAllRootContents(workspaceId);
  }

  @Override
  public void removeAllRepresentationTemplates(WorkspaceId workspaceId) {
    primaryWorkspaceService.removeAllRepresentationTemplates(workspaceId);
  }

  @Override
  public void removeAllFriendlies(WorkspaceId workspaceId) {
    primaryWorkspaceService.removeAllFriendlies(workspaceId);
  }

  @Override
  public VariationTemplate putVariationTemplate(WorkspaceId workspaceId, String name, TemplateType templateType,
                                                byte[] data) {
    return primaryWorkspaceService.putVariationTemplate(workspaceId, name, templateType, data);
  }

  @Override
  public RepresentationTemplate putRepresentationTemplate(WorkspaceId workspaceId, String name,
                                                          TemplateType templateType, byte[] data) {
    return primaryWorkspaceService.putRepresentationTemplate(workspaceId, name, templateType, data);
  }

  @Override
  public Workspace load(WorkspaceId workspaceId) {
    if (workspaceId == null) {
      return primaryWorkspaceService.load(workspaceId);
    }
    String key = workspaceId.toString();
    Workspace template = cacheProvider.retrieveFromCache(key);
    if (template != null) {
      return template;
    }
    else {
      try {
        Lock<String> lock = mutex.acquire(key);
        template = cacheProvider.retrieveFromCache(key);
        if (template != null) {
          return template;
        }
        template = primaryWorkspaceService.load(workspaceId);
        if (template != null) {
          putToCache(template, key);
        }
        mutex.release(lock);
      }
      catch (Exception ex) {
        logger.warn("Could not do cache lookup!", ex);
      }
      return template;
    }
  }

  @Override
  public Collection<Workspace> getWorkspaces() {
    return primaryWorkspaceService.getWorkspaces();
  }

  @Override
  public Collection<VariationTemplate> getVariationsWithoutData(WorkspaceId id, ResourceSortCriteria criteria) {
    return primaryWorkspaceService.getVariationsWithoutData(id, criteria);
  }

  @Override
  public VariationTemplate getVariationTemplate(WorkspaceId workspaceId, String name) {
    return primaryWorkspaceService.getVariationTemplate(workspaceId, name);
  }

  @Override
  public Collection<ContentId> getRootContents(WorkspaceId workspaceId) {
    return primaryWorkspaceService.getRootContents(workspaceId);
  }

  @Override
  public Collection<RepresentationTemplate> getRepresentationsWithoutData(WorkspaceId id, ResourceSortCriteria criteria) {
    return primaryWorkspaceService.getRepresentationsWithoutData(id, criteria);
  }

  @Override
  public RepresentationTemplate getRepresentationTemplate(WorkspaceId workspaceId, String name) {
    return primaryWorkspaceService.getRepresentationTemplate(workspaceId, name);
  }

  @Override
  public Collection<WorkspaceId> getFriendlies(WorkspaceId workspaceId) {
    return primaryWorkspaceService.getFriendlies(workspaceId);
  }

  @Override
  public Collection<ContentType> getContentDefintions(WorkspaceId workspaceId) {
    return primaryWorkspaceService.getContentDefintions(workspaceId);
  }

  @Override
  public void deleteVariation(VariationTemplate template) {
    primaryWorkspaceService.deleteVariation(template);
  }

  @Override
  public void deleteRepresentation(RepresentationTemplate template) {
    primaryWorkspaceService.deleteRepresentation(template);
  }

  @Override
  public Workspace delete(WorkspaceId workspaceId) {
    try {
      Workspace workspace = primaryWorkspaceService.delete(workspaceId);
      if (workspace == null) {
        expireFromCache(workspace.getId().toString());
      }
      return workspace;
    }
    catch (RuntimeException exception) {
      logger.info("Could not delete thus did not invalidate cache!", exception);
      throw exception;
    }
  }

  @Override
  public Workspace create(WorkspaceId workspaceId) throws IllegalArgumentException {
    return primaryWorkspaceService.create(workspaceId);
  }

  @Override
  public void addRootContent(WorkspaceId to, ContentId... contentIds) {
    primaryWorkspaceService.addRootContent(to, contentIds);
  }

  @Override
  public void addFriend(WorkspaceId to, WorkspaceId... workspaceIds) {
    primaryWorkspaceService.addFriend(to, workspaceIds);
  }

  @Override
  public void removeAllValidatorTemplates(WorkspaceId workspaceId) {
    primaryWorkspaceService.removeAllValidatorTemplates(workspaceId);
  }

  @Override
  public ValidatorTemplate putValidatorTemplate(WorkspaceId to, String name, ValidatorType templateType, byte[] template) {
    return primaryWorkspaceService.putValidatorTemplate(to, name, templateType, template);
  }

  @Override
  public Collection<ValidatorTemplate> getValidatorsWithoutData(WorkspaceId id,
                                                                ResourceSortCriteria resourceSortCriteria) {
    return primaryWorkspaceService.getValidatorsWithoutData(id, resourceSortCriteria);
  }

  @Override
  public ValidatorTemplate getValidationTemplate(WorkspaceId workspaceId, String name) {
    return primaryWorkspaceService.getValidationTemplate(workspaceId, name);
  }

  @Override
  public void deleteValidator(ValidatorTemplate template) {
    primaryWorkspaceService.deleteValidator(template);
  }

  protected void putToCache(Workspace template, String key) {
    cacheProvider.putToCache(key, template);
  }

  protected void expireFromCache(String key) {
    if (cacheProvider.containsKey(key)) {
      cacheProvider.expireFromCache(key);
    }
  }

  public ContentCoProcessorTemplate putContentCoProcessorTemplate(WorkspaceId workspaceId, String name,
                                                                  TemplateType templateType, byte[] data) {
    return primaryWorkspaceService.putContentCoProcessorTemplate(workspaceId, name, templateType, data);
  }

  public ContentCoProcessorTemplate getContentCoProcessorTemplate(WorkspaceId workspaceId, String name) {
    return primaryWorkspaceService.getContentCoProcessorTemplate(workspaceId, name);
  }

  public void removeAllContentCoProcessorTemplates(WorkspaceId workspaceId) {
    primaryWorkspaceService.removeAllContentCoProcessorTemplates(workspaceId);
  }

  public Collection<ContentCoProcessorTemplate> getContentCoProcessorsWithoutData(WorkspaceId id,
                                                                                  ResourceSortCriteria criteria) {
    return primaryWorkspaceService.getContentCoProcessorsWithoutData(id, criteria);
  }

  public void deleteContentCoProcessor(ContentCoProcessorTemplate template) {
    primaryWorkspaceService.deleteContentCoProcessor(template);
  }
}
