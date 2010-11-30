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
package com.smartitengineering.cms.type.xml;

import com.smartitengineering.cms.api.impl.type.CollectionDataTypeImpl;
import com.smartitengineering.cms.api.impl.type.ContentDataTypeImpl;
import com.smartitengineering.cms.api.impl.type.ContentStatusImpl;
import com.smartitengineering.cms.api.impl.type.ContentTypeIdImpl;
import com.smartitengineering.cms.api.impl.type.FieldDefImpl;
import com.smartitengineering.cms.api.impl.type.OtherDataTypeImpl;
import com.smartitengineering.cms.api.impl.type.RepresentationDefImpl;
import com.smartitengineering.cms.api.impl.type.ResourceUriImpl;
import com.smartitengineering.cms.api.impl.type.SearchDefImpl;
import com.smartitengineering.cms.api.impl.type.StringDataTypeImpl;
import com.smartitengineering.cms.api.impl.type.ValidatorDefImpl;
import com.smartitengineering.cms.api.impl.type.VariationDefImpl;
import com.smartitengineering.cms.api.type.CollectionDataType;
import com.smartitengineering.cms.api.type.ContentDataType;
import com.smartitengineering.cms.api.type.ContentStatus;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.api.type.DataType;
import com.smartitengineering.cms.api.type.FieldDef;
import com.smartitengineering.cms.api.type.MutableCollectionDataType;
import com.smartitengineering.cms.api.type.MutableContentDataType;
import com.smartitengineering.cms.api.type.MutableContentStatus;
import com.smartitengineering.cms.api.type.MutableContentType;
import com.smartitengineering.cms.api.type.MutableFieldDef;
import com.smartitengineering.cms.api.type.MutableOtherDataType;
import com.smartitengineering.cms.api.type.MutableRepresentationDef;
import com.smartitengineering.cms.api.type.MutableResourceUri;
import com.smartitengineering.cms.api.type.MutableSearchDef;
import com.smartitengineering.cms.api.type.MutableStringDataType;
import com.smartitengineering.cms.api.type.MutableValidatorDef;
import com.smartitengineering.cms.api.type.MutableVariationDef;
import com.smartitengineering.cms.api.type.RepresentationDef;
import com.smartitengineering.cms.api.type.ResourceUri;
import com.smartitengineering.cms.api.type.SearchDef;
import com.smartitengineering.cms.api.type.ValidatorDef;
import com.smartitengineering.cms.api.type.ValidatorType;
import com.smartitengineering.cms.api.type.VariationDef;
import com.smartitengineering.cms.api.workspace.WorkspaceId;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author kaisar
 */
public class XmlParser implements XmlConstants {

  private final InputStream source;
  private final WorkspaceId workspaceId;
  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final XMLParserIntrospector introspector;

  public XmlParser(WorkspaceId workspaceId, InputStream stream, XMLParserIntrospector introspector) {
    if (stream == null || workspaceId == null || introspector == null) {
      throw new IllegalArgumentException("Source stream or workspace id or instrospector can not be null!");
    }
    this.workspaceId = workspaceId;
    this.source = stream;
    this.introspector = introspector;
  }

