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
package com.smartitengineering.cms.api.impl;

import com.smartitengineering.cms.api.common.PersistentWriter;
import com.smartitengineering.cms.spi.SmartSPI;
import com.smartitengineering.cms.spi.persistence.PersistentService;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;

/**
 * An abstract implementation providing persistence operations for concrete
 * implementations of {@link PersistentWriter}.
 * @author imyousuf
 */
public abstract class AbstractPersistableDomain<T extends PersistentWriter>
    extends AbstractLockableDomain
    implements PersistentWriter {

  /**
   * The persistence service for this domain instance.
   * @see SmartSPI#getPersistentService(java.lang.Class)
   */
  protected PersistentService<T> service;
  /**
   * Set whether to invoke {@link AbstractLockableDomain#lock} or
   * {@link AbstractLockableDomain#tryLock()} next time {@link LockPerformer}
   * is used. If true will use <tt>lock</tt> else <tt>tryLock</tt>. Defaults
   * to false.
   */
  protected boolean nextPerformToWaitForLock;

  /**
   * Initializes itself with the beans it requires
   * @param pesistenceRegistryClass Persistence class to look for in the
   *																	registry
   */
  protected AbstractPersistableDomain() {
    Class<T> pesistenceRegistryClass =
                       (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).
        getActualTypeArguments()[0];
    service = SmartSPI.getInstance().getPersistentService(pesistenceRegistryClass);
    nextPerformToWaitForLock = false;
  }

  /**
   * Retrieve the service instance for this instance from the registry.
   * @return Service instance being used by this instance
   */
  public PersistentService<T> getService() {
    return service;
  }

  /**
   * Retrieves whether the next {@link LockPerformer} task should try for
   * lock or tryLock
   * @return If true then will be trying for lock else tryLock
   * @see AbstractPersistableDomain#nextPerformToWaitForLock
   */
  protected boolean isNextPerformToWaitForLock() {
    return nextPerformToWaitForLock;
  }

  /**
   * Sets what to use for next {@link LockPerformer} task,
   * @param nextPerformToWaitForLock If true lock will be used else tryLock
   * @see AbstractPersistableDomain#nextPerformToWaitForLock
   */
  protected void setNextPerformToWaitForLock(boolean nextPerformToWaitForLock) {
    this.nextPerformToWaitForLock = nextPerformToWaitForLock;
  }

  @Override
  public void put()
      throws IOException {
    if (isPersisted()) {
      create();
    }
    else {
      update();
    }
  }

  protected void create()
      throws IOException {
    try {
      service.create((T) this);
    }
    catch (Exception ex) {
      throw new IOException(ex);
    }
  }

  protected void update() throws IOException {
    new LockPerformer<Void>(isNextPerformToWaitForLock()) {

      @Override
      protected Void run() throws IOException {
        if (!isPersisted()) {
          throw new IOException("Can't update a domain not already persisted!");
        }
        try {
          service.update((T) AbstractPersistableDomain.this);
        }
        catch (Exception ex) {
          throw new IOException(ex);
        }
        return null;
      }
    }.perform();
  }

  @Override
  public void delete()
      throws IOException {
    new LockPerformer<Void>(isNextPerformToWaitForLock()) {

      @Override
      protected Void run()
          throws IOException {
        if (!isPersisted()) {
          throw new IOException(
              "Can't delete a domain not already persisted!");
        }
        try {
          service.delete((T) AbstractPersistableDomain.this);
        }
        catch (Exception ex) {
          throw new IOException(ex);
        }
        return null;
      }
    }.perform();
  }

  /**
   * Retrieves whether the domain is from a persistence storage, i.e. it has
   * be created in the storage.
   * @return True if its been created else false, that is not from persistence
   *					storage or not yet created.
   */
  public abstract boolean isPersisted();

  /**
   * An abstract Lock attaining and releasing code block. This uses the
   * {@link AbstractPersistableDomain#nextPerformToWaitForLock} setting to
   * determine how to attain the lock if and only if lock is not attained. It
   * will only unclock a lock if and only if it attains it itself.
   * @param <V> What the lock performance task returns
   */
  protected abstract class LockPerformer<V> {

    private boolean waitToAttain;

    /**
     * Defaults to false.
     * @see LockPerformer#LockPerformer(boolean)
     */
    public LockPerformer() {
      this(false);
    }

    /**
     * Initializes the lock performer with code whether to wait for lock or
     * not.
     * @param waitToAttain If true will wait to attain the lock.
     */
    public LockPerformer(boolean waitToAttain) {
      this.waitToAttain = waitToAttain;
    }

    /**
     * This operation should be implemented by concrete implementations to
     * perform lock attaining tasks.
     * @return Anything required to return by the original task
     * @throws IOException If waiting for lock is disabled and lock could not
     *											be attained or if there is any error in the
     *											{@link LockPerformer#run()} implementation
     */
    protected abstract V run()
        throws IOException;

    /**
     * Perform a specified task in lock mode. If lock is specified to be
     * waited for, it will do so. It uses {@link LockPerformer#run()} to
     * do the actual job while it focuses on attaining lock and unlocking it
     * once task is done; lock will be attempted to be attained or unlocked
     * if and only if it is already not attained. For this it will use
     * {@link AbstractLockableDomain#isLockAttained()}
     * @return Whatever is required by invoker.
     * @throws IOException If waiting for lock is disabled and lock could not
     *											be attained or if there is any error in the
     *											{@link LockPerformer#run()} implementation
     */
    public V perform()
        throws IOException {
      boolean attainLock = isLockOwned();
      if (attainLock) {
        if (!waitToAttain && !tryLock()) {
          throw new IOException("Lock could be attained!");
        }
        else {
          lock();
        }
      }
      V result = run();
      if (attainLock) {
        unlock();
      }
      return result;
    }
  }
}
