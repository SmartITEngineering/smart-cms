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
package com.smartitengineering.cms.spi.impl.type.search;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.smartitengineering.cms.api.event.Event.EventType;
import com.smartitengineering.cms.api.event.Event.Type;
import com.smartitengineering.cms.api.event.EventListener;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.cms.spi.impl.type.PersistentContentType;
import com.smartitengineering.cms.spi.type.ContentTypeSearcher;
import com.smartitengineering.common.dao.search.CommonFreeTextSearchDao;
import com.smartitengineering.dao.common.CommonReadDao;
import com.smartitengineering.dao.common.queryparam.MatchMode;
import com.smartitengineering.dao.common.queryparam.QueryParameter;
import com.smartitengineering.dao.common.queryparam.QueryParameterFactory;
import com.smartitengineering.dao.impl.hbase.spi.SchemaInfoProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class ContentTypeSearcherImpl implements ContentTypeSearcher {

  public static final String REINDEX_LISTENER_NAME = "typeReindexEventListener";
  protected final transient Logger logger = LoggerFactory.getLogger(getClass());
  @Inject
  private CommonFreeTextSearchDao<ContentType> textSearchDao;
  @Inject
  private CommonReadDao<PersistentContentType, ContentTypeId> readDao;
  @Inject
  private SchemaInfoProvider<PersistentContentType, ContentTypeId> schemaInfoProvider;
  @Inject
  @Named(REINDEX_LISTENER_NAME)
  private EventListener reindexListener;
  private final ExecutorService executorService = Executors.newSingleThreadExecutor();
  private static final String SOLR_DATE_FORMAT = DateFormatUtils.ISO_DATETIME_FORMAT.getPattern() + "'Z'";

  @Override
  public void reIndex(ContentTypeId typeId) {
    if (typeId != null) {
      PersistentContentType contentType = readDao.getById(typeId);
      if (contentType != null) {
        reindexListener.notify(SmartContentAPI.getInstance().getEventRegistrar().<ContentType>createEvent(
            EventType.UPDATE, Type.CONTENT_TYPE, contentType.getMutableContentType()));
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
        ContentTypeId lastId = null;
        List<QueryParameter> params = new ArrayList<QueryParameter>();
        while (hasMore) {
          params.clear();
          if (param != null) {
            params.add(param);
          }
          params.add(maxResultsParam);
          if (lastId != null) {
            try {
              params.add(QueryParameterFactory.getGreaterThanPropertyParam("id", schemaInfoProvider.getRowIdFromId(
                  lastId)));
            }
            catch (Exception ex) {
              logger.warn("Could not add last id clause " + lastId.toString(), ex);
            }
          }
          List<PersistentContentType> list = readDao.getList(params);
          if (list == null || list.isEmpty()) {
            hasMore = false;
          }
          else {
            final ContentType[] contents = new ContentType[list.size()];
            int index = 0;
            for (PersistentContentType content : list) {
              reindexListener.notify(SmartContentAPI.getInstance().getEventRegistrar().<ContentType>createEvent(
                  EventType.UPDATE, Type.CONTENT_TYPE, content.getMutableContentType()));
            }

            lastId = contents[contents.length - 1].getContentTypeID();
          }
        }
      }
    });
  }
}
