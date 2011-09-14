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
package com.smartitengineering.cms.api.impl.type;

import com.smartitengineering.cms.api.common.MediaType;
import com.smartitengineering.cms.api.common.SearchResult;
import com.smartitengineering.cms.api.exception.InvalidReferenceException;
import com.smartitengineering.cms.api.type.CollectionDataType;
import com.smartitengineering.cms.api.type.CompositeDataType;
import com.smartitengineering.cms.api.type.ContentDataType;
import com.smartitengineering.cms.api.type.ContentStatus;
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.api.factory.type.ContentTypeLoader;
import com.smartitengineering.cms.api.factory.type.WritableContentType;
import com.smartitengineering.cms.api.impl.content.SearchResultImpl;
import com.smartitengineering.cms.api.type.ContentCoProcessorDef;
import com.smartitengineering.cms.api.type.EnumDataType;
import com.smartitengineering.cms.api.type.FieldDef;
import com.smartitengineering.cms.api.type.Filter;
import com.smartitengineering.cms.api.type.MutableCollectionDataType;
import com.smartitengineering.cms.api.type.MutableCompositeDataType;
import com.smartitengineering.cms.api.type.MutableContentCoProcessorDef;
import com.smartitengineering.cms.api.type.MutableContentDataType;
import com.smartitengineering.cms.api.type.MutableContentStatus;
import com.smartitengineering.cms.api.type.MutableContentType;
import com.smartitengineering.cms.api.type.MutableContentTypeId;
import com.smartitengineering.cms.api.type.MutableEnumDataType;
import com.smartitengineering.cms.api.type.MutableFieldDef;
import com.smartitengineering.cms.api.type.MutableOtherDataType;
import com.smartitengineering.cms.api.type.MutableRepresentationDef;
import com.smartitengineering.cms.api.type.MutableSearchDef;
import com.smartitengineering.cms.api.type.MutableValidatorDef;
import com.smartitengineering.cms.api.type.MutableVariationDef;
import com.smartitengineering.cms.api.type.MutableResourceUri;
import com.smartitengineering.cms.api.type.MutableStringDataType;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.cms.spi.SmartContentSPI;
import com.smartitengineering.cms.spi.type.ContentTypeDefinitionParser;
import com.smartitengineering.cms.spi.type.PersistableContentType;
import com.smartitengineering.cms.spi.type.TypeValidator;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author kaisar
 */
public class ContentTypeLoaderImpl implements ContentTypeLoader {

  private final transient Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public ContentType loadContentType(ContentTypeId contentTypeID) throws NullPointerException {
    final Collection<? extends ContentType> reads =
                                            SmartContentSPI.getInstance().getContentTypeReader().
        readContentTypeFromPersistentStorage(
        contentTypeID);
    if (reads.size() > 0) {
      return reads.iterator().next();
    }
    else {
      return null;
    }
  }

  @Override
  public Collection<WritableContentType> parseContentTypes(WorkspaceId workspaceId,
                                                           InputStream contentTypeDefinitionStream, MediaType mediaType)
      throws NullPointerException, InvalidReferenceException, IOException {
    TypeValidator validator = SmartContentSPI.getInstance().getTypeValidators().getValidators().get(mediaType);
    ContentTypeDefinitionParser parser = SmartContentSPI.getInstance().getContentTypeDefinitionParsers().getParsers().
        get(mediaType);
    if (validator == null || parser == null) {
      throw new IOException("Media type " + mediaType.toString() + " is not supported!");
    }
    if (!contentTypeDefinitionStream.markSupported()) {
      contentTypeDefinitionStream = new BufferedInputStream(contentTypeDefinitionStream);
    }
    try {
      if (!validator.isValid(contentTypeDefinitionStream)) {
        throw new IOException("Content does not meet definition!");
      }
      final Collection<WritableContentType> types = parser.parseStream(workspaceId, contentTypeDefinitionStream);
      if (logger.isDebugEnabled()) {
        for (WritableContentType contentType : types) {
          logger.debug("ID " + contentType.getContentTypeID());
        }
      }
      List<ContentTypeImpl> resultingTypes = mergeWithStoredContentTypes(types);
      return Collections.<WritableContentType>unmodifiableCollection(resultingTypes);
    }
    catch (InvalidReferenceException ex) {
      throw ex;
    }
    catch (Exception ex) {
      throw new IOException(ex);
    }
  }

