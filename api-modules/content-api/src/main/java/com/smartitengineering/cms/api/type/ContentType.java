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

import com.smartitengineering.cms.api.common.MediaType;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * Defines a specific type of content identified by {@link ContentTypeID}.
 * This is the generalized form of content definition.
 * @author imyousuf
 * @since 0.1
 */
public interface ContentType {

  /**
   * Retrieve the unique ID of the content type to be used for relating
   * this content type to other objects.
   * @return the id representation of the content type
   */
  public ContentTypeId getContentTypeID();

  /**
   * Retrieve the statuses available for the workflow of contents of
   * this type. The collection returned could be unmodifiable.
   * @return {@link Collection} of statuses of this content type
   */
  public Map<String, ContentStatus> getStatuses();

  public String getPrimaryFieldName();

  public FieldDef getPrimaryFieldDef();

  public Map<String, FieldDef> getFieldDefs();

  /**
   * Retrieve the defined fields for this content type. The collection
   * returned could be unmodifiable.
   * @return defined fields
   */
  public Map<String, FieldDef> getOwnFieldDefs();

  public ContentTypeId getParent();

  public String getDisplayName();

  public Map<String, RepresentationDef> getRepresentationDefs();

  public RepresentationDef getRepresentationDefForMimeType(String mimeType);

  public Date getCreationDate();

  public Date getLastModifiedDate();

  public Map<MediaType, String> getRepresentations();

  public Map<ContentProcessingPhase, Collection<ContentCoProcessorDef>> getContentCoProcessorDefs();

  public String getEntityTagValue();

  public Map<String, String> getParameterizedDisplayNames();

  /**
   * Retrieves the definition type for this content type definition. It works as follows, if current defines its type 
   * then use that. If nothing is specified and there is no parent content type then return 
   * {@link DefinitionType#getDefaufltType()  default type} If nothing is specified and there is an inheritence in the 
   * content type definition then return as concrete type of that type, i.e. {@link DefinitionType#CONCRETE_TYPE} and 
   * {@link DefinitionType#CONCRETE_COMPONENT} for {@link DefinitionType#ABSTRACT_TYPE} and 
   * {@link DefinitionType#ABSTRACT_COMPONENT} respectively. With restriction that {@link DefinitionType#CONCRETE_TYPE} 
   * can be extended from {@link DefinitionType#ABSTRACT_COMPONENT}, Type must extend from type and component from 
   * component. The implication of this type is that, only {@link DefinitionType#CONCRETE_TYPE} can be created as 
   * content.
   * @return Type of the definition and it should never return null.
   */
  public DefinitionType getDefinitionType();

  /**
   * Simply retrieve the definition set this very type.
   */
  public DefinitionType getSelfDefinitionType();

  enum DefinitionType {

    ABSTRACT_TYPE,
    ABSTRACT_COMPONENT,
    CONCRETE_TYPE,
    CONCRETE_COMPONENT;

    public static DefinitionType getDefaufltType() {
      return CONCRETE_TYPE;
    }
  }

  enum ContentProcessingPhase {

    /**
     * This phase refers to after content is retrieved from persistent storage and converted to mutable content but yet
     * not returned to invoker.
     */
    READ,
    /**
     * This phase refers to once a mutable content is formed but yet not sent to validation.
     */
    WRITE;
  }
}
