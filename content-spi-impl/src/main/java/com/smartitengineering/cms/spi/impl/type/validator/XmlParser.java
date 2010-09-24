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
package com.smartitengineering.cms.spi.impl.type.validator;

import com.smartitengineering.cms.api.SmartContentAPI;
import com.smartitengineering.cms.api.type.ContentStatus;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.api.type.DataType;
import com.smartitengineering.cms.api.type.FieldDef;
import com.smartitengineering.cms.api.type.MutableContentStatus;
import com.smartitengineering.cms.api.type.MutableContentType;
import com.smartitengineering.cms.api.type.MutableContentTypeId;
import com.smartitengineering.cms.api.type.MutableFieldDef;
import com.smartitengineering.cms.api.type.MutableRepresentationDef;
import com.smartitengineering.cms.api.type.MutableResourceUri;
import com.smartitengineering.cms.api.type.MutableSearchDef;
import com.smartitengineering.cms.api.type.MutableValidatorDef;
import com.smartitengineering.cms.api.type.MutableVariationDef;
import com.smartitengineering.cms.api.type.RepresentationDef;
import com.smartitengineering.cms.api.type.ResourceUri;
import com.smartitengineering.cms.api.type.SearchDef;
import com.smartitengineering.cms.api.type.TemplateType;
import com.smartitengineering.cms.api.type.ValidatorDef;
import com.smartitengineering.cms.api.type.ValidatorType;
import com.smartitengineering.cms.api.type.VariationDef;
import com.smartitengineering.cms.spi.SmartContentSPI;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author kaisar
 */
public class XmlParser implements XmlConstants {

  private InputStream source;

  public XmlParser(InputStream stream) {
    this.source = stream;
    if (source == null) {
      throw new IllegalArgumentException("Source stream can not be null!");
    }
  }