  public Collection<MutableContentType> parse() {

    ContentTypeId contentTypeId = null;
    Collection<FieldDef> fieldDefs = new ArrayList<FieldDef>();
    Collection<MutableContentType> contentTypes = new ArrayList<MutableContentType>();
    Collection<RepresentationDef> representationDefs = new ArrayList<RepresentationDef>();
    Collection<ContentStatus> statuses = new ArrayList<ContentStatus>();
    String displayName = null;
    try {
      Builder builder = new Builder(false);
      Document document = builder.build(this.source);
      Element rootElement = document.getRootElement();
      Elements childRootElements = rootElement.getChildElements();
      for (int j = 0; j < childRootElements.size(); j++) {
        MutableContentType mutableContent = introspector.createMutableContentType();
        final Element contentTypeElement = childRootElements.get(j);
        Elements childElements = contentTypeElement.getChildElements();
        String name = parseMandatoryStringElement(contentTypeElement, NAME); //max=1,min=1
        String namespace = parseAttribute(contentTypeElement, ATTR_NAMESPACE);
        if (logger.isDebugEnabled()) {
          logger.debug(new StringBuilder("Iterating over ").append(j).toString());
          logger.debug(new StringBuilder("Namespace ").append(namespace).toString());
          logger.debug(new StringBuilder("Name ").append(name).toString());
        }
        contentTypeId = getContentTypeId(workspaceId, namespace, name);
        mutableContent.setContentTypeID(contentTypeId);
        displayName = parseOptionalStringElement(contentTypeElement, DISPLAY_NAME); //min=0,max=1
        for (int child = 0; child < childElements.size(); child++) {//fields min=1,max=unbounted
          if (StringUtils.equalsIgnoreCase(childElements.get(child).getLocalName(), FIELDS)) {
            fieldDefs.addAll(parseFieldDefs(childElements.get(child)));
          }
        }
        statuses = parseContentStatuses(contentTypeElement, STATUS);
        representationDefs = parseRepresentations(contentTypeElement, REPRESENTATIONS);
        contentTypeId = parseContentTypeId(contentTypeElement, PARENT, workspaceId);
        String primaryFieldName = parseOptionalStringElement(contentTypeElement, PRIMARY_FIELD);
        if (logger.isInfoEnabled()) {
          logger.info("Primary field parsed: " + primaryFieldName);
        }
        if (StringUtils.isNotBlank(primaryFieldName)) {
          mutableContent.setPrimaryFieldName(primaryFieldName);
        }
        mutableContent.setDisplayName(displayName);
        mutableContent.setParent(contentTypeId);
        mutableContent.getMutableFieldDefs().addAll(fieldDefs);
        if (representationDefs != null) {
          mutableContent.getMutableRepresentationDefs().addAll(representationDefs);
        }
        mutableContent.getMutableStatuses().addAll(statuses);
        contentTypes.add(mutableContent);
        introspector.processMutableContentType(mutableContent, contentTypeElement);
        fieldDefs.clear();
      }
    }
    catch (Exception e) {
      logger.warn(e.getMessage(), e);
    }
    return contentTypes;
  }

  protected ContentTypeId getContentTypeId(WorkspaceId id, String namespace, String name) throws
      IllegalArgumentException {
    ContentTypeIdImpl contentTypeId = new ContentTypeIdImpl();
    contentTypeId.setWorkspace(workspaceId);
    contentTypeId.setNamespace(namespace);
    contentTypeId.setName(name);
    return contentTypeId;
  }

  protected String parseMandatoryStringElement(Element rootElement, final String elementName) throws
      IllegalStateException {
    Element elem = getChildNode(rootElement, elementName);
    return elem.getValue();
  }

  protected Element getChildNode(Element rootElement, final String elementName) throws IllegalStateException {
    Elements elems = rootElement.getChildElements(elementName, NAMESPACE);
    if (elems.size() < 1) {
      throw new IllegalStateException("No " + elementName);
    }
    if (elems.size() > 1) {
      throw new IllegalStateException("More than one " + elementName);
    }
    Element elem = elems.get(0);
    return elem;
  }

  protected String parseOptionalStringElement(Element rootElement, final String elementName) throws
      IllegalStateException {
    Elements elems = rootElement.getChildElements(elementName, NAMESPACE);
    if (elems.size() > 1) {
      throw new IllegalStateException("More than one " + elementName);
    }
    if (elems.size() > 0) {
      Element elem = elems.get(0);
      return elem.getValue();
    }
    else {
      return null;
    }
  }

  protected String parseAttribute(Element rootElement, String attName) {
    return rootElement.getAttributeValue(attName);
  }

  protected Collection<RepresentationDef> parseRepresentations(Element rootElement, String elementName) throws
      IllegalStateException {
    Elements elems = rootElement.getChildElements(elementName, NAMESPACE);
    if (elems.size() > 1) {
      throw new IllegalStateException("More than one " + elementName);
    }
    List<RepresentationDef> representations = new ArrayList<RepresentationDef>();
    if (elems.size() > 0) {
      RepresentationDef representation;
      Elements elements = elems.get(0).getChildElements(REPRESENTATION, NAMESPACE);
      for (int i = 0; i < elements.size(); i++) {
        representation = parseRepresentation(elements.get(i));
        representations.add(representation);
      }
      return representations;
    }
    else {
      return null;
    }
  }

