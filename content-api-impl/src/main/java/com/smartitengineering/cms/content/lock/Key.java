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
package com.smartitengineering.cms.content.lock;

/**
 * A key for the object lock map. Please refer to {@link LockManager} for more
 * details.
 * @author imyousuf
 * @since 0.1
 */
public interface Key {

		/**
		 * Return a string representation of the key, its not similar to {@link Object#toString()}
		 * as it has bindings with the {@link Key#equals(java.lang.Object)} method.
		 * If 2 object are equal then their keyStringRep should also be equal and if
		 * the two object are not equal then their key string should also not be
		 * equal and this string is case sensitive.
		 * @return String representation of the key.
		 */
		public String getKeyStringRep();

		/**
		 * This object will be the key to the concrete lock for this object. So the
		 * equals method is most important for it. Two instances representing the
		 * same key should definitely return true and that is be equal.
		 * @param object To check the equality against
		 * @return True if equal or else false
		 * @see Object#equals(java.lang.Object) 
		 */
		@Override
		public boolean equals(Object object);

		@Override
		public int hashCode();
}
