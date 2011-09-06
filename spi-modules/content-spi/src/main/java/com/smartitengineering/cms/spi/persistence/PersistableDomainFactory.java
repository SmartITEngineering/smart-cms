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
package com.smartitengineering.cms.spi.persistence;

import com.smartitengineering.cms.spi.content.PersistableContent;
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
public interface PersistableDomainFactory {

  PersistableContentType createPersistableContentType();

  PersistableContent createPersistableContent(boolean supressChecking);

  PersistableWorkspace createPersistentWorkspace();

  PersistableRepresentationTemplate createPersistableRepresentationTemplate();
  
  PersistableContentCoProcessorTemplate createPersistableContentCoProcessorTemplate();

  PersistableVariationTemplate createPersistableVariationTemplate();

  PersistableValidatorTemplate createPersistableValidatorTemplate();
}
