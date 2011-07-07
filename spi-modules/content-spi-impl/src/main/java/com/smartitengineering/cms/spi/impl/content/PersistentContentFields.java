/*
 *
 * This is a simple Content Management System (CMS)
 * Copyright (C) 2011  Imran M Yousuf (imyousuf@smartitengineering.com)
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
package com.smartitengineering.cms.spi.impl.content;

import com.smartitengineering.cms.api.content.ContentId;
import com.smartitengineering.cms.api.content.Field;
import com.smartitengineering.domain.AbstractGenericPersistentDTO;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class PersistentContentFields extends AbstractGenericPersistentDTO<PersistentContentFields, ContentId, Long> {

  private final Map<String, Field> fields = new LinkedHashMap<String, Field>();
  protected final transient Logger logger = LoggerFactory.getLogger(getClass());

  public Map<String, Field> getFields() {
    return fields;
  }

  public void setFields(Map<String, Field> fields) {
    if (fields != null && !fields.isEmpty()) {
      this.fields.putAll(fields);
    }
  }

  public void addField(Field field) {
    if (field != null) {
      this.fields.put(field.getName(), field);
    }
  }

  public boolean isValid() {
    if (logger.isInfoEnabled()) {
      logger.info("Fields " + this.fields);
    }
    return !this.fields.isEmpty();
  }
}
