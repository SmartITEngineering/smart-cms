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
package com.smartitengineering.cms.spi.impl.workspace.search;

import com.google.inject.Inject;
import com.smartitengineering.cms.api.workspace.Sequence;
import com.smartitengineering.cms.api.workspace.SequenceId;
import com.smartitengineering.cms.spi.impl.SearchBeanLoader;
import com.smartitengineering.cms.spi.impl.events.SolrFieldNames;
import com.smartitengineering.dao.solr.MultivalueMap;
import com.smartitengineering.dao.solr.impl.MultivalueMapImpl;
import com.smartitengineering.util.bean.adapter.AbstractAdapterHelper;
import org.apache.commons.codec.binary.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class SequenceHelper extends AbstractAdapterHelper<Sequence, MultivalueMap<String, Object>> {

  public static final String SEQUENCE = "sequence";
  public static final String SEQUENCE_NAME = "name_STRING_i";
  @Inject
  private SearchBeanLoader<Sequence, SequenceId> contentTypeLoader;
  private final transient Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  protected MultivalueMap<String, Object> newTInstance() {
    return new MultivalueMapImpl<String, Object>();
  }

  @Override
  protected void mergeFromF2T(final Sequence sequence,
                              final MultivalueMap<String, Object> toBean) {
    toBean.addValue(SolrFieldNames.TYPE, SEQUENCE);
    final SequenceId id = sequence.getSequenceId();
    toBean.addValue(SolrFieldNames.ID, id.toString());
    toBean.addValue(SolrFieldNames.WORKSPACEID, id.getWorkspaceId().toString());
    toBean.addValue(SEQUENCE_NAME, id.getName());
  }

  @Override
  protected Sequence convertFromT2F(MultivalueMap<String, Object> toBean) {
    try {
      byte[] contentId = StringUtils.getBytesUtf8(toBean.getFirst(SolrFieldNames.ID).toString());
      SequenceId id = contentTypeLoader.getFromByteArray(contentId);
      return id.getSequence();
    }
    catch (Exception ex) {
      logger.error("Error converting to content type, returning null!", ex);
    }
    return null;
  }
}
