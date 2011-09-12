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
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.common.TemplateType;
import com.smartitengineering.cms.api.content.ContentId;
import com.smartitengineering.cms.api.type.ValidatorType;
import com.smartitengineering.cms.api.workspace.ContentCoProcessorTemplate;
import com.smartitengineering.cms.api.workspace.RepresentationTemplate;
import com.smartitengineering.cms.api.workspace.ResourceTemplate;
import com.smartitengineering.cms.api.workspace.ValidatorTemplate;
import com.smartitengineering.cms.api.workspace.VariationTemplate;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.cms.spi.SmartContentSPI;
import com.smartitengineering.cms.spi.impl.hbase.Utils;
import com.smartitengineering.cms.spi.impl.content.PersistentContent;
import com.smartitengineering.cms.spi.workspace.PersistableContentCoProcessorTemplate;
import com.smartitengineering.cms.spi.workspace.PersistableRepresentationTemplate;
import com.smartitengineering.cms.spi.workspace.PersistableResourceTemplate;
import com.smartitengineering.cms.spi.workspace.PersistableValidatorTemplate;
import com.smartitengineering.cms.spi.workspace.PersistableVariationTemplate;
import com.smartitengineering.cms.spi.workspace.PersistableWorkspace;
import com.smartitengineering.dao.impl.hbase.spi.ExecutorService;
import com.smartitengineering.dao.impl.hbase.spi.SchemaInfoProvider;
import com.smartitengineering.dao.impl.hbase.spi.impl.AbstractObjectRowConverter;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NavigableMap;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

/**
 *
 * @author imyousuf
 */
public class WorkspaceObjectConverter extends AbstractObjectRowConverter<PersistentWorkspace, WorkspaceId> {

  public static final String FRIENDLIES = "friendlies";
  public static final String ROOT_CONTENTS = "rootContents";
  public static final String LASTMODIFIED = "lastModified";
  public static final String NAME = "name";
  public static final String REP_DATA = "repData";
  public static final String REP_INFO = "repInfo";
  public static final String TEMPLATETYPE = "templateType";
  public static final String VAR_DATA = "varData";
  public static final String VAR_INFO = "varInfo";
  public static final String CCP_DATA = "ccpData";
  public static final String CCP_INFO = "ccpInfo";
  public static final String VAL_DATA = "valData";
  public static final String VAL_INFO = "valInfo";
  public static final String CREATED = "created";
  public static final String ENTITY_TAG = "entityTag";
  public static final byte[] FAMILY_SELF = Bytes.toBytes("self");
  public static final byte[] FAMILY_REPRESENTATIONS_INFO = Bytes.toBytes(REP_INFO);
  public static final byte[] FAMILY_REPRESENTATIONS_DATA = Bytes.toBytes(REP_DATA);
  public static final byte[] FAMILY_VARIATIONS_INFO = Bytes.toBytes(VAR_INFO);
  public static final byte[] FAMILY_VARIATIONS_DATA = Bytes.toBytes(VAR_DATA);
  public static final byte[] FAMILY_CCP_INFO = Bytes.toBytes(CCP_INFO);
  public static final byte[] FAMILY_CCP_DATA = Bytes.toBytes(CCP_DATA);
  public static final byte[] FAMILY_VALIDATORS_INFO = Bytes.toBytes(VAL_INFO);
  public static final byte[] FAMILY_VALIDATORS_DATA = Bytes.toBytes(VAL_DATA);
  public static final byte[] FAMILY_FRIENDLIES = Bytes.toBytes(FRIENDLIES);
  public static final byte[] FAMILY_ROOT_CONTENTS = Bytes.toBytes(ROOT_CONTENTS);
  public static final byte[] CELL_NAMESPACE = Bytes.toBytes("namespace");
  public static final byte[] CELL_NAME = Bytes.toBytes(NAME);
  public static final byte[] CELL_CREATED = Bytes.toBytes(CREATED);
  public static final byte[] CELL_LAST_MODIFIED = Bytes.toBytes(LASTMODIFIED);
  public static final byte[] CELL_TEMPLATE_TYPE = Bytes.toBytes(TEMPLATETYPE);
  public static final byte[] CELL_ENTITY_TAG = Bytes.toBytes(ENTITY_TAG);
  @Inject
  private SchemaInfoProvider<PersistentContent, ContentId> contentSchemaProvider;

