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
package com.smartitengineering.cms.spi.impl.content.template.persistent;

import com.google.inject.Inject;
import com.smartitengineering.cms.api.content.ContentId;
import com.smartitengineering.cms.api.content.MutableRepresentation;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.spi.impl.hbase.Utils;
import com.smartitengineering.cms.spi.impl.content.PersistentContent;
import com.smartitengineering.dao.impl.hbase.spi.ExecutorService;
import com.smartitengineering.dao.impl.hbase.spi.SchemaInfoProvider;
import com.smartitengineering.dao.impl.hbase.spi.impl.AbstractObjectRowConverter;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

/**
 *
 * @author imyousuf
 */
public class RepresentationObjectConverter extends AbstractObjectRowConverter<PersistentRepresentation, TemplateId> {

  public static final byte[] FAMILY_SELF = Bytes.toBytes("self");
  public static final byte[] CELL_NAME = Bytes.toBytes("name");
  public static final byte[] CELL_MIME_TYPE = Bytes.toBytes("mimeType");
  public static final byte[] CELL_LAST_MODIFIED = Bytes.toBytes("lastModified");
  public static final byte[] CELL_REP = Bytes.toBytes("rep");
  public static final byte[] CELL_CONTENT_ID = Bytes.toBytes("content");
  @Inject
  private SchemaInfoProvider<PersistentContent, ContentId> contentSchemaInfoProvider;

  @Override
  protected String[] getTablesToAttainLock() {
    return new String[]{getInfoProvider().getMainTableName()};
  }

  @Override
  protected void getPutForTable(PersistentRepresentation instance, ExecutorService service, Put put) {
    put.add(FAMILY_SELF, CELL_NAME, Bytes.toBytes(instance.getRepresentation().getName()));
    put.add(FAMILY_SELF, CELL_MIME_TYPE, Bytes.toBytes(instance.getRepresentation().getMimeType()));
    put.add(FAMILY_SELF, CELL_LAST_MODIFIED, Utils.toBytes(instance.getRepresentation().getLastModifiedDate()));
    try {
      put.add(FAMILY_SELF, CELL_CONTENT_ID, contentSchemaInfoProvider.getRowIdFromId(instance.getRepresentation().
          getContentId()));
    }
    catch (Exception ex) {
      logger.error("Could not put representation", ex);
      throw new RuntimeException(ex);
    }
    put.add(FAMILY_SELF, CELL_REP, instance.getRepresentation().getRepresentation());
  }

  @Override
  protected void getDeleteForTable(PersistentRepresentation instance, ExecutorService service, Delete put) {
    //Nothing to implement straight forward row deletion
  }

  @Override
  public PersistentRepresentation rowsToObject(Result startRow, ExecutorService executorService) {
    PersistentRepresentation representation = new PersistentRepresentation();
    try {
      representation.setId(getInfoProvider().getIdFromRowId(startRow.getRow()));
    }
    catch (Exception ex) {
      throw new RuntimeException(ex);
    }
    try {
      ContentId contentId = contentSchemaInfoProvider.getIdFromRowId(startRow.getValue(FAMILY_SELF, CELL_CONTENT_ID));
      MutableRepresentation mutableRepresentation = SmartContentAPI.getInstance().getContentLoader().
          createMutableRepresentation(contentId);
      mutableRepresentation.setLastModifiedDate(Utils.toDate(startRow.getValue(FAMILY_SELF, CELL_LAST_MODIFIED)));
      mutableRepresentation.setMimeType(Bytes.toString(startRow.getValue(FAMILY_SELF, CELL_MIME_TYPE)));
      mutableRepresentation.setName(Bytes.toString(startRow.getValue(FAMILY_SELF, CELL_NAME)));
      mutableRepresentation.setRepresentation(startRow.getValue(FAMILY_SELF, CELL_REP));
      representation.setRepresentation(mutableRepresentation);
      return representation;
    }
    catch (Exception ex) {
      logger.error("Could not create representation", ex);
      return null;
    }
  }
}
