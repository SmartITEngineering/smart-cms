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
package com.smartitengineering.cms.binder.guice;

import com.smartitengineering.cms.api.common.MediaType;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.factory.content.WriteableContent;
import com.smartitengineering.cms.api.factory.type.WritableContentType;
import com.smartitengineering.cms.api.impl.type.ContentTypeImpl;
import com.smartitengineering.cms.spi.SmartContentSPI;
import com.smartitengineering.cms.spi.impl.type.ContentTypePersistentService;
import com.smartitengineering.cms.spi.persistence.PersistentService;
import com.smartitengineering.cms.spi.type.PersistentContentTypeReader;
import junit.framework.TestCase;
import org.apache.commons.lang.StringUtils;

public class InjectionTest extends TestCase {

  @Override
  protected void setUp() throws Exception {
    Initializer.init();
  }

  public void testApi() {
    assertNotNull(SmartContentAPI.getInstance().getContentLoader());
    assertNotNull(SmartContentAPI.getInstance().getContentTypeLoader());
    assertNotNull(SmartContentAPI.getInstance().getWorkspaceApi());
    assertNotNull(SmartContentAPI.getInstance().getEventRegistrar());
  }

  public void testSpi() {
    assertNotNull(SmartContentSPI.getInstance().getTypeValidators());
    assertEquals(1, SmartContentSPI.getInstance().getTypeValidators().getValidators().size());
    assertNotNull(SmartContentSPI.getInstance().getTypeValidators().getValidators().get(MediaType.APPLICATION_XML));
    assertNotNull(SmartContentSPI.getInstance().getContentTypeDefinitionParsers());
    assertNotNull(SmartContentSPI.getInstance().getPersistentServiceRegistrar());
    final PersistentContentTypeReader contentTypeReader = SmartContentSPI.getInstance().getContentTypeReader();
    assertNotNull(contentTypeReader);
    assertNotNull(SmartContentSPI.getInstance().getContentReader());
    assertNotNull(SmartContentSPI.getInstance().getPersistentServiceRegistrar().getPersistentService(
        WriteableContent.class));
    assertNotNull(SmartContentSPI.getInstance().getWorkspaceService());
    assertNotNull(SmartContentSPI.getInstance().getRepresentationProvider());
    assertNotNull(SmartContentSPI.getInstance().getVariationProvider());
    assertNotNull(SmartContentSPI.getInstance().getValidatorProvider());
    assertNotNull(SmartContentSPI.getInstance().getContentSearcher());
    final PersistentService<WritableContentType> persistentService =
                                                 SmartContentSPI.getInstance().getPersistentService(
        WritableContentType.class);
    if (ContentTypePersistentService.class.isAssignableFrom(persistentService.getClass())) {
      ContentTypePersistentService service = (ContentTypePersistentService) persistentService;
      assertNotNull(service.getCommonReadDao());
      assertNotNull(service.getCommonWriteDao());
      assertSame(service.getCommonReadDao(), service.getCommonWriteDao());
    }
    else {
      fail("Could cast to expected instance!");
    }
    assertNotNull(persistentService);
    assertSame(contentTypeReader, persistentService);
    assertTrue(StringUtils.isNotBlank(SmartContentSPI.getInstance().getSchemaLocationForContentTypeXml()));
  }

  public void testPersistenceServiceLookup() {
    ContentTypeImpl typeImpl = new ContentTypeImpl();
    assertNotNull(typeImpl.getService());
  }
}
