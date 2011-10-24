/*
 *
 * This is a simple Content Management System (CMS)
 * Copyright (C) 2011  Imran M Yousuf (imyousuf@smartitengineering.com)
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

import com.smartitengineering.dao.impl.hbase.spi.ExecutorService;
import com.smartitengineering.dao.impl.hbase.spi.impl.AbstractObjectRowConverter;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

/**
 *
 * @author imyousuf
 */
public class SequenceObjectConverter extends AbstractObjectRowConverter<PersistentSequence, SequenceId> {

  public static final String FAMILY_SELF_STR = "self";
  public static final String CELL_VALUE_STR = "currentValue";
  private static final byte[] FAMILY_SELF = Bytes.toBytes(FAMILY_SELF_STR);
  private static final byte[] CELL_VALUE = Bytes.toBytes(CELL_VALUE_STR);

  @Override
  protected String[] getTablesToAttainLock() {
    return new String[]{getInfoProvider().getMainTableName()};
  }

  @Override
  protected void getPutForTable(PersistentSequence instance, ExecutorService service, Put put) {
    put.add(FAMILY_SELF, CELL_VALUE, Bytes.toBytes(instance.getCurrentValue()));
  }

  @Override
  protected void getDeleteForTable(PersistentSequence instance, ExecutorService service, Delete put) {
  }

  public PersistentSequence rowsToObject(Result startRow, ExecutorService executorService) {
    PersistentSequence sequence = new PersistentSequence();
    try {
      sequence.setId(getInfoProvider().getIdFromRowId(startRow.getRow()));
    }
    catch (Exception ex) {
      logger.warn("Could not reverse convert row id", ex);
    }
    sequence.setCurrentValue(Bytes.toLong(startRow.getValue(FAMILY_SELF, CELL_VALUE)));
    return sequence;
  }
}
