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

import com.smartitengineering.cms.api.SmartContentAPI;
import com.smartitengineering.cms.api.common.MediaType;
import com.smartitengineering.cms.api.impl.type.ContentTypeImpl;
import com.smartitengineering.cms.api.type.MutableContentType;
import com.smartitengineering.cms.spi.SmartSPI;
import com.smartitengineering.cms.spi.impl.type.ContentTypePersistentService;
import com.smartitengineering.cms.spi.persistence.PersistentService;
import com.smartitengineering.cms.spi.type.PersistentContentTypeReader;
import junit.framework.TestCase;

public class InjectionTest extends TestCase {

  @Override
  protected void setUp() throws Exception {
    Initializer.init();
  }

  public void testApi() {
    assertNotNull(SmartContentAPI.getInstance().getContentLoader());
    assertNotNull(SmartContentAPI.getInstance().getContentTypeLoader());
    assertNotNull(SmartContentAPI.getInstance().getWorkspaceApi());
  }

  public void testSpi() {
    assertNotNull(SmartSPI.getInstance().getTypeValidators());
    assertEquals(1, SmartSPI.getInstance().getTypeValidators().getValidators().size());
    assertNotNull(SmartSPI.getInstance().getTypeValidators().getValidators().get(MediaType.APPLICATION_XML));
    assertNotNull(SmartSPI.getInstance().getContentTypeDefinitionParsers());
    assertNotNull(SmartSPI.getInstance().getPersistentServiceRegistrar());
    final PersistentContentTypeReader contentTypeReader = SmartSPI.getInstance().getContentTypeReader();
    assertNotNull(contentTypeReader);
    final PersistentService<MutableContentType> persistentService =
                                                SmartSPI.getInstance().getPersistentService(MutableContentType.class);
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
  }

  public void testPersistenceServiceLookup() {
    ContentTypeImpl typeImpl = new ContentTypeImpl();
    assertNotNull(typeImpl.getService());
  }
}
