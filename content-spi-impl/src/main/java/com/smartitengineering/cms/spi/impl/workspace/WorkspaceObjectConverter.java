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
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.cms.spi.SmartContentSPI;
import com.smartitengineering.cms.spi.impl.Utils;
import com.smartitengineering.cms.spi.workspace.PersistableWorkspace;
import com.smartitengineering.dao.impl.hbase.spi.ExecutorService;
import com.smartitengineering.dao.impl.hbase.spi.impl.AbstactObjectRowConverter;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

/**
 *
 * @author imyousuf
 */
public class WorkspaceObjectConverter extends AbstactObjectRowConverter<PersistentWorkspace, WorkspaceId> {

  public static final String FRIENDLY_PREFIX = "friendly:";
  public static final String REPRESENTATION_PREFIX = "representation:";
  public static final String VARIATION_PREFIX = "variation:";
  public static final byte[] FAMILY_SELF = Bytes.toBytes("self");
  public static final byte[] FAMILY_RESOURCES = Bytes.toBytes("resources");
  public static final byte[] FAMILY_FRIENDLIES = Bytes.toBytes("friendlies");
  public static final byte[] CELL_NAMESPACE = Bytes.toBytes("namespace");
  public static final byte[] CELL_NAME = Bytes.toBytes("name");
  public static final byte[] CELL_CREATED = Bytes.toBytes("created");
  public static final byte[] CELL_REP_PREFIX = Bytes.toBytes(REPRESENTATION_PREFIX);
  public static final byte[] CELL_VAR_PREFIX = Bytes.toBytes(VARIATION_PREFIX);
  public static final byte[] CELL_FRIENDLY_PREFIX = Bytes.toBytes(FRIENDLY_PREFIX);

  @Override
  protected String[] getTablesToAttainLock() {
    return new String[]{getInfoProvider().getMainTableName()};
  }

  @Override
  protected void getPutForTable(PersistentWorkspace instance, ExecutorService service, Put put) {
    put.add(FAMILY_SELF, CELL_NAMESPACE, Bytes.toBytes(instance.getId().getGlobalNamespace()));
    put.add(FAMILY_SELF, CELL_NAMESPACE, Bytes.toBytes(instance.getId().getName()));
    put.add(FAMILY_SELF, CELL_CREATED, Utils.toBytes(instance.getWorkspace().getCreationDate()));
  }

  @Override
  protected void getDeleteForTable(PersistentWorkspace instance, ExecutorService service, Delete put) {
    //No need to do any additinal work
  }

  @Override
  public PersistentWorkspace rowsToObject(Result startRow, ExecutorService executorService) {
    PersistableWorkspace workspace = SmartContentSPI.getInstance().getPersistableDomainFactory().
        createPersistentWorkspace();
    workspace.setId(SmartContentAPI.getInstance().getWorkspaceApi().createWorkspaceId(Bytes.toString(startRow.getValue(
        FAMILY_SELF, CELL_NAMESPACE)), Bytes.toString(startRow.getValue(FAMILY_SELF, CELL_NAME))));
    workspace.setCreationDate(Utils.toDate(startRow.getValue(FAMILY_SELF, CELL_CREATED)));
    PersistentWorkspace persistentWorkspace = new PersistentWorkspace();
    persistentWorkspace.setWorkspace(workspace);
    return persistentWorkspace;
  }
}