  protected List<ContentTypeImpl> mergeWithStoredContentTypes(final Collection<WritableContentType> types) throws
      IllegalArgumentException {
    final List<ContentTypeImpl> resultingTypes =
                                new ArrayList<ContentTypeImpl>(types.size());
    final Collection<? extends ContentType> storedContentTypes;
    final ContentTypeId[] ids = new ContentTypeId[types.size()];
    int i = 0;
    for (ContentType type : types) {
      ids[i++] = type.getContentTypeID();
    }
    storedContentTypes =
    SmartContentSPI.getInstance().getContentTypeReader().readContentTypeFromPersistentStorage(ids);
    for (ContentType type : storedContentTypes) {
      final ContentTypeImpl contentTypeImpl = getContentTypeImpl(type);
      contentTypeImpl.setFromPersistentStorage(true);
      resultingTypes.add(contentTypeImpl);
    }
    if (logger.isDebugEnabled()) {
      logger.debug(new StringBuilder("After getting from persistent storage size is of 2b ReturnedList is ").append(resultingTypes.
          size()).toString());
    }
    for (MutableContentType type : types) {
      if (logger.isDebugEnabled()) {
        logger.debug(new StringBuilder("Type ID is ").append(type.getContentTypeID()).toString());
      }
      int index = resultingTypes.indexOf(type);
      if (index >= 0) {
        logger.debug("Just merging");
        final ContentTypeImpl get = resultingTypes.get(index);
        merge(get, type);
        get.setFromPersistentStorage(true);
      }
      else {
        logger.debug("Adding to list");
        resultingTypes.add(getContentTypeImpl(type));
      }
    }
    return resultingTypes;
  }

  @Override
  public MutableContentDataType getMutableContentDataType(ContentDataType contentDataType) {
    if (contentDataType.getTypeDef() != null) {
      ContentDataTypeImpl contentDataTypeImpl = new ContentDataTypeImpl();
      contentDataTypeImpl.setTypeDef(contentDataType.getTypeDef());
      contentDataTypeImpl.setBiBidirectionalFieldName(contentDataType.getBidirectionalFieldName());
      return contentDataTypeImpl;
    }
    else {
      throw new IllegalArgumentException("Argument can not be null.");
    }
  }

  @Override
  public MutableCollectionDataType getMutableCollectionDataType(CollectionDataType collectionDataType) {
    if (collectionDataType.getItemDataType() != null && collectionDataType.getMinSize() >= 0 && collectionDataType.
        getMaxSize() >= collectionDataType.getMinSize()) {
      CollectionDataTypeImpl collectionDataTypeImpl = new CollectionDataTypeImpl();
      collectionDataTypeImpl.setItemDataType(collectionDataType.getItemDataType());
      collectionDataTypeImpl.setMaxSize(collectionDataType.getMaxSize());
      collectionDataTypeImpl.setMinSize(collectionDataType.getMinSize());
      return collectionDataTypeImpl;
    }
    else {
      throw new IllegalArgumentException("Argument can not be null or min size has to be non-negative or max size can" +
          " not be smaller than min zie.");
    }
  }

  @Override
  public MutableContentStatus getMutableContentStatus(ContentStatus contentStatus) {
    ContentStatusImpl contentStatusImpl = new ContentStatusImpl();
    contentStatusImpl.setContentTypeID(contentStatus.getContentType());
    contentStatusImpl.setId(contentStatus.getId());
    contentStatusImpl.setName(contentStatus.getName());
    return contentStatusImpl;
  }

  @Override
  public MutableContentTypeId getMutableContentTypeID(ContentTypeId contentTypeID) {
    ContentTypeIdImpl contentTypeIdImpl = new ContentTypeIdImpl();
    contentTypeIdImpl.setName(contentTypeID.getName());
    contentTypeIdImpl.setNamespace(contentTypeID.getNamespace());
    contentTypeIdImpl.setWorkspace(contentTypeID.getWorkspace());
    return contentTypeIdImpl;
  }

