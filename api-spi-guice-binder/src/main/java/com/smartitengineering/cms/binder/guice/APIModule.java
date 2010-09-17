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
package com.smartitengineering.cms.binder.guice;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.smartitengineering.cms.api.WorkspaceAPI;
import com.smartitengineering.cms.api.content.ContentLoader;
import com.smartitengineering.cms.api.impl.WorkspaceAPIImpl;
import com.smartitengineering.cms.api.impl.content.ContentLoaderImpl;
import com.smartitengineering.cms.api.impl.type.ContentTypeLoaderImpl;
import com.smartitengineering.cms.api.type.ContentTypeLoader;

public class APIModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(ContentTypeLoader.class).annotatedWith(Names.named("apiContentTypeLoader")).to(ContentTypeLoaderImpl.class);
    bind(ContentLoader.class).annotatedWith(Names.named("apiContentLoader")).to(ContentLoaderImpl.class);
    bind(WorkspaceAPI.class).annotatedWith(Names.named("apiWorkspaceApi")).to(WorkspaceAPIImpl.class);
  }
}