  @Override
  protected String[] getTablesToAttainLock() {
    return new String[]{getInfoProvider().getMainTableName()};
  }

  @Override
  protected void getPutForTable(PersistentWorkspace instance, ExecutorService service, Put put) {
    put.add(FAMILY_SELF, CELL_NAMESPACE, Bytes.toBytes(instance.getId().getGlobalNamespace()));
    put.add(FAMILY_SELF, CELL_NAME, Bytes.toBytes(instance.getId().getName()));
    put.add(FAMILY_SELF, CELL_CREATED, Utils.toBytes(instance.getWorkspace().getCreationDate()));
    if (instance.isRepresentationPopulated() && !instance.getRepresentationTemplates().isEmpty()) {
      for (PersistableRepresentationTemplate template : instance.getRepresentationTemplates()) {
        if (logger.isDebugEnabled()) {
          logger.debug("PUTTING representation " + template.getName());
        }
        populatePutWithResource(FAMILY_REPRESENTATIONS_INFO, template, put);
        populatePutWithResourceData(FAMILY_REPRESENTATIONS_DATA, template, put);
      }
    }
    if (instance.isVariationPopulated() && !instance.getVariationTemplates().isEmpty()) {
      for (PersistableVariationTemplate template : instance.getVariationTemplates()) {
        populatePutWithResource(FAMILY_VARIATIONS_INFO, template, put);
        populatePutWithResourceData(FAMILY_VARIATIONS_DATA, template, put);
      }
    }
    if (logger.isDebugEnabled()) {
      logger.debug("ContentCoProcessor Populated " + instance.isContentCoProcessorPopulated() + ", checking " +
          (instance.isContentCoProcessorPopulated() && !instance.getContentCoProcessorTemplates().isEmpty()));
    }
    if (instance.isContentCoProcessorPopulated() && !instance.getContentCoProcessorTemplates().isEmpty()) {
      for (PersistableContentCoProcessorTemplate template : instance.getContentCoProcessorTemplates()) {
        if (logger.isDebugEnabled()) {
          logger.debug("ContentCoProcessor template with name " + template.getName() + " being saved");
        }
        populatePutWithResource(FAMILY_CCP_INFO, template, put);
        populatePutWithResourceData(FAMILY_CCP_DATA, template, put);
      }
    }
    if (instance.isValidatorsPopulated() && !instance.getValidatorTemplates().isEmpty()) {
      logger.info("Saving validators");
      for (PersistableValidatorTemplate template : instance.getValidatorTemplates()) {
        if (logger.isInfoEnabled()) {
          logger.info("Validator being saved is " + template.getName());
        }
        populatePutWithValidator(FAMILY_VALIDATORS_INFO, template, put);
        populatePutWithValidatorData(FAMILY_VALIDATORS_DATA, template, put);
      }
    }
    if (instance.isFriendliesPopulated()) {
      for (WorkspaceId friendly : instance.getFriendlies()) {
        try {
          put.add(FAMILY_FRIENDLIES, getInfoProvider().getRowIdFromId(friendly), Utils.toBytes(new Date()));
        }
        catch (IOException ex) {
          logger.warn("Error putting friendly", ex);
        }
      }
    }
    if (instance.isRootContentsPopulated()) {
      for (ContentId contentId : instance.getRootContents()) {
        try {
          put.add(FAMILY_ROOT_CONTENTS, contentSchemaProvider.getRowIdFromId(contentId), Utils.toBytes(new Date()));
        }
        catch (IOException ex) {
          logger.warn("Error putting friendly", ex);
        }
      }
    }
  }

  protected byte[] getPrefixForResource(ResourceTemplate template) {
    return Bytes.toBytes(new StringBuilder(template.getName()).append(':').toString());
  }

  protected byte[] getPrefixForValidator(ValidatorTemplate template) {
    return Bytes.toBytes(new StringBuilder(template.getName()).append(':').toString());
  }

