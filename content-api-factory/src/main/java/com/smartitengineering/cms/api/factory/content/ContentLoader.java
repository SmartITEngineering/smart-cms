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
package com.smartitengineering.cms.api.factory.content;

import com.smartitengineering.cms.api.content.BooleanFieldValue;
import com.smartitengineering.cms.api.content.CollectionFieldValue;
import com.smartitengineering.cms.api.content.Content;
import com.smartitengineering.cms.api.content.ContentFieldValue;
import com.smartitengineering.cms.api.content.ContentId;
import com.smartitengineering.cms.api.content.DateTimeFieldValue;
import com.smartitengineering.cms.api.content.Field;
import com.smartitengineering.cms.api.content.FieldValue;
import com.smartitengineering.cms.api.content.Filter;
import com.smartitengineering.cms.api.content.MutableBooleanFieldValue;
import com.smartitengineering.cms.api.content.MutableCollectionFieldValue;
import com.smartitengineering.cms.api.content.MutableContentFieldValue;
import com.smartitengineering.cms.api.content.MutableDateTimeFieldValue;
import com.smartitengineering.cms.api.content.MutableField;
import com.smartitengineering.cms.api.content.MutableNumberFieldValue;
import com.smartitengineering.cms.api.content.MutableOtherFieldValue;
import com.smartitengineering.cms.api.content.MutableRepresentation;
import com.smartitengineering.cms.api.content.MutableStringFieldValue;
import com.smartitengineering.cms.api.content.MutableVariation;
import com.smartitengineering.cms.api.content.NumberFieldValue;
import com.smartitengineering.cms.api.content.OtherFieldValue;
import com.smartitengineering.cms.api.content.SearchResult;
import com.smartitengineering.cms.api.content.StringFieldValue;
import com.smartitengineering.cms.api.content.Variation;
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.type.DataType;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.cms.api.type.FieldDef;
import java.util.Collection;

/**
 *
 * @author imyousuf
 */
public interface ContentLoader {

  MutableField createMutableField(FieldDef fieldDef);

  MutableField createMutableField(Field field);

  MutableDateTimeFieldValue createDateTimeFieldValue();

  MutableDateTimeFieldValue createDateTimeFieldValue(DateTimeFieldValue fieldValue);

  MutableBooleanFieldValue createBooleanFieldValue();

  MutableBooleanFieldValue createBooleanFieldValue(BooleanFieldValue fieldValue);

  MutableCollectionFieldValue createCollectionFieldValue();

  MutableCollectionFieldValue createCollectionFieldValue(CollectionFieldValue fieldValue);

  MutableContentFieldValue createContentFieldValue();

  MutableContentFieldValue createContentFieldValue(ContentFieldValue fieldValue);

  MutableNumberFieldValue createIntegerFieldValue();

  MutableNumberFieldValue createLongFieldValue();

  MutableNumberFieldValue createDoubleFieldValue();

  MutableNumberFieldValue createNumberFieldValue(NumberFieldValue fieldValue);

  MutableOtherFieldValue createOtherFieldValue();

  MutableOtherFieldValue createOtherFieldValue(OtherFieldValue fieldValue);

  MutableStringFieldValue createStringFieldValue();

  MutableStringFieldValue createStringFieldValue(StringFieldValue fieldValue);

  MutableRepresentation createMutableRepresentation();

  MutableVariation createMutableVariation();

  FieldValue getValueFor(String value, DataType fieldDef);

  ContentId createContentId(WorkspaceId workspaceId, byte[] id);

  WriteableContent createContent(ContentType contentType);

  WriteableContent getWritableContent(Content content);

  Content loadContent(ContentId contentId);

  Filter craeteFilter();

  SearchResult createSearchResult(Collection<Content> result, long totalResultsCount);

  SearchResult search(Filter filter);

  Variation getVariation(Content content, Field field, String name);

  boolean isValidContent(Content content);

  ContentId generateContentId(WorkspaceId workspaceId);

  String getEntityTagValueForContent(Content content);
}
