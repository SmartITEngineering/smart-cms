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

import com.smartitengineering.cms.api.event.Event;
import com.smartitengineering.cms.api.event.Event.EventType;
import com.smartitengineering.cms.api.event.Event.Type;

/**
 *
 * @author imyousuf
 */
public class EventImpl<T> implements Event<T> {

  private final Type sourceType;
  private final EventType eventType;
  private final T source;

  public EventImpl(Type sourceType, EventType eventType, T source) {
    if (sourceType == null || eventType == null) {
      throw new IllegalArgumentException("Source and event type is null!");
    }
    this.sourceType = sourceType;
    this.eventType = eventType;
    this.source = source;
  }

  @Override
  public Type getEventSourceType() {
    return sourceType;
  }

  @Override
  public EventType getEventType() {
    return eventType;
  }

  @Override
  public T getSource() {
    return source;
  }
}
