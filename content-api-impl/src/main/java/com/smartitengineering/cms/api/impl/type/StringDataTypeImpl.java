/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smartitengineering.cms.api.impl.type;

import com.smartitengineering.cms.api.type.FieldValueType;
import com.smartitengineering.cms.api.type.MutableStringDataType;

/**
 *
 * @author kaisar
 */
public class StringDataTypeImpl implements MutableStringDataType {

  private String encoding;
  private String mimeType;

  @Override
  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  @Override
  public String getEncoding() {
    return this.encoding;
  }

  @Override
  public void setMIMEType(String mimeType) {
    this.mimeType = mimeType;
  }

  @Override
  public String getMIMEType() {
    return this.mimeType;
  }

  @Override
  public FieldValueType getType() {
    return FieldValueType.STRING;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final StringDataTypeImpl other = (StringDataTypeImpl) obj;
    if ((this.encoding == null) ? (other.encoding != null) : !this.encoding.equals(other.encoding)) {
      return false;
    }
    if ((this.mimeType == null) ? (other.mimeType != null) : !this.mimeType.equals(other.mimeType)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 23 * hash + (this.encoding != null ? this.encoding.hashCode() : 0);
    hash = 23 * hash + (this.mimeType != null ? this.mimeType.hashCode() : 0);
    return hash;
  }
}
