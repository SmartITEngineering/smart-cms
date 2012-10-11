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
package com.smartitengineering.cms.spi.type;

import com.smartitengineering.cms.api.common.MediaType;
import com.smartitengineering.cms.api.factory.type.WritableContentType;
import com.smartitengineering.cms.api.factory.write.Lock;
import java.util.Date;
import java.util.Map;

/**
 *
 * @author imyousuf
 */
public interface PersistableContentType extends WritableContentType {

  public static final String LOCK_KEY_PREFIX = "content-type:";

  public boolean isFromPersistentStorage();

  public void setFromPersistentStorage(boolean fromPersistentStorage);

  public void setCreationDate(Date creationDate);

  public void setLastModifiedDate(Date lastModifiedDate);

  public void setRepresentations(Map<MediaType, String> reps);

  public void setEntityTagValue(String entityTagValue);

  public void setLock(Lock lock);
}
