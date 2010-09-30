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

import com.smartitengineering.cms.api.SmartContentAPI;
import com.smartitengineering.cms.api.type.CollectionDataType;
import com.smartitengineering.cms.api.type.ContentDataType;
import com.smartitengineering.cms.api.type.ContentStatus;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.api.type.DataType;
import com.smartitengineering.cms.api.type.FieldDef;
import com.smartitengineering.cms.api.type.FieldValueType;
import com.smartitengineering.cms.api.type.MutableCollectionDataType;
import com.smartitengineering.cms.api.type.MutableContentDataType;
import com.smartitengineering.cms.api.type.MutableContentStatus;
import com.smartitengineering.cms.api.type.MutableFieldDef;
import com.smartitengineering.cms.api.type.MutableOtherDataType;
import com.smartitengineering.cms.api.type.MutableRepresentationDef;
import com.smartitengineering.cms.api.type.MutableResourceDef;
import com.smartitengineering.cms.api.type.MutableResourceUri;
import com.smartitengineering.cms.api.type.MutableSearchDef;
import com.smartitengineering.cms.api.type.MutableStringDataType;
import com.smartitengineering.cms.api.type.MutableValidatorDef;
import com.smartitengineering.cms.api.type.MutableVariationDef;
import com.smartitengineering.cms.api.type.OtherDataType;
import com.smartitengineering.cms.api.type.RepresentationDef;
import com.smartitengineering.cms.api.type.ResourceDef;
import com.smartitengineering.cms.api.type.ResourceUri;
import com.smartitengineering.cms.api.type.SearchDef;
import com.smartitengineering.cms.api.type.StringDataType;
import com.smartitengineering.cms.api.common.TemplateType;
import com.smartitengineering.cms.api.type.ValidatorDef;
import com.smartitengineering.cms.api.type.ValidatorType;
import com.smartitengineering.cms.api.type.VariationDef;
import com.smartitengineering.cms.spi.SmartContentSPI;
import com.smartitengineering.cms.spi.impl.Utils;
import com.smartitengineering.cms.spi.type.PersistableContentType;
import com.smartitengineering.dao.impl.hbase.spi.ExecutorService;
import com.smartitengineering.dao.impl.hbase.spi.impl.AbstactObjectRowConverter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.FastDateFormat;
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
  public final static String CELL_FIELD_SEARCHDEF = "searchDef";
  public final static byte[] CELL_FIELD_SEARCHDEF_INDEXED = Bytes.toBytes(CELL_FIELD_SEARCHDEF + ":indexed");
  public final static byte[] CELL_FIELD_SEARCHDEF_STORED = Bytes.toBytes(CELL_FIELD_SEARCHDEF + ":stored");
  public final static byte[] CELL_FIELD_SEARCHDEF_BOOST_CONFIG = Bytes.toBytes(CELL_FIELD_SEARCHDEF + ":boost");
  public final static byte[] CELL_FIELD_COLLECTION_ITEM_TYPE = Bytes.toBytes("itemType");
  public final static byte[] CELL_FIELD_COLLECTION_ITEM = Bytes.toBytes("item");
  public final static byte[] CELL_FIELD_COLLECTION_MAX_SIZE = Bytes.toBytes("maxSize");
  public final static byte[] CELL_FIELD_COLLECTION_MIN_SIZE = Bytes.toBytes("minSize");
  public final static byte[] CELL_FIELD_CONTENT_BIDIRECTIONAL = Bytes.toBytes("bidirectionalFieldName");
  public final static byte[] CELL_FIELD_CONTENT_TYPE_ID = Bytes.toBytes("typeId");
  public final static byte[] CELL_FIELD_STRING_ENCODING = Bytes.toBytes("encoding");
  public final static byte[] CELL_FIELD_OTHER_MIME_TYPE = Bytes.toBytes("mimeType");
  public final static byte[] COLON = Bytes.toBytes(":");
  public static final String SPCL_FIELD_DATA_TYPE_PATTERN = ":(" + FieldValueType.COLLECTION.name() + "|" + FieldValueType.CONTENT.
      name() + "|" + FieldValueType.OTHER.name() + "|" + FieldValueType.STRING.name() + "):(.+)";
  public static final String COLLECTION_FIELD_ITEM_DATA_TYPE_PREFIX = ":COLLECTION:" + CELL_FIELD_COLLECTION_ITEM;

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
        if (logger.isDebugEnabled()) {
          logger.debug(new StringBuilder("Set displayName ").append(displayName).toString());
        }
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
        logger.debug("Found to be instanceof PersistableContentType thus setting date members in the object");
        PersistableContentType typeImpl = (PersistableContentType) instance.getMutableContentType();
        typeImpl.setCreationDate(creationDate);
        typeImpl.setLastModifiedDate(lastModifiedDate);
      }
      put.add(FAMILY_SIMPLE, CELL_CREATION_DATE, Utils.toBytes(creationDate));
      final ContentTypeId parent = instance.getMutableContentType().getParent();
      if (parent != null) {
        if (logger.isDebugEnabled()) {
          logger.debug(new StringBuilder("Has parent ").append(parent.toString()).toString());
        }
        put.add(FAMILY_SIMPLE, CELL_PARENT_ID, getInfoProvider().getRowIdFromId(parent));
      }
      /*
       * Content statuses
       */
      for (Entry<String, ContentStatus> entry : instance.getMutableContentType().getStatuses().entrySet()) {
        final String key = entry.getKey();
        if (logger.isDebugEnabled()) {
          logger.debug(new StringBuilder("Putting status ").append(key).toString());
        }
        put.add(FAMILY_STATUSES, Bytes.toBytes(key), Bytes.toBytes(entry.getValue().getName()));
      }
      /*
       * Representations
       */
      final byte[] repFamily = FAMILY_REPRESENTATIONS;
      for (Entry<String, RepresentationDef> entry : instance.getMutableContentType().getRepresentationDefs().entrySet()) {
        if (logger.isDebugEnabled()) {
          logger.debug(new StringBuilder("Putting representation def for ").append(entry.getKey()).append(" and value ").
              append(entry.getValue().toString()).toString());
        }
        final RepresentationDef value = entry.getValue();
        final byte[] toBytes = Bytes.add(Bytes.toBytes(entry.getKey()), COLON);
        putResourceDef(put, repFamily, toBytes, value);
      }
      /*
       * Fields
       */
      for (Entry<String, FieldDef> entry : instance.getMutableContentType().getFieldDefs().entrySet()) {
        if (logger.isDebugEnabled()) {
          logger.debug(new StringBuilder("Putting field with name ").append(entry.getKey()).append(" and value ").append(entry.
              getValue().toString()).toString());
        }
        final FieldDef value = entry.getValue();
        final byte[] toBytes = Bytes.add(Bytes.toBytes(entry.getKey()), COLON);
        /*
         * Simple field values
         */
        logger.debug("Putting simple field values");
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
            if (logger.isDebugEnabled()) {
              logger.debug(new StringBuilder("Putting variation with name ").append(def.getName()).toString());
            }
            putResourceDef(put, FAMILY_FIELDS, Bytes.add(toBytes, Bytes.toBytes(new StringBuilder(CELL_FIELD_VAR_DEF).
                append(':').append(index++).append(':').toString())), def);
          }
        }
        /*
         * Validator def
         */
        ValidatorDef validatorDef = value.getCustomValidator();
        if (validatorDef != null) {
          logger.debug("Put custom validator for field");
          put.add(FAMILY_FIELDS, Bytes.add(toBytes, Bytes.toBytes(new StringBuilder(CELL_FIELD_VALIDATOR).append(
              ':').append(CELL_FIELD_VALIDATOR_TYPE).toString())), Bytes.toBytes(validatorDef.geType().name()));
          putResourceUri(put, FAMILY_FIELDS, Bytes.add(toBytes, Bytes.toBytes(new StringBuilder(CELL_FIELD_VALIDATOR).
              append(':').toString())), validatorDef.getUri());
        }
        /*
         * Search def
         */
        SearchDef searchDef = value.getSearchDefinition();
        if (searchDef != null) {
          logger.debug("Putting search definition for field");
          put.add(FAMILY_FIELDS, Bytes.add(toBytes, CELL_FIELD_SEARCHDEF_INDEXED), Bytes.toBytes(searchDef.isIndexed()));
          put.add(FAMILY_FIELDS, Bytes.add(toBytes, CELL_FIELD_SEARCHDEF_STORED), Bytes.toBytes(searchDef.isStored()));
          if (StringUtils.isNotBlank(searchDef.getBoostConfig())) {
            put.add(FAMILY_FIELDS, Bytes.add(toBytes, CELL_FIELD_SEARCHDEF_BOOST_CONFIG), Bytes.toBytes(searchDef.
                getBoostConfig()));
          }
        }
        /*
         * Data type
         */
        final DataType valueDef = value.getValueDef();
        if (valueDef != null) {
          logger.debug("Work with field data type");
          final byte[] fieldValType = Bytes.add(toBytes, CELL_FIELD_VAL_TYPE);
          put.add(FAMILY_FIELDS, fieldValType, Bytes.toBytes(valueDef.getType().name()));
          handleSpecialDataTypes(put, Bytes.add(fieldValType, COLON), valueDef);
        }
        if (logger.isDebugEnabled()) {
          logger.debug(new StringBuilder("Finished putting field with name ").append(entry.getKey()).toString());
        }
      }
    }
    catch (Exception ex) {
      logger.warn("Error converting content type to Put throwing exception...", ex);
      throw new RuntimeException(ex);
    }
  }

  protected void handleSpecialDataTypes(final Put put, final byte[] myPrefix, final DataType valueDef) throws
      IOException {
    byte[] prefix = Bytes.add(Bytes.add(myPrefix, Bytes.toBytes(valueDef.getType().name())), COLON);
    switch (valueDef.getType()) {
      case COLLECTION:
        logger.debug("Working with COLLECTION Special data type");
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
        logger.debug("Working with CONTENT Special data type");
        ContentDataType contentDataType = (ContentDataType) valueDef;
        if (StringUtils.isNotBlank(contentDataType.getBidirectionalFieldName())) {
          put.add(FAMILY_FIELDS, Bytes.add(prefix, CELL_FIELD_CONTENT_BIDIRECTIONAL), Bytes.toBytes(contentDataType.
              getBidirectionalFieldName()));
        }
        put.add(FAMILY_FIELDS, Bytes.add(prefix, CELL_FIELD_CONTENT_TYPE_ID),
                getInfoProvider().getRowIdFromId(contentDataType.getTypeDef()));
        break;
      case STRING:
        logger.debug("Working with STRING Special data type");
        StringDataType stringDataType = (StringDataType) valueDef;
        put.add(FAMILY_FIELDS, Bytes.add(prefix, CELL_FIELD_STRING_ENCODING),
                Bytes.toBytes(stringDataType.getEncoding()));
      case OTHER:
        logger.debug("Working with OTHER Special data type");
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
    logger.debug("Nothing to process for Delete!");
  }

  @Override
  public PersistentContentType rowsToObject(Result startRow, ExecutorService executorService) {
    try {
      PersistableContentType contentType =
                             SmartContentSPI.getInstance().getPersistableDomainFactory().createPersistableContentType();
      PersistentContentType persistentContentType = new PersistentContentType();
      /*
       * Simple fields
       */
      persistentContentType.setMutableContentType(contentType);
      persistentContentType.setVersion(0l);
      logger.debug("Converting rowId to ContentTypeId");
      contentType.setContentTypeID(getInfoProvider().getIdFromRowId(startRow.getRow()));
      byte[] displayName = startRow.getValue(FAMILY_SIMPLE, CELL_DISPLAY_NAME);
      if (displayName != null) {
        logger.debug("Set display name of the content type!");
        contentType.setDisplayName(Bytes.toString(displayName));
      }
      logger.debug("Setting creation and last modified date");
      contentType.setCreationDate(Utils.toDate(startRow.getValue(FAMILY_SIMPLE, CELL_CREATION_DATE)));
      contentType.setLastModifiedDate(Utils.toDate(startRow.getValue(FAMILY_SIMPLE, CELL_LAST_MODIFIED_DATE)));
      final byte[] parentId = startRow.getValue(FAMILY_SIMPLE, CELL_PARENT_ID);
      if (parentId != null) {
        logger.debug("Setting parent id");
        contentType.setParent(getInfoProvider().getIdFromRowId(parentId));
      }
      if (logger.isDebugEnabled()) {
        final FastDateFormat formatter = DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT;
        logger.debug(String.format("Creation date is %s and last modified date is %s", formatter.format(contentType.
            getCreationDate()), formatter.format(contentType.getLastModifiedDate())));
        logger.debug(String.format("Id is %s and parent id is %s", contentType.getContentTypeID().toString(), contentType.
            getParent().toString()));
      }
      /*
       * Content status
       */
      logger.info("Form statuses");
      NavigableMap<byte[], byte[]> statusMap = startRow.getFamilyMap(FAMILY_STATUSES);
      int index = 0;
      for (byte[] statusName : statusMap.navigableKeySet()) {
        final String statusNameStr = Bytes.toString(statusName);
        if (logger.isDebugEnabled()) {
          logger.debug(new StringBuilder("Forming content status for ").append(statusNameStr).toString());
        }
        //Value not required as both are the same for status
        MutableContentStatus contentStatus = SmartContentAPI.getInstance().getContentTypeLoader().
            createMutableContentStatus();
        contentStatus.setContentTypeID(contentType.getContentTypeID());
        contentStatus.setId(++index);
        contentStatus.setName(statusNameStr);
        contentType.getMutableStatuses().add(contentStatus);
      }
      /*
       * Representations
       */
      logger.info("Form representations!");
      NavigableMap<byte[], byte[]> representationMap = startRow.getFamilyMap(FAMILY_REPRESENTATIONS);
      Map<String, MutableRepresentationDef> reps = new HashMap<String, MutableRepresentationDef>();
      for (byte[] keyBytes : representationMap.navigableKeySet()) {
        final String key = Bytes.toString(keyBytes);
        if (logger.isDebugEnabled()) {
          logger.debug(new StringBuilder("Work with key ").append(key).toString());
        }
        final int indexOfFirstColon = key.indexOf(':');
        final String repName = key.substring(0, indexOfFirstColon);
        final byte[] qualifier = Bytes.toBytes(key.substring(indexOfFirstColon + 1));
        if (logger.isDebugEnabled()) {
          logger.debug(new StringBuilder("Representation name ").append(repName).toString());
          logger.debug(new StringBuilder("Representation qualifier ").append(Bytes.toString(qualifier)).toString());
        }
        MutableRepresentationDef representationDef = reps.get(repName);
        if (representationDef == null) {
          logger.debug("Creating new representation def and putting to map");
          representationDef = SmartContentAPI.getInstance().getContentTypeLoader().createMutableRepresentationDef();
          reps.put(repName, representationDef);
          representationDef.setName(repName);
        }
        final byte[] value = representationMap.get(keyBytes);
        fillResourceDef(qualifier, representationDef, value);
      }
      contentType.getMutableRepresentationDefs().addAll(reps.values());
      /*
       * Fields
       */
      NavigableMap<byte[], byte[]> fieldMap = startRow.getFamilyMap(FAMILY_FIELDS);
      //From a map of all cells form a map of cells by field name
      Map<String, Map<byte[], byte[]>> fieldsByName = new LinkedHashMap<String, Map<byte[], byte[]>>();
      Utils.organizeByPrefix(fieldMap, fieldsByName, ':');
      for (String fieldName : fieldsByName.keySet()) {
        final Map<byte[], byte[]> fieldCells = fieldsByName.get(fieldName);
        final Map<Integer, MutableVariationDef> fieldVariations = new TreeMap<Integer, MutableVariationDef>();
        final MutableSearchDef searchDef = SmartContentAPI.getInstance().getContentTypeLoader().createMutableSearchDef();
        final MutableFieldDef fieldDef = SmartContentAPI.getInstance().getContentTypeLoader().createMutableFieldDef();
        final MutableValidatorDef validatorDef = SmartContentAPI.getInstance().getContentTypeLoader().
            createMutableValidatorDef();
        DataType mutableDataType = null;
        fieldDef.setName(fieldName);
        final String validatorPatternString = new StringBuilder(fieldName).append(':').append(CELL_FIELD_VALIDATOR).
            append(':').append(
            "(.*)").toString();
        if (logger.isDebugEnabled()) {
          logger.debug(new StringBuilder("Using following pattern to identify validator: ").append(
              validatorPatternString).toString());
        }
        Pattern validatorPattern = Pattern.compile(validatorPatternString);
        final String searchDefPatternString = new StringBuilder(fieldName).append(":(").append(CELL_FIELD_SEARCHDEF).
            append(':').append(".*)").toString();
        if (logger.isDebugEnabled()) {
          logger.debug(new StringBuilder("Using following pattern to identify search definition: ").append(
              searchDefPatternString).toString());
        }
        Pattern searchDefPattern = Pattern.compile(searchDefPatternString);
        final String variationPatternString = new StringBuilder(fieldName).append(':').append(CELL_FIELD_VAR_DEF).append(
            ":([\\d]+):(.*)").toString();
        if (logger.isDebugEnabled()) {
          logger.debug(new StringBuilder("Using following pattern to identify variation: ").append(
              variationPatternString).toString());
        }
        Pattern variationsDefPattern = Pattern.compile(variationPatternString);
        for (Entry<byte[], byte[]> cell : fieldCells.entrySet()) {
          final String key = Bytes.toString(cell.getKey());
          final byte[] value = cell.getValue();
          if (logger.isDebugEnabled()) {
            logger.debug(new StringBuilder("Matching following key against the patterns: ").append(key).toString());
            logger.debug(new StringBuilder("Cell value: ").append(Bytes.toString(value)).toString());
          }
          final Matcher searchDefMatcher = searchDefPattern.matcher(key);
          final Matcher variationsDefMatcher = variationsDefPattern.matcher(key);
          final Matcher validatorMatcher = validatorPattern.matcher(key);
          /*
           * Search Def
           */
          if (searchDefMatcher.matches()) {
            logger.debug("Matched search definition pattern");
            byte[] searchDefCell = Bytes.toBytes(searchDefMatcher.group(1));
            if (Arrays.equals(CELL_FIELD_SEARCHDEF_INDEXED, searchDefCell)) {
              searchDef.setIndexed(Bytes.toBoolean(value));
            }
            else if (Arrays.equals(CELL_FIELD_SEARCHDEF_STORED, searchDefCell)) {
              searchDef.setStored(Bytes.toBoolean(value));
            }
            else if (Arrays.equals(CELL_FIELD_SEARCHDEF_BOOST_CONFIG, searchDefCell)) {
              searchDef.setBoostConfig(Bytes.toString(value));
            }
          }
          /*
           * Variations
           */
          else if (variationsDefMatcher.matches()) {
            logger.debug("Matched variation pattern");
            Integer indexInt = NumberUtils.toInt(variationsDefMatcher.group(1), -1);
            if (indexInt > -1) {
              MutableVariationDef variationDef = fieldVariations.get(indexInt);
              if (variationDef == null) {
                variationDef = SmartContentAPI.getInstance().getContentTypeLoader().createMutableVariationDef();
                fieldVariations.put(indexInt, variationDef);
              }
              fillResourceDef(Bytes.toBytes(variationsDefMatcher.group(2)), variationDef, value);
            }
          }
          /*
           * Validator
           */
          else if (validatorMatcher.matches()) {
            logger.debug("Matched validator pattern");
            String validatorCell = validatorMatcher.group(1);
            byte[] validatorCellBytes = Bytes.toBytes(validatorCell);
            if (validatorCell.equals(CELL_FIELD_VALIDATOR_TYPE)) {
              logger.debug("Matched validator type");
              validatorDef.seType(ValidatorType.valueOf(Bytes.toString(value)));
            }
            else if (Arrays.equals(validatorCellBytes, CELL_RSRC_URI_TYPE)) {
              logger.debug("Matched Resource URI Type");
              final ResourceUri.Type valueOf = ResourceUri.Type.valueOf(Bytes.toString(value));
              MutableResourceUri uri =
                                 SmartContentAPI.getInstance().getContentTypeLoader().createMutableResourceUri();
              uri.setType(valueOf);
              final ResourceUri resourceUri = validatorDef.getUri();
              if (resourceUri != null) {
                logger.debug("Set value from old resource uri");
                uri.setValue(resourceUri.getValue());
              }
              validatorDef.setUri(uri);
            }
            else if (Arrays.equals(validatorCellBytes, CELL_RSRC_URI_VAL)) {
              logger.debug("Matched Resource URI Value");
              MutableResourceUri uri = SmartContentAPI.getInstance().getContentTypeLoader().createMutableResourceUri();
              final ResourceUri resourceUri = validatorDef.getUri();
              if (resourceUri != null) {
                logger.debug("Set type from old resource uri");
                uri.setType(resourceUri.getType());
              }
              uri.setValue(Bytes.toString(value));
              validatorDef.setUri(uri);
            }
          }
          /*
           * Simple and data type
           */
          else {
            logger.debug("Did not match any pattern");
            byte[] qualifier = Bytes.toBytes(key.substring(fieldName.length() + 1));
            if (Arrays.equals(CELL_FIELD_STANDALONE, qualifier)) {
              logger.debug("Its the basic standalone key");
              fieldDef.setFieldStandaloneUpdateAble(Bytes.toBoolean(value));
            }
            else if (Arrays.equals(CELL_FIELD_REQUIRED, qualifier)) {
              logger.debug("Its the basic required key");
              fieldDef.setRequired(Bytes.toBoolean(value));
            }
            /*
             * Data type
             */
            else {
              logger.debug("Its nothing but a data type cell key");
              if (Arrays.equals(CELL_FIELD_VAL_TYPE, qualifier) && mutableDataType == null) {
                final FieldValueType valueType = FieldValueType.valueOf(Bytes.toString(value));
                mutableDataType = createDataType(valueType);
              }
              /*
               * Handle special fields
               */
              else {
                logger.debug("Its a special data type cell!");
                final String specialFieldPatternPrefix = new StringBuilder(fieldName).append(':').append(
                    CELL_FIELD_VAL_TYPE).toString();
                mutableDataType = fillSpecialFields(specialFieldPatternPrefix, key, mutableDataType, value);
              }
            }
          }
        }
        if (mutableDataType != null) {
          fieldDef.setValueDef(mutableDataType);
        }
        else {
          final String msg = "Field value type can not be null!";
          logger.error(msg);
          throw new RuntimeException(msg);
        }
        logger.info("Set all fields into the field definition!");
        fieldDef.setCustomValidator(validatorDef);
        fieldDef.setSearchDefinition(searchDef);
        fieldDef.setVariations(fieldVariations.values());
        contentType.getMutableFieldDefs().add(fieldDef);
      }
      return persistentContentType;
    }
    catch (Exception ex) {
      logger.warn("Error converting result to content type, throwing exception...", ex);
      throw new RuntimeException(ex);
    }
  }

  protected DataType createDataType(FieldValueType valueType) {
    DataType mutableDataType;
    switch (valueType) {
      case BOOLEAN:
        mutableDataType = DataType.BOOLEAN;
        break;
      case INTEGER:
        mutableDataType = DataType.INTEGER;
        break;
      case LONG:
        mutableDataType = DataType.LONG;
        break;
      case DOUBLE:
        mutableDataType = DataType.DOUBLE;
        break;
      case DATE_TIME:
        mutableDataType = DataType.DATE_TIME;
        break;
      case COLLECTION:
        mutableDataType =
        SmartContentAPI.getInstance().getContentTypeLoader().createMutableCollectionDataType();
        break;
      case CONTENT:
        mutableDataType =
        SmartContentAPI.getInstance().getContentTypeLoader().createMutableContentDataType();
        break;
      case STRING:
        mutableDataType =
        SmartContentAPI.getInstance().getContentTypeLoader().createMutableStringDataType();
        break;
      case OTHER:
      default:
        mutableDataType =
        SmartContentAPI.getInstance().getContentTypeLoader().createMutableOtherDataType();
        break;
    }
    return mutableDataType;
  }

  protected DataType fillSpecialFields(final String patternPrefix, final String key, DataType mutableDataType,
                                       final byte[] value) throws ClassNotFoundException, IOException {
    final String specialFieldPatternString = new StringBuilder(patternPrefix).append(SPCL_FIELD_DATA_TYPE_PATTERN).
        toString();
    if (logger.isDebugEnabled()) {
      logger.debug(new StringBuilder("Using the following pattern to test valid special field ").append(
          specialFieldPatternString).toString());
    }
    Pattern pattern = Pattern.compile(specialFieldPatternString);
    Matcher matcher = pattern.matcher(key);
    if (matcher.matches()) {
      logger.debug("Verified special field");
      FieldValueType type = FieldValueType.valueOf(matcher.group(1));
      String specialFieldValueTypeInfoKey = matcher.group(2);
      if (logger.isDebugEnabled()) {
        logger.debug(new StringBuilder("Identified type as ").append(type).toString());
        logger.debug(new StringBuilder("The field value type's info key ").append(specialFieldValueTypeInfoKey).
            toString());
      }
      byte[] infoKey = Bytes.toBytes(specialFieldValueTypeInfoKey);
      if (mutableDataType == null) {
        mutableDataType = createDataType(type);
        logger.debug("Created mutable data type for special field");
      }
      switch (type) {
        case COLLECTION:
          logger.debug("Parsing collection");
          MutableCollectionDataType collectionDataType =
                                    (MutableCollectionDataType) mutableDataType;
          if (Arrays.equals(infoKey, CELL_FIELD_COLLECTION_MAX_SIZE)) {
            logger.debug("Parsing collection's max size");
            collectionDataType.setMaxSize(Bytes.toInt(value));
          }
          else if (Arrays.equals(infoKey, CELL_FIELD_COLLECTION_MIN_SIZE)) {
            logger.debug("Parsing collection's min size");
            collectionDataType.setMinSeize(Bytes.toInt(value));
          }
          else if (Arrays.equals(infoKey, CELL_FIELD_COLLECTION_ITEM_TYPE)) {
            logger.debug("Parsing collection's item data type");
            collectionDataType.setItemDataType(createDataType(FieldValueType.valueOf(Bytes.toString(value))));
          }
          else if (Bytes.startsWith(infoKey, CELL_FIELD_COLLECTION_ITEM)) {
            logger.debug("Parsing collection's item data type info");
            final String prefix = new StringBuilder(patternPrefix).append(COLLECTION_FIELD_ITEM_DATA_TYPE_PREFIX).
                toString();
            collectionDataType.setItemDataType(fillSpecialFields(prefix, key, collectionDataType.getItemDataType(),
                                                                 value));
          }
          break;
        case CONTENT:
          logger.debug("Parsing content");
          MutableContentDataType contentDataType =
                                 (MutableContentDataType) mutableDataType;
          if (Arrays.equals(infoKey, CELL_FIELD_CONTENT_TYPE_ID)) {
            logger.debug("Parsing content's item content id");
            contentDataType.setTypeDef(getInfoProvider().getIdFromRowId(value));
          }
          else if (Arrays.equals(infoKey, CELL_FIELD_CONTENT_BIDIRECTIONAL)) {
            logger.debug("Parsing content's bi-directional field name");
            contentDataType.setBiBidirectionalFieldName(Bytes.toString(value));
          }
          break;
        case STRING:
          logger.debug("Parsing string");
          MutableStringDataType stringDataType =
                                (MutableStringDataType) mutableDataType;
          if (Arrays.equals(infoKey, CELL_FIELD_STRING_ENCODING)) {
            logger.debug("Parsing String's encoding");
            stringDataType.setEncoding(Bytes.toString(value));
          }
        case OTHER:
          logger.debug("Parsing other");
          MutableOtherDataType otherDataType = (MutableOtherDataType) mutableDataType;
          if (Arrays.equals(infoKey, CELL_FIELD_OTHER_MIME_TYPE)) {
            logger.debug("Parsing other's mime type");
            otherDataType.setMIMEType(Bytes.toString(value));
          }
          break;
      }
    }
    else {
      logger.warn("Could not match field to any known field format! ");
    }
    return mutableDataType;
  }

  protected void fillResourceDef(final byte[] qualifier, final MutableResourceDef resourceDef, final byte[] value) {
    logger.debug("Filling resource!");
    if (Arrays.equals(qualifier, CELL_RSRC_NAME)) {
      logger.debug("Filling resource name");
      resourceDef.setName(Bytes.toString(value));
    }
    else if (Arrays.equals(qualifier, CELL_RSRC_MIME_TYPE)) {
      logger.debug("Filling resource mime type");
      resourceDef.setMIMEType(Bytes.toString(value));
    }
    else if (Arrays.equals(qualifier, CELL_RSRC_TEMPLATE)) {
      logger.debug("Filling resource template");
      resourceDef.setTemplateType(TemplateType.valueOf(Bytes.toString(value)));
    }
    else if (Arrays.equals(qualifier, CELL_RSRC_URI_TYPE)) {
      logger.debug("Filling resource uri type");
      final ResourceUri.Type valueOf = ResourceUri.Type.valueOf(Bytes.toString(value));
      MutableResourceUri uri = SmartContentAPI.getInstance().getContentTypeLoader().createMutableResourceUri();
      uri.setType(valueOf);
      final ResourceUri resourceUri = resourceDef.getResourceUri();
      if (resourceUri != null) {
        logger.debug("Set value from old resource uri");
        uri.setValue(resourceUri.getValue());
      }
      resourceDef.setResourceUri(uri);
    }
    else if (Arrays.equals(qualifier, CELL_RSRC_URI_VAL)) {
      logger.debug("Filling resource uri value");
      MutableResourceUri uri = SmartContentAPI.getInstance().getContentTypeLoader().createMutableResourceUri();
      final ResourceUri resourceUri = resourceDef.getResourceUri();
      if (resourceUri != null) {
        logger.debug("Set type from old resource uri");
        uri.setType(resourceUri.getType());
      }
      uri.setValue(Bytes.toString(value));
      resourceDef.setResourceUri(uri);
    }
  }
}
