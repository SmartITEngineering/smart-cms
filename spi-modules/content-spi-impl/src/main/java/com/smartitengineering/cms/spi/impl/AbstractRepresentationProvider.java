/*
 *
 * This is a simple Content Management System (CMS)
 * Copyright (C) 2009  Imran M Yousuf (imyousuf@smartitengineering.com)
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

import com.smartitengineering.cms.api.content.Content;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.type.RepresentationDef;
import com.smartitengineering.cms.api.type.ResourceUri;
import com.smartitengineering.cms.api.workspace.RepresentationTemplate;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import java.util.Collection;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class AbstractRepresentationProvider {

  protected final transient Logger logger = LoggerFactory.getLogger(getClass());

  protected RepresentationTemplate getTemplate(String repName, ContentType contentType, Content content) {
    RepresentationDef representationDef = contentType.getRepresentationDefs().get(repName);
    if (representationDef == null) {
      logger.info("Representation def is null!");
      return null;
    }
    if (representationDef.getResourceUri().getType().equals(ResourceUri.Type.EXTERNAL)) {
      logger.warn("External resource URI is not yet handled!");
      return null;
    }
    final WorkspaceId workspaceId = content.getContentId().getWorkspaceId();
    RepresentationTemplate representationTemplate =
                           SmartContentAPI.getInstance().getWorkspaceApi().getRepresentationTemplate(workspaceId, representationDef.
        getResourceUri().getValue());
    if (representationTemplate == null) {
      //Lookup friendlies
      Collection<WorkspaceId> friends = SmartContentAPI.getInstance().getWorkspaceApi().getFriendlies(workspaceId);
      if (friends != null && !friends.isEmpty()) {
        Iterator<WorkspaceId> friendsIterator = friends.iterator();
        while (representationTemplate == null && friendsIterator.hasNext()) {
          representationTemplate = SmartContentAPI.getInstance().getWorkspaceApi().getRepresentationTemplate(friendsIterator.
              next(), representationDef.getResourceUri().getValue());
        }
      }
    }
    return representationTemplate;
  }
}
