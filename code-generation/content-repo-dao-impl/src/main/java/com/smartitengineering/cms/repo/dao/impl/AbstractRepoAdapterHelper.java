package com.smartitengineering.cms.repo.dao.impl;

import com.google.inject.Inject;
import com.smartitengineering.cms.api.content.Content;
import com.smartitengineering.cms.api.content.ContentId;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.factory.content.ContentLoader;
import com.smartitengineering.cms.api.factory.content.WriteableContent;
import com.smartitengineering.cms.api.type.ContentStatus;
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.domain.PersistentDTO;
import com.smartitengineering.util.bean.adapter.AbstractAdapterHelper;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public abstract class AbstractRepoAdapterHelper<T extends AbstractRepositoryDomain<? extends PersistentDTO>>
    extends AbstractAdapterHelper<Content, T> {

  protected transient final Logger logger = LoggerFactory.getLogger(getClass());
  protected final Class<? extends T> beanClass;
  private ContentTypeId contentTypeId;
  @Inject
  private WorkspaceId defaultContainerWorkspace;

  protected AbstractRepoAdapterHelper() {
    beanClass = initializeEntityClassFromGenerics();
  }

  protected AbstractRepoAdapterHelper(Class<? extends T> entityClass) {
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

  protected ContentId getContentId(String id) {
    final byte[] bytesUtf8 = org.apache.commons.codec.binary.StringUtils.getBytesUtf8(id);
    ContentId cId = SmartContentAPI.getInstance().getContentLoader().createContentId(defaultContainerWorkspace,
                                                                                     bytesUtf8);
    return cId;
  }

  protected ContentId getContentId(String id, String workspaceId) {
    if (StringUtils.isBlank(workspaceId)) {
      return getContentId(id);
    }
    final byte[] bytesUtf8 = org.apache.commons.codec.binary.StringUtils.getBytesUtf8(id);
    String[] idParts = workspaceId.split(":");
    final WorkspaceId wId;
    if (idParts.length >= 2) {
      wId = SmartContentAPI.getInstance().getWorkspaceApi().createWorkspaceId(idParts[0], idParts[1]);
    }
    else {
      wId = SmartContentAPI.getInstance().getWorkspaceApi().createWorkspaceId(idParts[0]);
    }
    ContentId cId = SmartContentAPI.getInstance().getContentLoader().createContentId(wId, bytesUtf8);
    return cId;
  }

  protected WorkspaceId getWorkspaceId(T toBean) throws IllegalArgumentException {
    final WorkspaceId wId;
    if (StringUtils.isNotBlank(toBean.getWorkspaceId())) {
      String[] idParts = toBean.getWorkspaceId().split(":");
      if (idParts == null || idParts.length != 2) {
        throw new IllegalArgumentException("Workspace ID must be set as 'namespace:name'");
      }
      wId = SmartContentAPI.getInstance().getWorkspaceApi().createWorkspaceId(idParts[0], idParts[1]);
    }
    else {
      wId = defaultContainerWorkspace;
    }
    return wId;
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

  @Override
  protected void mergeFromF2T(Content fromBean, T toBean) {
    if (fromBean.getStatus() != null) {
      toBean.setStatus(fromBean.getStatus().getName());
    }
    if (fromBean.getContentId() != null) {
      toBean.setId(org.apache.commons.codec.binary.StringUtils.newStringUtf8(fromBean.getContentId().getId()));
      toBean.setWorkspaceId(fromBean.getContentId().getWorkspaceId().toString());
    }
    toBean.setCreationDate(fromBean.getCreationDate());
    toBean.setLastModificationDate(fromBean.getLastModifiedDate());
    toBean.setEntityValue(fromBean.getEntityTagValue());
    mergeContentIntoBean(fromBean, toBean);
  }

  @Override
  protected Content convertFromT2F(T toBean) {
    final SmartContentAPI instance = SmartContentAPI.getInstance();
    final ContentLoader contentLoader = instance.getContentLoader();
    WriteableContent content = contentLoader.createContent(getContentTypeId().getContentType());
    if (StringUtils.isNotBlank(toBean.getId())) {
      String[] idParts = toBean.getWorkspaceId().split(":");
      if (idParts == null || idParts.length != 2) {
        throw new IllegalArgumentException("Workspace ID must be set as 'namespace:name'");
      }
      final WorkspaceId wId = getWorkspaceId(toBean);
      final byte[] bytesUtf8 = org.apache.commons.codec.binary.StringUtils.getBytesUtf8(toBean.getId());
      ContentId cId = contentLoader.createContentId(wId, bytesUtf8);
      content.setContentId(cId);
    }
    else {
      final WorkspaceId wId;
      wId = getWorkspaceId(toBean);
      content.createContentId(wId);
      byte[] idBytes = content.getContentId().getId();
      toBean.setId(org.apache.commons.codec.binary.StringUtils.newStringUtf8(idBytes));
    }
    final ContentStatus status;
    final ContentType type = getContentTypeId().getContentType();
    if (StringUtils.isNotBlank(toBean.getStatus())) {

      status = type.getStatuses().get(toBean.getStatus());
    }
    else {
      status = type.getStatuses().entrySet().iterator().next().getValue();
    }
    content.setStatus(status);
    mergeBeanIntoContent(toBean, content);
    return content;
  }

  protected abstract void mergeContentIntoBean(Content fromBean, T toBean);

  protected abstract void mergeBeanIntoContent(T fromBean, WriteableContent toBean);
}
