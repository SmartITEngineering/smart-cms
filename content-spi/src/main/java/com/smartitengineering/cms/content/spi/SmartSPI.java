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
package com.smartitengineering.cms.content.spi;

import com.smartitengineering.cms.content.api.SmartContentAPI;
import com.smartitengineering.cms.content.spi.lock.LockHandler;
import com.smartitengineering.cms.content.spi.type.TypeValidator;
import com.smartitengineering.util.bean.BeanFactoryRegistrar;
import com.smartitengineering.util.bean.annotations.Aggregator;
import com.smartitengineering.util.bean.annotations.InjectableField;

/**
 * All SPI collection for SPI implementations.
 *
 */
@Aggregator(contextName = SmartSPI.SPI_CONTEXT)
public final class SmartSPI {

		public static final String SPI_CONTEXT = SmartContentAPI.CONTEXT_NAME +
																						 ".spi";
		/**
		 * The lock handler implementation to be used to receive lock implementations.
		 * Use <tt>lockHandler</tt> as bean name to be injected here.
		 */
		@InjectableField
		protected LockHandler lockHandler;

		/**
		 * The type validator implementation which validatates a content type
		 * definition file source. Use <tt>typeValidator</tt> as the bean name in
		 * bean factory to be injected here.
		 */
		@InjectableField
		protected TypeValidator typeValidator;

		public TypeValidator getTypeValidator() {
				return typeValidator;
		}

		public LockHandler getLockHandler() {
				return lockHandler;
		}

		private SmartSPI() {
		}
		private static SmartSPI spi;

		public static SmartSPI getInstance() {
				if (spi == null) {
						spi = new SmartSPI();
						BeanFactoryRegistrar.aggregate(spi);
				}
				return spi;
		}
}
