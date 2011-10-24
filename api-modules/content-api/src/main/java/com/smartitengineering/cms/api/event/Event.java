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
package com.smartitengineering.cms.api.event;

/**
 *
 * @author imyousuf
 */
public interface Event<T> {

  Type getEventSourceType();

  EventType getEventType();

  T getSource();

  enum EventType {

    CREATE,
    UPDATE,
    DELETE
  }

  enum Type {

    CONTENT,
    CONTENT_TYPE,
    WORKSPACE,
    SEQUENCE,
    REPRESENTATION,
    VARIATION,
    REPRESENTATION_TEMPLATE,
    VARIATION_TEMPLATE,
    VALIDATION_TEMPLATE,
    CONTENT_CO_PROCESSOR_TEMPLATE,
    ALL_REPRESENTATION_TEMPLATES,
    ALL_VARIATION_TEMPLATES,
    ALL_VALIDATION_TEMPLATES,
    ALL_CONTENT_CO_PROCESSOR_TEMPLATES,
    FRIENDLY,
    ALL_FRIENDLIES,
    ROOT_CONTENT,
    ALL_ROOT_CONTENTS,
  }
}
