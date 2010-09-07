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
package com.smartitengineering.cms.content.api;

import com.smartitengineering.cms.api.common.MediaType;
import junit.framework.TestCase;

/**
 *
 * @author imyousuf
 */
public class MediaTypeTest extends TestCase {

  public void testToString() {
    assertEquals("*/*", MediaType.WILDCARD.toString());
    assertEquals("text/html", MediaType.TEXT_HTML.toString());
    assertEquals("application/atom+xml;type=entry", MediaType.APPLICATION_ATOM_XML_ENTRY.toString());
    final String customType = "application/atom+xml;mode=test;type=entry";
    assertEquals(customType, MediaType.fromString(customType).toString());
    long start = System.currentTimeMillis();
    MediaType mediaType = MediaType.fromString(MediaType.TEXT_HTML.toString());
    long end = System.currentTimeMillis();
    System.out.println("HTML PARSE TIME: " + (end - start));
    assertEquals(MediaType.TEXT_HTML.toString(), mediaType.toString());
    assertEquals(MediaType.TEXT_HTML, mediaType);
    start = System.currentTimeMillis();
    mediaType = MediaType.fromString(MediaType.APPLICATION_ATOM_XML_ENTRY.toString());
    end = System.currentTimeMillis();
    System.out.println("ENTRY PARSE TIME: " + (end - start));
    assertEquals(MediaType.APPLICATION_ATOM_XML_ENTRY.toString(), mediaType.toString());
    assertEquals(MediaType.APPLICATION_ATOM_XML_ENTRY, mediaType);
  }

  public void testTestCacheForParse() {
    final String customType = "application/atom+xml;mode=cache;type=entry";
    long start = System.currentTimeMillis();
    int repeat = 1000000;
    for (int i = 0; i < repeat; ++i) {
      assertEquals(customType, MediaType.fromString(customType).toString());
    }
    long end = System.currentTimeMillis();
    System.out.println("REPEATED PARSE TIME for (" + repeat + "): " + (end - start) + "ms");
  }
}