  @Override
  public MutableFieldDef getMutableFieldDef(FieldDef fieldDef) {
    if (fieldDef != null) {
      FieldDefImpl fieldDefImpl = new FieldDefImpl();
      fieldDefImpl.setName(fieldDef.getName());
      fieldDefImpl.setSearchDefinition(fieldDef.getSearchDefinition());
      fieldDefImpl.setValueDef(fieldDef.getValueDef());
      fieldDefImpl.setVariations(fieldDef.getVariations().values());
      fieldDefImpl.setFieldStandaloneUpdateAble(true);
      fieldDefImpl.setRequired(true);
      return fieldDefImpl;
    }
    else {
      throw new IllegalArgumentException("Argument can not be null.");
    }
  }

  @Override
  public MutableCollectionDataType createMutableCollectionDataType() {
    return new CollectionDataTypeImpl();
  }

  @Override
  public MutableContentDataType createMutableContentDataType() {
    return new ContentDataTypeImpl();
  }

  @Override
  public MutableContentStatus createMutableContentStatus() {
    return new ContentStatusImpl();
  }

  @Override
  public MutableContentTypeId createMutableContentTypeID() {
    return new ContentTypeIdImpl();
  }

  @Override
  public MutableFieldDef createMutableFieldDef(FieldDef parentContainer) {
    final FieldDefImpl fieldDefImpl = new FieldDefImpl();
    fieldDefImpl.setParentContainer(parentContainer);
    return fieldDefImpl;

  }

  @Override
  public MutableFieldDef createMutableFieldDef() {
    return createMutableFieldDef(null);
  }

  @Override
  public ContentTypeId createContentTypeId(WorkspaceId workspaceId, String namespace, String name) {
    if (workspaceId != null && StringUtils.isNotBlank(namespace) && StringUtils.isNotBlank(name)) {
      ContentTypeIdImpl contentTypeIdImpl = new ContentTypeIdImpl();
      contentTypeIdImpl.setName(name);
      contentTypeIdImpl.setNamespace(namespace);
      contentTypeIdImpl.setWorkspace(workspaceId);
      return contentTypeIdImpl;
    }
    else {
      throw new IllegalArgumentException("Any of arguments can not be null or blank.");
    }
  }

  @Override
  public MutableContentType getMutableContentType(ContentType contentType) {
    if (contentType != null) {
      return getContentTypeImpl(contentType);
    }
    else {
      throw new IllegalArgumentException("Argument can not be null.");
    }
  }

  protected ContentTypeImpl getContentTypeImpl(ContentType contentType) throws IllegalArgumentException {
    ContentTypeImpl typeImpl = new ContentTypeImpl();
    merge(typeImpl, contentType);
    return typeImpl;
  }

  protected void merge(ContentTypeImpl typeImpl, ContentType contentType) throws IllegalArgumentException {
    typeImpl.setContentTypeID(contentType.getContentTypeID());
    typeImpl.setCreationDate(contentType.getCreationDate());
    typeImpl.setPrimaryFieldName(contentType.getPrimaryFieldName());
    typeImpl.setDisplayName(contentType.getDisplayName());
    typeImpl.setFromPersistentStorage(contentType instanceof PersistableContentType ?
        ((PersistableContentType) contentType).isFromPersistentStorage() : false);
    typeImpl.setLastModifiedDate(contentType.getLastModifiedDate());
    typeImpl.setEntityTagValue(contentType.getEntityTagValue());
    typeImpl.setParent(contentType.getParent());
    typeImpl.getMutableFieldDefs().clear();
    typeImpl.getMutableFieldDefs().addAll(contentType.getOwnFieldDefs().values());
    typeImpl.getMutableRepresentationDefs().clear();
    typeImpl.getMutableRepresentationDefs().addAll(contentType.getRepresentationDefs().values());
    typeImpl.getMutableStatuses().clear();
    typeImpl.getMutableStatuses().addAll(contentType.getStatuses().values());
    typeImpl.setRepresentations(contentType.getRepresentations());
    typeImpl.setParameterizedDisplayNames(contentType.getParameterizedDisplayNames());
    typeImpl.setDefinitionType(contentType.getSelfDefinitionType());
    for (Collection<ContentCoProcessorDef> defs : contentType.getContentCoProcessorDefs().values()) {
      for (ContentCoProcessorDef def : defs) {
        typeImpl.addContentCoProcessorDef(def);
      }
    }
  }

