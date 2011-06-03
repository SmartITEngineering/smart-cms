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
package com.smartitengineering.cms.maven.dto.generator.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang.StringUtils;

/**
 * Goal which touches a timestamp file. Configure any one of contentTypesResourceConfig, contentTypeResourceConfig and
 * contentTypeResource. Not configuring any one of them or more than one would result runtime exception.
 *
 * @goal generate
 * 
 * @phase generate-sources
 */
public class PojoGeneratorMojo extends AbstractMojo {

  /**
   * Location of the file.
   * @parameter expression="${generated.sources.directory}"
   * @required
   */
  private File outputDirectory;
  /**
   * A collection of content types for generating POJOs for beans in multiple workspaces. Content types could either be
   * a file or a classpath resource. If either is missing it would result a classpath exception
   * @parameter
   */
  private Map<String, String> contentTypesResourceConfig;
  /**
   * A content type classpath resource name
   * @parameter
   */
  private String contentTypeResourceConfig;
  /**
   * A content type file
   * @parameter
   */
  private File contentTypeResource;

  public void execute()
      throws MojoExecutionException {
    File f = outputDirectory;

    if (!f.exists()) {
      f.mkdirs();
    }
    if (contentTypesResourceConfig == null || contentTypesResourceConfig.isEmpty() || StringUtils.isBlank(
        contentTypeResourceConfig) || contentTypeResource == null) {
      throw new MojoExecutionException("Parameters not specified properly. Any one and only one of " +
          "contentTypesResourceConfig, contentTypeResourceConfig and contentTypeResource must be specified!");
    }
    final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    if (contentTypeResource != null && (contentTypeResource.exists() || !contentTypeResource.isFile())) {
      throw new MojoExecutionException("'contentTypeResource' file either does not exist or is not a file");
    }
    else if (StringUtils.isNotBlank(contentTypeResourceConfig) && classLoader.getResource(contentTypeResourceConfig) ==
        null) {
      throw new MojoExecutionException("'contentTypeResourceConfig' classpath resource does not exist");
    }
    else {
      for (Entry<String, String> contentTypeRsrc : contentTypesResourceConfig.entrySet()) {
        final String key = contentTypeRsrc.getKey();
        if (StringUtils.isBlank(key)) {
          throw new MojoExecutionException("'contentTypesResourceConfig' can not have empty key");
        }
        final String[] wId = key.split(":");
        if (wId.length != 2) {
          throw new MojoExecutionException("'contentTypesResourceConfig' key must be in 'string:string' format");
        }
        final String value = contentTypeRsrc.getValue();
        if (StringUtils.isBlank(value)) {
          throw new MojoExecutionException("'contentTypesResourceConfig' can not have empty resource config");
        }
        File file = new File(value);
        URL rsrcUrl = classLoader.getResource(value);
        if (file.exists() && file.isFile()) {
        }
        else if (rsrcUrl != null) {
        }
        else {
          throw new MojoExecutionException("'contentTypesResourceConfig' can not have a resource config that is neither" +
              "a file nor a classpath resoruce");
        }
      }
    }
  }
}