  protected void populatePutWithResource(byte[] family, PersistableResourceTemplate template, Put put) {
    byte[] prefix = getPrefixForResource(template);
    put.add(family, Bytes.add(prefix, CELL_TEMPLATE_TYPE), Bytes.toBytes(template.getTemplateType().name()));
    final Date created = template.getCreatedDate() == null ? new Date() : template.getCreatedDate();
    put.add(family, Bytes.add(prefix, CELL_CREATED), Utils.toBytes(created));
    template.setCreatedDate(created);
    final Date lastModified = template.getLastModifiedDate() == null ? created : template.getLastModifiedDate();
    put.add(family, Bytes.add(prefix, CELL_LAST_MODIFIED), Utils.toBytes(lastModified));
    template.setLastModifiedDate(lastModified);
    if (logger.isInfoEnabled()) {
      logger.info("PUTTING Entity Tag: " + template.getEntityTagValue());
    }
    put.add(family, Bytes.add(prefix, CELL_ENTITY_TAG), Bytes.toBytes(template.getEntityTagValue()));
  }

  protected void populatePutWithResourceData(byte[] family, ResourceTemplate template, Put put) {
    byte[] key = Bytes.toBytes(template.getName());
    put.add(family, key, template.getTemplate());
  }

  protected void populatePutWithValidator(byte[] family, PersistableValidatorTemplate template, Put put) {
    byte[] prefix = getPrefixForValidator(template);
    put.add(family, Bytes.add(prefix, CELL_TEMPLATE_TYPE), Bytes.toBytes(template.getTemplateType().name()));
    final Date created = template.getCreatedDate() == null ? new Date() : template.getCreatedDate();
    put.add(family, Bytes.add(prefix, CELL_CREATED), Utils.toBytes(created));
    template.setCreatedDate(created);
    final Date lastModified = template.getLastModifiedDate() == null ? created : template.getLastModifiedDate();
    put.add(family, Bytes.add(prefix, CELL_LAST_MODIFIED), Utils.toBytes(lastModified));
    template.setLastModifiedDate(lastModified);
    if (logger.isDebugEnabled()) {
      logger.debug("PUTTING Entity Tag: " + template.getEntityTagValue());
    }
    put.add(family, Bytes.add(prefix, CELL_ENTITY_TAG), Bytes.toBytes(template.getEntityTagValue()));
  }

  protected void populatePutWithValidatorData(byte[] family, ValidatorTemplate template, Put put) {
    byte[] key = Bytes.toBytes(template.getName());
    put.add(family, key, template.getTemplate());
  }

