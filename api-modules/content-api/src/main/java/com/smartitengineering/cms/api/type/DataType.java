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

/**
 * Represents the data type for field of the {@link FieldDef}
 * @author imyousuf
 * @since 0.1
 */
public interface DataType extends Serializable {

  /**
   * Represents the integer data type
   */
  public static final DataType INTEGER = new DataType() {

    @Override
    public FieldValueType getType() {
      return FieldValueType.INTEGER;
    }

    @Override
    public String toString() {
      return "Integer{type=" + getType() + '}';
    }
  };
  /**
   * Represents the boolean data type
   */
  public static final DataType BOOLEAN = new DataType() {

    @Override
    public FieldValueType getType() {
      return FieldValueType.BOOLEAN;
    }

    @Override
    public String toString() {
      return "Boolean{type=" + getType() + '}';
    }
  };
  /**
   * Represents the double data type
   */
  public static final DataType DOUBLE = new DataType() {

    @Override
    public FieldValueType getType() {
      return FieldValueType.DOUBLE;
    }

    @Override
    public String toString() {
      return "Double{type=" + getType() + '}';
    }
  };
  /**
   * Represents the datetime data type
   */
  public static final DataType DATE_TIME = new DataType() {

    @Override
    public FieldValueType getType() {
      return FieldValueType.DATE_TIME;
    }

    @Override
    public String toString() {
      return "DateTime{type=" + getType() + '}';
    }
  };
  /**
   * Represents the long data type
   */
  public static final DataType LONG = new DataType() {

    @Override
    public FieldValueType getType() {
      return FieldValueType.LONG;
    }

    @Override
    public String toString() {
      return "Long{type=" + getType() + '}';
    }
  };

  /**
   * Retrieves the type of the data type
   * @return the type of the data type
   */
  public FieldValueType getType();
}
