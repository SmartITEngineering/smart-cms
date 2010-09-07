/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smartitengineering.cms.api.content;

import com.smartitengineering.cms.api.WorkspaceId;

/**
 *
 * @author kaisar
 */
public interface MutableContentId extends ContentId {

  public void setWorkspaceId(WorkspaceId workspaceId);

  public void setId(byte[] id);
}
