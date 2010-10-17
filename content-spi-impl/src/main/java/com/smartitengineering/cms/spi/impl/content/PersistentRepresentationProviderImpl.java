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
package com.smartitengineering.cms.spi.impl.content;

import com.smartitengineering.cms.spi.impl.content.template.persistent.PersistentRepresentation;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.smartitengineering.cms.api.content.Content;
import com.smartitengineering.cms.api.content.ContentId;
import com.smartitengineering.cms.api.content.Representation;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.api.workspace.RepresentationTemplate;
import com.smartitengineering.cms.spi.content.RepresentationProvider;
import com.smartitengineering.cms.spi.impl.content.template.persistent.TemplateId;
import com.smartitengineering.dao.common.CommonReadDao;
import com.smartitengineering.dao.common.CommonWriteDao;
import java.util.Date;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author imyousuf
 */
public class PersistentRepresentationProviderImpl extends AbstractRepresentationProvider implements
    RepresentationProvider {

  @Inject
  @Named("mainProvider")
  private RepresentationProvider mainProvider;
  @Inject
  private CommonReadDao<PersistentRepresentation, TemplateId> readDao;
  @Inject
  private CommonWriteDao<PersistentRepresentation> writeDao;

  @Override
  public Representation getRepresentation(String repName, ContentTypeId contentTypeId, ContentId contentId) {
    return getRepresentation(repName, contentTypeId, SmartContentAPI.getInstance().getContentLoader().loadContent(
        contentId));
  }

  @Override
  public Representation getRepresentation(String repName, ContentTypeId contentTypeId, Content content) {
    return getRepresentation(repName,
                             SmartContentAPI.getInstance().getContentTypeLoader().loadContentType(contentTypeId),
                             content);
  }

  @Override
  public Representation getRepresentation(String repName, ContentType contentType, Content content) {
    if (StringUtils.isBlank(repName) || contentType == null || content == null) {
      logger.info("Representation name or content type or content is null or blank!");
      return null;
    }
    final TemplateId id = new TemplateId();
    id.setId(new StringBuilder(content.getContentId().toString()).append(':').append(repName).toString());
    PersistentRepresentation cachedRep = readDao.getById(id);
    boolean update = false;
    if (cachedRep != null) {
      update = true;
      Date cachedDate = cachedRep.getRepresentation().getLastModifiedDate();
      RepresentationTemplate template = getTemplate(repName, contentType, content);
      Date lastModified = content.getLastModifiedDate();
      if (template != null) {
        final Date lastModifiedDate = template.getLastModifiedDate();
        if (lastModified.before(lastModifiedDate)) {
          lastModified = lastModifiedDate;
        }
      }
      if (cachedDate.before(lastModified)) {
        cachedRep = null;
      }
    }
    if (cachedRep == null) {
      Representation rep = mainProvider.getRepresentation(repName, contentType, content);
      cachedRep = new PersistentRepresentation();
      cachedRep.setId(id);
      cachedRep.setRepresentation(rep);
      if (update) {
        writeDao.update(cachedRep);
      }
      else {
        writeDao.save(cachedRep);
      }
    }
    return cachedRep.getRepresentation();
  }

  @Override
  public boolean isValidTemplate(RepresentationTemplate template) {
    return mainProvider.isValidTemplate(template);
  }
}
