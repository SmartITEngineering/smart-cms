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
package com.smartitengineering.cms.content.api.impl;

import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.cms.api.impl.workspace.WorkspaceIdImpl;
import com.smartitengineering.cms.api.impl.type.ContentTypeIdImpl;
import com.smartitengineering.cms.api.type.ContentTypeId;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;

public class IdTest extends TestCase {

  private WorkspaceId workspaceId;
  private ContentTypeId contentTypeId;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    WorkspaceIdImpl workspaceIdImpl = new WorkspaceIdImpl();
    workspaceIdImpl.setGlobalNamespace("global");
    workspaceIdImpl.setName("workspace");
    workspaceId = workspaceIdImpl;
    ContentTypeIdImpl idImpl = new ContentTypeIdImpl();
    idImpl.setWorkspace(workspaceId);
    idImpl.setNamespace("contentTypeNS");
    idImpl.setName("contentTypeName");
    contentTypeId = idImpl;
  }

  public void testSettersWithColon() {
    final String suffix = ContentTypeIdImpl.STANDARD_ERROR_MSG.substring(2);
    WorkspaceIdImpl workspaceIdImpl = new WorkspaceIdImpl();
    try {
      workspaceIdImpl.setGlobalNamespace("global:");
      fail("Setter with colon should not exit normally");
    }
    catch (IllegalArgumentException exception) {
      assertTrue(exception.getMessage().endsWith(suffix));
    }
    try {
      workspaceIdImpl.setName("work:space");
      fail("Setter with colon should not exit normally");
    }
    catch (IllegalArgumentException exception) {
      assertTrue(exception.getMessage().endsWith(suffix));
    }
    ContentTypeIdImpl idImpl = new ContentTypeIdImpl();
    idImpl.setWorkspace(workspaceIdImpl);
    try {
      idImpl.setNamespace("contentType:NS");
      fail("Setter with colon should not exit normally");
    }
    catch (IllegalArgumentException exception) {
      assertTrue(exception.getMessage().endsWith(suffix));
    }
    try {
      idImpl.setName(":contentTypeName");
      fail("Setter with colon should not exit normally");
    }
    catch (IllegalArgumentException exception) {
      assertTrue(exception.getMessage().endsWith(suffix));
    }
  }

  public void testSettersWithBlank() {
    final String suffix = ContentTypeIdImpl.STANDARD_ERROR_MSG.substring(2);
    WorkspaceIdImpl workspaceIdImpl = new WorkspaceIdImpl();
    try {
      workspaceIdImpl.setGlobalNamespace("\t\n");
      fail("Setter with blank value should not exit normally");
    }
    catch (IllegalArgumentException exception) {
      assertTrue(exception.getMessage().endsWith(suffix));
    }
    try {
      workspaceIdImpl.setName("");
      fail("Setter with blank value should not exit normally");
    }
    catch (IllegalArgumentException exception) {
      assertTrue(exception.getMessage().endsWith(suffix));
    }
    ContentTypeIdImpl idImpl = new ContentTypeIdImpl();
    try {
      idImpl.setWorkspace(null);
      fail("Setter with blank value should not exit normally");
    }
    catch (IllegalArgumentException exception) {
      assertTrue(exception.getMessage().endsWith(suffix));
    }
    try {
      idImpl.setNamespace(" \t");
      fail("Setter with blank value should not exit normally");
    }
    catch (IllegalArgumentException exception) {
      assertTrue(exception.getMessage().endsWith(suffix));
    }
    try {
      idImpl.setName(null);
      fail("Setter with blank value should not exit normally");
    }
    catch (IllegalArgumentException exception) {
      assertTrue(exception.getMessage().endsWith(suffix));
    }
  }

  public void testToString() {
    assertEquals("global:workspace", workspaceId.toString());
    assertEquals("global:workspace:contentTypeNS:contentTypeName", contentTypeId.toString());
  }

  public void testEqualsAndHashCode() {
    WorkspaceIdImpl workspaceIdImpl = new WorkspaceIdImpl();
    workspaceIdImpl.setGlobalNamespace("global");
    workspaceIdImpl.setName("workspace");
    assertEquals(workspaceId, workspaceIdImpl);
    assertEquals(workspaceId.hashCode(), workspaceIdImpl.hashCode());
    ContentTypeIdImpl idImpl = new ContentTypeIdImpl();
    idImpl.setWorkspace(workspaceIdImpl);
    idImpl.setNamespace("contentTypeNS");
    idImpl.setName("contentTypeName");
    assertEquals(contentTypeId, idImpl);
    assertEquals(contentTypeId.hashCode(), idImpl.hashCode());
  }

  public void testWorkspaceIdSerialization() throws IOException, ClassNotFoundException {
    ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
    ObjectOutputStream outputStream = new ObjectOutputStream(baoStream);
    outputStream.writeObject(workspaceId);
    IOUtils.closeQuietly(outputStream);
    ByteArrayInputStream baiStream = new ByteArrayInputStream(baoStream.toByteArray());
    ObjectInputStream inputStream = new ObjectInputStream(baiStream);
    System.out.println(baoStream.toString());
    assertEquals(workspaceId, inputStream.readObject());
    baoStream = new ByteArrayOutputStream();
    DataOutputStream dataOutputStream = new DataOutputStream(baoStream);
    workspaceId.writeExternal(dataOutputStream);
    IOUtils.closeQuietly(dataOutputStream);
    System.out.println(workspaceId.toString() + " ================= " + baoStream.toString());
    assertEquals(workspaceId.toString(), baoStream.toString());
    WorkspaceIdImpl idImpl = new WorkspaceIdImpl();
    idImpl.readExternal(new DataInputStream(new ByteArrayInputStream(baoStream.toByteArray())));
    assertEquals(workspaceId, idImpl);
  }

  public void testContentTypeIdSerialization() throws IOException, ClassNotFoundException {
    ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
    ObjectOutputStream outputStream = new ObjectOutputStream(baoStream);
    outputStream.writeObject(contentTypeId);
    IOUtils.closeQuietly(outputStream);
    ByteArrayInputStream baiStream = new ByteArrayInputStream(baoStream.toByteArray());
    ObjectInputStream inputStream = new ObjectInputStream(baiStream);
    assertEquals(contentTypeId, inputStream.readObject());
    baoStream = new ByteArrayOutputStream();
    DataOutputStream dataOutputStream = new DataOutputStream(baoStream);
    contentTypeId.writeExternal(dataOutputStream);
    IOUtils.closeQuietly(dataOutputStream);
    assertEquals(contentTypeId.toString(), baoStream.toString());
    ContentTypeIdImpl idImpl = new ContentTypeIdImpl();
    idImpl.readExternal(new DataInputStream(new ByteArrayInputStream(baoStream.toByteArray())));
    assertEquals(contentTypeId, idImpl);
  }
}
