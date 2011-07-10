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

import com.smartitengineering.cms.spi.impl.AbstractVariationProvider;
import com.smartitengineering.cms.api.content.Field;
import com.smartitengineering.cms.api.content.Variation;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.smartitengineering.cms.api.content.Content;
import com.smartitengineering.cms.api.event.Event;
import com.smartitengineering.cms.api.event.Event.EventType;
import com.smartitengineering.cms.api.event.Event.Type;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.workspace.VariationTemplate;
import com.smartitengineering.cms.spi.content.VariationProvider;
import com.smartitengineering.cms.spi.impl.content.template.persistent.PersistentVariation;
import com.smartitengineering.cms.spi.impl.content.template.persistent.TemplateId;
import com.smartitengineering.dao.common.CommonReadDao;
import com.smartitengineering.dao.common.CommonWriteDao;
import java.util.Date;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author imyousuf
 */
public class PersistentVariationProviderImpl extends AbstractVariationProvider implements VariationProvider {

  @Inject
  @Named("mainProvider")
  private VariationProvider mainProvider;
  @Inject
  private CommonReadDao<PersistentVariation, TemplateId> readDao;
  @Inject
  private CommonWriteDao<PersistentVariation> writeDao;

  @Override
  public boolean isValidTemplate(VariationTemplate template) {
    return mainProvider.isValidTemplate(template);
  }

  @Override
  public Variation getVariation(String varName, Content content, Field field) {
    if (StringUtils.isBlank(varName) || field == null || content == null) {
      logger.info("Variation name or field or content is null or blank!");
      return null;
    }
    final TemplateId id = new TemplateId();
    id.setId(new StringBuilder(content.getContentId().toString()).append(':').append(field.getName()).append(':').append(
        varName).toString());
    PersistentVariation cachedVar = readDao.getById(id);
    boolean update = false;
    if (cachedVar != null) {
      update = true;
      Date cachedDate = cachedVar.getVariation().getLastModifiedDate();
      VariationTemplate template = getTemplate(varName, content, field);
      Date lastModified = content.getLastModifiedDate();
      if (template != null) {
        final Date lastModifiedDate = template.getLastModifiedDate();
        if (lastModified.before(lastModifiedDate)) {
          lastModified = lastModifiedDate;
        }
      }
      if (cachedDate.before(lastModified)) {
        cachedVar = null;
      }
    }
    if (cachedVar == null) {
      Variation var = mainProvider.getVariation(varName, content, field);
      cachedVar = new PersistentVariation();
      cachedVar.setId(id);
      cachedVar.setVariation(var);
      if (update) {
        writeDao.update(cachedVar);
        Event<Variation> event = SmartContentAPI.getInstance().getEventRegistrar().<Variation>createEvent(
            EventType.UPDATE, Type.VARIATION, var);
        SmartContentAPI.getInstance().getEventRegistrar().notifyEventAsynchronously(event);
      }
      else {
        writeDao.save(cachedVar);
        Event<Variation> event = SmartContentAPI.getInstance().getEventRegistrar().<Variation>createEvent(
            EventType.CREATE, Type.VARIATION, var);
        SmartContentAPI.getInstance().getEventRegistrar().notifyEventAsynchronously(event);
      }
    }
    return cachedVar.getVariation();
  }
}
