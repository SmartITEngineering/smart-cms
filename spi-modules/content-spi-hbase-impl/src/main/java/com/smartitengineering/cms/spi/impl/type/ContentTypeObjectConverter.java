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

import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.common.MediaType;
import com.smartitengineering.cms.api.type.CollectionDataType;
import com.smartitengineering.cms.api.type.CompositeDataType;
import com.smartitengineering.cms.api.type.ContentCoProcessorDef;
import com.smartitengineering.cms.api.type.ContentDataType;
import com.smartitengineering.cms.api.type.ContentStatus;
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.api.type.DataType;
import com.smartitengineering.cms.api.type.EnumDataType;
import com.smartitengineering.cms.api.type.FieldDef;
import com.smartitengineering.cms.api.type.FieldValueType;
import com.smartitengineering.cms.api.type.MutableCollectionDataType;
import com.smartitengineering.cms.api.type.MutableCompositeDataType;
import com.smartitengineering.cms.api.type.MutableContentCoProcessorDef;
import com.smartitengineering.cms.api.type.MutableContentDataType;
import com.smartitengineering.cms.api.type.MutableContentStatus;
import com.smartitengineering.cms.api.type.MutableEnumDataType;
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
import com.smartitengineering.cms.api.type.ValidatorDef;
import com.smartitengineering.cms.api.type.VariationDef;
import com.smartitengineering.cms.spi.SmartContentSPI;
import com.smartitengineering.cms.spi.impl.hbase.Utils;
import com.smartitengineering.cms.spi.type.PersistableContentType;
import com.smartitengineering.dao.impl.hbase.spi.ExecutorService;
import com.smartitengineering.dao.impl.hbase.spi.impl.AbstractObjectRowConverter;
import edu.emory.mathcs.backport.java.util.Collections;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

/**
 *
 * @author imyousuf
 */
public class ContentTypeObjectConverter extends AbstractObjectRowConverter<PersistentContentType, ContentTypeId> {

