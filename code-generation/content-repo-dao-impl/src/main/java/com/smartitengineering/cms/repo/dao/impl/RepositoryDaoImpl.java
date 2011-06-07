package com.smartitengineering.cms.repo.dao.impl;

import com.google.inject.Inject;
import com.smartitengineering.cms.api.content.Content;
import com.smartitengineering.cms.api.content.ContentId;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.factory.content.WriteableContent;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.dao.common.CommonDao;
import com.smartitengineering.dao.common.queryparam.QueryParameter;
import com.smartitengineering.domain.PersistentDTO;
import com.smartitengineering.util.bean.adapter.GenericAdapter;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class RepositoryDaoImpl<T extends AbstractRepositoryDomain<? extends PersistentDTO>>
    implements CommonDao<T, String> {

  @Inject
  private WorkspaceId defaultContainerWorkspace;
  @Inject
  private GenericAdapter<Content, T> adapter;
  protected transient final Logger logger = LoggerFactory.getLogger(getClass());

  protected ContentId getContentId(String id) {
    final byte[] bytesUtf8 = org.apache.commons.codec.binary.StringUtils.getBytesUtf8(id);
    ContentId cId = SmartContentAPI.getInstance().getContentLoader().createContentId(defaultContainerWorkspace,
                                                                                     bytesUtf8);
    return cId;
  }

  public Set<T> getAll() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public Set<T> getByIds(List<String> ids) {
    Set<T> cs = new LinkedHashSet<T>();
    for (String id : ids) {
      T bean = getById(id);
      if (bean != null) {
        cs.add(bean);
      }
    }
    return cs;
  }

  public T getById(String id) {
    ContentId contentId = getContentId(id);
    Content content = SmartContentAPI.getInstance().getContentLoader().loadContent(contentId);
    if (content == null) {
      return null;
    }
    else {
      return adapter.convert(content);
    }
  }

  public T getSingle(List<QueryParameter> query) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public List<T> getList(List<QueryParameter> query) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public <OtherTemplate> OtherTemplate getOther(List<QueryParameter> query) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public <OtherTemplate> List<OtherTemplate> getOtherList(List<QueryParameter> query) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public T getSingle(QueryParameter... query) {
    return getSingle(Arrays.asList(query));
  }

  public List<T> getList(QueryParameter... query) {
    return getList(Arrays.asList(query));
  }

  public <OtherTemplate> OtherTemplate getOther(QueryParameter... query) {
    return this.<OtherTemplate>getOther(Arrays.asList(query));
  }

  public <OtherTemplate> List<OtherTemplate> getOtherList(QueryParameter... query) {
    return this.<OtherTemplate>getOtherList(Arrays.asList(query));
  }

  public void save(T... states) {
    for (T state : states) {
      Content content = adapter.convertInversely(state);
      if (content != null) {
        WriteableContent wContent = SmartContentAPI.getInstance().getContentLoader().getWritableContent(content);
        try {
          wContent.put();
        }
        catch (Exception ex) {
          logger.warn("Could not save content ", ex);
          throw new RuntimeException(ex);
        }
      }
    }
  }

  public void update(T... states) {
    for (T state : states) {
      Content content = adapter.convertInversely(state);
      if (content != null) {
        WriteableContent wContent = SmartContentAPI.getInstance().getContentLoader().getWritableContent(content);
        try {
          wContent.put();
        }
        catch (Exception ex) {
          logger.warn("Could not save content ", ex);
          throw new RuntimeException(ex);
        }
      }
    }
  }

  public void delete(T... states) {
    for (T state : states) {
      Content content = adapter.convertInversely(state);
      if (content != null) {
        WriteableContent wContent = SmartContentAPI.getInstance().getContentLoader().getWritableContent(content);
        try {
          wContent.delete();
        }
        catch (Exception ex) {
          logger.warn("Could not save content ", ex);
          throw new RuntimeException(ex);
        }
      }
    }
  }
}
