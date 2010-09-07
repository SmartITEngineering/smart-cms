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
package com.smartitengineering.cms.api;

import com.smartitengineering.cms.api.content.ContentLoader;
import com.smartitengineering.cms.api.type.ContentTypeLoader;
import com.smartitengineering.util.bean.BeanFactoryRegistrar;
import com.smartitengineering.util.bean.annotations.Aggregator;
import com.smartitengineering.util.bean.annotations.InjectableField;

/**
 * The single point of entry to the APIs' of Smart CMS. All other APIs will be
 * avaiable from this class using the getInstance singleton intializer.<p /> For
 * developers wanting to inject to this intializer use {@link SmartContentAPI#CONTEXT_NAME}
 * as the context name to {@link BeanFactoryRegistrar}. Consult the protected
 * members section to find out the beans available for injection and their class
 * and expected name.
 * @author imyousuf
 * @since 0.1
 */
@Aggregator(contextName = SmartContentAPI.CONTEXT_NAME)
public final class SmartContentAPI {

  /**
   * Name of the context to bind all the beans to be injected into this
   * class.
   */
  public static final String CONTEXT_NAME = "com.smartitnengineering.smart-cms";
  private static SmartContentAPI api;

  private SmartContentAPI() {
    BeanFactoryRegistrar.aggregate(this);
  }

  /**
   * Retrieves the singleton instance of the API entry point. The API will
   * have all its beans injected before returning itself.
   * @return The API entry point
   */
  public static final SmartContentAPI getInstance() {
    if (api == null) {
      api = new SmartContentAPI();
    }
    return api;
  }
  /**
   * The API for loading all DTOs related to {@link ContentType}. Use the name
   * 'apiContentTypeLoader' as the bean's name to be injected to it.
   */
  @InjectableField(beanName = "apiContentTypeLoader")
  protected ContentTypeLoader contentTypeLoader;
  @InjectableField(beanName = "apiContentLoader")
  protected ContentLoader contentLoader;

  public ContentTypeLoader getContentTypeLoader() {
    return contentTypeLoader;
  }

  public ContentLoader getContentLoader() {
    return contentLoader;
  }
}
