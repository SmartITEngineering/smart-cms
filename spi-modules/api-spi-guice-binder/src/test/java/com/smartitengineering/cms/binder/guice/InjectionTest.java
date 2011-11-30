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
import com.smartitengineering.cms.spi.lock.impl.distributed.AppTest;
import com.smartitengineering.cms.spi.persistence.PersistentService;
import com.smartitengineering.cms.spi.type.PersistentContentTypeReader;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.server.NIOServerCnxn;
import org.apache.zookeeper.server.ZooKeeperServer;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class InjectionTest {

  private static NIOServerCnxn.Factory standaloneServerFactory;
  private static final int CLIENT_PORT = 3882;
  private static final int CONNECTION_TIMEOUT = 30000;

  @BeforeClass
  public static void setUpTests() throws Exception {
    Initializer.init();
    try {
      File snapDir = new File("./target/zk/");
      snapDir.mkdirs();
      ZooKeeperServer server = new ZooKeeperServer(snapDir, snapDir, 2000);
      standaloneServerFactory = new NIOServerCnxn.Factory(new InetSocketAddress(CLIENT_PORT));
      standaloneServerFactory.startup(server);
      if (!AppTest.waitForServerUp(CLIENT_PORT, CONNECTION_TIMEOUT)) {
        throw new IOException("Waiting for startup of standalone server");
      }
    }
    catch (Exception ex) {
      throw new IllegalStateException(ex);
    }
  }

  @AfterClass
  public static void tearDownTests() throws Exception {
    standaloneServerFactory.shutdown();
    if (!AppTest.waitForServerDown(CLIENT_PORT, CONNECTION_TIMEOUT)) {
      throw new IllegalStateException("Waiting for shutdown of standalone server");
    }
  }

  @Test
  public void testApi() {
    Assert.assertNotNull(SmartContentAPI.getInstance().getContentLoader());
    Assert.assertNotNull(SmartContentAPI.getInstance().getContentTypeLoader());
    Assert.assertNotNull(SmartContentAPI.getInstance().getWorkspaceApi());
    Assert.assertNotNull(SmartContentAPI.getInstance().getEventRegistrar());
  }

  @Test
  public void testSpi() {
    Assert.assertNotNull(SmartContentSPI.getInstance().getTypeValidators());
    Assert.assertEquals(1, SmartContentSPI.getInstance().getTypeValidators().getValidators().size());
    Assert.assertNotNull(SmartContentSPI.getInstance().getTypeValidators().getValidators().get(MediaType.APPLICATION_XML));
    Assert.assertNotNull(SmartContentSPI.getInstance().getContentTypeDefinitionParsers());
    Assert.assertNotNull(SmartContentSPI.getInstance().getPersistentServiceRegistrar());
    final PersistentContentTypeReader contentTypeReader = SmartContentSPI.getInstance().getContentTypeReader();
    Assert.assertNotNull(contentTypeReader);
    Assert.assertNotNull(SmartContentSPI.getInstance().getContentReader());
    Assert.assertNotNull(SmartContentSPI.getInstance().getPersistentServiceRegistrar().getPersistentService(
        WriteableContent.class));
    Assert.assertNotNull(SmartContentSPI.getInstance().getWorkspaceService());
    Assert.assertNotNull(SmartContentSPI.getInstance().getRepresentationProvider());
    Assert.assertNotNull(SmartContentSPI.getInstance().getVariationProvider());
    Assert.assertNotNull(SmartContentSPI.getInstance().getValidatorProvider());
    Assert.assertNotNull(SmartContentSPI.getInstance().getContentSearcher());
    Assert.assertNotNull(SmartContentSPI.getInstance().getContentTypeSearcher());
    final PersistentService<WritableContentType> persistentService =
                                                 SmartContentSPI.getInstance().getPersistentService(
        WritableContentType.class);
    if (ContentTypePersistentService.class.isAssignableFrom(persistentService.getClass())) {
      ContentTypePersistentService service = (ContentTypePersistentService) persistentService;
      Assert.assertNotNull(service.getCommonReadDao());
      Assert.assertNotNull(service.getCommonWriteDao());
      Assert.assertSame(service.getCommonReadDao(), service.getCommonWriteDao());
    }
    else {
      Assert.fail("Could cast to expected instance!");
    }
    Assert.assertNotNull(persistentService);
    Assert.assertSame(contentTypeReader, persistentService);
    Assert.assertTrue(StringUtils.isNotBlank(SmartContentSPI.getInstance().getSchemaLocationForContentTypeXml()));
  }

  @Test
  public void testPersistenceServiceLookup() {
    ContentTypeImpl typeImpl = new ContentTypeImpl();
    Assert.assertNotNull(typeImpl.getService());
  }
}
