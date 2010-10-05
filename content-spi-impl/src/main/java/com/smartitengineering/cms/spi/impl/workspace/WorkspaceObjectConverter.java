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

import com.smartitengineering.cms.api.SmartContentAPI;
import com.smartitengineering.cms.api.common.TemplateType;
import com.smartitengineering.cms.api.workspace.RepresentationTemplate;
import com.smartitengineering.cms.api.workspace.ResourceTemplate;
import com.smartitengineering.cms.api.workspace.VariationTemplate;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.cms.spi.SmartContentSPI;
import com.smartitengineering.cms.spi.impl.Utils;
import com.smartitengineering.cms.spi.workspace.PersistableRepresentationTemplate;
import com.smartitengineering.cms.spi.workspace.PersistableResourceTemplate;
import com.smartitengineering.cms.spi.workspace.PersistableVariationTemplate;
import com.smartitengineering.cms.spi.workspace.PersistableWorkspace;
import com.smartitengineering.dao.impl.hbase.spi.ExecutorService;
import com.smartitengineering.dao.impl.hbase.spi.impl.AbstactObjectRowConverter;
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
public class WorkspaceObjectConverter extends AbstactObjectRowConverter<PersistentWorkspace, WorkspaceId> {

  public static final String FRIENDLIES = "friendlies";
  public static final String LASTMODIFIED = "lastModified";
  public static final String NAME = "name";
  public static final String REP_DATA = "repData";
  public static final String REP_INFO = "repInfo";
  public static final String TEMPLATETYPE = "templateType";
  public static final String VAR_DATA = "varData";
  public static final String VAR_INFO = "varInfo";
  public static final String CREATED = "created";
  public static final byte[] FAMILY_SELF = Bytes.toBytes("self");
  public static final byte[] FAMILY_REPRESENTATIONS_INFO = Bytes.toBytes(REP_INFO);
  public static final byte[] FAMILY_REPRESENTATIONS_DATA = Bytes.toBytes(REP_DATA);
  public static final byte[] FAMILY_VARIATIONS_INFO = Bytes.toBytes(VAR_INFO);
  public static final byte[] FAMILY_VARIATIONS_DATA = Bytes.toBytes(VAR_DATA);
  public static final byte[] FAMILY_FRIENDLIES = Bytes.toBytes(FRIENDLIES);
  public static final byte[] CELL_NAMESPACE = Bytes.toBytes("namespace");
  public static final byte[] CELL_NAME = Bytes.toBytes(NAME);
  public static final byte[] CELL_CREATED = Bytes.toBytes(CREATED);
  public static final byte[] CELL_LAST_MODIFIED = Bytes.toBytes(LASTMODIFIED);
  public static final byte[] CELL_TEMPLATE_TYPE = Bytes.toBytes(TEMPLATETYPE);

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
        popolatePutWithResource(FAMILY_REPRESENTATIONS_INFO, template, put);
        popolatePutWithResourceData(FAMILY_REPRESENTATIONS_DATA, template, put);
      }
    }
    if (instance.isVariationPopulated() && !instance.getVariationTemplates().isEmpty()) {
      for (PersistableVariationTemplate template : instance.getVariationTemplates()) {
        popolatePutWithResource(FAMILY_VARIATIONS_INFO, template, put);
        popolatePutWithResourceData(FAMILY_VARIATIONS_DATA, template, put);
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
  }

  protected byte[] getPrefixForResource(ResourceTemplate template) {
    return Bytes.toBytes(new StringBuilder(template.getName()).append(':').toString());
  }

  protected void popolatePutWithResource(byte[] family, PersistableResourceTemplate template, Put put) {
    byte[] prefix = getPrefixForResource(template);
    put.add(family, Bytes.add(prefix, CELL_TEMPLATE_TYPE), Bytes.toBytes(template.getTemplateType().name()));
    final Date created = template.getCreatedDate() == null ? new Date() : template.getCreatedDate();
    put.add(family, Bytes.add(prefix, CELL_CREATED), Utils.toBytes(created));
    template.setCreatedDate(created);
    final Date lastModified = template.getLastModifiedDate() == null ? created : template.getLastModifiedDate();
    put.add(family, Bytes.add(prefix, CELL_LAST_MODIFIED), Utils.toBytes(lastModified));
    template.setLastModifiedDate(lastModified);
  }

  protected void popolatePutWithResourceData(byte[] family, ResourceTemplate template, Put put) {
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
    if (!(instance.isFriendliesPopulated() || instance.isRepresentationPopulated() || instance.isVariationPopulated())) {
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
    }
  }

  protected void addResourceColumnsToDelete(byte[] family, Delete delete, ResourceTemplate resourceTemplate) {
    byte[] prefix = getPrefixForResource(resourceTemplate);
    delete.deleteColumns(family, Bytes.add(prefix, CELL_CREATED));
    delete.deleteColumns(family, Bytes.add(prefix, CELL_LAST_MODIFIED));
    delete.deleteColumns(family, Bytes.add(prefix, CELL_TEMPLATE_TYPE));
  }

  protected void addResourceDataColumnsToDelete(byte[] family, Delete delete, ResourceTemplate resourceTemplate) {
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
  }

  protected void populateResourceTemplateData(String repName, PersistableResourceTemplate template,
                                              Map<String, byte[]> cells) {
    if (cells != null) {
      template.setTemplate(cells.get(repName));
    }
  }
}
