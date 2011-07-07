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
package com.smartitengineering.cms.api.impl.content;

import com.smartitengineering.cms.api.common.MediaType;
import com.smartitengineering.cms.api.content.ContentId;
import com.smartitengineering.cms.api.content.MutableVariation;
import com.smartitengineering.cms.api.type.FieldDef;
import com.smartitengineering.cms.spi.SmartContentSPI;
import java.net.URI;
import java.util.Date;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author kaisar
 */
public class VariationImpl implements MutableVariation {

  private String name;
  private byte[] variation;
  private String mimeType;
  private Date lastModifiedDate;
  private ContentId contentId;
  private FieldDef fieldDef;

  public VariationImpl(ContentId contentId, FieldDef fieldDef) {
    this.contentId = contentId;
    this.fieldDef = fieldDef;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public void setVariation(byte[] variation) {
    this.variation = variation;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public byte[] getVariation() {
    return this.variation;
  }

  @Override
  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  @Override
  public String getMimeType() {
    return StringUtils.isNotBlank(mimeType) ? mimeType : MediaType.APPLICATION_OCTET_STREAM.toString();
  }

  @Override
  public void setLastModifiedDate(Date lastModifiedDate) {
    this.lastModifiedDate = lastModifiedDate;
  }

  @Override
  public Date getLastModifiedDate() {
    return lastModifiedDate;
  }

  @Override
  public ContentId getContentId() {
    return contentId;
  }

  @Override
  public FieldDef getFieldDef() {
    return fieldDef;
  }

  @Override
  public URI getUri() {
    return SmartContentSPI.getInstance().getUriProvider().getVariationUri(getContentId(), fieldDef, name);
  }

  @Override
  public String getEncodedUriString() {
    URI uri = getUri();
    return uri == null ? null : uri.toASCIIString();
  }
}
