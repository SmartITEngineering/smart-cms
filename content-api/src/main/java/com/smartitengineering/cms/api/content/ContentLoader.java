/*
 *
 * This is a simple Content Management System (CMS)
 * Copyright (C) 2009  Imran M Yousuf (imyousuf@smartitengineering.com)
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
package com.smartitengineering.cms.api.content;

import com.smartitengineering.cms.api.WorkspaceId;
import com.smartitengineering.cms.api.type.CollectionDataType;
import com.smartitengineering.cms.api.type.ContentDataType;
import com.smartitengineering.cms.api.type.DataType;
import com.smartitengineering.cms.api.type.FieldDef;
import com.smartitengineering.cms.api.type.OtherDataType;
import com.smartitengineering.cms.api.type.StringDataType;

/**
 *
 * @author imyousuf
 */
public interface ContentLoader {

  MutableField createMutableField(FieldDef fieldDef);

  MutableField createMutableField(Field field);

  MutableDateTimeFieldValue createDateTimeFieldValue(DataType dataType);

  MutableDateTimeFieldValue createDateTimeFieldValue(DateTimeFieldValue fieldValue);

  MutableBooleanFieldValue createBooleanFieldValue(DataType dataType);

  MutableBooleanFieldValue createBooleanFieldValue(BooleanFieldValue fieldValue);

  MutableCollectionFieldValue createCollectionFieldValue(CollectionDataType dataType);

  MutableCollectionFieldValue createCollectionFieldValue(CollectionFieldValue fieldValue);

  MutableContentFieldValue createContentFieldValue(ContentDataType dataType);

  MutableContentFieldValue createContentFieldValue(ContentFieldValue fieldValue);

  MutableNumberFieldValue createNumberFieldValue(DataType dataType);

  MutableNumberFieldValue createNumberFieldValue(NumberFieldValue fieldValue);

  MutableOtherFieldValue createOtherFieldValue(OtherDataType dataType);

  MutableOtherFieldValue createOtherFieldValue(OtherFieldValue fieldValue);

  MutableStringFieldValue createStringFieldValue(StringDataType dataType);

  MutableStringFieldValue createStringFieldValue(StringFieldValue fieldValue);

  ContentId createContentId(WorkspaceId workspaceId, byte[] id);

  Content loadContent(ContentId contentId);
}