  protected RepresentationDef parseRepresentation(Element rootElement) {
    MutableRepresentationDef representationDef = new RepresentationDefImpl();
    Elements childElements = rootElement.getChildElements();
    for (int i = 0; i < childElements.size(); i++) {
      if (StringUtils.equals(childElements.get(i).getLocalName(), NAME)) {
        representationDef.setName(childElements.get(i).getValue());
      }
      if (StringUtils.equals(childElements.get(i).getLocalName(), MIME_TYPE)) {
        representationDef.setMIMEType(childElements.get(i).getValue());
      }
      representationDef.setResourceUri(parseUri(rootElement, URI));
    }
    return representationDef;
  }

  protected ResourceUri parseUri(Element rootElement, String elementName) throws IllegalStateException {
    MutableResourceUri resourceUri = new ResourceUriImpl();
    Elements elems = rootElement.getChildElements(elementName, NAMESPACE);
    if (elems.size() < 1) {
      throw new IllegalStateException("No " + elementName);
    }
    if (elems.size() > 1) {
      throw new IllegalStateException("More than one " + elementName);
    }
    if (StringUtils.equalsIgnoreCase(elems.get(0).getChildElements().get(0).getLocalName(), INTERNAL)) {
      resourceUri.setType(ResourceUri.Type.INTERNAL);
      resourceUri.setValue(elems.get(0).getChildElements().get(0).getChildElements().get(0).getValue());
    }
    if (StringUtils.equalsIgnoreCase(elems.get(0).getChildElements().get(0).getLocalName(), EXTERNAL)) {
      resourceUri.setType(ResourceUri.Type.EXTERNAL);
      resourceUri.setValue(elems.get(0).getChildElements().get(0).getValue());
    }
    return resourceUri;
  }

  protected ContentTypeId parseContentTypeId(Element rootElement, String elementName, WorkspaceId workspaceId) throws
      IllegalStateException {
    Elements elems = rootElement.getChildElements(elementName, NAMESPACE);
    if (elems.size() > 1) {
      throw new IllegalStateException("More than one " + elementName);
    }
    if (elems.size() > 0) {
      ContentTypeId contentTypeId = getContentTypeId(workspaceId, parseMandatoryStringElement(elems.get(0), TYPE_NS), parseMandatoryStringElement(elems.
          get(0), TYPE_NAME));
      return contentTypeId;
    }
    else {
      return null;
    }
  }

  protected ContentDataType parseContent(Element rootElement) {
    MutableContentDataType type = new ContentDataTypeImpl();
    for (int i = 0; i < rootElement.getChildElements().size(); i++) {
      if (logger.isInfoEnabled()) {
        logger.info("Config name for content data type: " + rootElement.getChildElements().get(i).getLocalName());
      }
      if (StringUtils.equalsIgnoreCase(rootElement.getChildElements().get(i).getLocalName(), DEFINITION)) {
        type.setTypeDef(parseContentTypeId(rootElement, DEFINITION, workspaceId));
      }
      if (StringUtils.equalsIgnoreCase(rootElement.getChildElements().get(i).getLocalName(), BIDIRECTIONAL)) {
        type.setBiBidirectionalFieldName(parseOptionalStringElement(rootElement, BIDIRECTIONAL));
      }
      if (StringUtils.equalsIgnoreCase(rootElement.getChildElements().get(i).getLocalName(), AVAILABLE_FOR_SEARCH)) {
        final String availStrVal = parseOptionalStringElement(rootElement, AVAILABLE_FOR_SEARCH);
        if (logger.isInfoEnabled()) {
          logger.info("Available For Search " + availStrVal);
        }
        type.setAvailableForSearch(Boolean.parseBoolean(availStrVal));
      }
    }
    return type;
  }

  protected CollectionDataType parseCollection(Element rootElement) {
    MutableCollectionDataType type = new CollectionDataTypeImpl();
    for (int i = 0; i < rootElement.getChildElements().size(); i++) {
      final Element element = rootElement.getChildElements().get(i);
      if (StringUtils.equalsIgnoreCase(element.getLocalName(), SIMPLE_VALUE)) {
        final Element simpleElement = (Element) element.getChild(1);
        type.setItemDataType(parseSimpleValue(simpleElement));
      }
      type.setMinSize(NumberUtils.toInt(parseOptionalStringElement(rootElement, MIN_SIZE), Integer.MIN_VALUE));
      if (logger.isDebugEnabled()) {
        logger.debug("minSize of collection " + type.getMinSize() + " parsed minSize is " + parseOptionalStringElement(
            rootElement, MIN_SIZE));
      }
      type.setMaxSize(NumberUtils.toInt(parseOptionalStringElement(rootElement, MAX_SIZE), Integer.MAX_VALUE));
      if (logger.isDebugEnabled()) {
        logger.debug("maxSize of collection " + type.getMaxSize() + " parsed MaxSize is " + parseOptionalStringElement(
            rootElement, MAX_SIZE));
      }
    }
    return type;
  }

