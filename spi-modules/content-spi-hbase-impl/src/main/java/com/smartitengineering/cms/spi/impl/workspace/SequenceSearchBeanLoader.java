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

import com.google.inject.Inject;
import com.smartitengineering.cms.api.workspace.Sequence;
import com.smartitengineering.cms.api.workspace.SequenceId;
import com.smartitengineering.cms.spi.impl.SearchBeanLoader;
import com.smartitengineering.dao.common.CommonReadDao;
import com.smartitengineering.dao.common.queryparam.QueryParameter;
import com.smartitengineering.dao.impl.hbase.spi.SchemaInfoProvider;
import com.smartitengineering.util.bean.adapter.GenericAdapter;
import edu.emory.mathcs.backport.java.util.Collections;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author imyousuf
 */
public class SequenceSearchBeanLoader implements SearchBeanLoader<Sequence, SequenceId> {

  @Inject
  private CommonReadDao<PersistentSequence, SequenceId> readDao;
  @Inject
  private SchemaInfoProvider<PersistentSequence, SequenceId> schemaInfoProvider;
  @Inject
  private GenericAdapter<Sequence, PersistentSequence> sequenceAdapter;

  public Sequence loadById(SequenceId id) {
    final PersistentSequence byId = readDao.getById(id);
    if (byId == null) {
      return null;
    }
    return sequenceAdapter.convertInversely(byId);
  }

  public SequenceId getFromByteArray(byte[] byteArray) {
    try {
      return schemaInfoProvider.getIdFromRowId(byteArray);
    }
    catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public List<Sequence> getQueryResult(List<QueryParameter> params) {
    List<PersistentSequence> sequences = readDao.getList(params);
    if (sequences == null || sequences.isEmpty()) {
      return Collections.emptyList();
    }
    List<Sequence> mainSequences = new ArrayList<Sequence>(sequences.size());
    for (PersistentSequence pc : sequences) {
      mainSequences.add(sequenceAdapter.convertInversely(pc));
    }
    return mainSequences;
  }

  public byte[] getByteArrayFromId(SequenceId id) {
    try {
      return schemaInfoProvider.getRowIdFromId(id);
    }
    catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }
}
