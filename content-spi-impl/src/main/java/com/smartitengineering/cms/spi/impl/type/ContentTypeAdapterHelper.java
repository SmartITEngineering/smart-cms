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
package com.smartitengineering.cms.spi.impl.type;

import com.smartitengineering.cms.api.factory.type.WritableContentType;
import com.smartitengineering.util.bean.adapter.AbstractAdapterHelper;

/**
 *
 * @author imyousuf
 */
public class ContentTypeAdapterHelper extends AbstractAdapterHelper<WritableContentType, PersistentContentType> {

  @Override
  protected PersistentContentType newTInstance() {
    return new PersistentContentType();
  }

  @Override
  protected void mergeFromF2T(WritableContentType fromBean, PersistentContentType toBean) {
    toBean.setMutableContentType(fromBean);
  }

  @Override
  protected WritableContentType convertFromT2F(PersistentContentType toBean) {
    return toBean.getMutableContentType();
  }
}
