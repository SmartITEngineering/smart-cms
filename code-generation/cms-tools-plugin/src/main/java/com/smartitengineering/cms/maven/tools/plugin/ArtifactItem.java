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
package com.smartitengineering.cms.maven.tools.plugin;

/**
 *
 * @author imyousuf
 */
public class ArtifactItem {

  /**
   * Group Id of Artifact
   * 
   * @parameter
   * @required
   */
  private String groupId;
  /**
   * Name of Artifact
   * 
   * @parameter
   * @required
   */
  private String artifactId;
  /**
   * Version of Artifact
   * 
   * @parameter
   * @required
   */
  private String version = null;
  /**
   * Type of Artifact (War,Jar,etc)
   * 
   * @parameter
   */
  private String type = "jar";
  /**
   * Classifier for Artifact (tests,sources,etc)
   * 
   * @parameter
   */
  private String classifier;

  public String getArtifactId() {
    return artifactId;
  }

  public void setArtifactId(String artifactId) {
    this.artifactId = artifactId;
  }

  public String getClassifier() {
    return classifier;
  }

  public void setClassifier(String classifier) {
    this.classifier = classifier;
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }
}
