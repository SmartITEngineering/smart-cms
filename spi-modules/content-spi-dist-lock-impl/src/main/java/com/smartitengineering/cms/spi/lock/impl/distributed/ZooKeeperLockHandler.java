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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.smartitengineering.cms.api.factory.write.Lock;
import com.smartitengineering.cms.spi.lock.Key;
import com.smartitengineering.cms.spi.lock.LockHandler;
import java.io.IOException;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
@Singleton
public class ZooKeeperLockHandler implements LockHandler, Watcher {

  private final transient Logger logger = LoggerFactory.getLogger(getClass());
  protected final ZKConfig config;

  @Inject
  public ZooKeeperLockHandler(@Named("zkConnectString") final String connectString,
                              @Named("zkRootNode") final String rootNode,
                              @Named("zkNodeId") final String nodeId,
                              @Named("zkTimeout") final int timeout,
                              final LocalLockRegistrar registrar) {
    final ZooKeeper zooKeeper;
    try {
      zooKeeper = new ZooKeeper(connectString, timeout, this);
    }
    catch (IOException ex) {
      logger.error("Could not intialize ZooKeeper connection!");
      throw new IllegalStateException(ex);
    }
    final String srootNode;
    if (rootNode.startsWith("/")) {
      srootNode = rootNode;
    }
    else {
      srootNode = new StringBuilder("/").append(rootNode).toString();
    }
    config = new ZKConfig(zooKeeper, srootNode, nodeId, registrar);
    logger.info("Connected to ZooKeeper server");
    try {
      initializeRootNode();
    }
    catch (Exception ex) {
      logger.error("Could not create root node!");
      throw new IllegalStateException(ex);
    }
    logger.info("Created root node");
  }

  public ZKConfig getConfig() {
    return config;
  }

  public Lock register(Key key) {
    ZKLock lock = getLock(key);
    return lock;
  }

  public void unregister(Key key) {
    ZKLock lock = getLock(key);
    if (lock.isLockOwned()) {
      lock.unlock();
    }
  }

  protected void createNodeIfNotExists(String node, boolean retry, byte... data) throws KeeperException,
                                                                                        InterruptedException {
    final ZooKeeper zooKeeper = config.getZooKeeper();
    final Stat stat = zooKeeper.exists(node, false);
    if (stat == null) {
      zooKeeper.create(node, data == null ? new byte[]{} : data, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }
    else if (data != null && data.length > 0) {
      zooKeeper.setData(node, data, stat.getVersion());
    }
  }

  protected ZKLock getLock(Key key) {
    ZKLock lock = new ZKLock(config, key);
    return lock;
  }

  public void process(WatchedEvent event) {
    //TODO Process WatchedEvent
  }

  private void initializeRootNode() throws KeeperException, InterruptedException {
    String node = config.getRootNode();
    createNodeIfNotExists(node, false);
  }
}
