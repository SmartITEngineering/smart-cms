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
package com.smartitengineering.cms.api.impl.workspace;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.smartitengineering.cms.api.common.TemplateType;
import com.smartitengineering.cms.api.content.ContentId;
import com.smartitengineering.cms.api.content.template.ContentCoProcessor;
import com.smartitengineering.cms.api.content.template.ContentCoProcessorGenerator;
import com.smartitengineering.cms.api.event.Event;
import com.smartitengineering.cms.api.event.Event.EventType;
import com.smartitengineering.cms.api.event.Event.Type;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.workspace.ContentCoProcessorTemplate;
import com.smartitengineering.cms.api.workspace.RepresentationTemplate;
import com.smartitengineering.cms.api.workspace.ResourceTemplate;
import com.smartitengineering.cms.api.workspace.ValidatorTemplate;
import com.smartitengineering.cms.api.workspace.VariationTemplate;
import com.smartitengineering.cms.api.workspace.Workspace;
import com.smartitengineering.cms.api.factory.workspace.WorkspaceAPI;
import com.smartitengineering.cms.api.type.ValidatorType;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.cms.spi.SmartContentSPI;
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class WorkspaceAPIImpl implements WorkspaceAPI {

  private String globalNamespace;
  private final transient Logger logger = LoggerFactory.getLogger(getClass());
  @Inject
  private Map<TemplateType, ContentCoProcessorGenerator> contentCoProcessorGenerators;

  @Inject
  public void setGlobalNamespace(@Named("globalNamespace") String globalNamespace) {
    this.globalNamespace = globalNamespace;
  }

  @Override
  public String getGlobalNamespace() {
    return globalNamespace;
  }

  @Override
  public WorkspaceId createWorkspace(String name) {
    WorkspaceId workspaceIdImpl = createWorkspaceId(name);
    return createWorkspace(workspaceIdImpl);
  }

  @Override
  public WorkspaceId createWorkspace(String globalNamespace, String name) {
    return createWorkspace(createWorkspaceId(globalNamespace, name));
  }

  @Override
  public WorkspaceId createWorkspace(WorkspaceId workspaceId) {
    SmartContentSPI.getInstance().getWorkspaceService().create(workspaceId);
    Event<Workspace> event = SmartContentAPI.getInstance().getEventRegistrar().<Workspace>createEvent(
        EventType.UPDATE, Type.WORKSPACE, getWorkspace(workspaceId));
    SmartContentAPI.getInstance().getEventRegistrar().notifyEventAsynchronously(event);
    return workspaceId;
  }

  @Override
  public WorkspaceId createWorkspaceId(String name) {
    return createWorkspaceId(null, name);
  }

  @Override
  public WorkspaceId createWorkspaceId(final String namespace, String name) {
    final WorkspaceIdImpl workspaceIdImpl = new WorkspaceIdImpl();
    workspaceIdImpl.setGlobalNamespace(StringUtils.isBlank(namespace) ? getGlobalNamespace() : namespace);
    workspaceIdImpl.setName(name);
    return workspaceIdImpl;
  }

  @Override
  public WorkspaceId getWorkspaceIdIfExists(String name) {
    final WorkspaceId createdWorkspaceId = createWorkspaceId(name);
    return getWorkspaceIdIfExists(createdWorkspaceId);
  }

  @Override
  public WorkspaceId getWorkspaceIdIfExists(WorkspaceId workspaceId) {
    Workspace workspace = getWorkspace(workspaceId);
    if (workspace != null) {
      return workspaceId;
    }
    return null;
  }

  @Override
  public Workspace getWorkspace(WorkspaceId workspaceId) {
    return SmartContentSPI.getInstance().getWorkspaceService().load(workspaceId);
  }

  @Override
  public Collection<Workspace> getWorkspaces() {
    return SmartContentSPI.getInstance().getWorkspaceService().getWorkspaces();
  }

  @Override
  public RepresentationTemplate putRepresentationTemplate(WorkspaceId to, String name, TemplateType templateType,
                                                          InputStream stream)
      throws IOException {
    return putRepresentationTemplate(to, name, templateType, IOUtils.toByteArray(stream));
  }

  @Override
  public RepresentationTemplate putRepresentationTemplate(WorkspaceId to, String name, TemplateType templateType,
                                                          byte[] data) {
    final RepresentationTemplate putRepresentationTemplate = SmartContentSPI.getInstance().getWorkspaceService().
        putRepresentationTemplate(to, name, templateType, data);
    Event<RepresentationTemplate> event = SmartContentAPI.getInstance().getEventRegistrar().<RepresentationTemplate>
        createEvent(EventType.UPDATE, Type.REPRESENTATION_TEMPLATE, putRepresentationTemplate);
    SmartContentAPI.getInstance().getEventRegistrar().notifyEventAsynchronously(event);
    return putRepresentationTemplate;
  }

  @Override
  public VariationTemplate putVariationTemplate(WorkspaceId to, String name, TemplateType templateType,
                                                InputStream stream) throws
      IOException {
    return putVariationTemplate(to, name, templateType, IOUtils.toByteArray(stream));
  }

  @Override
  public VariationTemplate putVariationTemplate(WorkspaceId to, String name, TemplateType templateType, byte[] data) {
    final VariationTemplate putVariationTemplate = SmartContentSPI.getInstance().getWorkspaceService().
        putVariationTemplate(to, name, templateType, data);
    Event<VariationTemplate> event = SmartContentAPI.getInstance().getEventRegistrar().<VariationTemplate>
        createEvent(EventType.UPDATE, Type.VARIATION_TEMPLATE, putVariationTemplate);
    SmartContentAPI.getInstance().getEventRegistrar().notifyEventAsynchronously(event);
    return putVariationTemplate;
  }

  @Override
  public void delete(RepresentationTemplate template) {
    SmartContentSPI.getInstance().getWorkspaceService().deleteRepresentation(template);
    Event<RepresentationTemplate> event = SmartContentAPI.getInstance().getEventRegistrar().<RepresentationTemplate>
        createEvent(EventType.DELETE, Type.REPRESENTATION_TEMPLATE, template);
    SmartContentAPI.getInstance().getEventRegistrar().notifyEventAsynchronously(event);
  }

  @Override
  public void delete(VariationTemplate template) {
    SmartContentSPI.getInstance().getWorkspaceService().deleteVariation(template);
    Event<VariationTemplate> event = SmartContentAPI.getInstance().getEventRegistrar().<VariationTemplate>
        createEvent(EventType.DELETE, Type.VARIATION_TEMPLATE, template);
    SmartContentAPI.getInstance().getEventRegistrar().notifyEventAsynchronously(event);
  }

  @Override
  public Collection<WorkspaceId> getFriendlies(WorkspaceId workspaceId) {
    return SmartContentSPI.getInstance().getWorkspaceService().getFriendlies(workspaceId);
  }

  @Override
  public void addFriend(WorkspaceId to, WorkspaceId... workspaceIds) {
    SmartContentSPI.getInstance().getWorkspaceService().addFriend(to, workspaceIds);
    for (WorkspaceId friend : workspaceIds) {
      Event<Entry<WorkspaceId, WorkspaceId>> event = SmartContentAPI.getInstance().getEventRegistrar().<Entry<WorkspaceId, WorkspaceId>>
          createEvent(EventType.CREATE, Type.FRIENDLY, new SimpleEntry<WorkspaceId, WorkspaceId>(to, friend));
      SmartContentAPI.getInstance().getEventRegistrar().notifyEventAsynchronously(event);
    }
  }

  @Override
  public void removeFriend(WorkspaceId from, WorkspaceId workspaceId) {
    SmartContentSPI.getInstance().getWorkspaceService().removeFriend(from, workspaceId);
    Event<Entry<WorkspaceId, WorkspaceId>> event = SmartContentAPI.getInstance().getEventRegistrar().<Entry<WorkspaceId, WorkspaceId>>
        createEvent(EventType.DELETE, Type.FRIENDLY, new SimpleEntry<WorkspaceId, WorkspaceId>(from, workspaceId));
    SmartContentAPI.getInstance().getEventRegistrar().notifyEventAsynchronously(event);
  }

  @Override
  public void removeAllFriendlies(WorkspaceId workspaceId) {
    SmartContentSPI.getInstance().getWorkspaceService().removeAllFriendlies(workspaceId);
    Event<WorkspaceId> event = SmartContentAPI.getInstance().getEventRegistrar().<WorkspaceId>
        createEvent(EventType.DELETE, Type.ALL_FRIENDLIES, workspaceId);
    SmartContentAPI.getInstance().getEventRegistrar().notifyEventAsynchronously(event);
  }

  @Override
  public void removeAllRepresentationTemplates(WorkspaceId workspaceId) {
    SmartContentSPI.getInstance().getWorkspaceService().removeAllRepresentationTemplates(workspaceId);
    Event<WorkspaceId> event = SmartContentAPI.getInstance().getEventRegistrar().<WorkspaceId>
        createEvent(EventType.DELETE, Type.ALL_REPRESENTATION_TEMPLATES, workspaceId);
    SmartContentAPI.getInstance().getEventRegistrar().notifyEventAsynchronously(event);
  }

  @Override
  public void removeAllVariationTemplates(WorkspaceId workspaceId) {
    SmartContentSPI.getInstance().getWorkspaceService().removeAllVariationTemplates(workspaceId);
    Event<WorkspaceId> event = SmartContentAPI.getInstance().getEventRegistrar().<WorkspaceId>
        createEvent(EventType.DELETE, Type.ALL_VARIATION_TEMPLATES, workspaceId);
    SmartContentAPI.getInstance().getEventRegistrar().notifyEventAsynchronously(event);
  }

  @Override
  public Collection<String> getRepresentationNames(WorkspaceId id, ResourceSortCriteria criteria, String startPoint,
                                                   int count) {
    if (count == 0 || startPoint == null) {
      return Collections.emptyList();
    }
    List<String> list = new ArrayList<String>(getRepresentationNames(id, criteria));
    return cutList(list, startPoint, count);
  }

  @Override
  public Collection<String> getVariationNames(WorkspaceId id, ResourceSortCriteria criteria, String startPoint,
                                              int count) {
    if (count == 0 || startPoint == null) {
      return Collections.emptyList();
    }
    List<String> list = new ArrayList<String>(getVariationNames(id, criteria));
    return cutList(list, startPoint, count);
  }

  @Override
  public Collection<String> getRepresentationNames(WorkspaceId id, ResourceSortCriteria criteria) {
    final Collection<? extends ResourceTemplate> repsWithoutData = SmartContentSPI.getInstance().
        getWorkspaceService().getRepresentationsWithoutData(id, criteria);
    return getResourceNames(repsWithoutData);
  }

  @Override
  public Collection<String> getVariationNames(WorkspaceId id, ResourceSortCriteria criteria) {
    final Collection<? extends ResourceTemplate> variationsWithoutData = SmartContentSPI.getInstance().
        getWorkspaceService().getVariationsWithoutData(id, criteria);
    return getResourceNames(variationsWithoutData);
  }

  protected Collection<String> getResourceNames(Collection<? extends ResourceTemplate> templates) {
    ArrayList<String> list = new ArrayList<String>(templates.size());
    for (ResourceTemplate template : templates) {
      list.add(template.getName());
    }
    return list;
  }

  protected Collection<String> getValidatorNames(Collection<ValidatorTemplate> templates) {
    ArrayList<String> list = new ArrayList<String>(templates.size());
    for (ValidatorTemplate template : templates) {
      list.add(template.getName());
    }
    return list;
  }

  @Override
  public Collection<String> getRepresentationNames(WorkspaceId id) {
    return getRepresentationNames(id, ResourceSortCriteria.BY_NAME);
  }

  @Override
  public Collection<String> getVariationNames(WorkspaceId id) {
    return getVariationNames(id, ResourceSortCriteria.BY_NAME);
  }

  @Override
  public Collection<String> getRepresentationNames(WorkspaceId id, String startPoint, int count) {
    return getRepresentationNames(id, ResourceSortCriteria.BY_NAME, startPoint, count);
  }

  @Override
  public Collection<String> getVariationNames(WorkspaceId id, String startPoint, int count) {
    return getVariationNames(id, ResourceSortCriteria.BY_NAME, startPoint, count);
  }

  @Override
  public RepresentationTemplate getRepresentationTemplate(WorkspaceId id, String name) {
    return SmartContentSPI.getInstance().getWorkspaceService().getRepresentationTemplate(id, name);
  }

  @Override
  public VariationTemplate getVariationTemplate(WorkspaceId id, String name) {
    return SmartContentSPI.getInstance().getWorkspaceService().getVariationTemplate(id, name);
  }

  @Override
  public String getEntityTagValueForResourceTemplate(ResourceTemplate template) {
    final String toString = new StringBuilder(DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.format(template.
        getLastModifiedDate())).append(':').append(Arrays.toString(template.getTemplate())).append(':').append(template.
        getTemplateType().name()).toString();
    final String etag = DigestUtils.md5Hex(toString);
    if (logger.isDebugEnabled()) {
      logger.debug("Generated etag " + etag + " for " + template.getClass().getName() + " with name " +
          template.getName());
    }
    return etag;
  }

  @Override
  public Collection<ContentId> getRootContents(WorkspaceId workspaceId) {
    return SmartContentSPI.getInstance().getWorkspaceService().getRootContents(workspaceId);
  }

  @Override
  public void addRootContent(WorkspaceId to, ContentId... contentIds) {
    SmartContentSPI.getInstance().getWorkspaceService().addRootContent(to, contentIds);
    for (ContentId rootContent : contentIds) {
      Event<Entry<WorkspaceId, ContentId>> event = SmartContentAPI.getInstance().getEventRegistrar().<Entry<WorkspaceId, ContentId>>
          createEvent(EventType.CREATE, Type.ROOT_CONTENT, new SimpleEntry<WorkspaceId, ContentId>(to, rootContent));
      SmartContentAPI.getInstance().getEventRegistrar().notifyEventAsynchronously(event);
    }
  }

  @Override
  public void removeRootContent(WorkspaceId from, ContentId contentId) {
    SmartContentSPI.getInstance().getWorkspaceService().removeRootContent(from, contentId);
    Event<Entry<WorkspaceId, ContentId>> event = SmartContentAPI.getInstance().getEventRegistrar().<Entry<WorkspaceId, ContentId>>
        createEvent(EventType.DELETE, Type.ROOT_CONTENT, new SimpleEntry<WorkspaceId, ContentId>(from, contentId));
    SmartContentAPI.getInstance().getEventRegistrar().notifyEventAsynchronously(event);
  }

  @Override
  public void removeAllRootContents(WorkspaceId workspaceId) {
    SmartContentSPI.getInstance().getWorkspaceService().removeAllRootContents(workspaceId);
    Event<WorkspaceId> event = SmartContentAPI.getInstance().getEventRegistrar().<WorkspaceId>
        createEvent(EventType.DELETE, Type.ALL_ROOT_CONTENTS, workspaceId);
    SmartContentAPI.getInstance().getEventRegistrar().notifyEventAsynchronously(event);
  }

  @Override
  public ValidatorTemplate getValidatorTemplate(WorkspaceId workspaceId, String name) {
    return SmartContentSPI.getInstance().getWorkspaceService().getValidationTemplate(workspaceId, name);
  }

  @Override
  public void delete(ValidatorTemplate template) {
    SmartContentSPI.getInstance().getWorkspaceService().deleteValidator(template);
    Event<ValidatorTemplate> event = SmartContentAPI.getInstance().getEventRegistrar().<ValidatorTemplate>
        createEvent(EventType.DELETE, Type.VALIDATION_TEMPLATE, template);
    SmartContentAPI.getInstance().getEventRegistrar().notifyEventAsynchronously(event);
  }

  @Override
  public ValidatorTemplate putValidatorTemplate(WorkspaceId to, String name, ValidatorType templateType,
                                                InputStream stream) throws IOException {
    return putValidatorTemplate(to, name, templateType, IOUtils.toByteArray(stream));
  }

  @Override
  public ValidatorTemplate putValidatorTemplate(WorkspaceId to, String name, ValidatorType templateType, byte[] data) {
    final ValidatorTemplate validatorTemplate = SmartContentSPI.getInstance().getWorkspaceService().putValidatorTemplate(
        to, name, templateType, data);
    Event<ValidatorTemplate> event = SmartContentAPI.getInstance().getEventRegistrar().<ValidatorTemplate>
        createEvent(EventType.UPDATE, Type.VALIDATION_TEMPLATE, validatorTemplate);
    SmartContentAPI.getInstance().getEventRegistrar().notifyEventAsynchronously(event);
    return validatorTemplate;
  }

  @Override
  public String getEntityTagValueForValidatorTemplate(ValidatorTemplate template) {
    final String toString = new StringBuilder(DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.format(template.
        getLastModifiedDate())).append(':').append(Arrays.toString(template.getTemplate())).append(':').append(template.
        getTemplateType().name()).toString();
    final String etag = DigestUtils.md5Hex(toString);
    if (logger.isDebugEnabled()) {
      logger.debug("Generated etag " + etag + " for " + template.getClass().getName() + " with name " +
          template.getName());
    }
    return etag;
  }

  @Override
  public Collection<String> getValidatorNames(WorkspaceId id) {
    return getVariationNames(id, ResourceSortCriteria.BY_NAME);
  }

  @Override
  public Collection<String> getValidatorNames(WorkspaceId id, String startPoint, int count) {
    return getVariationNames(id, ResourceSortCriteria.BY_NAME, startPoint, count);
  }

  @Override
  public Collection<String> getValidatorNames(WorkspaceId id, ResourceSortCriteria criteria) {
    final Collection<ValidatorTemplate> variationsWithoutData = SmartContentSPI.getInstance().getWorkspaceService().
        getValidatorsWithoutData(id, criteria);
    return getValidatorNames(variationsWithoutData);
  }

  @Override
  public Collection<String> getValidatorNames(WorkspaceId id, ResourceSortCriteria criteria, String startPoint,
                                              int count) {
    if (count == 0 || startPoint == null) {
      return Collections.emptyList();
    }
    List<String> list = new ArrayList<String>(getValidatorNames(id, criteria));
    return cutList(list, startPoint, count);
  }

  @Override
  public void removeAllValidatorTemplates(WorkspaceId workspaceId) {
    SmartContentSPI.getInstance().getWorkspaceService().removeAllValidatorTemplates(workspaceId);
    Event<WorkspaceId> event = SmartContentAPI.getInstance().getEventRegistrar().<WorkspaceId>
        createEvent(EventType.DELETE, Type.ALL_VALIDATION_TEMPLATES, workspaceId);
    SmartContentAPI.getInstance().getEventRegistrar().notifyEventAsynchronously(event);
  }

  protected Collection<String> cutList(List<String> list, String startPoint, int count) {
    if (logger.isDebugEnabled()) {
      logger.debug("All names " + list);
    }
    int index = Collections.binarySearch(list, startPoint);
    if (logger.isDebugEnabled()) {
      logger.debug("Index " + index);
    }
    if (index < 0) {
      index = index * - 1;
    }
    if (count > 0 && index + 1 >= list.size() && StringUtils.isNotBlank(startPoint)) {
      logger.debug("Index is equal to size and count is greater than 0");
      return Collections.emptyList();
    }
    if (count < 0 && index <= 0) {
      logger.debug("Index is zero to size and count is smaller than 0");
      return Collections.emptyList();
    }
    final int fromIndex;
    final int toIndex;
    if (count > 0) {
      fromIndex = StringUtils.isBlank(startPoint) ? 0 : index + 1;
      toIndex = (fromIndex + count >= list.size()) ? list.size() : fromIndex + count;
    }
    else {
      toIndex = index;
      fromIndex = (toIndex + count >= 0) ? toIndex + count : 0;
    }
    if (logger.isDebugEnabled()) {
      logger.debug("Sublisting starts at " + fromIndex + " and ends before " + toIndex);
    }
    final List<String> result = list.subList(fromIndex, toIndex);
    if (logger.isDebugEnabled()) {
      logger.debug("Returning " + result);
    }
    return result;
  }

  public void delete(ContentCoProcessorTemplate template) {
    SmartContentSPI.getInstance().getWorkspaceService().deleteContentCoProcessor(template);
  }

  public ContentCoProcessorTemplate putContentCoProcessorTemplate(WorkspaceId to, String name, TemplateType templateType,
                                                                  InputStream stream) throws IOException {
    return putContentCoProcessorTemplate(to, name, templateType, IOUtils.toByteArray(stream));
  }

  public ContentCoProcessorTemplate putContentCoProcessorTemplate(WorkspaceId to, String name, TemplateType templateType,
                                                                  byte[] data) {
    if (templateType.equals(TemplateType.JASPER) || templateType.equals(TemplateType.VELOCITY)) {
      throw new IllegalArgumentException("TemplateType not supported for content type co processor");
    }
    return SmartContentSPI.getInstance().getWorkspaceService().putContentCoProcessorTemplate(to, name, templateType,
                                                                                             data);
  }

  public ContentCoProcessorTemplate getContentCoProcessorTemplate(WorkspaceId id, String name) {
    return SmartContentSPI.getInstance().getWorkspaceService().getContentCoProcessorTemplate(id, name);
  }

  public void removeAllContentCoProcessorTemplates(WorkspaceId workspaceId) {
    SmartContentSPI.getInstance().getWorkspaceService().removeAllContentCoProcessorTemplates(workspaceId);
  }

  public ContentCoProcessor getContentCoProcessor(WorkspaceId id, String name) {
    ContentCoProcessorTemplate template = getContentCoProcessorTemplate(id, name);
    if (template == null) {
      return null;
    }
    ContentCoProcessorGenerator generator = contentCoProcessorGenerators.get(template.getTemplateType());
    try {
      return generator.getGenerator(template);
    }
    catch (Exception ex) {
      logger.warn("Could not retrieve processor", ex);
      return null;
    }
  }

  public Collection<String> getContentCoProcessorNames(WorkspaceId id, ResourceSortCriteria criteria) {
    final Collection<ContentCoProcessorTemplate> procsWithoutData = SmartContentSPI.getInstance().
        getWorkspaceService().getContentCoProcessorsWithoutData(id, criteria);
    return getResourceNames(procsWithoutData);
  }

  public Collection<String> getContentCoProcessorNames(WorkspaceId id, ResourceSortCriteria criteria, String startPoint,
                                                       int count) {
    if (count == 0 || startPoint == null) {
      return Collections.emptyList();
    }
    List<String> list = new ArrayList<String>(getContentCoProcessorNames(id, criteria));
    return cutList(list, startPoint, count);
  }

  public Collection<String> getContentCoProcessorNames(WorkspaceId id) {
    return getContentCoProcessorNames(id, ResourceSortCriteria.BY_NAME);
  }

  public Collection<String> getContentCoProcessorNames(WorkspaceId id, String startPoint, int count) {
    return getContentCoProcessorNames(id, ResourceSortCriteria.BY_NAME, startPoint, count);
  }
}
