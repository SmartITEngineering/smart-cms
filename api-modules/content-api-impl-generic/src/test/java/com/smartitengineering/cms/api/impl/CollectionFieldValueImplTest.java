package com.smartitengineering.cms.api.impl;

import com.smartitengineering.cms.api.content.FieldValue;
import com.smartitengineering.cms.api.impl.content.CollectionFieldValueImpl;
import com.smartitengineering.cms.api.impl.content.StringFieldValueImpl;
import java.util.Arrays;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollectionFieldValueImplTest extends TestCase {

  private final transient Logger logger = LoggerFactory.getLogger(getClass());

  public void testToString() {
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
    logger.info("Collection field value toString() " + impl.toString());
    assertEquals("[\"TEST1\",\"TEST2\",\"TEST3\",\"TEST4\"]", impl.toString());
  }
}
