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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Goal which starts the tools
 *
 * @author imyousuf
 * @goal stop
 */
public class StopMojo extends AbstractMojo {

  /**
   * Embedded tools port
   * @parameter expression="40404"
   * @required
   */
  private Integer embeddedPort;
  /**
   * Embedded tools host
   * @parameter expression="localhost"
   * @required
   */
  private String host;

  public void execute() throws MojoExecutionException, MojoFailureException {
    try {
      Socket clientSocket = new Socket(host, embeddedPort);
      Writer writer = new OutputStreamWriter(clientSocket.getOutputStream());
      BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
      writer.write("stop\n");
      writer.flush();
      String doneLine = reader.readLine();
      getLog().info("Received response: " + doneLine);
      reader.close();
      writer.close();
      clientSocket.close();
    }
    catch (Exception ex) {
      throw new MojoFailureException("Could send stop signal!", ex);
    }
  }
}
