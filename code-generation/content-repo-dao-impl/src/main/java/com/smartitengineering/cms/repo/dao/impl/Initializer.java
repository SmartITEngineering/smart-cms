package com.smartitengineering.cms.repo.dao.impl;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.smartitengineering.cms.api.common.MediaType;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.factory.type.WritableContentType;
import com.smartitengineering.cms.api.factory.workspace.WorkspaceAPI;
import com.smartitengineering.cms.api.workspace.Workspace;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import com.smartitengineering.dao.hbase.ddl.HBaseTableGenerator;
import com.smartitengineering.dao.hbase.ddl.config.json.ConfigurationJsonParser;
import com.smartitengineering.dao.impl.hbase.HBaseConfigurationFactory;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Semaphore;
import org.apache.commons.lang.mutable.MutableBoolean;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class Initializer {

  private final String workspaceNamespace;
  private final String workspaceName;
  private final List<String> contentTypeClasspathResourcePaths;
  private final static Logger LOGGER = LoggerFactory.getLogger(Initializer.class);
  private final static Semaphore SEMAPHORE = new Semaphore(1);
  private final static MutableBoolean INITIALIZED = new MutableBoolean(false);

  @Inject
  public Initializer(@Named("cmsWorkspaceNamespace") String workspaceNamespace,
                     @Named("cmsWorkspaceName") String workspaceName,
                     @Named("contentTypePath") List<String> contentTypeClasspathResourcePath) {
    this.workspaceNamespace = workspaceNamespace;
    this.workspaceName = workspaceName;
    this.contentTypeClasspathResourcePaths = contentTypeClasspathResourcePath;
  }

  @Inject
  public void init() {
    final ClassLoader classLoader = Initializer.class.getClassLoader();
    try {
      SEMAPHORE.acquire();
    }
    catch (Exception ex) {
      throw new IllegalStateException(ex);
    }
    try {
      if (!INITIALIZED.booleanValue()) {
        Configuration config = HBaseConfigurationFactory.getConfigurationInstance();        
        try {
          new HBaseTableGenerator(ConfigurationJsonParser.getConfigurations(classLoader.getResourceAsStream(
              "com/smartitengineering/cms/spi/impl/schema.json")), config, false).generateTables();
        }
        catch (MasterNotRunningException ex) {
          LOGGER.error("Master could not be found!", ex);
        }
        catch (Exception ex) {
          LOGGER.error("Could not create table!", ex);
        }

        // CMS API Initializer
        com.smartitengineering.cms.binder.guice.Initializer.init();
        INITIALIZED.setValue(true);
      }
    }
    finally {
      SEMAPHORE.release();
    }

    // creating workspace and namespace
    final WorkspaceAPI workspaceApi = SmartContentAPI.getInstance().getWorkspaceApi();
    WorkspaceId workspaceId = workspaceApi.createWorkspaceId(workspaceNamespace, workspaceName);
    Workspace workspace = workspaceId.getWorkspace();
    if (workspace == null) {
      if (LOGGER.isInfoEnabled()) {
        LOGGER.info("Workspace does not exist so attempting to create " + workspaceId.toString());
      }
      workspaceApi.createWorkspace(workspaceId);
      if (LOGGER.isInfoEnabled()) {
        LOGGER.info("Workspace created " + workspaceId.toString());
      }
    }
    else if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Workspace exists: " + workspaceId.toString());
    }
    for (String contentTypeClasspathResourcePath : contentTypeClasspathResourcePaths) {
      try {
        InputStream stream = classLoader.getResourceAsStream(
            contentTypeClasspathResourcePath);
        if (stream == null) {
          throw new IllegalArgumentException("Content Type XML Does not exist");
        }
        Collection<WritableContentType> types = SmartContentAPI.getInstance().getContentTypeLoader().
            parseContentTypes(workspaceId, stream, MediaType.APPLICATION_XML);
        for (WritableContentType type : types) {
          type.put();
        }
      }
      catch (Exception ex) {
        throw new IllegalStateException(ex);
      }
    }
  }
}
