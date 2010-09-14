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
package com.smartitengineering.cms.api.type;

import java.util.Collection;

/**
 * Editable version of {@link FieldDef}
 * @author imyousuf
 * @since 0.1
 */
public interface MutableFieldDef
    extends FieldDef {

  /**
   * Sets the new name of the field.
   * @param newFieldName New name - a non-blank string
   * @throws IllegalArgumentException If field name is blank
   */
  public void setName(String newFieldName)
      throws IllegalArgumentException;

  /**
   * Sets whether the field is required or not
   * @param required True if required else false
   */
  public void setRequired(boolean required);

  public void setValueDef(DataType dataType);

  public void setVariations(Collection<VariationDef> variationDefs);

  public void setCustomValidator(ValidatorDef validatorDef);

  public void setSearchDefinition(SearchDef searchDef);

  public void setFieldStandaloneUpdateAble(boolean standaloneUpdateAble);
}
