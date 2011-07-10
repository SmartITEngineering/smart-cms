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
package com.smartitengineering.cms.spi.impl.content.template.persistent;

import com.smartitengineering.cms.spi.impl.hbase.Utils;
import com.smartitengineering.dao.impl.hbase.spi.Externalizable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author imyousuf
 */
public class TemplateId implements Externalizable, Comparable<TemplateId> {

  private String id;

  @Override
  public void writeExternal(DataOutput output) throws IOException {
    output.write(org.apache.commons.codec.binary.StringUtils.getBytesUtf8(toString()));
  }

  @Override
  public void readExternal(DataInput input) throws IOException, ClassNotFoundException {
    String idString = Utils.readStringInUTF8(input);
    if (StringUtils.isBlank(idString)) {
      throw new IOException("No content!");
    }
    setId(idString);
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public String toString() {
    return getId();
  }

  @Override
  public int compareTo(TemplateId o) {
    if (o == null) {
      return 1;
    }
    if (StringUtils.equals(id, o.id)) {
      return 0;
    }
    if (id != null) {
      return id.compareTo(o.id);
    }
    return -1;
  }
}