  public static final String COMPOSITE_EMBED_SEPARATOR_STR = "embed";
  public static final String COMPOSITE_FIELDS_SEPARATOR_STR = "fields";
  public static final String CELL_PARAMS_PREFIX = "params";
  public static final String CELL_PARAMETERIZED_DISPLAY_NAME_PREFIX = "displayNames";
  public static final byte[] COMPOSITE_EMBED_SEPARATOR = Bytes.toBytes(COMPOSITE_EMBED_SEPARATOR_STR);
  public static final byte[] COMPOSITE_FIELDS_SEPARATOR = Bytes.toBytes(COMPOSITE_FIELDS_SEPARATOR_STR);
  public final static byte[] FAMILY_SIMPLE = Bytes.toBytes("simple");
  public final static byte[] FAMILY_FIELDS = Bytes.toBytes("fields");
  public final static byte[] FAMILY_REPRESENTATIONS = Bytes.toBytes("representations");
  public final static byte[] FAMILY_STATUSES = Bytes.toBytes("statuses");
  public final static byte[] FAMILY_TYPE_REPRESENTATIONS = Bytes.toBytes("variants");
  public final static byte[] FAMILY_CCP = Bytes.toBytes("ccp");
  public final static byte[] CELL_DISPLAY_NAME = Bytes.toBytes("displayName");
  public final static byte[] CELL_PRIMARY_FIELD_NAME = Bytes.toBytes("primaryFieldName");
  public final static byte[] CELL_DEF_TYPE = Bytes.toBytes("defType");
  public final static byte[] CELL_CREATION_DATE = Bytes.toBytes("creationDate");
  public final static byte[] CELL_LAST_MODIFIED_DATE = Bytes.toBytes("lastModifiedDate");
  public final static byte[] CELL_PARENT_ID = Bytes.toBytes("parent");
  public final static byte[] CELL_ENTITY_TAG = Bytes.toBytes("entityTag");
  public final static byte[] CELL_RSRC_NAME = Bytes.toBytes("name");
  public final static byte[] CELL_RSRC_MIME_TYPE = Bytes.toBytes("mimeType");
  public final static byte[] CELL_RSRC_URI_TYPE = Bytes.toBytes("resourceUri.type");
  public final static byte[] CELL_RSRC_URI_VAL = Bytes.toBytes("resourceUri.value");
  public final static byte[] CELL_CCP_PHASE = Bytes.toBytes("ccp.phase");
  public final static byte[] CELL_CCP_PRIORITY = Bytes.toBytes("ccp.priority");
  public final static byte[] CELL_FIELD_VAL_TYPE = Bytes.toBytes("fieldValType");
  public final static byte[] CELL_FIELD_STANDALONE = Bytes.toBytes("fieldStandalone");
  public final static byte[] CELL_FIELD_DISPLAY_NAME = Bytes.toBytes("fieldDisplayName");
  public final static byte[] CELL_FIELD_TYPE_CONTENT_AVAILABLE_FOR_SEARCH = Bytes.toBytes(
      "contentTypeFieldAvailableForSearch");
  public final static byte[] CELL_FIELD_REQUIRED = Bytes.toBytes("fieldRequired");
  public final static String CELL_FIELD_VAR_DEF = "fieldVariations";
  public final static String CELL_FIELD_VALIDATOR = "fieldValidator";
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
  public final static byte[] CELL_FIELD_ENUM_CHOICES = Bytes.toBytes("enumChoices");
  public final static byte[] COLON = Bytes.toBytes(":");
  public static final String SPCL_FIELD_DATA_TYPE_PATTERN = ":(" + FieldValueType.COLLECTION.name() + "|" +
      FieldValueType.CONTENT.name() + "|" + FieldValueType.OTHER.name() + "|" + FieldValueType.STRING.name() + "|" +
      FieldValueType.COMPOSITE.name() + "|" + FieldValueType.ENUM.name() + "):(.+)";
  public static final String COLLECTION_FIELD_ITEM_DATA_TYPE_PREFIX = ":COLLECTION:" + Bytes.toString(
      CELL_FIELD_COLLECTION_ITEM);
  private final ObjectMapper mapper = new ObjectMapper();

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
      final String primaryFieldName = instance.getMutableContentType().getPrimaryFieldName();
      if (logger.isInfoEnabled()) {
        logger.info("Primary field name being saved: " + primaryFieldName);
      }
      if (StringUtils.isNotBlank(primaryFieldName)) {
        put.add(FAMILY_SIMPLE, CELL_PRIMARY_FIELD_NAME, Bytes.toBytes(primaryFieldName));
      }
      if (instance.getMutableContentType().getSelfDefinitionType() != null) {
        put.add(FAMILY_SIMPLE, CELL_DEF_TYPE, Bytes.toBytes(instance.getMutableContentType().getSelfDefinitionType().
            name()));
      }
      put.add(FAMILY_SIMPLE, CELL_ENTITY_TAG, Bytes.toBytes(instance.getMutableContentType().getEntityTagValue()));
      final Date lastModifiedDate = date;
      logger.debug("Set creation date if necessary and set last modification date");
      put.add(FAMILY_SIMPLE, CELL_LAST_MODIFIED_DATE, Utils.toBytes(lastModifiedDate));
      final Date creationDate;
      if (instance.getMutableContentType().getCreationDate() != null) {
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
      putParams(put, FAMILY_SIMPLE, HConstants.EMPTY_BYTE_ARRAY, instance.getMutableContentType().
          getParameterizedDisplayNames(), CELL_PARAMETERIZED_DISPLAY_NAME_PREFIX);
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
       * Content Co-Processors
       */
      if (instance.getMutableContentType().getContentCoProcessorDefs().isEmpty()) {
        if (logger.isDebugEnabled()) {
          logger.debug("No content co processor for " + instance.getId());
        }
      }
      else {
        if (logger.isDebugEnabled()) {
          logger.debug("Found content co processor for " + instance.getId());
        }
        final byte[] ccpFamily = FAMILY_CCP;
        List<ContentCoProcessorDef> procDefs = new ArrayList<ContentCoProcessorDef>();
        if (instance.getMutableContentType().getContentCoProcessorDefs().containsKey(
            ContentType.ContentProcessingPhase.READ)) {
          procDefs.addAll(instance.getMutableContentType().getContentCoProcessorDefs().get(
              ContentType.ContentProcessingPhase.READ));
        }
        if (instance.getMutableContentType().getContentCoProcessorDefs().containsKey(
            ContentType.ContentProcessingPhase.WRITE)) {
          procDefs.addAll(instance.getMutableContentType().getContentCoProcessorDefs().get(
              ContentType.ContentProcessingPhase.WRITE));
        }
        for (ContentCoProcessorDef entry : procDefs) {
          if (logger.isDebugEnabled()) {
            logger.debug(new StringBuilder("Putting content co processor def for ").append(entry.getName()).append(
                " and value ").append(entry.toString()).toString());
          }
          final byte[] prefix = Bytes.add(Bytes.toBytes(entry.getName()), COLON);
          putResourceDef(put, ccpFamily, prefix, entry);
          put.add(ccpFamily, Bytes.add(prefix, CELL_CCP_PRIORITY), Bytes.toBytes(entry.getPriority()));
          put.add(ccpFamily, Bytes.add(prefix, CELL_CCP_PHASE), Bytes.toBytes(entry.getPhase().name()));
        }
      }
      /*
       * Fields
       */
      final Map<String, FieldDef> ownFieldDefs = instance.getMutableContentType().getOwnFieldDefs();
      putFields(ownFieldDefs, put);
      /*
       * Variants of content type
       */
      Map<MediaType, String> reps = instance.getMutableContentType().getRepresentations();
      for (MediaType type : reps.keySet()) {
        String data = reps.get(type);
        if (logger.isInfoEnabled()) {
          logger.debug("Putting data as variant for " + type.toString());
        }
        if (logger.isDebugEnabled()) {
          logger.debug("Putting " + data + " as variant for " + type.toString());
        }
        put.add(FAMILY_TYPE_REPRESENTATIONS, Bytes.toBytes(type.toString()), Bytes.toBytes(data));
      }
    }
    catch (Exception ex) {
      logger.warn("Error converting content type to Put throwing exception...", ex);
      throw new RuntimeException(ex);
    }
  }

  protected void putFields(final Map<String, FieldDef> ownFieldDefs, Put put, byte... prefix) throws IOException {
    for (Entry<String, FieldDef> entry : ownFieldDefs.entrySet()) {
      if (logger.isDebugEnabled()) {
        logger.debug(new StringBuilder("Putting field with name ").append(entry.getKey()).append(" and value ").append(entry.
            getValue().toString()).toString());
      }
      final FieldDef value = entry.getValue();
      final byte[] toBytes = Bytes.add(((prefix == null) ? HConstants.EMPTY_BYTE_ARRAY : prefix), Bytes.toBytes(entry.
          getKey()), COLON);
      /*
       * Simple field values
       */
      logger.debug("Putting simple field values");
      put.add(FAMILY_FIELDS, Bytes.add(toBytes, CELL_FIELD_STANDALONE), Bytes.toBytes(
          value.isFieldStandaloneUpdateAble()));
      put.add(FAMILY_FIELDS, Bytes.add(toBytes, CELL_FIELD_REQUIRED), Bytes.toBytes(value.isRequired()));
      if (StringUtils.isNotBlank(value.getDisplayName())) {
        put.add(FAMILY_FIELDS, Bytes.add(toBytes, CELL_FIELD_DISPLAY_NAME), Bytes.toBytes(value.getDisplayName()));
      }
      putParams(put, FAMILY_FIELDS, toBytes, value.getParameters());
      putParams(put, FAMILY_FIELDS, toBytes, value.getParameterizedDisplayNames(),
                CELL_PARAMETERIZED_DISPLAY_NAME_PREFIX);
      /*
       * Variations
       */
      Collection<VariationDef> varDefs = value.getVariations().values();
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
      Collection<ValidatorDef> validatorDefs = value.getCustomValidators();
      if (validatorDefs != null && !validatorDefs.isEmpty()) {
        int index = 0;
        for (ValidatorDef validatorDef : validatorDefs) {
          if (validatorDef != null) {
            logger.debug("Put custom validator for field");
            final byte[] validatorPrefix = Bytes.add(toBytes, Bytes.toBytes(new StringBuilder(CELL_FIELD_VALIDATOR).
                append(':').append(index++).append(':').toString()));
            putResourceUri(put, FAMILY_FIELDS, validatorPrefix, validatorDef.getUri());
            putParams(put, FAMILY_FIELDS, validatorPrefix, validatorDef.getParameters());
          }
        }
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
        put.add(FAMILY_FIELDS, Bytes.add(prefix, CELL_FIELD_TYPE_CONTENT_AVAILABLE_FOR_SEARCH), Bytes.toBytes(String.
            valueOf(contentDataType.isAvaialbleForSearch())));
        put.add(FAMILY_FIELDS, Bytes.add(prefix, CELL_FIELD_CONTENT_TYPE_ID),
                getInfoProvider().getRowIdFromId(contentDataType.getTypeDef()));
        break;
      case COMPOSITE: {
        CompositeDataType compositeDataType = (CompositeDataType) valueDef;
        contentDataType = compositeDataType.getEmbeddedContentType();
        if (contentDataType != null) {
          byte[] contentPrefix = Bytes.add(prefix, COMPOSITE_EMBED_SEPARATOR, COLON);
          handleSpecialDataTypes(put, contentPrefix, contentDataType);
        }
        Collection<FieldDef> defs = compositeDataType.getOwnComposition();
        if (defs != null && !defs.isEmpty()) {
          LinkedHashMap<String, FieldDef> defMap = new LinkedHashMap<String, FieldDef>(defs.size());
          for (FieldDef def : defs) {
            defMap.put(def.getName(), def);
          }
          byte[] fieldPrefix = Bytes.add(prefix, COMPOSITE_FIELDS_SEPARATOR, COLON);
          putFields(defMap, put, fieldPrefix);
        }
        break;
      }
      case STRING:
        logger.debug("Working with STRING Special data type");
        StringDataType stringDataType = (StringDataType) valueDef;
        final String encoding = stringDataType.getEncoding();
        if (StringUtils.isNotBlank(encoding)) {
          put.add(FAMILY_FIELDS, Bytes.add(prefix, CELL_FIELD_STRING_ENCODING),
                  Bytes.toBytes(encoding));
        }
      case OTHER:
        logger.debug("Working with OTHER Special data type");
        OtherDataType otherDataType = (OtherDataType) valueDef;
        put.add(FAMILY_FIELDS, Bytes.add(prefix, CELL_FIELD_OTHER_MIME_TYPE), Bytes.toBytes(otherDataType.getMIMEType()));
        break;
      case ENUM:
        logger.debug("Working with ENUM Special data type");
        EnumDataType enumDataType = (EnumDataType) valueDef;
        put.add(FAMILY_FIELDS, Bytes.add(prefix, CELL_FIELD_ENUM_CHOICES), Bytes.toBytes(mapper.writeValueAsString(
            enumDataType.getChoices())));
        break;
    }
  }

  protected void putResourceDef(Put put, final byte[] family, final byte[] prefix, final ResourceDef value) {
    put.add(family, Bytes.add(prefix, CELL_RSRC_NAME), Bytes.toBytes(value.getName()));
    if (StringUtils.isNotBlank(value.getMIMEType())) {
      put.add(family, Bytes.add(prefix, CELL_RSRC_MIME_TYPE), Bytes.toBytes(value.getMIMEType()));
    }
    final ResourceUri resourceUri = value.getResourceUri();
    putResourceUri(put, family, prefix, resourceUri);
    putParams(put, family, prefix, value.getParameters());
  }

  protected void putResourceUri(Put put, final byte[] family, final byte[] prefix, final ResourceUri resourceUri) {
    put.add(family, Bytes.add(prefix, CELL_RSRC_URI_TYPE), Bytes.toBytes(resourceUri.getType().name()));
    put.add(family, Bytes.add(prefix, CELL_RSRC_URI_VAL), Bytes.toBytes(resourceUri.getValue()));
  }

  protected void putParams(Put put, final byte[] family, final byte[] prefix, final Map<String, String> params) {
    final String paramsPrefixStr = CELL_PARAMS_PREFIX;
    putParams(put, family, prefix, params, paramsPrefixStr);
  }

  protected void putParams(Put put, final byte[] family, final byte[] prefix, final Map<String, String> params,
                           final String paramsPrefixStr) {
    final byte[] paramsPrefix = Bytes.add(prefix, Bytes.toBytes(new StringBuilder(paramsPrefixStr).append(":").
        toString()));
    if (params != null && !params.isEmpty()) {
      for (Entry<String, String> param : params.entrySet()) {
        final byte[] qualifier = Bytes.add(paramsPrefix, Bytes.toBytes(param.getKey()));
        if (logger.isInfoEnabled()) {
          logger.info("Putting params " + Bytes.toString(qualifier) + " - " + param.getValue());
        }
        put.add(family, qualifier, Bytes.toBytes(param.getValue()));
      }
    }
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
      logger.info("::::::::::::::::::::: Converting rowId to ContentTypeId :::::::::::::::::::::");
      contentType.setContentTypeID(getInfoProvider().getIdFromRowId(startRow.getRow()));
      if (logger.isInfoEnabled()) {
        logger.info("ContentTypeId " + contentType.getContentTypeID());
      }
      Map<byte[], byte[]> simpleValues = startRow.getFamilyMap(FAMILY_SIMPLE);
      byte[] displayName = simpleValues.remove(CELL_DISPLAY_NAME);
      if (displayName != null) {
        logger.debug("Set display name of the content type!");
        contentType.setDisplayName(Bytes.toString(displayName));
      }
      byte[] primaryFieldName = simpleValues.remove(CELL_PRIMARY_FIELD_NAME);
      if (primaryFieldName != null) {
        final String toString = Bytes.toString(primaryFieldName);
        if (logger.isInfoEnabled()) {
          logger.info("Set primary field name of the content type!" + toString);
        }
        contentType.setPrimaryFieldName(toString);
      }
      byte[] defTyoe = simpleValues.remove(CELL_DEF_TYPE);
      if (defTyoe != null) {
        final String toString = Bytes.toString(defTyoe);
        if (logger.isInfoEnabled()) {
          logger.info("Set primary field name of the content type!" + toString);
        }
        contentType.setDefinitionType(ContentType.DefinitionType.valueOf(toString));
      }
      contentType.setEntityTagValue(Bytes.toString(simpleValues.remove(CELL_ENTITY_TAG)));
      logger.debug("Setting creation and last modified date");
      contentType.setCreationDate(Utils.toDate(simpleValues.remove(CELL_CREATION_DATE)));
      contentType.setLastModifiedDate(Utils.toDate(simpleValues.remove(CELL_LAST_MODIFIED_DATE)));
      final byte[] parentId = simpleValues.remove(CELL_PARENT_ID);
      if (parentId != null) {
        logger.debug("Setting parent id");
        contentType.setParent(getInfoProvider().getIdFromRowId(parentId));
      }
      if (!simpleValues.isEmpty()) {
        String displayNamesPrefix = new StringBuilder(CELL_PARAMETERIZED_DISPLAY_NAME_PREFIX).append(':').toString();
        final byte[] toBytes = Bytes.toBytes(CELL_PARAMETERIZED_DISPLAY_NAME_PREFIX);
        for (Entry<byte[], byte[]> entry : simpleValues.entrySet()) {
          if (logger.isInfoEnabled()) {
            logger.info("Extra simple fields Key " + Bytes.toString(entry.getKey()) + " " + Bytes.startsWith(entry.
                getKey(), toBytes));
            logger.info("Extra simple fields Value " + Bytes.toString(entry.getValue()));
          }
          if (Bytes.startsWith(entry.getKey(), toBytes)) {
            String paramKey = Bytes.toString(entry.getKey()).substring(displayNamesPrefix.length());
            String paramVal = Bytes.toString(entry.getValue());
            contentType.getMutableParameterizedDisplayNames().put(paramKey, paramVal);
          }
        }
      }
      if (logger.isDebugEnabled()) {
        final FastDateFormat formatter = DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT;
        logger.debug(String.format("Creation date is %s and last modified date is %s", formatter.format(contentType.
            getCreationDate()), formatter.format(contentType.getLastModifiedDate())));
        logger.debug(String.format("Id is %s and parent id is %s", contentType.getContentTypeID().toString(),
                                   ObjectUtils.toString(contentType.getParent())));
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
       * Content Co-Processors
       */
      logger.info("Form Content Co-Processors!");
      NavigableMap<byte[], byte[]> ccpMap = startRow.getFamilyMap(FAMILY_CCP);
      Map<String, MutableContentCoProcessorDef> ccps = new HashMap<String, MutableContentCoProcessorDef>();
      for (byte[] keyBytes : ccpMap.navigableKeySet()) {
        final String key = Bytes.toString(keyBytes);
        if (logger.isDebugEnabled()) {
          logger.debug(new StringBuilder("CCP Work with key ").append(key).toString());
        }
        final int indexOfFirstColon = key.indexOf(':');
        final String ccpName = key.substring(0, indexOfFirstColon);
        final byte[] qualifier = Bytes.toBytes(key.substring(indexOfFirstColon + 1));
        if (logger.isDebugEnabled()) {
          logger.debug(new StringBuilder("CCP name ").append(ccpName).toString());
          logger.debug(new StringBuilder("CCP qualifier ").append(Bytes.toString(qualifier)).toString());
        }
        MutableContentCoProcessorDef ccpDef = ccps.get(ccpName);
        if (ccpDef == null) {
          logger.debug("Creating new content co processor def and putting to map");
          ccpDef = SmartContentAPI.getInstance().getContentTypeLoader().createMutableContentCoProcessorDef();
          ccps.put(ccpName, ccpDef);
          ccpDef.setName(ccpName);
        }
        final byte[] value = ccpMap.get(keyBytes);
        if (Arrays.equals(qualifier, CELL_CCP_PHASE)) {
          ccpDef.setPhase(ContentType.ContentProcessingPhase.valueOf(Bytes.toString(value)));
        }
        else if (Arrays.equals(qualifier, CELL_CCP_PRIORITY)) {
          ccpDef.setPriority(Bytes.toInt(value));
        }
        else {
          fillResourceDef(qualifier, ccpDef, value);
        }
      }
      List<MutableContentCoProcessorDef> ccpDefs = new ArrayList<MutableContentCoProcessorDef>(ccps.values());
      Collections.sort(ccpDefs, new Comparator<ContentCoProcessorDef>() {

        public int compare(ContentCoProcessorDef o1, ContentCoProcessorDef o2) {
          return o1.getPriority() - o2.getPriority();
        }
      });
      for (MutableContentCoProcessorDef ccpDef : ccpDefs) {
        contentType.addContentCoProcessorDef(ccpDef);
      }
      /*
       * Fields
       */
      NavigableMap<byte[], byte[]> fieldMap = startRow.getFamilyMap(FAMILY_FIELDS);
      //From a map of all cells form a map of cells by field name
      Map<String, Map<String, byte[]>> fieldsByName = new LinkedHashMap<String, Map<String, byte[]>>();
      Utils.organizeByPrefix(fieldMap, fieldsByName, ':');
      for (String fieldName : fieldsByName.keySet()) {
        final MutableFieldDef fieldDef = SmartContentAPI.getInstance().getContentTypeLoader().createMutableFieldDef();
        final Map<String, byte[]> fieldCells = fieldsByName.get(fieldName);
        populateFieldDef(fieldDef, fieldName, fieldCells);
        contentType.getMutableFieldDefs().add(fieldDef);
      }
      /*
       * Variants of content type
       */
      Map<byte[], byte[]> variants = startRow.getFamilyMap(FAMILY_TYPE_REPRESENTATIONS);
      if (variants != null && !variants.isEmpty()) {
        final Map<MediaType, String> variantMap = new HashMap<MediaType, String>(variants.size());
        for (byte[] mediaType : variants.keySet()) {
          variantMap.put(MediaType.fromString(Bytes.toString(mediaType)), Bytes.toString(variants.get(mediaType)));
        }
        contentType.setRepresentations(variantMap);
        contentType.setFromPersistentStorage(true);
      }
      return persistentContentType;
    }
    catch (Exception ex) {
      logger.warn("Error converting result to content type, throwing exception...", ex);
      throw new RuntimeException(ex);
    }
  }

  private CompositeStatus distinguishCompositeFields(Map<String, byte[]> fieldCells,
                                                     final Map<String, byte[]> compositeFields,
                                                     final String fieldName) {
    final String directCompositeFieldPrefix = new StringBuilder(fieldName).append(':').append(Bytes.toString(
        CELL_FIELD_VAL_TYPE)).append(':').append(FieldValueType.COMPOSITE).append(':').append(
        COMPOSITE_FIELDS_SEPARATOR_STR).append(':').toString();
    final String collectionCompositeFieldPrefix = new StringBuilder(fieldName).append(':').append(Bytes.toString(
        CELL_FIELD_VAL_TYPE)).append(COLLECTION_FIELD_ITEM_DATA_TYPE_PREFIX).append(':').append(FieldValueType.COMPOSITE).
        append(':').append(COMPOSITE_FIELDS_SEPARATOR_STR).append(':').toString();
    if (logger.isInfoEnabled()) {
      logger.info("Prefixes being checked " + directCompositeFieldPrefix + " and " + collectionCompositeFieldPrefix);
    }
    CompositeStatus status = CompositeStatus.NOT_COMPOSITE;
    Iterator<Entry<String, byte[]>> cells = fieldCells.entrySet().iterator();
    while (cells.hasNext()) {
      Entry<String, byte[]> cell = cells.next();
      boolean isCompositeCell = false;
      String cutStr = "";
      if (cell.getKey().startsWith(directCompositeFieldPrefix)) {
        status = CompositeStatus.DIRECT_COMPOSITE;
        cutStr = directCompositeFieldPrefix;
        isCompositeCell = true;
      }
      if (cell.getKey().startsWith(collectionCompositeFieldPrefix)) {
        status = CompositeStatus.COMPOSITE_AS_COLLECTION_ITEM;
        cutStr = collectionCompositeFieldPrefix;
        isCompositeCell = true;
      }
      final String substring = cell.getKey().substring(cutStr.length());
      if (logger.isInfoEnabled()) {
        logger.info("Key " + cell.getKey());
        logger.info("Cut Str " + cutStr);
        logger.info("Is Composite " + isCompositeCell);
        logger.info("Composite field key " + substring);
      }
      if (isCompositeCell) {
        compositeFields.put(substring, cell.getValue());
        cells.remove();
      }
    }
    return status;
  }

  private enum CompositeStatus {

    NOT_COMPOSITE,
    DIRECT_COMPOSITE,
    COMPOSITE_AS_COLLECTION_ITEM
  }

  protected void populateFieldDef(final MutableFieldDef fieldDef, String fieldName,
                                  final Map<String, byte[]> fieldCells) throws ClassNotFoundException,
                                                                               IllegalArgumentException,
                                                                               RuntimeException, IOException {
    if (logger.isInfoEnabled()) {
      logger.info("::::::::::::::::::::: POPULATING FIELD " + fieldName + " :::::::::::::::::::::");
    }
    final Map<Integer, MutableVariationDef> fieldVariations = new TreeMap<Integer, MutableVariationDef>();
    final MutableSearchDef searchDef = SmartContentAPI.getInstance().getContentTypeLoader().createMutableSearchDef();
    final Map<Integer, MutableValidatorDef> validatorDefs = new TreeMap<Integer, MutableValidatorDef>();
    DataType mutableDataType = null;
    fieldDef.setName(fieldName);
    final Map<String, byte[]> compositeFields = new LinkedHashMap<String, byte[]>();
    final Map<String, String> fieldParams = new LinkedHashMap<String, String>();
    CompositeStatus compositeStatus = distinguishCompositeFields(fieldCells, compositeFields, fieldName);
    if (logger.isInfoEnabled()) {
      logger.info("Composition status " + compositeStatus.name());
      logger.info("Composed fields " + compositeFields);
    }
    final String validatorPatternString = new StringBuilder(fieldName).append(':').append(CELL_FIELD_VALIDATOR).
        append(":([\\d]+):(.*)").toString();
    if (logger.isDebugEnabled()) {
      logger.debug(new StringBuilder("Using following pattern to identify validator: ").append(
          validatorPatternString).toString());
    }
    Pattern validatorPattern = Pattern.compile(validatorPatternString);
    final String paramsPatternString = new StringBuilder(fieldName).append(':').append(CELL_PARAMS_PREFIX).
        append(":(.+)").toString();
    if (logger.isDebugEnabled()) {
      logger.debug(new StringBuilder("Using following pattern to identify params: ").append(
          paramsPatternString).toString());
    }
    Pattern paramsPattern = Pattern.compile(paramsPatternString);
    final String displayNamesPatternStr = new StringBuilder(fieldName).append(':').append(
        CELL_PARAMETERIZED_DISPLAY_NAME_PREFIX).append(":(.+)").toString();
    if (logger.isDebugEnabled()) {
      logger.debug(new StringBuilder("Using following pattern to identify params: ").append(
          displayNamesPatternStr).toString());
    }
    Pattern displayNamesPattern = Pattern.compile(displayNamesPatternStr);
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
    for (Entry<String, byte[]> cell : fieldCells.entrySet()) {
      final String key = cell.getKey();
      final byte[] value = cell.getValue();
      if (logger.isDebugEnabled()) {
        logger.debug(new StringBuilder("Matching following key against the patterns: ").append(key).toString());
        logger.debug(new StringBuilder("Cell value: ").append(Bytes.toString(value)).toString());
      }
      final Matcher searchDefMatcher = searchDefPattern.matcher(key);
      final Matcher variationsDefMatcher = variationsDefPattern.matcher(key);
      final Matcher validatorMatcher = validatorPattern.matcher(key);
      final Matcher paramsMatcher = paramsPattern.matcher(key);
      final Matcher displayNamesMatcher = displayNamesPattern.matcher(key);

      if (displayNamesMatcher.matches()) {
        final String paramKey = displayNamesMatcher.group(1);
        final String paramValue = Bytes.toString(cell.getValue());
        fieldDef.getMutableParameterizedDisplayNames().put(paramKey, paramValue);
      }
      else if (paramsMatcher.matches()) {
        final String paramKey = paramsMatcher.group(1);
        final String paramValue = Bytes.toString(cell.getValue());
        fieldParams.put(paramKey, paramValue);
      }
      /*
       * Search Def
       */
      else if (searchDefMatcher.matches()) {
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
        Integer indexInt = NumberUtils.toInt(validatorMatcher.group(1), -1);
        MutableValidatorDef validatorDef = null;
        if (indexInt > -1) {
          validatorDef = validatorDefs.get(indexInt);
          if (validatorDef == null) {
            validatorDef = SmartContentAPI.getInstance().getContentTypeLoader().createMutableValidatorDef();
            validatorDefs.put(indexInt, validatorDef);
          }
        }
        if (validatorDef != null) {
          String validatorCell = validatorMatcher.group(2);
          if (logger.isInfoEnabled()) {
            logger.info("Validator Cell " + validatorCell);
          }
          byte[] validatorCellBytes = Bytes.toBytes(validatorCell);
          if (Arrays.equals(validatorCellBytes, CELL_RSRC_URI_TYPE)) {
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
          else if (StringUtils.isNotBlank(validatorCell) && validatorCell.startsWith(CELL_PARAMS_PREFIX) &&
              validatorCell.indexOf(
              ':') > -1) {
            logger.info("Match params");
            String paramKey = validatorCell.split(":")[1];
            String paramVal = Bytes.toString(value);
            Map<String, String> params = new LinkedHashMap<String, String>(validatorDef.getParameters());
            params.put(paramKey, paramVal);
            if (logger.isInfoEnabled()) {
              logger.info("Key " + paramKey);
              logger.info("Val " + paramVal);
              logger.info("Setting params " + params);
            }
            validatorDef.setParameters(params);
          }
          else {
            logger.warn("Found validator key not matching anything!");
          }
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
        if (Arrays.equals(CELL_FIELD_DISPLAY_NAME, qualifier)) {
          logger.debug("Its the field's display name");
          fieldDef.setDisplayName(Bytes.toString(value));
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
                Bytes.toString(CELL_FIELD_VAL_TYPE)).toString();
            mutableDataType = fillSpecialFields(specialFieldPatternPrefix, key, mutableDataType, value, fieldDef);
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
    /**
     * Work with composed fields
     */
    MutableCompositeDataType compositeDataType = null;
    switch (compositeStatus) {
      case NOT_COMPOSITE:
        // Do nothing
        break;
      case DIRECT_COMPOSITE:
        compositeDataType = (MutableCompositeDataType) mutableDataType;
        break;
      case COMPOSITE_AS_COLLECTION_ITEM:
        compositeDataType = (MutableCompositeDataType) ((CollectionDataType) mutableDataType).getItemDataType();
        break;
    }
    if (compositeDataType != null) {
      Map<String, Map<String, byte[]>> compositeFieldsMap = new LinkedHashMap<String, Map<String, byte[]>>();
      Utils.organizeByPrefixOnString(compositeFields, compositeFieldsMap, ':');
      for (String composedFieldName : compositeFieldsMap.keySet()) {
        final MutableFieldDef composedFieldDef = SmartContentAPI.getInstance().getContentTypeLoader().
            createMutableFieldDef(fieldDef);
        final Map<String, byte[]> composedFieldCells = compositeFieldsMap.get(composedFieldName);
        if (logger.isInfoEnabled()) {
          logger.info("::::::::::::::::::::: Nested composite field from " + fieldName + " for " + composedFieldName +
              " :::::::::::::::::::::");
        }
        populateFieldDef(composedFieldDef, composedFieldName, composedFieldCells);
        compositeDataType.getOwnMutableComposition().add(composedFieldDef);
      }
    }
    logger.info("Set all fields into the field definition!");
    if (!validatorDefs.isEmpty()) {
      fieldDef.setCustomValidators(validatorDefs.values());
    }
    if (searchDef.isIndexed() || searchDef.isStored() || StringUtils.isNotBlank(searchDef.getBoostConfig())) {
      fieldDef.setSearchDefinition(searchDef);
    }
    if (!fieldVariations.isEmpty()) {
      fieldDef.setVariations(fieldVariations.values());
    }
    fieldDef.setParameters(fieldParams);
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
      case COMPOSITE:
        mutableDataType = SmartContentAPI.getInstance().getContentTypeLoader().createMutableCompositeDataType();
        break;
      case ENUM:
        mutableDataType = SmartContentAPI.getInstance().getContentTypeLoader().createMutableEnumDataType();
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
                                       final byte[] value, FieldDef def) throws ClassNotFoundException, IOException {
    final String specialFieldPatternString = new StringBuilder(patternPrefix).append(
        SPCL_FIELD_DATA_TYPE_PATTERN).
        toString();
    Pattern pattern = Pattern.compile(specialFieldPatternString);
    Matcher matcher = pattern.matcher(key);
    if (matcher.matches()) {

      FieldValueType type = FieldValueType.valueOf(matcher.group(1));
      String specialFieldValueTypeInfoKey = matcher.group(2);
      if (logger.isDebugEnabled()) {
        logger.debug(new StringBuilder("Identified type as ").append(type).toString());
        logger.debug(new StringBuilder("The field value type's info key ").append(specialFieldValueTypeInfoKey).
            toString());
      }
      if (mutableDataType == null) {
        mutableDataType = createDataType(type);
        logger.debug("Created mutable data type for special field");
      }
      byte[] infoKey = Bytes.toBytes(specialFieldValueTypeInfoKey);
      if (mutableDataType == null) {
        mutableDataType = createDataType(type);
        logger.debug("Created mutable data type for special field");
      }
      switch (type) {
        case COMPOSITE:
          MutableCompositeDataType compositeDataType = (MutableCompositeDataType) mutableDataType;

          if (specialFieldValueTypeInfoKey.startsWith(COMPOSITE_EMBED_SEPARATOR_STR)) {
            MutableContentDataType contentDataType = (MutableContentDataType) compositeDataType.getEmbeddedContentType();
            if (contentDataType == null) {
              contentDataType = (MutableContentDataType) createDataType(FieldValueType.CONTENT);
              if (contentDataType != null) {
                compositeDataType.setEmbeddedContentDataType(SmartContentAPI.getInstance().getContentLoader().
                    createEmbeddedContentDataType(def, contentDataType));
              }
            }
            final String prefix = "test";
            String dummyKey = new StringBuilder(prefix).append(':').
                append(specialFieldValueTypeInfoKey.substring(COMPOSITE_EMBED_SEPARATOR_STR.length() + 1)).toString();
            MutableContentDataType returnedContentDataType = (MutableContentDataType) fillSpecialFields(prefix, dummyKey,
                                                                                                        contentDataType,
                                                                                                        value, def);
            if (contentDataType != returnedContentDataType) {
              if (returnedContentDataType != null) {
                compositeDataType.setEmbeddedContentDataType(SmartContentAPI.getInstance().getContentLoader().
                    createEmbeddedContentDataType(def, returnedContentDataType));
              }
            }
          }
          break;
        case COLLECTION:
          logger.debug("Parsing collection");
          MutableCollectionDataType collectionDataType = (MutableCollectionDataType) mutableDataType;
          if (Arrays.equals(infoKey, CELL_FIELD_COLLECTION_MAX_SIZE)) {
            final int toInt = Bytes.toInt(value);
            logger.debug("Parsing collection's max size " + toInt);
            collectionDataType.setMaxSize(toInt);
          }
          else if (Arrays.equals(infoKey, CELL_FIELD_COLLECTION_MIN_SIZE)) {
            final int toInt = Bytes.toInt(value);
            logger.debug("Parsing collection's min size " + toInt);
            collectionDataType.setMinSize(toInt);
          }
          else if (Arrays.equals(infoKey, CELL_FIELD_COLLECTION_ITEM_TYPE)) {
            logger.debug("Parsing collection's item data type");
            if (collectionDataType.getItemDataType() == null) {
              collectionDataType.setItemDataType(createDataType(FieldValueType.valueOf(Bytes.toString(value))));
            }
          }
          else if (Bytes.startsWith(infoKey, CELL_FIELD_COLLECTION_ITEM)) {
            logger.debug("Parsing collection's item data type info");
            final String prefix = new StringBuilder(patternPrefix).append(COLLECTION_FIELD_ITEM_DATA_TYPE_PREFIX).
                toString();
            collectionDataType.setItemDataType(fillSpecialFields(prefix, key, collectionDataType.getItemDataType(),
                                                                 value, def));
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
          else if (Arrays.equals(infoKey, CELL_FIELD_TYPE_CONTENT_AVAILABLE_FOR_SEARCH)) {
            logger.debug("Parsing content's available for search");
            contentDataType.setAvailableForSearch(Boolean.valueOf(Bytes.toString(value)));
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
        case ENUM: {
          logger.debug("Parsing ENUM");
          MutableEnumDataType enumDataType = (MutableEnumDataType) mutableDataType;
          if (Arrays.equals(infoKey, CELL_FIELD_ENUM_CHOICES)) {
            logger.debug("Parsing Enum Choices");
            final String stringVal = Bytes.toString(value);
            List<String> list = mapper.readValue(stringVal, new TypeReference<List<String>>() {
            });
            enumDataType.setChoices(list);
          }
          break;
        }
      }
    }
    else {
      logger.warn("Could not match field to any known field format! ");
    }
    return mutableDataType;
  }

  protected void fillResourceDef(final byte[] qualifier, final MutableResourceDef resourceDef, final byte[] value) {
    String qualifierStr = Bytes.toString(qualifier);
    logger.debug("Filling resource!");
    if (Arrays.equals(qualifier, CELL_RSRC_NAME)) {
      logger.debug("Filling resource name");
      resourceDef.setName(Bytes.toString(value));
    }
    else if (Arrays.equals(qualifier, CELL_RSRC_MIME_TYPE)) {
      logger.debug("Filling resource mime type");
      resourceDef.setMIMEType(Bytes.toString(value));
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
    else if (StringUtils.isNotBlank(qualifierStr) && qualifierStr.startsWith(CELL_PARAMS_PREFIX) &&
        qualifierStr.indexOf(':') >
        -1) {
      String paramKey = qualifierStr.split(":")[1];
      String paramVal = Bytes.toString(value);
      Map<String, String> params = new LinkedHashMap<String, String>(resourceDef.getParameters());
      params.put(paramKey, paramVal);
      resourceDef.setParameters(params);
    }
  }
}
