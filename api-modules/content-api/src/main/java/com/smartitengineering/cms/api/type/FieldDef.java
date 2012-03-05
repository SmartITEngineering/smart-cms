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

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

/**
 * Represents the definition of the fields of the {@link ContentType}
 * @author imyousuf
 * @since 0.1
 */
public interface FieldDef extends Serializable {

  /**
   * Retrieves the name of this field
   * @return the name
   */
  public String getName();

  /**
   * Return the display label for this field. If it is not set then it would be mean identical to invoking 
   * {@link FieldDef#getName()}
   * @return The display label if set, else the field's name
   */
  public String getDisplayName();

  /**
   * Retrieve the definition of the data type of this field
   * @return definition of the data type
   */
  public DataType getValueDef();

  /**
   * Returns whether the field is required or not
   * @return whether the field is required or not
   */
  public boolean isRequired();

  /**
   * Get the field def that contains this def. This is primarily the case when current field is a field of a composite
   * field.
   * @return The immediate field def that contains this definition, null if not such container
   */
  public FieldDef getParentContainer();

  public Map<String, String> getParameters();

  public Map<String, String> getParameterizedDisplayNames();

  public Map<String, VariationDef> getVariations();

  public VariationDef getVariationDefForMimeType(String mimeType);

  public Collection<ValidatorDef> getCustomValidators();

  public SearchDef getSearchDefinition();

  public boolean isFieldStandaloneUpdateAble();
}