  @Override
  protected void getDeleteForTable(PersistentWorkspace instance, ExecutorService service, Delete delete) {
    if (logger.isInfoEnabled()) {
      logger.info(new StringBuilder("Deleting workspace (full/partial) with ID: ").append(instance.getId()).toString());
    }
    /*
     * Delete whole workspace
     */
    if (!(instance.isFriendliesPopulated() || instance.isRepresentationPopulated() || instance.isVariationPopulated() ||
          instance.isRootContentsPopulated() || instance.isContentCoProcessorPopulated() || instance.
          isValidatorsPopulated())) {
      if (logger.isInfoEnabled()) {
        logger.info(new StringBuilder("Deleting whole workspace with ID: ").append(instance.getId()).toString());
      }
      // Do nothing
    }
    /*
     * Might need to delete any part of it
     */
    else {
      if (instance.isRepresentationPopulated()) {
        /*
         * Delete all representations
         */
        if (instance.getRepresentationTemplates().isEmpty()) {
          logger.info("Delete all representations");
          delete.deleteFamily(FAMILY_REPRESENTATIONS_INFO);
          delete.deleteFamily(FAMILY_REPRESENTATIONS_DATA);
        }
        /*
         * Delete particular representation(s)
         */
        else {
          logger.info("Delete selected representations");
          for (RepresentationTemplate representationTemplate : instance.getRepresentationTemplates()) {
            if (logger.isDebugEnabled()) {
              logger.debug(new StringBuilder("Deleting representation ").append(representationTemplate.getName()).
                  toString());
            }
            addResourceColumnsToDelete(FAMILY_REPRESENTATIONS_INFO, delete, representationTemplate);
            addResourceDataColumnsToDelete(FAMILY_REPRESENTATIONS_DATA, delete, representationTemplate);
          }
        }
      }
      if (instance.isVariationPopulated()) {
        /*
         * Delete all variations
         */
        if (instance.getVariationTemplates().isEmpty()) {
          logger.info("Delete all variations");
          delete.deleteFamily(FAMILY_VARIATIONS_INFO);
          delete.deleteFamily(FAMILY_VARIATIONS_DATA);
        }
        /*
         * Delete particular variation(s)
         */
        else {
          logger.info("Delete selected variations");
          for (VariationTemplate varTemplate : instance.getVariationTemplates()) {
            if (logger.isDebugEnabled()) {
              logger.debug(new StringBuilder("Deleting variation ").append(varTemplate.getName()).toString());
            }
            addResourceColumnsToDelete(FAMILY_VARIATIONS_INFO, delete, varTemplate);
            addResourceDataColumnsToDelete(FAMILY_VARIATIONS_DATA, delete, varTemplate);
          }
        }
      }
      if (instance.isContentCoProcessorPopulated()) {
        /*
         * Delete all ContentCoProcessors
         */
        if (instance.getContentCoProcessorTemplates().isEmpty()) {
          logger.info("Delete all content co-processor templates");
          delete.deleteFamily(FAMILY_CCP_INFO);
          delete.deleteFamily(FAMILY_CCP_DATA);
        }
        /*
         * Delete particular variation(s)
         */
        else {
          logger.info("Delete selected content co-processor templates");
          for (ContentCoProcessorTemplate template : instance.getContentCoProcessorTemplates()) {
            if (logger.isDebugEnabled()) {
              logger.debug(new StringBuilder("Deleting content co-processor template ").append(template.getName()).
                  toString());
            }
            addResourceColumnsToDelete(FAMILY_CCP_INFO, delete, template);
            addResourceDataColumnsToDelete(FAMILY_CCP_DATA, delete, template);
          }
        }
      }
      if (instance.isValidatorsPopulated()) {
        /*
         * Delete all validators
         */
        if (instance.getValidatorTemplates().isEmpty()) {
          logger.info("Delete all validators");
          delete.deleteFamily(FAMILY_VALIDATORS_INFO);
          delete.deleteFamily(FAMILY_VALIDATORS_DATA);
        }
        /*
         * Delete particular validator(s)
         */
        else {
          logger.info("Delete selected validators");
          for (ValidatorTemplate valTemplate : instance.getValidatorTemplates()) {
            if (logger.isDebugEnabled()) {
              logger.debug(new StringBuilder("Deleting validator ").append(valTemplate.getName()).toString());
            }
            addValidatorColumnsToDelete(FAMILY_VALIDATORS_INFO, delete, valTemplate);
            addValidatorDataColumnsToDelete(FAMILY_VALIDATORS_DATA, delete, valTemplate);
          }
        }
      }
      if (instance.isFriendliesPopulated()) {
        if (instance.getFriendlies().isEmpty()) {
          logger.info("Delete all friendlies");
          delete.deleteFamily(FAMILY_FRIENDLIES);
        }
        else {
          logger.info("Delete selected friendlies");
          for (WorkspaceId friendly : instance.getFriendlies()) {
            try {
              if (logger.isDebugEnabled()) {
                logger.debug(new StringBuilder("Deleting friendly ").append(friendly.toString()).toString());
              }
              delete.deleteColumns(FAMILY_FRIENDLIES, getInfoProvider().getRowIdFromId(friendly));
            }
            catch (IOException ex) {
              logger.warn("Error deleting friendly", ex);
            }
          }
        }
      }
      if (instance.isRootContentsPopulated()) {
        /*
         * Delete all root content relations
         */
        if (instance.getRootContents().isEmpty()) {
          logger.info("Delete all root content  relations");
          delete.deleteFamily(FAMILY_ROOT_CONTENTS);
        }
        /*
         * Delete selected root content relations
         */
        else {
          logger.info("Delete selected root content relations");
          for (ContentId contentId : instance.getRootContents()) {
            try {
              if (logger.isDebugEnabled()) {
                logger.debug(new StringBuilder("Deleting content relation ").append(contentId.toString()).toString());
              }
              delete.deleteColumns(FAMILY_ROOT_CONTENTS, contentSchemaProvider.getRowIdFromId(contentId));
            }
            catch (IOException ex) {
              logger.warn("Error deleting root content relation", ex);
            }
          }
        }
      }
    }
  }