  public Collection<MutableContentType> parse() {

    ContentTypeId contentTypeId = null;
    Collection<FieldDef> fieldDefs = new ArrayList<FieldDef>();
    Collection<MutableContentType> contentTypes = new ArrayList<MutableContentType>();
    Collection<RepresentationDef> representationDefs = new ArrayList<RepresentationDef>();
    Collection<ContentStatus> statuses = new ArrayList<ContentStatus>();
    String displayName = null;
    MutableContentType mutableContent = SmartContentSPI.getInstance().getPersistableDomainFactory().
        createPersistableContentType();
    try {
      Builder builder = new Builder(false);
      Document document = builder.build(this.source);
      Element rootElement = document.getRootElement();
      Elements childRootElements = rootElement.getChildElements();
      for (int j = 0; j < childRootElements.size(); j++) {
        Elements childElements = childRootElements.get(j).getChildElements();
        String name = parseMandatoryStringElement(childRootElements.get(j), NAME); //max=1,min=1
        String attributeName = parseAttribute(childRootElements.get(j), "namespace");
        displayName = parseOptionalStringElement(childRootElements.get(j), DISPLAY_NAME); //min=0,max=1
        for (int child = 0; child < childElements.size(); child++) {//fields min=1,max=unbounted
          if (StringUtils.equalsIgnoreCase(childElements.get(child).getLocalName(), FIELDS)) {
            fieldDefs.addAll(parseFieldDefs(childElements.get(child)));
          }
        }
        statuses = parseContentStatus(childRootElements.get(j), STATUS);
        representationDefs = parseRepresentations(childRootElements.get(j), REPRESENTATIONS);
        contentTypeId = parseContentTypeId(childRootElements.get(j), PARENT);
        mutableContent.setContentTypeID(contentTypeId);
        mutableContent.setDisplayName(displayName);
        mutableContent.setParent(contentTypeId);
        mutableContent.getMutableFieldDefs().addAll(fieldDefs);
        if (representationDefs != null) {
          mutableContent.getMutableRepresentationDefs().addAll(representationDefs);
        }
        mutableContent.getMutableStatuses().addAll(statuses);
        contentTypes.add(mutableContent);
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return contentTypes;
  }

  protected String parseMandatoryStringElement(Element rootElement, final String elementName) throws
      IllegalStateException {
    Elements elems = rootElement.getChildElements(elementName, NAMESPACE);
    if (elems.size() < 1) {
      throw new IllegalStateException("No " + elementName);
    }
    if (elems.size() > 1) {
      throw new IllegalStateException("More than one " + elementName);
    }
    Element elem = elems.get(0);
    return elem.getValue();
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
      RepresentationDef representation = SmartContentAPI.getInstance().getContentTypeLoader().
          createMutableRepresentationDef();
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
    MutableRepresentationDef representationDef = SmartContentAPI.getInstance().getContentTypeLoader().
        createMutableRepresentationDef();
    Elements childElements = rootElement.getChildElements();
    for (int i = 0; i < childElements.size(); i++) {
      if (StringUtils.equals(childElements.get(i).getLocalName(), NAME)) {
        representationDef.setName(childElements.get(i).getValue());
      }
      if (StringUtils.equals(childElements.get(i).getLocalName(), MIME_TYPE)) {
        representationDef.setMIMEType(childElements.get(i).getValue());
      }
      if (StringUtils.equals(childElements.get(i).getLocalName(), TEMPLATE_TYPE)) {
        if (StringUtils.equalsIgnoreCase(childElements.get(i).getValue(), "groovy")) {
          representationDef.setTemplateType(TemplateType.GROOVY);
        }
        if (StringUtils.equalsIgnoreCase(childElements.get(i).getValue(), "velocity")) {
          representationDef.setTemplateType(TemplateType.VELOCITY);
        }
        if (StringUtils.equalsIgnoreCase(childElements.get(i).getValue(), "ruby")) {
          representationDef.setTemplateType(TemplateType.RUBY);
        }
        if (StringUtils.equalsIgnoreCase(childElements.get(i).getValue(), "javascript")) {
          representationDef.setTemplateType(TemplateType.JAVASCRIPT);
        }
      }
      representationDef.setResourceUri(parseUri(rootElement, URI));
    }
    return representationDef;
  }

  protected ResourceUri parseUri(Element rootElement, String elementName) throws IllegalStateException {
    MutableResourceUri resourceUri = SmartContentAPI.getInstance().getContentTypeLoader().createMutableResourceUri();
    Elements elems = rootElement.getChildElements(elementName, NAMESPACE);
    if (elems.size() < 1) {
      throw new IllegalStateException("No " + elementName);
    }
    if (elems.size() > 1) {
      throw new IllegalStateException("More than one " + elementName);
    }
    if (StringUtils.equalsIgnoreCase(elems.get(0).getChildElements().get(0).getLocalName(), INTERNAL)) {
      resourceUri.setType(ResourceUri.Type.INTERNAL);
      resourceUri.setValue(elems.get(0).getChildElements().get(0).getValue());
    }
    if (StringUtils.equalsIgnoreCase(elems.get(0).getChildElements().get(0).getLocalName(), EXTERNAL)) {
      resourceUri.setType(ResourceUri.Type.EXTERNAL);
      resourceUri.setValue(elems.get(0).getChildElements().get(0).getValue());
    }
    return resourceUri;
  }

  protected ContentTypeId parseContentTypeId(Element rootElement, String elementName) throws IllegalStateException {
    Elements elems = rootElement.getChildElements(elementName, NAMESPACE);
    if (elems.size() > 1) {
      throw new IllegalStateException("More than one " + elementName);
    }
    MutableContentTypeId contentTypeId = SmartContentAPI.getInstance().getContentTypeLoader().
        createMutableContentTypeID();
    if (elems.size() > 0) {
      contentTypeId.setName(parseMandatoryStringElement(elems.get(0), TYPE_NAME));
      contentTypeId.setNamespace(parseMandatoryStringElement(elems.get(0), TYPE_NS));
      return contentTypeId;
    }
    else {
      return null;
    }
  }

  protected Map<String, String> parseValue(Element rootElement) {
    Map<String, String> value = new HashMap<String, String>();
    Elements elements;
    elements = rootElement.getChildElements();
    for (int i = 0; i < elements.size(); i++) {
      if (StringUtils.equalsIgnoreCase(elements.get(i).getLocalName(), CONTENT)) {
        value.putAll(parseContent(rootElement.getChildElements().get(i)));
      }
      else if (StringUtils.equalsIgnoreCase(elements.get(i).getLocalName(), COLLECTION)) {
        value.putAll(parseCollection(rootElement.getChildElements().get(i)));
      }
      else {
      }
    }
    return value;
  }

  protected Map<String, String> parseContent(Element rootElement) {
    Map<String, String> content = new HashMap<String, String>();
    for (int i = 0; i < rootElement.getChildElements().size(); i++) {
      if (StringUtils.equalsIgnoreCase(rootElement.getChildElements().get(i).getLocalName(), DEFINITION)) {
        content.putAll(parseDefinition(rootElement.getChildElements().get(i)));
      }
      if (StringUtils.equalsIgnoreCase(rootElement.getChildElements().get(i).getLocalName(), BIDIRECTIONAL)) {
        content.put(BIDIRECTIONAL, parseOptionalStringElement(rootElement.getChildElements().get(i), BIDIRECTIONAL));
      }
    }
    return content;
  }

  protected Map<String, String> parseDefinition(Element rootElement) {
    Map<String, String> definition = new HashMap<String, String>();
    for (int i = 0; i < rootElement.getChildElements().size(); i++) {
      String key = rootElement.getChildElements().get(i).getLocalName();
      String value = rootElement.getChildElements().get(i).getValue();
      definition.put(key, value);
    }
    return definition;
  }

  protected Map<String, String> parseCollection(Element rootElement) {
    Map<String, String> collection = new HashMap<String, String>();
    for (int i = 0; i < rootElement.getChildElements().size(); i++) {
      if (StringUtils.equalsIgnoreCase(rootElement.getChildElements().get(i).getLocalName(), SIMPLE_VALUE)) {
        collection.putAll(parseSimpleValue(rootElement.getChildElements().get(i)));
      }
      else {
        collection.put(rootElement.getChildElements().get(i).getLocalName(), rootElement.getChildElements().get(i).
            getValue());
      }
    }
    return collection;
  }

  protected Map<String, String> parseSimpleValue(Element rootElement) {
    Map<String, String> simple_value = new HashMap<String, String>();
    for (int i = 0; i < rootElement.getChildElements().size(); i++) {
      if (StringUtils.equalsIgnoreCase(rootElement.getChildElements().get(i).getLocalName(), CONTENT)) {
        simple_value.putAll(parseContent(rootElement.getChildElements().get(i)));
      }
      else {
        simple_value.put(rootElement.getChildElements().get(i).getLocalName(), rootElement.getChildElements().get(i).
            getValue());
      }
    }
    return simple_value;
  }

  protected Collection<FieldDef> parseFieldDefs(Element rootElement) {
    List<FieldDef> fieldDefs = new ArrayList<FieldDef>();
    for (int i = 0; i < rootElement.getChildElements().size(); i++) {
      fieldDefs.add(parseFieldDef(rootElement.getChildElements().get(i)));
    }
    return fieldDefs;
  }

  protected FieldDef parseFieldDef(Element rootElement) {
    MutableFieldDef fieldDef = SmartContentAPI.getInstance().getContentTypeLoader().createMutableFieldDef();
    fieldDef.setCustomValidator(parseValidator(rootElement, VALIDATOR));
    fieldDef.setFieldStandaloneUpdateAble(Boolean.parseBoolean(
        parseOptionalStringElement(rootElement, UPDATE_STANDALONE)));
    fieldDef.setName(parseMandatoryStringElement(rootElement, NAME));
    fieldDef.setRequired(Boolean.parseBoolean(parseOptionalStringElement(rootElement, REQUIRED)));
    fieldDef.setSearchDefinition(parseSearchDef(rootElement, SEARCH));
    fieldDef.setValueDef(DataType.INTEGER);
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
      return null;
    }
  }

  protected VariationDef parseVariationDef(Element rootElement) throws IllegalStateException {
    Elements elems = rootElement.getChildElements();
    if (elems.size() < 1) {
      throw new IllegalStateException("Minimum No. of variation should 1");
    }
    else {
      MutableVariationDef variationDef =
                          SmartContentAPI.getInstance().getContentTypeLoader().createMutableVariationDef();
      variationDef.setName(parseMandatoryStringElement(rootElement, NAME));
      variationDef.setMIMEType(parseMandatoryStringElement(rootElement, MIME_TYPE));
      variationDef.setResourceUri(parseUri(rootElement, URI));
      if (StringUtils.equalsIgnoreCase(parseMandatoryStringElement(rootElement, TEMPLATE_TYPE), "javascript")) {
        variationDef.setTemplateType(TemplateType.JAVASCRIPT);
      }
      if (StringUtils.equalsIgnoreCase(parseMandatoryStringElement(rootElement, TEMPLATE_TYPE), "groovy")) {
        variationDef.setTemplateType(TemplateType.GROOVY);
      }
      if (StringUtils.equalsIgnoreCase(parseMandatoryStringElement(rootElement, TEMPLATE_TYPE), "ruby")) {
        variationDef.setTemplateType(TemplateType.RUBY);
      }
      if (StringUtils.equalsIgnoreCase(parseMandatoryStringElement(rootElement, TEMPLATE_TYPE), "velocity")) {
        variationDef.setTemplateType(TemplateType.VELOCITY);
      }
      return variationDef;
    }
  }

  protected ValidatorDef parseValidator(Element rootElement, String elementName) throws IllegalStateException {
    MutableValidatorDef validatorDef = SmartContentAPI.getInstance().getContentTypeLoader().createMutableValidatorDef();
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
    MutableSearchDef searchDef = SmartContentAPI.getInstance().getContentTypeLoader().createMutableSearchDef();
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

  /********************************** CONFUSED ****************************************/
  protected Collection<ContentStatus> parseContentStatus(Element rootElement, String elementName) throws
      IllegalStateException {
    Elements elems = rootElement.getChildElements(elementName, NAMESPACE);
    if (elems.size() > 1) {
      throw new IllegalStateException("More than one " + elementName);
    }
    if (elems.size() > 0) {
      MutableContentStatus contentStatus =
                           SmartContentAPI.getInstance().getContentTypeLoader().createMutableContentStatus();
      List<ContentStatus> contentStatuses = new ArrayList<ContentStatus>();
      for (int i = 0; i < elems.get(0).getChildElements().size(); i++) {
        contentStatus.setId(i);
        if (StringUtils.equalsIgnoreCase(elems.get(0).getChildElements().get(i).getLocalName(), STATUS_NAME)) {
          contentStatus.setName(elems.get(0).getChildElements().get(i).getValue());
        }
        //contentStatus.setContentTypeID(parseContentTypeId(rootElement, elementName));
        contentStatuses.add(contentStatus);
      }
      return contentStatuses;
    }
    else {
      return null;
    }
  }

  protected DataType parseValueDef(Element rootElement) {
    return DataType.LONG;
  }
  /************************end of confsion **********************/
}
