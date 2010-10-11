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
package com.smartitengineering.cms.content.api.impl;

import com.smartitengineering.cms.api.content.CollectionFieldValue;
import com.smartitengineering.cms.api.content.FieldValue;
import com.smartitengineering.cms.api.impl.content.CollectionFieldValueImpl;
import com.smartitengineering.cms.api.impl.content.ContentLoaderImpl;
import com.smartitengineering.cms.api.impl.content.StringFieldValueImpl;
import com.smartitengineering.cms.api.impl.type.CollectionDataTypeImpl;
import com.smartitengineering.cms.api.impl.type.FieldDefImpl;
import com.smartitengineering.cms.api.impl.type.StringDataTypeImpl;
import com.smartitengineering.cms.api.type.MutableFieldDef;
import java.util.Arrays;
import java.util.Collection;
import junit.framework.TestCase;

/**
 *
 * @author imyousuf
 */
public class FieldValueParseTest extends TestCase {

  public void testFieldValueParseForCollection() {
    CollectionFieldValueImpl impl = new CollectionFieldValueImpl();
    StringFieldValueImpl stringFieldValueImpl = new StringFieldValueImpl();
    stringFieldValueImpl.setValue("TEST1");
    StringFieldValueImpl stringFieldValueImpl2 = new StringFieldValueImpl();
    stringFieldValueImpl2.setValue("TEST2");
    StringFieldValueImpl stringFieldValueImpl3 = new StringFieldValueImpl();
    stringFieldValueImpl3.setValue("TEST3");
    StringFieldValueImpl stringFieldValueImpl4 = new StringFieldValueImpl();
    stringFieldValueImpl4.setValue("TEST4");
    impl.setValue(Arrays.<FieldValue>asList(stringFieldValueImpl, stringFieldValueImpl2, stringFieldValueImpl3,
                                            stringFieldValueImpl4));
    MutableFieldDef fieldDef = new FieldDefImpl();
    final CollectionDataTypeImpl collectionDataTypeImpl = new CollectionDataTypeImpl();
    collectionDataTypeImpl.setItemDataType(new StringDataTypeImpl());
    fieldDef.setValueDef(collectionDataTypeImpl);
    CollectionFieldValue fieldValue = (CollectionFieldValue) new ContentLoaderImpl().getValueFor(impl.toString(),
                                                                                                 fieldDef);
    Collection<FieldValue> values = fieldValue.getValue();
    assertEquals(4, values.size());
    assertEquals(impl.toString(), fieldValue.toString());
  }
}