  protected void addResourceColumnsToDelete(byte[] family, Delete delete, ResourceTemplate resourceTemplate) {
    byte[] prefix = getPrefixForResource(resourceTemplate);
    delete.deleteColumns(family, Bytes.add(prefix, CELL_CREATED));
    delete.deleteColumns(family, Bytes.add(prefix, CELL_LAST_MODIFIED));
    delete.deleteColumns(family, Bytes.add(prefix, CELL_TEMPLATE_TYPE));
    delete.deleteColumns(family, Bytes.add(prefix, CELL_ENTITY_TAG));
  }

  protected void addResourceDataColumnsToDelete(byte[] family, Delete delete, ResourceTemplate resourceTemplate) {
    delete.deleteColumns(family, Bytes.toBytes(resourceTemplate.getName()));
  }

  protected void addValidatorColumnsToDelete(byte[] family, Delete delete, ValidatorTemplate resourceTemplate) {
    byte[] prefix = getPrefixForValidator(resourceTemplate);
    delete.deleteColumns(family, Bytes.add(prefix, CELL_CREATED));
    delete.deleteColumns(family, Bytes.add(prefix, CELL_LAST_MODIFIED));
    delete.deleteColumns(family, Bytes.add(prefix, CELL_TEMPLATE_TYPE));
    delete.deleteColumns(family, Bytes.add(prefix, CELL_ENTITY_TAG));
  }

  protected void addValidatorDataColumnsToDelete(byte[] family, Delete delete, ValidatorTemplate resourceTemplate) {
    delete.deleteColumns(family, Bytes.toBytes(resourceTemplate.getName()));
  }