  @Override
  public MutableRepresentationDef createMutableRepresentationDef() {
    return new RepresentationDefImpl();
  }

  @Override
  public MutableVariationDef createMutableVariationDef() {
    return new VariationDefImpl();
  }

  @Override
  public MutableValidatorDef createMutableValidatorDef() {
    return new ValidatorDefImpl();
  }

  @Override
  public MutableResourceUri createMutableResourceUri() {
    return new ResourceUriImpl();
  }

  @Override
  public MutableSearchDef createMutableSearchDef() {
    return new SearchDefImpl();
  }

  @Override
  public MutableOtherDataType createMutableOtherDataType() {
    return new OtherDataTypeImpl();
  }

  @Override
  public MutableStringDataType createMutableStringDataType() {
    return new StringDataTypeImpl();
  }

  @Override
  public String getSearchFieldName(FieldDef fieldDef) {
    return SmartContentSPI.getInstance().getSearchFieldNameGenerator().getSearchFieldName(fieldDef);
  }

  public String getSearchFieldNameWithoutTypeSpecifics(FieldDef fieldDef) {
    return SmartContentSPI.getInstance().getSearchFieldNameGenerator().getFieldName(fieldDef);
  }

  @Override
  public WritableContentType getWritableContentType(ContentType contentType) {
    return getContentTypeImpl(contentType);
  }

  @Override
  public String getEntityTagValueForContentType(ContentType contentType) {
    return DigestUtils.md5Hex(new StringBuilder(DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.format(contentType.
        getLastModifiedDate())).append('~').append(contentType.getRepresentations().get(MediaType.APPLICATION_XML)).
        toString());
  }

  @Override
  public void reIndexTypes(WorkspaceId workspaceId) {
    SmartContentSPI.getInstance().getContentTypeSearcher().reIndex(workspaceId);
  }

  @Override
  public void reIndexType(ContentTypeId contentTypeId) {
    SmartContentSPI.getInstance().getContentTypeSearcher().reIndex(contentTypeId);
  }

  @Override
  public Filter createFilter() {
    return new FilterImpl();
  }

  @Override
  public SearchResult<ContentType> createSearchResult(Collection<ContentType> result, long totalResultsCount) {
    SearchResultImpl<ContentType> resultImpl = new SearchResultImpl<ContentType>();
    resultImpl.setResult(result);
    resultImpl.setTotalResultsCount(totalResultsCount);
    return resultImpl;
  }

  @Override
  public SearchResult<ContentType> search(Filter filter) {
    return SmartContentSPI.getInstance().getContentTypeSearcher().search(filter);
  }

  public MutableCompositeDataType createMutableCompositeDataType() {
    return new CompositionDataTypeImpl();
  }

  public MutableCompositeDataType createMutableCompositeDataType(CompositeDataType dataType) {
    CompositionDataTypeImpl dataTypeImpl = new CompositionDataTypeImpl();
    if (dataType != null) {
      dataTypeImpl.setEmbeddedContentDataType(dataType.getEmbeddedContentDataType());
      dataTypeImpl.getOwnMutableComposition().addAll(dataType.getOwnComposition());
    }
    return dataTypeImpl;
  }

  public MutableEnumDataType createMutableEnumDataType() {
    return new EnumDataTypeImpl();
  }

  public MutableEnumDataType createMutableEnumDataType(EnumDataType dataType) {
    MutableEnumDataType type = createMutableEnumDataType();
    if (dataType != null && dataType.getChoices() != null) {
      type.setChoices(dataType.getChoices());
    }
    return type;
  }

  public MutableContentCoProcessorDef createMutableContentCoProcessorDef() {
    return new ContentCoProcessorDefImpl();
  }
}
