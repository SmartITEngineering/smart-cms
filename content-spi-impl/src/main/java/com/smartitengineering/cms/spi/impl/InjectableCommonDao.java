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
package com.smartitengineering.cms.spi.impl;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.smartitengineering.dao.impl.hbase.CommonDao;
import com.smartitengineering.dao.impl.hbase.spi.AsyncExecutorService;
import com.smartitengineering.dao.impl.hbase.spi.ObjectRowConverter;
import com.smartitengineering.dao.impl.hbase.spi.SchemaInfoProvider;
import com.smartitengineering.domain.PersistentDTO;
import java.io.Serializable;

/**
 *
 * @author imyousuf
 */
public class InjectableCommonDao<Template extends PersistentDTO, IdType extends Serializable> extends CommonDao<Template, IdType> {

  @Inject
  @Override
  public void setConverter(ObjectRowConverter<Template> converter) {
    super.setConverter(converter);
  }

  @Inject
  @Override
  public void setExecutorService(AsyncExecutorService executorService) {
    super.setExecutorService(executorService);
  }

  @Inject
  @Override
  public void setInfoProvider(SchemaInfoProvider<Template> infoProvider) {
    super.setInfoProvider(infoProvider);
  }

  @Inject
  public void setMaxRows(@Named("maxRows") Integer maxRows) {
    super.setMaxRows(maxRows);
  }
}
