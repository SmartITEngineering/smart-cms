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
import com.smartitengineering.cms.api.content.Field;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.type.ResourceUri;
import com.smartitengineering.cms.api.type.VariationDef;
import com.smartitengineering.cms.api.workspace.VariationTemplate;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import java.util.Collection;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class AbstractVariationProvider {

  protected final transient Logger logger = LoggerFactory.getLogger(getClass());

  protected VariationTemplate getTemplate(String varName, Content content, Field field) {
    VariationDef variationDef = field.getFieldDef().getVariations().get(varName);
    if (variationDef == null) {
      logger.info("Representation def is null!");
      return null;
    }
    if (variationDef.getResourceUri().getType().equals(ResourceUri.Type.EXTERNAL)) {
      logger.warn("External resource URI is not yet handled!");
      return null;
    }
    final WorkspaceId workspaceId = content.getContentId().getWorkspaceId();
    VariationTemplate variationTemplate =
                      SmartContentAPI.getInstance().getWorkspaceApi().getVariationTemplate(workspaceId, variationDef.
        getResourceUri().getValue());
    if (variationTemplate == null) {
      //Lookup friendlies
      Collection<WorkspaceId> friends = SmartContentAPI.getInstance().getWorkspaceApi().getFriendlies(workspaceId);
      if (friends != null && !friends.isEmpty()) {
        Iterator<WorkspaceId> friendsIterator = friends.iterator();
        while (variationTemplate == null && friendsIterator.hasNext()) {
          variationTemplate = SmartContentAPI.getInstance().getWorkspaceApi().getVariationTemplate(
              friendsIterator.next(), variationDef.getResourceUri().getValue());
        }
      }
    }
    return variationTemplate;
  }
}