  @Override
  public PersistentWorkspace rowsToObject(Result startRow, ExecutorService executorService) {
    PersistableWorkspace workspace = SmartContentSPI.getInstance().getPersistableDomainFactory().
        createPersistentWorkspace();
    final PersistentWorkspace persistentWorkspace = new PersistentWorkspace();
    NavigableMap<byte[], NavigableMap<byte[], byte[]>> allFamilies = startRow.getNoVersionMap();
    final NavigableMap<byte[], byte[]> self = allFamilies.get(FAMILY_SELF);
    if (self != null && !self.isEmpty()) {
      workspace.setId(SmartContentAPI.getInstance().getWorkspaceApi().createWorkspaceId(Bytes.toString(self.get(
          CELL_NAMESPACE)), Bytes.toString(self.get(CELL_NAME))));
      workspace.setCreationDate(Utils.toDate(self.get(CELL_CREATED)));
      persistentWorkspace.setWorkspace(workspace);
    }
    {
      final NavigableMap<byte[], byte[]> repInfo = allFamilies.get(FAMILY_REPRESENTATIONS_INFO);
      final NavigableMap<byte[], byte[]> repData = allFamilies.get(FAMILY_REPRESENTATIONS_DATA);
      if (repInfo != null) {
        persistentWorkspace.setRepresentationPopulated(true);
        final Map<String, Map<String, byte[]>> repsByName = new LinkedHashMap<String, Map<String, byte[]>>();
        Utils.organizeByPrefix(repInfo, repsByName, ':');
        final Map<String, Map<String, byte[]>> repsDataByName = new LinkedHashMap<String, Map<String, byte[]>>();
        if (repData != null) {
          Utils.organizeByPrefix(repData, repsDataByName, ':');
        }
        for (String repName : repsByName.keySet()) {
          PersistableRepresentationTemplate template = SmartContentSPI.getInstance().getPersistableDomainFactory().
              createPersistableRepresentationTemplate();
          template.setWorkspaceId(workspace.getId());
          populateResourceTemplateInfo(repName, template, repsByName.get(repName));
          populateResourceTemplateData(repName, template, repsDataByName.get(repName));
          persistentWorkspace.addRepresentationTemplate(template);
        }
      }
    }
    {
      final NavigableMap<byte[], byte[]> varInfo = allFamilies.get(FAMILY_VARIATIONS_INFO);
      final NavigableMap<byte[], byte[]> varData = allFamilies.get(FAMILY_VARIATIONS_DATA);
      if (varInfo != null) {
        persistentWorkspace.setVariationPopulated(true);
        final Map<String, Map<String, byte[]>> varsByName = new LinkedHashMap<String, Map<String, byte[]>>();
        Utils.organizeByPrefix(varInfo, varsByName, ':');
        final Map<String, Map<String, byte[]>> varsDataByName = new LinkedHashMap<String, Map<String, byte[]>>();
        if (varData != null) {
          Utils.organizeByPrefix(varData, varsDataByName, ':');
        }
        for (String varName : varsByName.keySet()) {
          PersistableVariationTemplate template = SmartContentSPI.getInstance().getPersistableDomainFactory().
              createPersistableVariationTemplate();
          template.setWorkspaceId(workspace.getId());
          populateResourceTemplateInfo(varName, template, varsByName.get(varName));
          populateResourceTemplateData(varName, template, varsDataByName.get(varName));
          persistentWorkspace.addVariationTemplate(template);
        }
      }
    }
    {
      final NavigableMap<byte[], byte[]> ccpInfo = allFamilies.get(FAMILY_CCP_INFO);
      final NavigableMap<byte[], byte[]> ccpData = allFamilies.get(FAMILY_CCP_DATA);
      if (ccpInfo != null) {
        persistentWorkspace.setContentCoProcessorPopulated(true);
        final Map<String, Map<String, byte[]>> ccpsByName = new LinkedHashMap<String, Map<String, byte[]>>();
        Utils.organizeByPrefix(ccpInfo, ccpsByName, ':');
        final Map<String, Map<String, byte[]>> ccpsDataByName = new LinkedHashMap<String, Map<String, byte[]>>();
        if (logger.isDebugEnabled()) {
          logger.debug("CCPs By Name " + ccpsByName);
        }
        if (ccpData != null) {
          Utils.organizeByPrefix(ccpData, ccpsDataByName, ':');
        }
        for (String ccpName : ccpsByName.keySet()) {
          if (logger.isDebugEnabled()) {
            logger.debug("Populate CCP with name " + ccpName);
          }
          PersistableContentCoProcessorTemplate template = SmartContentSPI.getInstance().getPersistableDomainFactory().
              createPersistableContentCoProcessorTemplate();
          template.setWorkspaceId(workspace.getId());
          populateResourceTemplateInfo(ccpName, template, ccpsByName.get(ccpName));
          populateResourceTemplateData(ccpName, template, ccpsDataByName.get(ccpName));
          persistentWorkspace.addContentCoProcessorTemplate(template);
        }
      }
    }
    {
      final Map<byte[], byte[]> friendlies = allFamilies.get(FAMILY_FRIENDLIES);
      if (friendlies != null && !friendlies.isEmpty()) {
        persistentWorkspace.setFriendliesPopulated(true);
        for (byte[] workspaceId : friendlies.keySet()) {
          try {
            persistentWorkspace.addFriendly(getInfoProvider().getIdFromRowId(workspaceId));
          }
          catch (Exception ex) {
            logger.warn("Error putting friendly", ex);
          }
        }
      }
    }
    {
      final Map<byte[], byte[]> rootContents = allFamilies.get(FAMILY_ROOT_CONTENTS);
      if (rootContents != null && !rootContents.isEmpty()) {
        persistentWorkspace.setRootContentsPopulated(true);
        for (byte[] conentId : rootContents.keySet()) {
          try {
            persistentWorkspace.addRootContent(contentSchemaProvider.getIdFromRowId(conentId));
          }
          catch (Exception ex) {
            logger.warn("Error putting root content", ex);
          }
        }
      }
    }
    {
      final NavigableMap<byte[], byte[]> valInfo = allFamilies.get(FAMILY_VALIDATORS_INFO);
      final NavigableMap<byte[], byte[]> valData = allFamilies.get(FAMILY_VALIDATORS_DATA);
      if (valInfo != null) {
        logger.info("Loading validators");
        persistentWorkspace.setValidatorsPopulated(true);
        final Map<String, Map<String, byte[]>> valsByName = new LinkedHashMap<String, Map<String, byte[]>>();
        Utils.organizeByPrefix(valInfo, valsByName, ':');
        final Map<String, Map<String, byte[]>> valsDataByName = new LinkedHashMap<String, Map<String, byte[]>>();
        if (valData != null) {
          Utils.organizeByPrefix(valData, valsDataByName, ':');
        }
        for (String valName : valsByName.keySet()) {
          if (logger.isInfoEnabled()) {
            logger.info("Loading validator by name " + valName);
          }
          PersistableValidatorTemplate template = SmartContentSPI.getInstance().getPersistableDomainFactory().
              createPersistableValidatorTemplate();
          template.setWorkspaceId(workspace.getId());
          populateValidatorTemplateInfo(valName, template, valsByName.get(valName));
          populateValidatorTemplateData(valName, template, valsDataByName.get(valName));
          persistentWorkspace.addValidatorTemplate(template);
        }
      }
    }
    return persistentWorkspace;
  }

