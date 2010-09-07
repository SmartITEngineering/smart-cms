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
package com.smartitengineering.cms.api.common;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author imyousuf
 */
public final class MediaType {

  private final String type;
  private final String subtype;
  private final Map<String, String> parameters;
  private static final String MEDIA_TYPE_WILDCARD = "*";
  private static final Pattern WHITESPACE_OR_QUOTE = Pattern.compile("[\\s\"]");
  public static final Pattern MEDIA_TYPE_REGEX = Pattern.compile(
      "([\\S&&[^/]]+)/([\\S&&[^;]]+)(;([\\S&&[^=]]+)=([\\S&&[^;]]+))*");
  public static final MediaType WILDCARD = new MediaType();
  public static final MediaType APPLICATION_XML = new MediaType("application", "xml");
  public static final MediaType TEXT_XML = new MediaType("text", "xml");
  public static final MediaType TEXT_HTML = new MediaType("text", "html");
  public static final MediaType TEXT_PLAIN = new MediaType("text", "plain");
  public static final MediaType APPLICATION_JSON = new MediaType("application", "json");
  public static final MediaType APPLICATION_ATOM_XML = new MediaType("application", "atom+xml");
  public static final MediaType APPLICATION_ATOM_XML_ENTRY = new MediaType("application", "atom+xml", Collections.
      singletonMap("type", "entry"));
  private static final Map<String, MediaType> parseResult = new WeakHashMap<String, MediaType>();
  public static int hitCount;

  public static MediaType fromString(String mediaType) {
    if (StringUtils.isBlank(mediaType)) {
      return MediaType.WILDCARD;
    }
    MediaType cacheResult = parseResult.get(mediaType);
    if (cacheResult != null) {
      return cacheResult;
    }
    Matcher matcher = MEDIA_TYPE_REGEX.matcher(mediaType);
    if (matcher.matches()) {
      String type = matcher.group(1);
      String subtype = matcher.group(2);
      final int endOfSubtype = matcher.end(2);
      Map<String, String> params = new HashMap<String, String>();
      if (endOfSubtype < mediaType.length()) {
        String paramsStr = mediaType.substring(endOfSubtype);
        if (StringUtils.isNotBlank(paramsStr)) {
          String[] parameters = paramsStr.split(";");
          for (String parameter : parameters) {
            final int indexOfEquals = parameter.indexOf('=');
            if (indexOfEquals >= 0) {
              params.put(parameter.substring(0, indexOfEquals), indexOfEquals < parameter.length() - 1 ? parameter.
                  substring(indexOfEquals + 1) : "");
            }
          }
        }
      }
      final MediaType result = new MediaType(type, subtype, params);
      parseResult.put(mediaType, result);
      return result;
    }
    else {
      return MediaType.WILDCARD;
    }
  }

  public MediaType() {
    this(null, null);
  }

  public MediaType(String type, String subtype) {
    this(type, subtype, Collections.<String, String>emptyMap());
  }

  public MediaType(String type, String subtype, Map<String, String> parameters) {
    this.type = type == null ? MEDIA_TYPE_WILDCARD : type;
    this.subtype = subtype == null ? MEDIA_TYPE_WILDCARD : subtype;
    if (parameters == null) {
      this.parameters = Collections.emptyMap();
    }
    else {
      Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {

        @Override
        public int compare(String key1, String key2) {
          return key1.compareToIgnoreCase(key2);
        }
      });
      for (Map.Entry<String, String> entry : parameters.entrySet()) {
        map.put(entry.getKey().toLowerCase(), entry.getValue());
      }
      this.parameters = Collections.unmodifiableMap(map);
    }
  }

  public Map<String, String> getParameters() {
    return parameters;
  }

  public String getSubtype() {
    return subtype;
  }

  public String getType() {
    return type;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof MediaType)) {
      return false;
    }
    MediaType other = (MediaType) obj;
    return (this.type.equalsIgnoreCase(other.type) && this.subtype.equalsIgnoreCase(other.subtype) && this.parameters.
            equals(other.parameters));
  }

  @Override
  public int hashCode() {
    return (this.type.toLowerCase() + this.subtype.toLowerCase()).hashCode() + this.parameters.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(this.getType()).append('/').append(this.getSubtype());
    for (Map.Entry<String, String> e : this.getParameters().entrySet()) {
      builder.append(';').append(e.getKey()).append('=');
      escapeValueForWhitespaceOrQuotes(builder, e.getValue());
    }
    return builder.toString();
  }

  private static void escapeValueForWhitespaceOrQuotes(StringBuilder builder, String value) {
    if (value == null) {
      return;
    }
    Matcher m = WHITESPACE_OR_QUOTE.matcher(value);
    boolean quote = m.find();
    if (quote) {
      builder.append('"');
    }
    appendEscapingQuotes(builder, value);
    if (quote) {
      builder.append('"');
    }
  }

  private static void appendEscapingQuotes(StringBuilder builder, String value) {
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      if (c == '"') {
        builder.append('\\');
      }
      builder.append(c);
    }
  }
}
