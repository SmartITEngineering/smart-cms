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
package com.smartitengineering.cms.api.impl;

import com.smartitengineering.cms.api.impl.content.ContentImpl;
import com.smartitengineering.cms.api.impl.type.ContentTypeImpl;
import com.smartitengineering.cms.api.impl.workspace.ContentCoProcessorTemplateImpl;
import com.smartitengineering.cms.api.impl.workspace.RepresentationTemplateImpl;
import com.smartitengineering.cms.api.impl.workspace.ValidatorTemplateImpl;
import com.smartitengineering.cms.api.impl.workspace.VariationTemplateImpl;
import com.smartitengineering.cms.api.impl.workspace.WorkspaceImpl;
import com.smartitengineering.cms.spi.content.PersistableContent;
import com.smartitengineering.cms.spi.persistence.PersistableDomainFactory;
import com.smartitengineering.cms.spi.type.PersistableContentType;
import com.smartitengineering.cms.spi.workspace.PersistableContentCoProcessorTemplate;
import com.smartitengineering.cms.spi.workspace.PersistableRepresentationTemplate;
import com.smartitengineering.cms.spi.workspace.PersistableValidatorTemplate;
import com.smartitengineering.cms.spi.workspace.PersistableVariationTemplate;
import com.smartitengineering.cms.spi.workspace.PersistableWorkspace;

/**
 *
 * @author imyousuf
 */
public class PersistableDomainFactoryImpl implements PersistableDomainFactory {

  @Override
  public PersistableContentType createPersistableContentType() {
    return new ContentTypeImpl();
  }

  @Override
  public PersistableWorkspace createPersistentWorkspace() {
    return new WorkspaceImpl();
  }

  @Override
  public PersistableRepresentationTemplate createPersistableRepresentationTemplate() {
    return new RepresentationTemplateImpl();
  }

  @Override
  public PersistableVariationTemplate createPersistableVariationTemplate() {
    return new VariationTemplateImpl();
  }

  @Override
  public PersistableContent createPersistableContent(boolean supressChecking) {
    final ContentImpl contentImpl = new ContentImpl();
    contentImpl.setSupressChecking(supressChecking);
    return contentImpl;
  }

  @Override
  public PersistableValidatorTemplate createPersistableValidatorTemplate() {
    return new ValidatorTemplateImpl();
  }

  public PersistableContentCoProcessorTemplate createPersistableContentCoProcessorTemplate() {
    return new ContentCoProcessorTemplateImpl();
  }
}
