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
import com.smartitengineering.cms.api.content.ContentId;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.workspace.ContentCoProcessorTemplate;
import com.smartitengineering.cms.api.workspace.RepresentationTemplate;
import com.smartitengineering.cms.api.workspace.ResourceTemplate;
import com.smartitengineering.cms.api.workspace.ValidatorTemplate;
import com.smartitengineering.cms.api.workspace.VariationTemplate;
import com.smartitengineering.cms.api.workspace.Workspace;
import com.smartitengineering.cms.api.factory.workspace.WorkspaceAPI.ResourceSortCriteria;
import com.smartitengineering.cms.api.type.ValidatorType;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.cms.spi.SmartContentSPI;
import com.smartitengineering.cms.spi.type.PersistentContentTypeReader;
import com.smartitengineering.cms.spi.workspace.PersistableContentCoProcessorTemplate;
import com.smartitengineering.cms.spi.workspace.PersistableRepresentationTemplate;
import com.smartitengineering.cms.spi.workspace.PersistableResourceTemplate;
import com.smartitengineering.cms.spi.workspace.PersistableValidatorTemplate;
import com.smartitengineering.cms.spi.workspace.PersistableVariationTemplate;
import com.smartitengineering.cms.spi.workspace.PersistableWorkspace;
import com.smartitengineering.cms.spi.workspace.WorkspaceService;
import com.smartitengineering.dao.common.queryparam.MatchMode;
import com.smartitengineering.dao.common.queryparam.QueryParameter;
import com.smartitengineering.dao.common.queryparam.QueryParameterFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class WorkspaceServiceImpl extends AbstractWorkspaceService implements WorkspaceService {

  protected final transient Logger logger = LoggerFactory.getLogger(getClass());
  public static final QueryParameter<Void> SELF_PARAM = QueryParameterFactory.getPropProjectionParam("workspace");
  private static final Comparator<ResourceTemplate> TEMPLATE_DATE_COMPARATOR = new Comparator<ResourceTemplate>() {

    @Override
    public int compare(ResourceTemplate o1, ResourceTemplate o2) {
      return o1.getLastModifiedDate().compareTo(o2.getLastModifiedDate());
    }
  };
  private static final Comparator<ResourceTemplate> TEMPLATE_NAME_COMPARATOR = new Comparator<ResourceTemplate>() {

    @Override
    public int compare(ResourceTemplate o1, ResourceTemplate o2) {
      return o1.getName().compareTo(o2.getName());
    }
  };
  private static final Comparator<ValidatorTemplate> VALIDATOR_DATE_COMPARATOR = new Comparator<ValidatorTemplate>() {

    @Override
    public int compare(ValidatorTemplate o1, ValidatorTemplate o2) {
      return o1.getLastModifiedDate().compareTo(o2.getLastModifiedDate());
    }
  };
  private static final Comparator<ValidatorTemplate> VALIDATOR_NAME_COMPARATOR = new Comparator<ValidatorTemplate>() {

    @Override
    public int compare(ValidatorTemplate o1, ValidatorTemplate o2) {
      return o1.getName().compareTo(o2.getName());
    }
  };

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
    final List<PersistentWorkspace> list = commonReadDao.getList(SELF_PARAM, QueryParameterFactory.
        getStringLikePropertyParam("id", "", MatchMode.START));
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
    final QueryParameter friendliesProp = QueryParameterFactory.getPropProjectionParam("friendlies");
    final QueryParameter idParam = getIdParam(workspaceId);
    PersistentWorkspace workspace = commonReadDao.getSingle(idParam, SELF_PARAM, friendliesProp);
    return workspace.getFriendlies();
  }

  @Override
  public void addFriend(WorkspaceId to, WorkspaceId... workspaceIds) {
    PersistentWorkspace workspace = getWorkspace(to);
    for (WorkspaceId id : workspaceIds) {
      workspace.addFriendly(id);
    }
    workspace.setFriendliesPopulated(true);
    commonWriteDao.update(workspace);
  }

  @Override
  public void removeFriend(WorkspaceId from, WorkspaceId workspaceId) {
    PersistentWorkspace workspace = getWorkspace(from);
    workspace.addFriendly(workspaceId);
    workspace.setFriendliesPopulated(true);
    commonWriteDao.delete(workspace);
  }

  protected PersistentWorkspace getWorkspace(WorkspaceId from) {
    PersistentWorkspace workspace = new PersistentWorkspace();
    workspace.setWorkspace(load(from));
    return workspace;
  }

  @Override
  public RepresentationTemplate putRepresentationTemplate(WorkspaceId workspaceId, String name,
                                                          TemplateType templateType, byte[] data) {
    PersistentWorkspace workspace = getWorkspace(workspaceId);
    PersistableRepresentationTemplate template = SmartContentSPI.getInstance().getPersistableDomainFactory().
        createPersistableRepresentationTemplate();
    workspace.addRepresentationTemplate(template);
    template.setName(name);
    template.setTemplateType(templateType);
    template.setWorkspaceId(workspaceId);
    template.setTemplate(data);
    RepresentationTemplate oldTemplate = getRepresentationTemplate(workspaceId, name);
    updateFields(template, oldTemplate);
    workspace.setRepresentationPopulated(true);
    commonWriteDao.update(workspace);
    return template;
  }

  @Override
  public RepresentationTemplate getRepresentationTemplate(WorkspaceId workspaceId, String name) {
    List<QueryParameter> params = new ArrayList<QueryParameter>();
    final String info = WorkspaceObjectConverter.REP_INFO;
    if (logger.isDebugEnabled()) {
      logger.debug("Sample column name " + new StringBuilder(info).append(':').append(name).append(':').
          append(WorkspaceObjectConverter.TEMPLATETYPE).toString());
    }
    params.add(QueryParameterFactory.getPropProjectionParam(new StringBuilder(info).append(':').append(name).append(':').
        append(WorkspaceObjectConverter.TEMPLATETYPE).toString()));
    params.add(QueryParameterFactory.getPropProjectionParam(new StringBuilder(info).append(':').append(name).append(':').
        append(WorkspaceObjectConverter.CREATED).toString()));
    params.add(QueryParameterFactory.getPropProjectionParam(new StringBuilder(info).append(':').append(name).append(':').
        append(WorkspaceObjectConverter.LASTMODIFIED).toString()));
    params.add(QueryParameterFactory.getPropProjectionParam(new StringBuilder(info).append(':').append(name).append(':').
        append(WorkspaceObjectConverter.ENTITY_TAG).toString()));
    params.add(QueryParameterFactory.getPropProjectionParam(new StringBuilder(WorkspaceObjectConverter.REP_DATA).append(
        ':').append(name).toString()));
    params.add(SELF_PARAM);
    params.add(getIdParam(workspaceId));
    final List<PersistableRepresentationTemplate> list = commonReadDao.getSingle(params).getRepresentationTemplates();
    if (list.isEmpty()) {
      return null;
    }
    return list.get(0);
  }

  @Override
  public VariationTemplate putVariationTemplate(WorkspaceId workspaceId, String name, TemplateType templateType,
                                                byte[] data) {
    PersistentWorkspace workspace = getWorkspace(workspaceId);
    PersistableVariationTemplate template = SmartContentSPI.getInstance().getPersistableDomainFactory().
        createPersistableVariationTemplate();
    workspace.addVariationTemplate(template);
    template.setName(name);
    template.setTemplateType(templateType);
    template.setWorkspaceId(workspaceId);
    template.setTemplate(data);
    VariationTemplate oldTemplate = getVariationTemplate(workspaceId, name);
    updateFields(template, oldTemplate);
    workspace.setVariationPopulated(true);
    commonWriteDao.update(workspace);
    return template;
  }

  @Override
  public VariationTemplate getVariationTemplate(WorkspaceId workspaceId, String name) {
    List<QueryParameter> params = new ArrayList<QueryParameter>();
    final String info = WorkspaceObjectConverter.VAR_INFO;
    params.add(QueryParameterFactory.getPropProjectionParam(new StringBuilder(info).append(':').append(name).append(':').
        append(WorkspaceObjectConverter.TEMPLATETYPE).toString()));
    params.add(QueryParameterFactory.getPropProjectionParam(new StringBuilder(info).append(':').append(name).append(':').
        append(WorkspaceObjectConverter.CREATED).toString()));
    params.add(QueryParameterFactory.getPropProjectionParam(new StringBuilder(info).append(':').append(name).append(':').
        append(WorkspaceObjectConverter.LASTMODIFIED).toString()));
    params.add(QueryParameterFactory.getPropProjectionParam(new StringBuilder(info).append(':').append(name).append(':').
        append(WorkspaceObjectConverter.ENTITY_TAG).toString()));
    params.add(QueryParameterFactory.getPropProjectionParam(new StringBuilder(WorkspaceObjectConverter.VAR_DATA).append(
        ':').append(name).toString()));
    params.add(SELF_PARAM);
    params.add(getIdParam(workspaceId));
    final List<PersistableVariationTemplate> list = commonReadDao.getSingle(params).getVariationTemplates();
    if (list.isEmpty()) {
      return null;
    }
    return list.get(0);
  }

  @Override
  public void deleteRepresentation(RepresentationTemplate template) {
    PersistentWorkspace workspace = getWorkspace(template.getWorkspaceId());
    workspace.setRepresentationPopulated(true);
    PersistableRepresentationTemplate repTemplate = SmartContentSPI.getInstance().getPersistableDomainFactory().
        createPersistableRepresentationTemplate();
    repTemplate.setCreatedDate(template.getCreatedDate());
    repTemplate.setLastModifiedDate(template.getLastModifiedDate());
    repTemplate.setWorkspaceId(template.getWorkspaceId());
    repTemplate.setName(template.getName());
    repTemplate.setTemplate(template.getTemplate());
    repTemplate.setTemplateType(template.getTemplateType());
    workspace.addRepresentationTemplate(repTemplate);
    commonWriteDao.delete(workspace);
  }

  @Override
  public void deleteVariation(VariationTemplate template) {
    PersistentWorkspace workspace = getWorkspace(template.getWorkspaceId());
    workspace.setVariationPopulated(true);
    PersistableVariationTemplate varTemplate = SmartContentSPI.getInstance().getPersistableDomainFactory().
        createPersistableVariationTemplate();
    varTemplate.setCreatedDate(template.getCreatedDate());
    varTemplate.setLastModifiedDate(template.getLastModifiedDate());
    varTemplate.setWorkspaceId(template.getWorkspaceId());
    varTemplate.setName(template.getName());
    varTemplate.setTemplate(template.getTemplate());
    varTemplate.setTemplateType(template.getTemplateType());
    workspace.addVariationTemplate(varTemplate);
    commonWriteDao.delete(workspace);
  }

  protected QueryParameter<String> getIdParam(WorkspaceId workspaceId) {
    return QueryParameterFactory.getStringLikePropertyParam("id", workspaceId.toString(), MatchMode.EXACT);
  }

  @Override
  public void removeAllFriendlies(WorkspaceId workspaceId) {
    PersistentWorkspace workspace = getWorkspace(workspaceId);
    workspace.setFriendliesPopulated(true);
    commonWriteDao.delete(workspace);
  }

  @Override
  public void removeAllRepresentationTemplates(WorkspaceId workspaceId) {
    PersistentWorkspace workspace = getWorkspace(workspaceId);
    workspace.setRepresentationPopulated(true);
    commonWriteDao.delete(workspace);
  }

  @Override
  public void removeAllVariationTemplates(WorkspaceId workspaceId) {
    PersistentWorkspace workspace = getWorkspace(workspaceId);
    workspace.setVariationPopulated(true);
    commonWriteDao.delete(workspace);
  }

  @Override
  public Collection<RepresentationTemplate> getRepresentationsWithoutData(WorkspaceId id, ResourceSortCriteria criteria) {
    List<QueryParameter> params = new ArrayList<QueryParameter>();
    final String info = WorkspaceObjectConverter.REP_INFO;
    params.add(QueryParameterFactory.getPropProjectionParam(info));
    params.add(getIdParam(id));
    final PersistentWorkspace single = commonReadDao.getSingle(params);
    List<? extends RepresentationTemplate> templates = new ArrayList(single == null ?
        Collections.<RepresentationTemplate>emptyList() : single.getRepresentationTemplates());
    if (templates.isEmpty()) {
      return Collections.emptyList();
    }
    final Comparator<ResourceTemplate> comp;
    if (ResourceSortCriteria.BY_DATE.equals(criteria)) {
      comp = TEMPLATE_DATE_COMPARATOR;
    }
    else {
      comp = TEMPLATE_NAME_COMPARATOR;
    }
    Collections.sort(templates, comp);
    return Collections.unmodifiableCollection(templates);

  }

  @Override
  public Collection<VariationTemplate> getVariationsWithoutData(WorkspaceId id, ResourceSortCriteria criteria) {
    List<QueryParameter> params = new ArrayList<QueryParameter>();
    final String info = WorkspaceObjectConverter.VAR_INFO;
    params.add(QueryParameterFactory.getPropProjectionParam(info));
    params.add(getIdParam(id));
    final PersistentWorkspace single = commonReadDao.getSingle(params);
    List<? extends VariationTemplate> templates = new ArrayList(single == null ? Collections.<VariationTemplate>
        emptyList() : single.getVariationTemplates());
    if (templates.isEmpty()) {
      return Collections.emptyList();
    }
    final Comparator<ResourceTemplate> comp;
    if (ResourceSortCriteria.BY_DATE.equals(criteria)) {
      comp = TEMPLATE_DATE_COMPARATOR;
    }
    else {
      comp = TEMPLATE_NAME_COMPARATOR;
    }
    Collections.sort(templates, comp);
    return Collections.unmodifiableCollection(templates);
  }

  private void updateFields(PersistableResourceTemplate template, ResourceTemplate oldTemplate) {
    final Date date = new Date();
    if (oldTemplate != null) {
      template.setCreatedDate(oldTemplate.getCreatedDate());
    }
    else {
      template.setCreatedDate(date);
    }
    template.setLastModifiedDate(date);
    template.setEntityTagValue(SmartContentAPI.getInstance().getWorkspaceApi().getEntityTagValueForResourceTemplate(
        template));
  }

  private void updateFields(PersistableValidatorTemplate template, ValidatorTemplate oldTemplate) {
    final Date date = new Date();
    if (oldTemplate != null) {
      template.setCreatedDate(oldTemplate.getCreatedDate());
    }
    else {
      template.setCreatedDate(date);
    }
    template.setLastModifiedDate(date);
    template.setEntityTagValue(SmartContentAPI.getInstance().getWorkspaceApi().getEntityTagValueForValidatorTemplate(
        template));
  }

  @Override
  public Collection<ContentId> getRootContents(WorkspaceId workspaceId) {
    final QueryParameter rootContentsProp = QueryParameterFactory.getPropProjectionParam("rootContents");
    final QueryParameter idParam = getIdParam(workspaceId);
    PersistentWorkspace workspace = commonReadDao.getSingle(idParam, SELF_PARAM, rootContentsProp);
    return workspace.getRootContents();
  }

  @Override
  public void addRootContent(WorkspaceId to, ContentId... contentIds) {
    PersistentWorkspace workspace = getWorkspace(to);
    for (ContentId id : contentIds) {
      workspace.addRootContent(id);
    }
    workspace.setRootContentsPopulated(true);
    commonWriteDao.update(workspace);
  }

  @Override
  public void removeRootContent(WorkspaceId from, ContentId contentId) {
    PersistentWorkspace workspace = getWorkspace(from);
    workspace.addRootContent(contentId);
    workspace.setRootContentsPopulated(true);
    commonWriteDao.delete(workspace);
  }

  @Override
  public void removeAllRootContents(WorkspaceId workspaceId) {
    PersistentWorkspace workspace = getWorkspace(workspaceId);
    workspace.setRootContentsPopulated(true);
    commonWriteDao.delete(workspace);
  }

  @Override
  public ValidatorTemplate getValidationTemplate(WorkspaceId workspaceId, String name) {
    List<QueryParameter> params = new ArrayList<QueryParameter>();
    final String info = WorkspaceObjectConverter.VAL_INFO;
    params.add(QueryParameterFactory.getPropProjectionParam(new StringBuilder(info).append(':').append(name).append(':').
        append(WorkspaceObjectConverter.TEMPLATETYPE).toString()));
    params.add(QueryParameterFactory.getPropProjectionParam(new StringBuilder(info).append(':').append(name).append(':').
        append(WorkspaceObjectConverter.CREATED).toString()));
    params.add(QueryParameterFactory.getPropProjectionParam(new StringBuilder(info).append(':').append(name).append(':').
        append(WorkspaceObjectConverter.LASTMODIFIED).toString()));
    params.add(QueryParameterFactory.getPropProjectionParam(new StringBuilder(info).append(':').append(name).append(':').
        append(WorkspaceObjectConverter.ENTITY_TAG).toString()));
    params.add(QueryParameterFactory.getPropProjectionParam(new StringBuilder(WorkspaceObjectConverter.VAL_DATA).append(
        ':').append(name).toString()));
    params.add(SELF_PARAM);
    params.add(getIdParam(workspaceId));
    final PersistentWorkspace single = commonReadDao.getSingle(params);
    if (logger.isInfoEnabled()) {
      logger.info("Workspace ID " + single.getId().toString());
      logger.info("Validators " + single.isValidatorsPopulated());
      logger.info("Validators " + single.getValidatorTemplates());
    }
    final List<PersistableValidatorTemplate> list = single.getValidatorTemplates();
    if (list.isEmpty()) {
      return null;
    }
    return list.get(0);
  }

  @Override
  public void deleteValidator(ValidatorTemplate template) {
    PersistentWorkspace workspace = getWorkspace(template.getWorkspaceId());
    workspace.setValidatorsPopulated(true);
    PersistableValidatorTemplate valTemplate = SmartContentSPI.getInstance().getPersistableDomainFactory().
        createPersistableValidatorTemplate();
    valTemplate.setCreatedDate(template.getCreatedDate());
    valTemplate.setLastModifiedDate(template.getLastModifiedDate());
    valTemplate.setWorkspaceId(template.getWorkspaceId());
    valTemplate.setName(template.getName());
    valTemplate.setTemplate(template.getTemplate());
    valTemplate.setTemplateType(template.getTemplateType());
    workspace.addValidatorTemplate(valTemplate);
    commonWriteDao.delete(workspace);
  }

  @Override
  public ValidatorTemplate putValidatorTemplate(WorkspaceId workspaceId, String name, ValidatorType templateType,
                                                byte[] data) {
    PersistentWorkspace workspace = getWorkspace(workspaceId);
    PersistableValidatorTemplate template = SmartContentSPI.getInstance().getPersistableDomainFactory().
        createPersistableValidatorTemplate();
    workspace.addValidatorTemplate(template);
    template.setName(name);
    template.setTemplateType(templateType);
    template.setWorkspaceId(workspaceId);
    template.setTemplate(data);
    ValidatorTemplate oldTemplate = getValidationTemplate(workspaceId, name);
    updateFields(template, oldTemplate);
    workspace.setValidatorsPopulated(true);
    commonWriteDao.update(workspace);
    return template;

  }

  @Override
  public Collection<ValidatorTemplate> getValidatorsWithoutData(WorkspaceId id,
                                                                ResourceSortCriteria criteria) {
    List<QueryParameter> params = new ArrayList<QueryParameter>();
    final String info = WorkspaceObjectConverter.VAL_INFO;
    params.add(QueryParameterFactory.getPropProjectionParam(info));
    params.add(getIdParam(id));
    final PersistentWorkspace single = commonReadDao.getSingle(params);
    List<? extends ValidatorTemplate> templates = new ArrayList(single == null ? Collections.<ValidatorTemplate>
        emptyList() : single.getValidatorTemplates());
    if (templates.isEmpty()) {
      return Collections.emptyList();
    }
    final Comparator<ValidatorTemplate> comp;
    if (ResourceSortCriteria.BY_DATE.equals(criteria)) {
      comp = VALIDATOR_DATE_COMPARATOR;
    }
    else {
      comp = VALIDATOR_NAME_COMPARATOR;
    }
    Collections.sort(templates, comp);
    return Collections.unmodifiableCollection(templates);
  }

  @Override
  public void removeAllValidatorTemplates(WorkspaceId workspaceId) {
    PersistentWorkspace workspace = getWorkspace(workspaceId);
    workspace.setValidatorsPopulated(true);
    commonWriteDao.delete(workspace);
  }

  public ContentCoProcessorTemplate putContentCoProcessorTemplate(WorkspaceId workspaceId, String name,
                                                                  TemplateType templateType, byte[] data) {
    PersistentWorkspace workspace = getWorkspace(workspaceId);
    PersistableContentCoProcessorTemplate template = SmartContentSPI.getInstance().getPersistableDomainFactory().
        createPersistableContentCoProcessorTemplate();
    workspace.addContentCoProcessorTemplate(template);
    template.setName(name);
    template.setTemplateType(templateType);
    template.setWorkspaceId(workspaceId);
    template.setTemplate(data);
    ContentCoProcessorTemplate oldTemplate = getContentCoProcessorTemplate(workspaceId, name);
    updateFields(template, oldTemplate);
    workspace.setContentCoProcessorPopulated(true);
    commonWriteDao.update(workspace);
    return template;
  }

  public ContentCoProcessorTemplate getContentCoProcessorTemplate(WorkspaceId workspaceId, String name) {
    List<QueryParameter> params = new ArrayList<QueryParameter>();
    final String info = WorkspaceObjectConverter.CCP_INFO;
    if (logger.isDebugEnabled()) {
      logger.debug("Sample column name " + new StringBuilder(info).append(':').append(name).append(':').
          append(WorkspaceObjectConverter.TEMPLATETYPE).toString());
    }
    params.add(QueryParameterFactory.getPropProjectionParam(new StringBuilder(info).append(':').append(name).append(':').
        append(WorkspaceObjectConverter.TEMPLATETYPE).toString()));
    params.add(QueryParameterFactory.getPropProjectionParam(new StringBuilder(info).append(':').append(name).append(':').
        append(WorkspaceObjectConverter.CREATED).toString()));
    params.add(QueryParameterFactory.getPropProjectionParam(new StringBuilder(info).append(':').append(name).append(':').
        append(WorkspaceObjectConverter.LASTMODIFIED).toString()));
    params.add(QueryParameterFactory.getPropProjectionParam(new StringBuilder(info).append(':').append(name).append(':').
        append(WorkspaceObjectConverter.ENTITY_TAG).toString()));
    params.add(QueryParameterFactory.getPropProjectionParam(new StringBuilder(WorkspaceObjectConverter.CCP_DATA).append(
        ':').append(name).toString()));
    params.add(SELF_PARAM);
    params.add(getIdParam(workspaceId));
    final PersistentWorkspace singleWs = commonReadDao.getSingle(params);
    if (logger.isDebugEnabled()) {
      logger.debug("Loaded CCP " + singleWs.isContentCoProcessorPopulated());
    }
    final List<PersistableContentCoProcessorTemplate> list = singleWs.getContentCoProcessorTemplates();
    if (list.isEmpty()) {
      if (logger.isWarnEnabled()) {
        logger.warn("No content co processor with name " + name + " in " + workspaceId);
      }
      return null;
    }
    return list.get(0);
  }

  public void removeAllContentCoProcessorTemplates(WorkspaceId workspaceId) {
    PersistentWorkspace workspace = getWorkspace(workspaceId);
    workspace.setContentCoProcessorPopulated(true);
    commonWriteDao.delete(workspace);
  }

  public Collection<ContentCoProcessorTemplate> getContentCoProcessorsWithoutData(WorkspaceId id,
                                                                                  ResourceSortCriteria criteria) {
    List<QueryParameter> params = new ArrayList<QueryParameter>();
    final String info = WorkspaceObjectConverter.CCP_INFO;
    params.add(QueryParameterFactory.getPropProjectionParam(info));
    params.add(getIdParam(id));
    final PersistentWorkspace single = commonReadDao.getSingle(params);
    if (logger.isDebugEnabled()) {
      if (single == null) {
        logger.debug("Query result is null!");
      }
      else {
        logger.debug("Loaded CCP " + single.isContentCoProcessorPopulated() + " " +
            single.getContentCoProcessorTemplates());
      }
    }
    List<? extends ContentCoProcessorTemplate> templates = new ArrayList(single == null ?
        Collections.<ContentCoProcessorTemplate>emptyList() : single.getContentCoProcessorTemplates());
    if (templates.isEmpty()) {
      return Collections.emptyList();
    }
    final Comparator<ResourceTemplate> comp;
    if (ResourceSortCriteria.BY_DATE.equals(criteria)) {
      comp = TEMPLATE_DATE_COMPARATOR;
    }
    else {
      comp = TEMPLATE_NAME_COMPARATOR;
    }
    Collections.sort(templates, comp);
    return Collections.unmodifiableCollection(templates);
  }

  public void deleteContentCoProcessor(ContentCoProcessorTemplate template) {
    PersistentWorkspace workspace = getWorkspace(template.getWorkspaceId());
    workspace.setContentCoProcessorPopulated(true);
    PersistableContentCoProcessorTemplate varTemplate = SmartContentSPI.getInstance().getPersistableDomainFactory().
        createPersistableContentCoProcessorTemplate();
    varTemplate.setCreatedDate(template.getCreatedDate());
    varTemplate.setLastModifiedDate(template.getLastModifiedDate());
    varTemplate.setWorkspaceId(template.getWorkspaceId());
    varTemplate.setName(template.getName());
    varTemplate.setTemplate(template.getTemplate());
    varTemplate.setTemplateType(template.getTemplateType());
    workspace.addContentCoProcessorTemplate(varTemplate);
    commonWriteDao.delete(workspace);
  }
}
