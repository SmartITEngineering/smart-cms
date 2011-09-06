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
package com.smartitengineering.cms.api.content.template;

import com.smartitengineering.cms.api.content.Field;
import java.util.Map;

/**
 *
 * @author imyousuf
 */
public interface VariationGenerator {

  /**
   * Generate the output of a variation template for a field
   * @param field Field for which the variation is requested
   * @param params Parameters from the content type definition
   * @return May return any of String, InputStream, byte[]. Any other type of object would result invocation of
   *          {@link Object#toString()}
   */
  Object getVariationForField(Field field, Map<String, String> params);
}
