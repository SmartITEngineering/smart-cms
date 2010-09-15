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
package com.smartitengineering.cms.spi.type;

import com.smartitengineering.cms.api.common.MediaType;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author imyousuf
 */
public final class ContentTypeDefinitionParsers {

  private final Map<MediaType, ContentTypeDefinitionParser> parsers =
                                                            new ConcurrentHashMap<MediaType, ContentTypeDefinitionParser>();

  public Map<MediaType, ContentTypeDefinitionParser> getParsers() {
    return Collections.unmodifiableMap(parsers);
  }

  public void setParsers(Map<MediaType, ContentTypeDefinitionParser> parsers) {
    if (parsers == null) {
      return;
    }
    this.parsers.clear();
    this.parsers.putAll(parsers);
  }

  public void addParser(ContentTypeDefinitionParser parser) {
    if (parser != null) {
      for (MediaType type : parser.getSupportedTypes()) {
        this.parsers.put(type, parser);
      }
    }
  }

  public void removeParser(ContentTypeDefinitionParser parser) {
    if (parser != null) {
      for (MediaType type : parser.getSupportedTypes()) {
        if (this.parsers.get(type).equals(parser)) {
          this.parsers.remove(type);
        }
      }
    }
  }
}
