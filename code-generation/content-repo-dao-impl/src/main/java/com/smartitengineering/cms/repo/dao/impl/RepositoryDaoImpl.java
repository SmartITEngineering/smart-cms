package com.smartitengineering.cms.repo.dao.impl;

import com.google.inject.Inject;
import com.smartitengineering.cms.api.common.SearchResult;
import com.smartitengineering.cms.api.content.Content;
import com.smartitengineering.cms.api.content.ContentId;
import com.smartitengineering.cms.api.content.Filter;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.factory.content.WriteableContent;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.dao.common.CommonDao;
import com.smartitengineering.dao.common.queryparam.QueryParameter;
import com.smartitengineering.domain.PersistentDTO;
import com.smartitengineering.util.bean.adapter.GenericAdapter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
  protected final Class<? extends T> beanClass;
  private ContentTypeId contentTypeId;
  protected transient final Logger logger = LoggerFactory.getLogger(getClass());

  @Inject
  public RepositoryDaoImpl(Class<? extends T> entityClass) {
    beanClass = entityClass;
  }

  protected ContentTypeId getContentTypeId() {
    if (contentTypeId == null) {
      String typeNS = beanClass.getPackage().getName();
      String typeName = beanClass.getSimpleName();
      contentTypeId = SmartContentAPI.getInstance().getContentTypeLoader().createContentTypeId(defaultContainerWorkspace,
                                                                                               typeNS, typeName);
    }
    return contentTypeId;
  }

  protected Filter getDefaultFilter() {
    Filter filter = SmartContentAPI.getInstance().getContentLoader().craeteFilter();
    filter.addContentTypeToFilter(getContentTypeId());
    filter.setWorkspaceId(defaultContainerWorkspace);
    filter.setFriendliesIncluded(true);
    return filter;
  }

  protected final Class<? extends T> initializeEntityClassFromGenerics() {
    Class<? extends T> extractedEntityClass = null;
    try {
      Type paramType =
           ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
      if (paramType instanceof ParameterizedType) {
        paramType = ((ParameterizedType) paramType).getRawType();
      }
      Class<T> pesistenceRegistryClass = paramType instanceof Class ? (Class<T>) paramType : null;
      if (logger.isDebugEnabled()) {
        logger.debug("Entity class predicted to: " + pesistenceRegistryClass.toString());
      }
      extractedEntityClass = pesistenceRegistryClass;
    }
    catch (Exception ex) {
      logger.warn("Could not predict entity class ", ex);
    }
    return extractedEntityClass;
  }

  protected ContentId getContentId(String id) {
    final ContentId cId;
    if (id.contains(":")) {
      String[] idTokens = id.split(":");
      if (idTokens.length != 3) {
        final byte[] bytesUtf8 = org.apache.commons.codec.binary.StringUtils.getBytesUtf8(idTokens[0]);
        cId = SmartContentAPI.getInstance().getContentLoader().createContentId(defaultContainerWorkspace, bytesUtf8);
      }
      else {
        final byte[] bytesUtf8 = org.apache.commons.codec.binary.StringUtils.getBytesUtf8(idTokens[2]);
        cId = SmartContentAPI.getInstance().getContentLoader().createContentId(SmartContentAPI.getInstance().
            getWorkspaceApi().createWorkspaceId(idTokens[0], idTokens[1]), bytesUtf8);
      }
    }
    else {
      final byte[] bytesUtf8 = org.apache.commons.codec.binary.StringUtils.getBytesUtf8(id);
      cId = SmartContentAPI.getInstance().getContentLoader().createContentId(defaultContainerWorkspace, bytesUtf8);
    }
    return cId;
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

  public Set<T> getAll() {
    return new LinkedHashSet<T>(searchCms(getDefaultFilter(), true));
  }

  public T getSingle(List<QueryParameter> queries) {
    Filter filter = getDefaultFilter();
    processQueryParams(filter, queries);
    filter.setMaxContents(1);
    List<T> result = searchCms(filter, false);
    if (!result.isEmpty()) {
      return result.get(0);
    }
    return null;
  }

  public List<T> getList(List<QueryParameter> queries) {
    return searchCms(processQueryParams(getDefaultFilter(), queries), false);
  }

  protected List<T> searchCms(Filter filter, boolean getAll) {
    if (getAll) {
      filter.setStartFrom(0);
      filter.setMaxContents(10);
    }
    SearchResult<Content> searchResult = SmartContentAPI.getInstance().getContentLoader().search(filter);
    final List<T> result;
    result = new ArrayList<T>();
    Collection<Content> collectionResult = searchResult.getResult();
    if (collectionResult != null && !collectionResult.isEmpty()) {
      result.addAll(adapter.convert(collectionResult.toArray(new Content[collectionResult.size()])));
    }
    if (getAll) {
      long totalResult = searchResult.getTotalResultsCount();
      long cTotal = result.size();
      if (totalResult > 10) {
        while (cTotal < totalResult) {
          filter.setStartFrom((int) cTotal);
          filter.setMaxContents(20);
          searchResult = SmartContentAPI.getInstance().getContentLoader().search(filter);
          collectionResult = searchResult.getResult();
          if (collectionResult != null && !collectionResult.isEmpty()) {
            result.addAll(adapter.convert(collectionResult.toArray(new Content[collectionResult.size()])));
            cTotal += collectionResult.size();
          }
          else {
            break;
          }
        }
      }
    }
    return result;
  }

  private Filter processQueryParams(Filter filter,
                                    List<QueryParameter> queries) {
    return filter;
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
