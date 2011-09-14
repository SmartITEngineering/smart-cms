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
package com.smartitengineering.cms.api.factory.type;

import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.cms.api.common.MediaType;
import com.smartitengineering.cms.api.common.SearchResult;
import com.smartitengineering.cms.api.exception.InvalidReferenceException;
import com.smartitengineering.cms.api.type.CollectionDataType;
import com.smartitengineering.cms.api.type.CompositeDataType;
import com.smartitengineering.cms.api.type.ContentDataType;
import com.smartitengineering.cms.api.type.ContentStatus;
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.type.ContentTypeId;
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
import com.smartitengineering.cms.api.type.MutableResourceUri;
import com.smartitengineering.cms.api.type.MutableSearchDef;
import com.smartitengineering.cms.api.type.MutableStringDataType;
import com.smartitengineering.cms.api.type.MutableValidatorDef;
import com.smartitengineering.cms.api.type.MutableVariationDef;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

/**
 * The API for loading all DTOs related to {@link ContentType}. It will also
 * provide the editable versions of those DTOs.
 * @author imyousuf
 * @since 0.1
 */
public interface ContentTypeLoader {

  /**
   * Loads the content type definition specified by the content type ID.
   * @param contentTypeID The id of content type to retrieve.
   * @return Content type represented by the parameter. Null if no such type
   *					exists.
   * @throws NullPointerException If contentTypeID parameter is null
   */
  public ContentType loadContentType(ContentTypeId contentTypeID)
      throws NullPointerException;

  /**
   * Re-Index the content types of the workspace
   * @param workspaceId The workspace of which the content types are to be re-indexed
   */
  public void reIndexTypes(WorkspaceId workspaceId);

  public Filter createFilter();

  public SearchResult<ContentType> createSearchResult(Collection<ContentType> result, long totalResultsCount);

  public SearchResult<ContentType> search(Filter filter);

  /**
   * Re-Index a specific content type
   * @param contentTypeId The content type to reindex.
   */
  public void reIndexType(ContentTypeId contentTypeId);

  /**
   * Parse a {@link InputStream} to retrieve the content type definitions. The format
   * of the file should be specified by the implementor.
   * @param contentTypeDefinitionStream Stream to parse for content type definitions.
   * @return Return ordered collection of parsed content types. Could be empty
   *					but never null.
   * @throws NullPointerException If contentTypeDefinition is null
   * @throws IOException If there is any error during parsing.
   */
  public Collection<WritableContentType> parseContentTypes(WorkspaceId workspaceId,
                                                           InputStream contentTypeDefinitionStream, MediaType mediaType)
      throws NullPointerException, InvalidReferenceException, IOException;

  public MutableContentType getMutableContentType(ContentType contentType);

  public WritableContentType getWritableContentType(ContentType contentType);

  /**
   * Get the mutable version of the content data type.
   * @param contentDataType Content data type to get the mutable version for
   * @return Editable content data type
   */
  public MutableContentDataType getMutableContentDataType(
      ContentDataType contentDataType);

  /**
   * Get the mutable version of the collection data type.
   * @param collectionDataType Collection data type to get the mutable one for
   * @return Editable collection data type
   */
  public MutableCollectionDataType getMutableCollectionDataType(
      CollectionDataType collectionDataType);

  /**
   * Get the mutable version of the content status
   * @param contentStatus Content status to get the mutable version for
   * @return Editable content status
   */
  public MutableContentStatus getMutableContentStatus(
      ContentStatus contentStatus);

  /**
   * Creates a new mutable composite data type
   * @return new mutable composite data type
   */
  public MutableCompositeDataType createMutableCompositeDataType();

  /**
   * Converts the read-only composite data type to a mutable composite data type
   * @param dataType The immutable data type intended to edit
   * @return Mutable composite data type version of the intended composite data type
   */
  public MutableCompositeDataType createMutableCompositeDataType(CompositeDataType dataType);

  /**
   * Get the mutable version of the content type ID
   * @param contentTypeID content type id to get the mutable version for
   * @return Editable content type id
   */
  public MutableContentTypeId getMutableContentTypeID(
      ContentTypeId contentTypeID);

  /**
   * Get the mutable version of the field definition
   * @param fieldDef Field definition to get the mutable version for
   * @return Editable field definition
   */
  public MutableFieldDef getMutableFieldDef(FieldDef fieldDef);

  /**
   * Create a new editable collection data type
   * @return New collection data type
   */
  public MutableCollectionDataType createMutableCollectionDataType();

  /**
   * Create a new editable content data type
   * @return New content data type
   */
  public MutableContentDataType createMutableContentDataType();

  public MutableEnumDataType createMutableEnumDataType();

  public MutableEnumDataType createMutableEnumDataType(EnumDataType dataType);

  /**
   * Create a new editable content status
   * @return
   */
  public MutableContentStatus createMutableContentStatus();

  /**
   * Create a new editable content type id.
   * @return New content type id
   */
  public MutableContentTypeId createMutableContentTypeID();

  /**
   * Create a new editable field definition
   * @return New field definition
   */
  public MutableFieldDef createMutableFieldDef();

  public MutableFieldDef createMutableFieldDef(FieldDef parentContainer);

  public ContentTypeId createContentTypeId(WorkspaceId workspaceId, String namespace, String name);

  public MutableRepresentationDef createMutableRepresentationDef();
  
  public MutableContentCoProcessorDef createMutableContentCoProcessorDef();

  public MutableVariationDef createMutableVariationDef();

  public MutableValidatorDef createMutableValidatorDef();

  public MutableResourceUri createMutableResourceUri();

  public MutableSearchDef createMutableSearchDef();

  public MutableOtherDataType createMutableOtherDataType();

  public MutableStringDataType createMutableStringDataType();

  public String getSearchFieldName(FieldDef fieldDef);

  public String getSearchFieldNameWithoutTypeSpecifics(FieldDef fieldDef);

  public String getEntityTagValueForContentType(ContentType contentType);
}
