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
package com.smartitengineering.cms.api.impl.content;

import com.smartitengineering.cms.api.WorkspaceId;
import com.smartitengineering.cms.api.content.BooleanFieldValue;
import com.smartitengineering.cms.api.content.CollectionFieldValue;
import com.smartitengineering.cms.api.content.Content;
import com.smartitengineering.cms.api.content.ContentFieldValue;
import com.smartitengineering.cms.api.content.ContentId;
import com.smartitengineering.cms.api.content.ContentLoader;
import com.smartitengineering.cms.api.content.DateTimeFieldValue;
import com.smartitengineering.cms.api.content.Field;
import com.smartitengineering.cms.api.content.Filter;
import com.smartitengineering.cms.api.content.MutableBooleanFieldValue;
import com.smartitengineering.cms.api.content.MutableCollectionFieldValue;
import com.smartitengineering.cms.api.content.MutableContentFieldValue;
import com.smartitengineering.cms.api.content.MutableDateTimeFieldValue;
import com.smartitengineering.cms.api.content.MutableField;
import com.smartitengineering.cms.api.content.MutableNumberFieldValue;
import com.smartitengineering.cms.api.content.MutableOtherFieldValue;
import com.smartitengineering.cms.api.content.MutableStringFieldValue;
import com.smartitengineering.cms.api.content.NumberFieldValue;
import com.smartitengineering.cms.api.content.OtherFieldValue;
import com.smartitengineering.cms.api.content.StringFieldValue;
import com.smartitengineering.cms.api.type.FieldDef;
import java.util.Set;

/**
 *
 * @author kaisar
 */
public class ContentLoaderImpl implements ContentLoader {

  @Override
  public MutableField createMutableField(FieldDef fieldDef) {
    if (fieldDef != null) {
      FieldImpl fieldImpl = new FieldImpl();
      fieldImpl.setFieldDef(fieldDef);
      fieldImpl.setName(fieldDef.getName());
      return fieldImpl;
    }
    else {
      throw new IllegalArgumentException("Argument can not be null.");

    }
  }

  @Override
  public MutableField createMutableField(Field field) {
    if (field.getFieldDef() != null) {
      FieldImpl fieldImpl = new FieldImpl();
      fieldImpl.setName(field.getName());
      fieldImpl.setValue(field.getValue());
      fieldImpl.setFieldDef(field.getFieldDef());
      return fieldImpl;
    }
    else {
      throw new IllegalArgumentException("FieldDef can not be null.");
    }
  }

  @Override
  public MutableDateTimeFieldValue createDateTimeFieldValue(DateTimeFieldValue fieldValue) {
    if (fieldValue.getValue() != null) {
      DateTimeFieldValueImpl dateTimeFieldValueImpl = new DateTimeFieldValueImpl();
      dateTimeFieldValueImpl.setValue(fieldValue.getValue());
      return dateTimeFieldValueImpl;
    }
    else {
      throw new IllegalArgumentException("Argument can not be null.");
    }
  }

  @Override
  public MutableBooleanFieldValue createBooleanFieldValue(BooleanFieldValue fieldValue) {
    BooleanFieldValueImpl booleanFieldValueImpl = new BooleanFieldValueImpl();
    booleanFieldValueImpl.setValue(fieldValue.getValue());
    return booleanFieldValueImpl;
  }

  @Override
  public MutableCollectionFieldValue createCollectionFieldValue(CollectionFieldValue fieldValue) {
    if (fieldValue.getValue() != null) {
      CollectionFieldValueImpl collectionFieldValueImpl = new CollectionFieldValueImpl();
      collectionFieldValueImpl.setValue(fieldValue.getValue());
      return collectionFieldValueImpl;
    }
    else {
      throw new IllegalArgumentException("Argument can not be null.");
    }
  }

  @Override
  public MutableContentFieldValue createContentFieldValue(ContentFieldValue fieldValue) {
    if (fieldValue.getValue() != null) {
      ContentFieldValueImpl contentFieldValueImpl = new ContentFieldValueImpl();
      contentFieldValueImpl.setValue(fieldValue.getValue());
      return contentFieldValueImpl;
    }
    else {
      throw new IllegalArgumentException("Argument can not be null.");
    }
  }

  @Override
  public MutableNumberFieldValue createNumberFieldValue(NumberFieldValue fieldValue) {
    if (fieldValue.getValue() != null) {
      NumberFieldValueImpl fieldValueImpl = new NumberFieldValueImpl();
      fieldValueImpl.setValue(fieldValue.getValue());
      return fieldValueImpl;
    }
    else {
      throw new IllegalArgumentException("Argument can not be null.");
    }
  }

  @Override
  public MutableOtherFieldValue createOtherFieldValue(OtherFieldValue fieldValue) {
    if (fieldValue.getValue() != null) {
      OtherFieldValueImpl otherFieldValueImpl = new OtherFieldValueImpl();
      otherFieldValueImpl.setValue(fieldValue.getValue());
      return otherFieldValueImpl;
    }
    else {
      throw new IllegalArgumentException("Argument can not be null.");
    }
  }

  @Override
  public MutableStringFieldValue createStringFieldValue(StringFieldValue fieldValue) {
    if (fieldValue.getValue() != null) {
      StringFieldValueImpl stringFieldValueImpl = new StringFieldValueImpl();
      stringFieldValueImpl.setValue(fieldValue.getValue());
      return stringFieldValueImpl;
    }
    else {
      throw new IllegalArgumentException("Argument can not be null.");
    }
  }

  @Override
  public ContentId createContentId(WorkspaceId workspaceId, byte[] id) {
    if (workspaceId != null && id != null) {
      ContentIdImpl contentIdImpl = new ContentIdImpl();
      contentIdImpl.setId(id);
      contentIdImpl.setWorkspaceId(workspaceId);
      return contentIdImpl;
    }
    else {
      throw new IllegalArgumentException("Argument can not be null.");
    }
  }

  @Override
  public Content loadContent(ContentId contentId) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Filter craeteFilter() {
    return new FilterImpl();
  }

  @Override
  public Set<Content> search(Filter filter) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public MutableDateTimeFieldValue createDateTimeFieldValue() {
    return new DateTimeFieldValueImpl();
  }

  @Override
  public MutableBooleanFieldValue createBooleanFieldValue() {
    return new BooleanFieldValueImpl();
  }

  @Override
  public MutableCollectionFieldValue createCollectionFieldValue() {
    return new CollectionFieldValueImpl();
  }

  @Override
  public MutableContentFieldValue createContentFieldValue() {
    return new ContentFieldValueImpl();
  }

  @Override
  public MutableNumberFieldValue createNumberFieldValue() {
    return new NumberFieldValueImpl();
  }

  @Override
  public MutableOtherFieldValue createOtherFieldValue() {
    return new OtherFieldValueImpl();
  }

  @Override
  public MutableStringFieldValue createStringFieldValue() {
    return new StringFieldValueImpl();
  }
}
