/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.smartitengineering.cms.api.impl.type;

import com.smartitengineering.cms.api.type.MutableStringDataType;

/**
 *
 * @author kaisar
 */
public class StringDataTypeImpl implements MutableStringDataType{

  private String encoding;
  private String mimeType;

  @Override
  public void setEncoding(String encoding) {
    this.encoding=encoding;
  }

  @Override
  public String getEncoding() {
    return this.encoding;
  }

  @Override
  public void setMIMEType(String mimeType) {
    this.mimeType=mimeType;
  }

  @Override
  public String getMIMEType() {
    return this.mimeType;
  }

}
