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
package com.smartitengineering.cms.binder.guice;

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import com.smartitengineering.dao.solr.ServerConfiguration;
import com.smartitengineering.dao.solr.ServerFactory;
import com.smartitengineering.dao.solr.SolrQueryDao;
import com.smartitengineering.dao.solr.impl.ServerConfigurationImpl;
import com.smartitengineering.dao.solr.impl.SingletonRemoteServerFactory;
import com.smartitengineering.dao.solr.impl.SolrDao;
import java.util.Properties;

/**
 *
 * @author imyousuf
 */
public class SolrQueryDaoModule extends PrivateModule {

  private final String solrUri;

  public SolrQueryDaoModule(Properties properties) {
    solrUri = properties.getProperty("com.smartitengineering.cms.solrReadUri", properties.getProperty(
        "com.smartitengineering.cms.solrUri", SPIModule.DEFAULT_SOLR_URI));
  }

  @Override
  protected void configure() {
    bind(ServerFactory.class).to(SingletonRemoteServerFactory.class).in(Scopes.SINGLETON);
    bind(ServerConfiguration.class).to(ServerConfigurationImpl.class).in(Scopes.SINGLETON);
    bind(String.class).annotatedWith(Names.named("uri")).toInstance(solrUri);
    bind(SolrQueryDao.class).to(SolrDao.class).in(Scopes.SINGLETON);
    binder().expose(SolrQueryDao.class);
  }
}