  protected DataType parseSimpleValue(Element rootElement) {
    final String localName = rootElement.getLocalName();
    if (logger.isDebugEnabled()) {
      logger.debug("Local name for simple value " + localName);
    }
    if (StringUtils.equalsIgnoreCase(localName, CONTENT)) {
      return parseContent(rootElement);
    }
    else if (StringUtils.equalsIgnoreCase(localName, OTHER)) {
      return parseOtherDataType(rootElement);
    }
    else if (StringUtils.equalsIgnoreCase(localName, STRING)) {
      return parseStringDataType(rootElement);
    }
    else {
      if (StringUtils.equalsIgnoreCase(localName, LONG)) {
        return DataType.LONG;
      }
      else if (StringUtils.equalsIgnoreCase(localName, DOUBLE)) {
        return DataType.DOUBLE;
      }
      else if (StringUtils.equalsIgnoreCase(localName, DATE_TIME)) {
        return DataType.DATE_TIME;
      }
      else if (StringUtils.equalsIgnoreCase(localName, BOOLEAN)) {
        return DataType.BOOLEAN;
      }
      else {
        return DataType.INTEGER;
      }
    }
  }

  protected Collection<FieldDef> parseFieldDefs(Element rootElement) {
    List<FieldDef> fieldDefs = new ArrayList<FieldDef>();
    for (int i = 0; i < rootElement.getChildElements().size(); i++) {
      fieldDefs.add(parseFieldDef(rootElement.getChildElements().get(i)));
    }
    return fieldDefs;
  }

  protected FieldDef parseFieldDef(Element rootElement) {
    MutableFieldDef fieldDef = new FieldDefImpl();
    fieldDef.setCustomValidator(parseValidator(rootElement, VALIDATOR));
    fieldDef.setFieldStandaloneUpdateAble(Boolean.parseBoolean(
        parseOptionalStringElement(rootElement, UPDATE_STANDALONE)));
    fieldDef.setName(parseMandatoryStringElement(rootElement, NAME));
    fieldDef.setRequired(Boolean.parseBoolean(parseOptionalStringElement(rootElement, REQUIRED)));
    fieldDef.setSearchDefinition(parseSearchDef(rootElement, SEARCH));
    fieldDef.setValueDef(parseValueDef(rootElement, VALUE));
    if (parseVariations(rootElement, VARIATIONS) != null) {
      fieldDef.setVariations(parseVariations(rootElement, VARIATIONS));
    }
    return fieldDef;
  }

  protected Collection<VariationDef> parseVariations(Element rootElement, String elementName) throws
      IllegalStateException {
    Collection<VariationDef> variationDefs = new ArrayList<VariationDef>();
    Elements elems = rootElement.getChildElements(elementName, NAMESPACE);
    if (elems.size() > 1) {
      throw new IllegalStateException("more than one element");
    }
    if (elems.size() > 0) {
      for (int i = 0; i < elems.get(0).getChildElements().size(); i++) {
        variationDefs.add(parseVariationDef(elems.get(0).getChildElements().get(i)));
      }
      return variationDefs;
    }
    else {
      return Collections.emptyList();
    }
  }

  protected VariationDef parseVariationDef(Element rootElement) throws IllegalStateException {
    Elements elems = rootElement.getChildElements();
    if (elems.size() < 1) {
      throw new IllegalStateException("Minimum No. of variation should 1");
    }
    else {
      MutableVariationDef variationDef = new VariationDefImpl();
      variationDef.setName(parseMandatoryStringElement(rootElement, NAME));
      variationDef.setMIMEType(parseMandatoryStringElement(rootElement, MIME_TYPE));
      variationDef.setResourceUri(parseUri(rootElement, URI));
      return variationDef;
    }
  }

