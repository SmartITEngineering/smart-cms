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

import com.smartitengineering.util.rest.client.jersey.cache.CacheableClient;
import com.sun.jersey.api.client.Client;
import java.io.BufferedReader;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.HBaseTestingUtility;
import org.apache.hadoop.hbase.zookeeper.MiniZooKeeperCluster;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.webapp.WebAppClassLoader;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Goal which starts the tools
 *
 * @author imyousuf
 * @goal start
 */
public class StartMojo
    extends AbstractMojo {

  /**
   * Solr HBase war artifact
   * @parameter
   * @required
   */
  private ArtifactItem solrArtifact;
  /**
   * Event Hub HBase war artifact
   * @parameter
   * @required
   */
  private ArtifactItem eventHubArtifact;
  /**
   * CMS Event Hub war artifact
   * @parameter
   * @required
   */
  private ArtifactItem cmsWebServiceArtifact;
  /**
   * Location of the output files.
   * @parameter expression="${project.build.directory}"
   * @required
   */
  private File outputDirectory;
  /**
   * Location of the Solr config files.
   * @parameter 
   * @required
   */
  private File solrHomeDirectory;
  /**
   * Whether the tools will run in daemon mode
   * @parameter expression="false"
   * @required
   */
  private boolean daemonMode;
  /**
   * Embedded Jetty port
   * @parameter expression="10080"
   * @required
   */
  private Integer jettyPort;
  /**
   * Embedded ZooKeeper port
   * @parameter expression="2181"
   * @required
   */
  private Integer zooKeeperPort;
  /**
   * Embedded tools port
   * @parameter expression="40404"
   * @required
   */
  private Integer embeddedPort;
  /**
   * HBase version
   * @parameter expression="${hbase.version}"
   * @required
   */
  private String hbaseVersion;
  /**
   * Hadoop Version
   * @parameter expression="${hadoop.version}"
   * @required
   */
  private String hadoopVersion;
  /**
   * Jersey version
   * @parameter expression="${jersey.version}"
   * @required
   */
  private String jerseyVersion;
  /**
   * @component
   */
  private ArtifactFactory factory;
  /**
   * Used to look up Artifacts in the remote repository.
   *
   * @component
   */
  protected ArtifactResolver resolver;
  /**
   * Location of the local repository.
   *
   * @parameter expression="${localRepository}"
   * @readonly
   * @required
   */
  protected ArtifactRepository local;
  /**
   * List of Remote Repositories used by the resolver
   *
   * @parameter expression="${project.remoteArtifactRepositories}"
   * @readonly
   * @required
   */
  protected List remoteRepos;

  public String getJerseyVersion() {
    return jerseyVersion;
  }

  public void setJerseyVersion(String jerseyVersion) {
    this.jerseyVersion = jerseyVersion;
  }

  public String getHadoopVersion() {
    return hadoopVersion;
  }

  public void setHadoopVersion(String hadoopVersion) {
    this.hadoopVersion = hadoopVersion;
  }

  public String getHbaseVersion() {
    return hbaseVersion;
  }

  public void setHbaseVersion(String hbaseVersion) {
    this.hbaseVersion = hbaseVersion;
  }

  public ArtifactItem getEventHubArtifact() {
    return eventHubArtifact;
  }

  public void setEventHubArtifact(ArtifactItem eventHubArtifact) {
    this.eventHubArtifact = eventHubArtifact;
  }

  public ArtifactItem getSolrArtifact() {
    return solrArtifact;
  }

  public void setSolrArtifact(ArtifactItem solrArtifact) {
    this.solrArtifact = solrArtifact;
  }

  public boolean isDaemonMode() {
    return daemonMode;
  }

  public void setDaemonMode(boolean daemonMode) {
    this.daemonMode = daemonMode;
  }

  public File getOutputDirectory() {
    return outputDirectory;
  }

  public void setOutputDirectory(File outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  public void execute()
      throws MojoExecutionException {
    {
      Artifact artifact = getArtifact(solrArtifact);
      File solrOutDir = new File(outputDirectory, "solr");
      solrOutDir.mkdirs();
      extract(artifact.getFile(), solrOutDir);
    }
    {
      Artifact artifact = getArtifact(eventHubArtifact);
      File hubOutDir = new File(outputDirectory, "hub");
      hubOutDir.mkdirs();
      extract(artifact.getFile(), hubOutDir);
      File libDir = new File(new File(hubOutDir, "WEB-INF"), "lib");
      {
        new File(libDir, new StringBuilder("hbase-").append(hbaseVersion).append(".jar").toString()).delete();
        new File(libDir, new StringBuilder("hadoop-core-").append(hadoopVersion).append(".jar").toString()).delete();
        new File(libDir, new StringBuilder("jersey-core-").append(jerseyVersion).append(".jar").toString()).delete();
        new File(libDir, new StringBuilder("jersey-json-").append(jerseyVersion).append(".jar").toString()).delete();
        new File(libDir, new StringBuilder("jersey-server-").append(jerseyVersion).append(".jar").toString()).delete();
      }
    }
    {
      Artifact artifact = getArtifact(cmsWebServiceArtifact);
      File cmsOutDir = new File(outputDirectory, "cms");
      cmsOutDir.mkdirs();
      extract(artifact.getFile(), cmsOutDir);
      File libDir = new File(new File(cmsOutDir, "WEB-INF"), "lib");
      {
        new File(libDir, new StringBuilder("hbase-").append(hbaseVersion).append(".jar").toString()).delete();
        new File(libDir, new StringBuilder("hadoop-core-").append(hadoopVersion).append(".jar").toString()).delete();
        new File(libDir, new StringBuilder("jersey-core-").append(jerseyVersion).append(".jar").toString()).delete();
        new File(libDir, new StringBuilder("jersey-json-").append(jerseyVersion).append(".jar").toString()).delete();
        new File(libDir, new StringBuilder("jersey-server-").append(jerseyVersion).append(".jar").toString()).delete();
      }
    }
    Thread thread = new Thread(new ToolsSuite());
    thread.start();
    if (!daemonMode) {
      try {
        thread.join();
      }
      catch (Exception ex) {
        getLog().warn(ex.getMessage(), ex);
      }
    }
  }

  protected Artifact getArtifact(ArtifactItem item) {
    VersionRange vr;
    try {
      vr = VersionRange.createFromVersionSpec(item.getVersion());
    }
    catch (InvalidVersionSpecificationException e1) {
      vr = VersionRange.createFromVersion(item.getVersion());
    }
    final Artifact artifact;
    if (StringUtils.isBlank(item.getClassifier())) {
      artifact = factory.createDependencyArtifact(item.getGroupId(), item.getArtifactId(), vr, item.getType(), item.
          getClassifier(), Artifact.SCOPE_COMPILE);
    }
    else {
      artifact = factory.createDependencyArtifact(item.getGroupId(), item.getArtifactId(), vr, item.getType(), null,
                                                  Artifact.SCOPE_COMPILE);
    }
    try {
      resolver.resolve(artifact, remoteRepos, local);
    }
    catch (ArtifactResolutionException ex) {
      throw new IllegalArgumentException(ex);
    }
    catch (ArtifactNotFoundException ex) {
      throw new IllegalArgumentException(ex);
    }
    return artifact;
  }

  protected void extract(File jarFile, File outDir) {
    try {
      getLog().info(new StringBuilder("Extracting ").append(jarFile.getAbsolutePath()).append(" to ").append(outDir.
          getAbsolutePath()).toString());
      java.util.jar.JarFile jar = new java.util.jar.JarFile(jarFile);
      java.util.Enumeration enumeration = jar.entries();
      while (enumeration.hasMoreElements()) {
        java.util.jar.JarEntry file = (java.util.jar.JarEntry) enumeration.nextElement();
        final String str = new StringBuilder(outDir.getAbsolutePath()).append(File.separator).append(file.getName()).
            toString();
        File f = new File(str);
        if (file.isDirectory()) {
          f.mkdir();
          continue;
        }
        getLog().debug(new StringBuilder("Extracting ").append(file.getName()).append(" to ").append(str).toString());
        java.io.InputStream is = jar.getInputStream(file); // get the input stream
        java.io.FileOutputStream fos = new java.io.FileOutputStream(f);
        IOUtils.copy(is, fos);
        fos.close();
        is.close();
      }
    }
    catch (Exception ex) {
      throw new IllegalStateException(ex);
    }
  }

  class ToolsSuite implements Runnable {

    private MiniZooKeeperCluster zooKeeperCluster = new MiniZooKeeperCluster();
    private HBaseTestingUtility hBaseTestingUtility;
    private Server jettyServer;

    public ToolsSuite() {
      System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
                         "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
      try {
        zooKeeperCluster.setClientPort(zooKeeperPort);
        File file = new File(outputDirectory, "zk-server/");
        file.mkdirs();
        zooKeeperCluster.startup(file);
      }
      catch (Exception ex) {
        throw new IllegalStateException(ex);
      }
      try {
        hBaseTestingUtility = new HBaseTestingUtility();
        hBaseTestingUtility.setZkCluster(zooKeeperCluster);
        hBaseTestingUtility.startMiniCluster();
      }
      catch (Exception ex) {
        getLog().warn(ex.getMessage(), ex);
        try {
          zooKeeperCluster.shutdown();
        }
        catch (Exception ex1) {
          getLog().warn(ex1.getMessage(), ex1);
        }
        throw new IllegalStateException(ex);
      }
      try {
        jettyServer = new Server(jettyPort);
        HandlerList handlerList = new HandlerList();
        /*
         * The following is for solr for later, when this is to be used it
         */
        File solrOutDir = new File(outputDirectory, "solr");
        File hubOutDir = new File(outputDirectory, "hub");
        File cmsOutDir = new File(outputDirectory, "cms");
        System.setProperty("solr.solr.home", solrHomeDirectory.getAbsolutePath());
        Handler solr = new WebAppContext(solrOutDir.getAbsolutePath(), "/solr");
        handlerList.addHandler(solr);
        WebAppContext hub = new WebAppContext(hubOutDir.getAbsolutePath(), "/hub");
        final WebAppClassLoader webAppClassLoader = new WebAppClassLoader(hub);
        hub.setClassLoader(webAppClassLoader);
        handlerList.addHandler(hub);
        WebAppContext cms = new WebAppContext(cmsOutDir.getAbsolutePath(), "/cms");
        final WebAppClassLoader cmsWebAppClassLoader = new WebAppClassLoader(cms);
        cms.setClassLoader(cmsWebAppClassLoader);
        handlerList.addHandler(cms);
        jettyServer.setHandler(handlerList);
        jettyServer.setSendDateHeader(true);
        jettyServer.start();
      }
      catch (Exception ex) {
        getLog().warn(ex.getMessage(), ex);
        try {
          hBaseTestingUtility.shutdownMiniCluster();
          zooKeeperCluster.shutdown();
        }
        catch (Exception ex1) {
          getLog().warn(ex1.getMessage(), ex1);
        }
        throw new IllegalStateException(ex);
      }
      Client client = CacheableClient.create();
      client.resource("http://localhost:10080/hub/api/channels/test").header(HttpHeaders.CONTENT_TYPE,
                                                                             MediaType.APPLICATION_JSON).put(
          "{\"name\":\"test\"}");
      getLog().debug("Created test channel!");
    }

    public void run() {
      try {
        ServerSocket serverSocket = new ServerSocket(embeddedPort);
        while (true) {
          Socket socket = serverSocket.accept();
          InputStream inputStream = socket.getInputStream();
          Writer writer = new OutputStreamWriter(socket.getOutputStream());
          BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
          String firstLine = reader.readLine();
          if (StringUtils.isNotBlank(firstLine)) {
            if ("stop".equalsIgnoreCase(firstLine.split(" ")[0])) {
              getLog().info("Received STOP signal!");
              stopAll();
              writer.write("done\n");
              writer.flush();
              Thread.sleep(1000);
              reader.close();
              writer.close();
              socket.close();
              serverSocket.close();
              break;
            }
          }
        }
      }
      catch (Exception ex) {
        getLog().warn(ex.getMessage(), ex);
        throw new IllegalStateException(ex);
      }
    }

    protected void stopAll() {
      try {
        jettyServer.stop();
      }
      catch (Exception ex) {
        getLog().warn(ex.getMessage(), ex);
      }
      try {
        hBaseTestingUtility.shutdownMiniCluster();
      }
      catch (Exception ex) {
        getLog().warn(ex.getMessage(), ex);
      }
      try {
        zooKeeperCluster.shutdown();
      }
      catch (Exception ex) {
        getLog().warn(ex.getMessage(), ex);
      }
    }
  }
}
