/*
 *
 * This is a simple Content Management System (CMS)
 * Copyright (C) 2011 Imran M Yousuf (imyousuf@smartitengineering.com)
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
package com.smartitengineering.cms.spi.lock.impl.distributed;

import com.smartitengineering.cms.api.factory.write.Lock;
import com.smartitengineering.cms.spi.lock.Key;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.net.Socket;
import junit.framework.Assert;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.server.NIOServerCnxn;
import org.apache.zookeeper.server.ZooKeeperServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class AppTest {

  public static final String ROOT_NODE = "/smart-cms";
  private static NIOServerCnxn.Factory standaloneServerFactory;
  private static final int CLIENT_PORT = 3882;
  private static final int CONNECTION_TIMEOUT = 30000;
  private final String connectString = "localhost:" + CLIENT_PORT;
  private final Key key = new Key() {

    public String getKeyStringRep() {
      return "random-key-1";
    }
  };

  @BeforeClass
  public static void startZooKeeperServer() {
    try {
      File snapDir = new File("./target/zk/");
      snapDir.mkdirs();
      ZooKeeperServer server = new ZooKeeperServer(snapDir, snapDir, 2000);
      standaloneServerFactory = new NIOServerCnxn.Factory(new InetSocketAddress(CLIENT_PORT));
      standaloneServerFactory.startup(server);
      if (!waitForServerUp(CLIENT_PORT, CONNECTION_TIMEOUT)) {
        throw new IOException("Waiting for startup of standalone server");
      }
    }
    catch (Exception ex) {
      throw new IllegalStateException(ex);
    }
  }

  @AfterClass
  public static void shutdownZooKeeperServer() {
    standaloneServerFactory.shutdown();
    if (!waitForServerDown(CLIENT_PORT, CONNECTION_TIMEOUT)) {
      throw new IllegalStateException("Waiting for shutdown of standalone server");
    }
  }

  // XXX: From o.a.zk.t.ClientBase
  public static boolean waitForServerUp(int port, long timeout) {
    long start = System.currentTimeMillis();
    while (true) {
      try {
        Socket sock = new Socket("localhost", port);
        BufferedReader reader = null;
        try {
          OutputStream outstream = sock.getOutputStream();
          outstream.write("stat".getBytes());
          outstream.flush();

          Reader isr = new InputStreamReader(sock.getInputStream());
          reader = new BufferedReader(isr);
          String line = reader.readLine();
          if (line != null && line.startsWith("Zookeeper version:")) {
            return true;
          }
        }
        finally {
          sock.close();
          if (reader != null) {
            reader.close();
          }
        }
      }
      catch (IOException e) {
        // ignore as this is expected
      }

      if (System.currentTimeMillis() > start + timeout) {
        break;
      }
      try {
        Thread.sleep(250);
      }
      catch (InterruptedException e) {
        // ignore
      }
    }
    return false;
  }

  // XXX: From o.a.zk.t.ClientBase
  public static boolean waitForServerDown(int port, long timeout) {
    long start = System.currentTimeMillis();
    while (true) {
      try {
        Socket sock = new Socket("localhost", CLIENT_PORT);
        try {
          OutputStream outstream = sock.getOutputStream();
          outstream.write("stat".getBytes());
          outstream.flush();
        }
        finally {
          sock.close();
        }
      }
      catch (IOException e) {
        return true;
      }

      if (System.currentTimeMillis() > start + timeout) {
        break;
      }
      try {
        Thread.sleep(250);
      }
      catch (InterruptedException e) {
        // ignore
      }
    }
    return false;
  }

  @Test
  public void testInitialization() throws Exception {

    ZooKeeper zooKeeper = new ZooKeeper(connectString, CONNECTION_TIMEOUT, new Watcher() {

      public void process(WatchedEvent event) {
      }
    });
    Stat stat = zooKeeper.exists(ROOT_NODE, false);
    Assert.assertNull(stat);
    final LocalLockRegistrarImpl localLockRegistrarImpl = new LocalLockRegistrarImpl();
    localLockRegistrarImpl.initTimeoutChecking();
    ZooKeeperLockHandler handler = new ZooKeeperLockHandler(connectString, ROOT_NODE, "node1", CONNECTION_TIMEOUT,
                                                            localLockRegistrarImpl);
    stat = zooKeeper.exists(ROOT_NODE, false);
    Assert.assertNotNull(stat);
  }

  @Test
  public void testSimpleLocking() throws Exception {
    final LocalLockRegistrarImpl localLockRegistrarImpl1 = new LocalLockRegistrarImpl();
    localLockRegistrarImpl1.initTimeoutChecking();
    ZooKeeperLockHandler handler1 = new ZooKeeperLockHandler(connectString, ROOT_NODE, "node1", CONNECTION_TIMEOUT,
                                                             localLockRegistrarImpl1);
    Lock lock1 = handler1.getLock(key);
    Assert.assertNotNull(lock1);
    Assert.assertTrue(lock1.tryLock());
    Assert.assertTrue(lock1.isLockOwned());
    lock1.unlock();
    Assert.assertFalse(lock1.isLockOwned());
  }

  @Test
  public void testRepeatativeLocking() throws Exception {
    final LocalLockRegistrarImpl localLockRegistrarImpl1 = new LocalLockRegistrarImpl();
    localLockRegistrarImpl1.initTimeoutChecking();
    ZooKeeperLockHandler handler1 = new ZooKeeperLockHandler(connectString, ROOT_NODE, "node1", CONNECTION_TIMEOUT,
                                                             localLockRegistrarImpl1);
    Lock lock2 = handler1.getLock(key);
    Assert.assertNotNull(lock2);
    Assert.assertTrue(lock2.tryLock());
    Assert.assertTrue(lock2.tryLock());
    lock2.unlock();
  }

  @Test
  public void testSignleJVMLocking() throws Exception {
    final LocalLockRegistrarImpl localLockRegistrarImpl1 = new LocalLockRegistrarImpl();
    localLockRegistrarImpl1.initTimeoutChecking();
    ZooKeeperLockHandler handler1 = new ZooKeeperLockHandler(connectString, ROOT_NODE, "node1", CONNECTION_TIMEOUT,
                                                             localLockRegistrarImpl1);
    Lock lock1 = handler1.getLock(key);
    Lock lock2 = handler1.getLock(key);
    Assert.assertNotNull(lock1);
    Assert.assertNotNull(lock2);
    Assert.assertTrue(lock1.tryLock());
    Assert.assertTrue(lock1.isLockOwned());
    Assert.assertFalse(lock2.tryLock());
    lock1.unlock();
    Assert.assertFalse(lock1.isLockOwned());
    Assert.assertTrue(lock2.tryLock());
    Assert.assertTrue(lock2.tryLock());
    lock2.unlock();
    Assert.assertFalse(lock2.isLockOwned());
  }

  @Test
  public void testMultiJVMLocking() throws Exception {
    final LocalLockRegistrarImpl localLockRegistrarImpl1 = new LocalLockRegistrarImpl();
    localLockRegistrarImpl1.initTimeoutChecking();
    ZooKeeperLockHandler handler1 = new ZooKeeperLockHandler(connectString, ROOT_NODE, "node1", CONNECTION_TIMEOUT,
                                                             localLockRegistrarImpl1);
    Lock lock1 = handler1.getLock(key);
    //Simulate multi-jvm
    final LocalLockRegistrarImpl localLockRegistrarImpl2 = new LocalLockRegistrarImpl();
    localLockRegistrarImpl2.initTimeoutChecking();
    ZooKeeperLockHandler handler2 = new ZooKeeperLockHandler(connectString, ROOT_NODE, "node2", CONNECTION_TIMEOUT,
                                                             localLockRegistrarImpl2);
    Lock lock3 = handler2.getLock(key);
    Assert.assertTrue(lock1.tryLock());
    Assert.assertFalse(lock3.tryLock());
    Assert.assertFalse(lock3.isLockOwned());
    lock1.unlock();
    Assert.assertFalse(lock1.isLockOwned());
    Assert.assertTrue(lock3.tryLock());
    lock3.unlock();
    Assert.assertFalse(lock3.isLockOwned());
  }
}