  protected ValidatorDef parseValidator(Element rootElement, String elementName) throws IllegalStateException {
    MutableValidatorDef validatorDef = new ValidatorDefImpl();
    Elements elems = rootElement.getChildElements(elementName, NAMESPACE);
    if (elems.size() > 1) {
      throw new IllegalStateException("More than one " + elementName);
    }
    if (elems.size() > 0) {
      String validatorType = parseMandatoryStringElement(elems.get(0), VALIDATOR_TYPE);
      if (StringUtils.equalsIgnoreCase(validatorType, "groovy")) {
        validatorDef.seType(ValidatorType.GROOVY);
      }

      if (StringUtils.equalsIgnoreCase(validatorType, "javascript")) {
        validatorDef.seType(ValidatorType.JAVASCRIPT);
      }
      if (StringUtils.equalsIgnoreCase(validatorType, "ruby")) {
        validatorDef.seType(ValidatorType.RUBY);
      }
      validatorDef.setUri(parseUri(elems.get(0), URI));
      return validatorDef;
    }
    else {
      return null;
    }
  }

  protected SearchDef parseSearchDef(Element rootElement, String elementName) {
    MutableSearchDef searchDef = new SearchDefImpl();
    Elements elems = rootElement.getChildElements(elementName, NAMESPACE);
    if (elems.size() > 1) {
      throw new IllegalStateException("More than one " + elementName);
    }
    if (elems.size() > 0) {
      searchDef.setBoostConfig(parseOptionalStringElement(elems.get(0), BOOST));
      searchDef.setIndexed(Boolean.parseBoolean(parseMandatoryStringElement(elems.get(0), INDEX)));
      searchDef.setStored(Boolean.parseBoolean(parseOptionalStringElement(elems.get(0), STORE)));
      return searchDef;
    }
    else {
      return null;
    }
  }

  protected Collection<ContentStatus> parseContentStatuses(Element rootElement, String elementName) throws
      IllegalStateException {
    Elements elems = rootElement.getChildElements(elementName, NAMESPACE);
    if (elems.size() > 1) {
      throw new IllegalStateException("More than one " + elementName);
    }
    if (elems.size() > 0) {
      List<ContentStatus> contentStatuses = new ArrayList<ContentStatus>();
      for (int i = 0; i < elems.get(0).getChildElements().size(); i++) {
        MutableContentStatus contentStatus = new ContentStatusImpl();
        if (StringUtils.equalsIgnoreCase(elems.get(0).getChildElements().get(i).getLocalName(), STATUS_NAME)) {
          contentStatus.setName(elems.get(0).getChildElements().get(i).getValue());
        }
        //contentStatus.setContentTypeID(parseContentTypeId(rootElement, elementName));
        contentStatuses.add(contentStatus);
      }
      return contentStatuses;
    }
    else {
      return Collections.emptyList();
    }
  }

  /********************************** CONFUSED ****************************************/
  protected DataType parseValueDef(Element rootElement, String elementName) {
    final Element childNode = getChildNode(rootElement, elementName);
    if (logger.isDebugEnabled()) {
      logger.debug("Local name for root element " + rootElement.getLocalName());
      logger.debug("Local name for child element " + childNode.getLocalName());
    }
    return parseValue(childNode);
  }

  protected DataType parseValue(Element valueElement) {
    final Element element = (Element) valueElement.getChild(1);
    if (logger.isDebugEnabled()) {
      logger.debug("Local name for value element " + valueElement.getLocalName());
      logger.debug("Local name for main element " + element.getLocalName());
    }
    if (StringUtils.equalsIgnoreCase(element.getLocalName(), COLLECTION)) {
      return parseCollection(element);
    }
    else {
      return parseSimpleValue(element);
    }
  }

  /************************end of confsion **********************/
  private DataType parseOtherDataType(Element element) {
    MutableOtherDataType type = new OtherDataTypeImpl();
    type.setMIMEType(parseMandatoryStringElement(element, MIME_TYPE));
    return type;
  }

  private DataType parseStringDataType(Element element) {
    MutableStringDataType type = new StringDataTypeImpl();
    type.setMIMEType(parseMandatoryStringElement(element, MIME_TYPE));
    type.setEncoding(parseOptionalStringElement(element, ENCODING));
    return type;
  }
}