  protected void populateResourceTemplateInfo(String repName, PersistableResourceTemplate template,
                                              Map<String, byte[]> cells) {
    template.setName(repName);
    String prefix = Bytes.toString(getPrefixForResource(template));
    String key = new StringBuilder(prefix).append(TEMPLATETYPE).toString();
    String type = Bytes.toString(cells.get(key));
    if (logger.isDebugEnabled()) {
      logger.debug("CELLS " + cells);
      logger.debug("PREFIX " + prefix);
      logger.debug("For " + key + " value is " + type);
    }
    template.setTemplateType(TemplateType.valueOf(type));
    template.setCreatedDate(Utils.toDate(cells.get(new StringBuilder(prefix).append(CREATED).toString())));
    template.setLastModifiedDate(Utils.toDate(cells.get(new StringBuilder(prefix).append(LASTMODIFIED).toString())));
    template.setEntityTagValue(Bytes.toString(cells.get(new StringBuilder(prefix).append(ENTITY_TAG).toString())));
  }

  protected void populateResourceTemplateData(String repName, PersistableResourceTemplate template,
                                              Map<String, byte[]> cells) {
    if (cells != null) {
      template.setTemplate(cells.get(repName));
    }
  }

  protected void populateValidatorTemplateInfo(String valName, PersistableValidatorTemplate template,
                                               Map<String, byte[]> cells) {
    template.setName(valName);
    String prefix = Bytes.toString(getPrefixForValidator(template));
    String key = new StringBuilder(prefix).append(TEMPLATETYPE).toString();
    String type = Bytes.toString(cells.get(key));
    if (logger.isDebugEnabled()) {
      logger.debug("CELLS " + cells);
      logger.debug("PREFIX " + prefix);
      logger.debug("For " + key + " value is " + type);
    }
    template.setTemplateType(ValidatorType.valueOf(type));
    template.setCreatedDate(Utils.toDate(cells.get(new StringBuilder(prefix).append(CREATED).toString())));
    template.setLastModifiedDate(Utils.toDate(cells.get(new StringBuilder(prefix).append(LASTMODIFIED).toString())));
    template.setEntityTagValue(Bytes.toString(cells.get(new StringBuilder(prefix).append(ENTITY_TAG).toString())));
  }

  protected void populateValidatorTemplateData(String valName, PersistableValidatorTemplate template,
                                               Map<String, byte[]> cells) {
    if (cells != null) {
      template.setTemplate(cells.get(valName));
    }
  }
}
