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
package com.smartitengineering.cms.spi;

import com.smartitengineering.cms.api.factory.write.PersistentWriter;
import com.smartitengineering.cms.api.type.MutableContentType;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.spi.content.ContentSearcher;
import com.smartitengineering.cms.spi.content.PersistentContentReader;
import com.smartitengineering.cms.spi.content.RepresentationProvider;
import com.smartitengineering.cms.spi.content.ValidatorProvider;
import com.smartitengineering.cms.spi.content.VariationProvider;
import com.smartitengineering.cms.spi.lock.LockHandler;
import com.smartitengineering.cms.spi.persistence.PersistableDomainFactory;
import com.smartitengineering.cms.spi.persistence.PersistentService;
import com.smartitengineering.cms.spi.persistence.PersistentServiceRegistrar;
import com.smartitengineering.cms.spi.type.ContentTypeDefinitionParsers;
import com.smartitengineering.cms.spi.type.PersistentContentTypeReader;
import com.smartitengineering.cms.spi.type.SearchFieldNameGenerator;
import com.smartitengineering.cms.spi.type.TypeValidators;
import com.smartitengineering.cms.spi.workspace.WorkspaceService;
import com.smartitengineering.util.bean.BeanFactoryRegistrar;
import com.smartitengineering.util.bean.annotations.Aggregator;
import com.smartitengineering.util.bean.annotations.InjectableField;

/**
 * All SPI collection for SPI implementations.
 *
 */
@Aggregator(contextName = SmartContentSPI.SPI_CONTEXT)
public final class SmartContentSPI {

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
  protected TypeValidators typeValidators;
  @InjectableField
  protected RepresentationProvider representationProvider;
  @InjectableField
  protected VariationProvider variationProvider;
  @InjectableField
  protected ValidatorProvider validatorProvider;
  @InjectableField
  private SearchFieldNameGenerator searchFieldNameGenerator;
  /**
   * The registrar for aggregating different implementations of
   * {@link PersistentService} for diffent domain types. Use the bean name
   * <tt>persistentServiceRegistrar</tt> for injecting it here.
   */
  @InjectableField
  protected PersistentServiceRegistrar persistentServiceRegistrar;
  @InjectableField
  private ContentTypeDefinitionParsers contentTypeDefinitionParsers;
  @InjectableField
  private PersistentContentTypeReader contentTypeReader;
  @InjectableField
  private PersistentContentReader contentReader;
  @InjectableField
  private WorkspaceService workspaceService;
  @InjectableField
  private PersistableDomainFactory persistableDomainFactory;
  @InjectableField(beanName = "schemaLocationForContentTypeXml")
  private String schemaLocationForContentTypeXml;
  @InjectableField
  private ContentSearcher contentSearcher;

  public ValidatorProvider getValidatorProvider() {
    return validatorProvider;
  }

  public ContentSearcher getContentSearcher() {
    return contentSearcher;
  }

  public String getSchemaLocationForContentTypeXml() {
    return schemaLocationForContentTypeXml;
  }

  public PersistableDomainFactory getPersistableDomainFactory() {
    return persistableDomainFactory;
  }

  public WorkspaceService getWorkspaceService() {
    return workspaceService;
  }

  public PersistentContentReader getContentReader() {
    return contentReader;
  }

  public PersistentContentTypeReader getContentTypeReader() {
    return contentTypeReader;
  }

  public ContentTypeDefinitionParsers getContentTypeDefinitionParsers() {
    return contentTypeDefinitionParsers;
  }

  public PersistentServiceRegistrar getPersistentServiceRegistrar() {
    return persistentServiceRegistrar;
  }

  /**
   * An operation for retrieving the concrete implementation of persistent
   * service implementaion for the given persistable API bean.
   * @param <T> Should represent the class to be used in concrete SPI
   *					  implementations. For example, {@link MutableContentType}
   * @param writerClass The class to look for in the registrar.
   * @return Service for persisting the bean.
   * @see PersistentServiceRegistrar#getPersistentService(java.lang.Class)
   */
  public <T extends PersistentWriter> PersistentService<T> getPersistentService(Class<T> writerClass) {
    return getPersistentServiceRegistrar().getPersistentService(writerClass);
  }

  public SearchFieldNameGenerator getSearchFieldNameGenerator() {
    return searchFieldNameGenerator;
  }

  public TypeValidators getTypeValidators() {
    return typeValidators;
  }

  public LockHandler getLockHandler() {
    return lockHandler;
  }

  public RepresentationProvider getRepresentationProvider() {
    return representationProvider;
  }

  public VariationProvider getVariationProvider() {
    return variationProvider;
  }

  private SmartContentSPI() {
  }
  private static SmartContentSPI spi;

  public static SmartContentSPI getInstance() {
    if (spi == null) {
      spi = new SmartContentSPI();
      BeanFactoryRegistrar.aggregate(spi);
    }
    return spi;
  }
}
