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
package com.smartitengineering.cms.spi.impl.type;

import com.smartitengineering.cms.api.type.CollectionDataType;
import com.smartitengineering.cms.api.type.ContentDataType;
import com.smartitengineering.cms.api.type.ContentStatus;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.api.type.DataType;
import com.smartitengineering.cms.api.type.FieldDef;
import com.smartitengineering.cms.api.type.OtherDataType;
import com.smartitengineering.cms.api.type.RepresentationDef;
import com.smartitengineering.cms.api.type.ResourceDef;
import com.smartitengineering.cms.api.type.ResourceUri;
import com.smartitengineering.cms.api.type.SearchDef;
import com.smartitengineering.cms.api.type.ValidatorDef;
import com.smartitengineering.cms.api.type.VariationDef;
import com.smartitengineering.cms.spi.SmartContentSPI;
import com.smartitengineering.cms.spi.impl.Utils;
import com.smartitengineering.cms.spi.type.PersistableContentType;
import com.smartitengineering.dao.impl.hbase.spi.ExecutorService;
import com.smartitengineering.dao.impl.hbase.spi.impl.AbstactObjectRowConverter;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Map.Entry;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class ContentTypeObjectConverter extends AbstactObjectRowConverter<PersistentContentType, ContentTypeId> {

  public final static byte[] FAMILY_SIMPLE = Bytes.toBytes("simple");
  public final static byte[] FAMILY_FIELDS = Bytes.toBytes("fields");
  public final static byte[] FAMILY_REPRESENTATIONS = Bytes.toBytes("representations");
  public final static byte[] FAMILY_STATUSES = Bytes.toBytes("statuses");
  public final static byte[] CELL_DISPLAY_NAME = Bytes.toBytes("displayName");
  public final static byte[] CELL_CREATION_DATE = Bytes.toBytes("creationDate");
  public final static byte[] CELL_LAST_MODIFIED_DATE = Bytes.toBytes("lastModifiedDate");
  public final static byte[] CELL_PARENT_ID = Bytes.toBytes("parent");
  public final static byte[] CELL_RSRC_NAME = Bytes.toBytes("name");
  public final static byte[] CELL_RSRC_MIME_TYPE = Bytes.toBytes("mimeType");
  public final static byte[] CELL_RSRC_URI_TYPE = Bytes.toBytes("resourceUri.type");
  public final static byte[] CELL_RSRC_URI_VAL = Bytes.toBytes("resourceUri.value");
  public final static byte[] CELL_RSRC_TEMPLATE = Bytes.toBytes("templateType");
  public final static byte[] CELL_FIELD_VAL_TYPE = Bytes.toBytes("fieldValType");
  public final static byte[] CELL_FIELD_STANDALONE = Bytes.toBytes("fieldStandalone");
  public final static byte[] CELL_FIELD_REQUIRED = Bytes.toBytes("fieldRequired");
  public final static String CELL_FIELD_VAR_DEF = "fieldVariations";
  public final static String CELL_FIELD_VALIDATOR = "fieldValidator";
  public final static String CELL_FIELD_VALIDATOR_TYPE = "validatorType";
  public final static byte[] CELL_FIELD_SEARCHDEF_INDEXED = Bytes.toBytes("searchDef:indexed");
  public final static byte[] CELL_FIELD_SEARCHDEF_STORED = Bytes.toBytes("searchDef:stored");
  public final static byte[] CELL_FIELD_SEARCHDEF_BOOST_CONFIG = Bytes.toBytes("searchDef:boost");
  public final static byte[] CELL_FIELD_COLLECTION_ITEM_TYPE = Bytes.toBytes("itemType");
  public final static byte[] CELL_FIELD_COLLECTION_ITEM = Bytes.toBytes("item");
  public final static byte[] CELL_FIELD_COLLECTION_MAX_SIZE = Bytes.toBytes("maxSize");
  public final static byte[] CELL_FIELD_COLLECTION_MIN_SIZE = Bytes.toBytes("minSize");
  public final static byte[] CELL_FIELD_CONTENT_BIDIRECTIONAL = Bytes.toBytes("bidirectionalFieldName");
  public final static byte[] CELL_FIELD_CONTENT_TYPE_ID = Bytes.toBytes("typeId");
  public final static byte[] CELL_FIELD_OTHER_MIME_TYPE = Bytes.toBytes("mimeType");
  public final static byte[] COLON = Bytes.toBytes(":");
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  protected String[] getTablesToAttainLock() {
    return new String[]{getInfoProvider().getMainTableName()};
  }

  @Override
  protected void getPutForTable(PersistentContentType instance, ExecutorService service, Put put) {
    if (logger.isInfoEnabled()) {
      logger.info("Put formation for content type id " + instance.getId());
    }
    logger.debug("Set creation date if necessary and set last modification date");
    final Date date = new Date();
    try {
      /*
       * Simple content type values
       */
      final String displayName = instance.getMutableContentType().getDisplayName();
      if (StringUtils.isNotBlank(displayName)) {
        logger.debug("Set displayName");
        put.add(FAMILY_SIMPLE, CELL_DISPLAY_NAME, Bytes.toBytes(displayName));
      }
      final Date lastModifiedDate = date;
      logger.debug("Set creation date if necessary and set last modification date");
      put.add(FAMILY_SIMPLE, CELL_LAST_MODIFIED_DATE, Utils.toBytes(lastModifiedDate));
      final Date creationDate;
      if (instance.getMutableContentType().getCreationDate() == null) {
        logger.debug("Using old creation date");
        creationDate = instance.getMutableContentType().getCreationDate();
      }
      else {
        logger.debug("Setting new creation date");
        creationDate = date;
      }
      if (instance.getMutableContentType() instanceof com.smartitengineering.cms.spi.type.PersistableContentType) {
        PersistableContentType typeImpl = (PersistableContentType) instance.getMutableContentType();
        typeImpl.setCreationDate(creationDate);
        typeImpl.setLastModifiedDate(lastModifiedDate);
      }
      put.add(FAMILY_SIMPLE, CELL_CREATION_DATE, Utils.toBytes(creationDate));
      if (instance.getMutableContentType().getParent() != null) {
        put.add(FAMILY_SIMPLE, CELL_PARENT_ID, getInfoProvider().getRowIdFromId(instance.getMutableContentType().
            getParent()));
      }
      /*
       * Content statuses
       */
      for (Entry<String, ContentStatus> entry : instance.getMutableContentType().getStatuses().entrySet()) {
        put.add(FAMILY_STATUSES, Bytes.toBytes(entry.getKey()), Bytes.toBytes(entry.getValue().getName()));
      }
      /*
       * Representations
       */
      final byte[] repFamily = FAMILY_REPRESENTATIONS;
      for (Entry<String, RepresentationDef> entry : instance.getMutableContentType().getRepresentations().entrySet()) {
        final RepresentationDef value = entry.getValue();
        final byte[] toBytes = Bytes.add(Bytes.toBytes(entry.getKey()), COLON);
        putResourceDef(put, repFamily, toBytes, value);
      }
      /*
       * Fields
       */
      for (Entry<String, FieldDef> entry : instance.getMutableContentType().getFields().entrySet()) {
        final FieldDef value = entry.getValue();
        final byte[] toBytes = Bytes.add(Bytes.toBytes(entry.getKey()), COLON);
        /*
         * Simple field values
         */
        put.add(FAMILY_FIELDS, Bytes.add(toBytes, CELL_FIELD_STANDALONE), Bytes.toBytes(value.
            isFieldStandaloneUpdateAble()));
        put.add(FAMILY_FIELDS, Bytes.add(toBytes, CELL_FIELD_REQUIRED), Bytes.toBytes(value.isRequired()));
        /*
         * Variations
         */
        Collection<VariationDef> varDefs = value.getVariations();
        if (varDefs != null && !varDefs.isEmpty()) {
          int index = 0;
          for (VariationDef def : varDefs) {
            putResourceDef(put, FAMILY_FIELDS, Bytes.add(toBytes, Bytes.toBytes(new StringBuilder(CELL_FIELD_VAR_DEF).
                append('.').append(index++).append(':').toString())), def);
          }
        }
        /*
         * Validator def
         */
        ValidatorDef validatorDef = value.getCustomValidator();
        put.add(FAMILY_FIELDS, Bytes.add(toBytes, Bytes.toBytes(new StringBuilder(CELL_FIELD_VALIDATOR).append(
            ':').append(CELL_FIELD_VALIDATOR_TYPE).toString())), Bytes.toBytes(validatorDef.geType().name()));
        putResourceUri(put, FAMILY_FIELDS, Bytes.add(toBytes, Bytes.toBytes(new StringBuilder(CELL_FIELD_VALIDATOR).
            append(':').toString())), validatorDef.getUri());
        /*
         * Search def
         */
        SearchDef searchDef = value.getSearchDefinition();
        put.add(FAMILY_FIELDS, Bytes.add(toBytes, CELL_FIELD_SEARCHDEF_INDEXED), Bytes.toBytes(searchDef.isIndexed()));
        put.add(FAMILY_FIELDS, Bytes.add(toBytes, CELL_FIELD_SEARCHDEF_STORED), Bytes.toBytes(searchDef.isStored()));
        if (StringUtils.isNotBlank(searchDef.getBoostConfig())) {
          put.add(FAMILY_FIELDS, Bytes.add(toBytes, CELL_FIELD_SEARCHDEF_BOOST_CONFIG), Bytes.toBytes(searchDef.
              getBoostConfig()));
        }
        /*
         * Data type
         */
        final DataType valueDef = value.getValueDef();
        put.add(FAMILY_FIELDS, Bytes.add(toBytes, CELL_FIELD_VAL_TYPE), Bytes.toBytes(valueDef.getType().name()));
        handleSpecialDataTypes(put, toBytes, valueDef);
      }
    }
    catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  protected void handleSpecialDataTypes(final Put put, final byte[] myPrefix, final DataType valueDef) throws
      IOException {
    byte[] prefix = Bytes.add(Bytes.add(myPrefix, Bytes.toBytes(valueDef.getType().name())), COLON);
    switch (valueDef.getType()) {
      case COLLECTION:
        CollectionDataType collectionDataType = (CollectionDataType) valueDef;
        put.add(FAMILY_FIELDS, Bytes.add(prefix, CELL_FIELD_COLLECTION_ITEM_TYPE), Bytes.toBytes(collectionDataType.
            getItemDataType().getType().name()));
        put.add(FAMILY_FIELDS, Bytes.add(prefix, CELL_FIELD_COLLECTION_MAX_SIZE), Bytes.toBytes(collectionDataType.
            getMaxSize()));
        put.add(FAMILY_FIELDS, Bytes.add(prefix, CELL_FIELD_COLLECTION_MIN_SIZE), Bytes.toBytes(collectionDataType.
            getMinSize()));
        final byte[] nestedPrefix = Bytes.add(Bytes.add(prefix, CELL_FIELD_COLLECTION_ITEM), COLON);
        handleSpecialDataTypes(put, nestedPrefix, collectionDataType.getItemDataType());
        break;
      case CONTENT:
        ContentDataType contentDataType = (ContentDataType) valueDef;
        if (StringUtils.isNotBlank(contentDataType.getBidirectionalFieldName())) {
          put.add(FAMILY_FIELDS, Bytes.add(prefix, CELL_FIELD_CONTENT_BIDIRECTIONAL), Bytes.toBytes(contentDataType.
              getBidirectionalFieldName()));
        }
        put.add(FAMILY_FIELDS, Bytes.add(prefix, CELL_FIELD_COLLECTION_ITEM_TYPE),
                getInfoProvider().getRowIdFromId(contentDataType.getTypeDef()));
        break;
      case OTHER:
      case STRING:
        OtherDataType otherDataType = (OtherDataType) valueDef;
        put.add(FAMILY_FIELDS, Bytes.add(prefix, CELL_FIELD_OTHER_MIME_TYPE), Bytes.toBytes(otherDataType.getMIMEType()));
        break;
    }
  }

  protected void putResourceDef(Put put, final byte[] family, final byte[] prefix, final ResourceDef value) {
    put.add(family, Bytes.add(prefix, CELL_RSRC_NAME), Bytes.toBytes(value.getName()));
    put.add(family, Bytes.add(prefix, CELL_RSRC_MIME_TYPE), Bytes.toBytes(value.getMIMEType()));
    final ResourceUri resourceUri = value.getResourceUri();
    putResourceUri(put, family, prefix, resourceUri);
    put.add(family, Bytes.add(prefix, CELL_RSRC_TEMPLATE), Bytes.toBytes(value.getTemplateType().name()));
  }

  protected void putResourceUri(Put put, final byte[] family, final byte[] prefix, final ResourceUri resourceUri) {
    put.add(family, Bytes.add(prefix, CELL_RSRC_URI_TYPE), Bytes.toBytes(resourceUri.getType().name()));
    put.add(family, Bytes.add(prefix, CELL_RSRC_URI_VAL), Bytes.toBytes(resourceUri.getValue()));
  }

  @Override
  protected void getDeleteForTable(PersistentContentType instance, ExecutorService service, Delete put) {
    // No further implementation is supposed to needed.
  }

  @Override
  public PersistentContentType rowsToObject(Result startRow, ExecutorService executorService) {
    PersistableContentType contentType = SmartContentSPI.getInstance().getPersistableDomainFactory().
        createPersistableContentType();
    PersistentContentType persistentContentType = new PersistentContentType();
    persistentContentType.setMutableContentType(contentType);
    persistentContentType.setVersion(0l);
    byte[] displayName = startRow.getValue(FAMILY_SIMPLE, CELL_DISPLAY_NAME);
    if(displayName != null) {
      contentType.setDisplayName(Bytes.toString(displayName));
    }
    return persistentContentType;
  }
}
