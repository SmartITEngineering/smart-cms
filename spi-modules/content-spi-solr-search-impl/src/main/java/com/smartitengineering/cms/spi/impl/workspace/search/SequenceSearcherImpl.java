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
import com.google.inject.name.Named;
import com.smartitengineering.cms.api.event.Event.EventType;
import com.smartitengineering.cms.api.event.Event.Type;
import com.smartitengineering.cms.api.event.EventListener;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.workspace.Sequence;
import com.smartitengineering.cms.api.workspace.SequenceId;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.cms.spi.impl.SearchBeanLoader;
import com.smartitengineering.cms.spi.impl.events.SolrFieldNames;
import com.smartitengineering.cms.spi.workspace.SequenceSearcher;
import com.smartitengineering.common.dao.search.CommonFreeTextSearchDao;
import com.smartitengineering.dao.common.queryparam.MatchMode;
import com.smartitengineering.dao.common.queryparam.QueryParameter;
import com.smartitengineering.dao.common.queryparam.QueryParameterFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class SequenceSearcherImpl implements SequenceSearcher {

  public static final String REINDEX_LISTENER_NAME = "sequenceReindexEventListener";
  private static final String SEQ_BY_WS_Q_PREFIX = new StringBuilder(SolrFieldNames.TYPE).append(": \"").append(
      SequenceHelper.SEQUENCE).append("\" AND ").append(SolrFieldNames.WORKSPACEID).append(": \"").toString();
  private static final String SEQ_BY_WS_Q_SUFFIX = "\"";
  protected final transient Logger logger = LoggerFactory.getLogger(getClass());
  @Inject
  private CommonFreeTextSearchDao<Sequence> textSearchDao;
  @Inject
  private SearchBeanLoader<Sequence, SequenceId> sequenceLoader;
  @Inject
  @Named(REINDEX_LISTENER_NAME)
  private EventListener reindexListener;
  private final ExecutorService executorService = Executors.newSingleThreadExecutor();

  @Override
  public void reIndex(SequenceId seqId) {
    if (seqId != null) {
      Sequence sequence = sequenceLoader.loadById(seqId);
      if (sequence != null) {
        reindexListener.notify(SmartContentAPI.getInstance().getEventRegistrar().<Sequence>createEvent(
            EventType.CREATE, Type.SEQUENCE, sequence));
      }
    }
  }

  @Override
  public void reIndex(final WorkspaceId workspaceId) {
    executorService.submit(new Runnable() {

      @Override
      public void run() {
        final QueryParameter param;
        if (workspaceId == null) {
          param = null;
        }
        else {
          param = QueryParameterFactory.getStringLikePropertyParam("id", new StringBuilder(workspaceId.toString()).
              append(':').toString(), MatchMode.START);
        }
        final QueryParameter<Integer> maxResultsParam = QueryParameterFactory.getMaxResultsParam(100);
        boolean hasMore = true;
        SequenceId lastId = null;
        List<QueryParameter> params = new ArrayList<QueryParameter>();
        while (hasMore) {
          params.clear();
          if (param != null) {
            params.add(param);
          }
          params.add(maxResultsParam);
          if (lastId != null) {
            try {
              params.add(QueryParameterFactory.getGreaterThanPropertyParam("id", sequenceLoader.getByteArrayFromId(
                  lastId)));
            }
            catch (Exception ex) {
              logger.warn("Could not add last id clause " + lastId.toString(), ex);
            }
          }
          List<Sequence> list = sequenceLoader.getQueryResult(params);
          if (list == null || list.isEmpty()) {
            hasMore = false;
          }
          else {
            final Sequence[] contents = new Sequence[list.size()];
            int index = 0;
            for (Sequence seq : list) {
              reindexListener.notify(SmartContentAPI.getInstance().getEventRegistrar().<Sequence>createEvent(
                  EventType.CREATE, Type.SEQUENCE, seq));
              contents[index++] = seq;
            }

            lastId = contents[contents.length - 1].getSequenceId();
          }
        }
      }
    });
  }

  public Collection<Sequence> getSequencesForWorkspace(WorkspaceId id) {
    if (id == null) {
      return Collections.emptyList();
    }
    StringBuilder finalQuery = new StringBuilder(SEQ_BY_WS_Q_PREFIX).append(id.toString()).append(SEQ_BY_WS_Q_SUFFIX);
    int start = -1;
    final int pageSize = 20;
    int currentTotal = 0;
    boolean continueLoop = true;
    final Collection<Sequence> result = new ArrayList<Sequence>();
    do {
      start = (start + 1) * pageSize;
      final com.smartitengineering.common.dao.search.SearchResult<Sequence> searchResult =
                                                                            textSearchDao.detailedSearch(QueryParameterFactory.
          getStringLikePropertyParam("q", finalQuery.toString()), QueryParameterFactory.getFirstResultParam(start),
                                                                                                         QueryParameterFactory.
          getMaxResultsParam(pageSize));
      if (searchResult == null || searchResult.getResult() == null || searchResult.getResult().isEmpty()) {
        if (logger.isDebugEnabled()) {
          logger.debug("No sequence search result for " + finalQuery.toString());
        }
        continueLoop = false;
      }
      else {
        currentTotal += searchResult.getResult().size();
        continueLoop = currentTotal < searchResult.getTotalResults();
        for (Sequence content : searchResult.getResult()) {
          if (content != null) {
            result.add(content);
          }
        }
      }
    }
    while (continueLoop);
    return result;
  }
}
