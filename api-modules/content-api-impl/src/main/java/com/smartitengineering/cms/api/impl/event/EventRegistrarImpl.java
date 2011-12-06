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
package com.smartitengineering.cms.api.impl.event;

import com.google.inject.Inject;
import com.smartitengineering.cms.api.event.Event;
import com.smartitengineering.cms.api.event.Event.EventType;
import com.smartitengineering.cms.api.event.Event.Type;
import com.smartitengineering.cms.api.event.EventListener;
import com.smartitengineering.cms.api.factory.event.EventRegistrar;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class EventRegistrarImpl implements EventRegistrar {

  private final Collection<EventListener> listeners;
  private final ExecutorService executorService;
  protected final transient Logger logger = LoggerFactory.getLogger(getClass());

  {
    listeners = new CopyOnWriteArrayList<EventListener>();
    executorService = Executors.newCachedThreadPool();
  }

  @Inject(optional = true)
  public void setInitialListeners(Collection<EventListener> listeners) {
    if (logger.isDebugEnabled()) {
      logger.debug("Injected Listeners " + listeners);
    }
    if (listeners == null || listeners.isEmpty()) {
      return;
    }
    this.listeners.addAll(listeners);
  }

  @Override
  public void addListener(EventListener listener) {
    if (listeners.contains(listener)) {
      return;
    }
    listeners.add(listener);
  }

  @Override
  public void removeListener(EventListener listener) {
    if (listeners.contains(listener)) {
      listeners.remove(listener);
    }
  }

  @Override
  public void notifyEvent(Event event) {
    for (EventListener listener : listeners) {
      if (listener.accepts(event)) {
        listener.notify(event);
      }
    }
  }

  @Override
  public void notifyEventAsynchronously(final Event event) {
    executorService.submit(new Runnable() {

      @Override
      public void run() {
        notifyEvent(event);
      }
    });
  }

  @Override
  public <T> Event<T> createEvent(EventType eventType, Type sourceType, T source) {
    EventImpl<T> impl = new EventImpl<T>(sourceType, eventType, source);
    return impl;
  }
}
