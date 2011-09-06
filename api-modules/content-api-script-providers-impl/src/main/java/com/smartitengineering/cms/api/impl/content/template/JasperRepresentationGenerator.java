/*
 *
 * This is a simple Content Management System (CMS)
 * Copyright (C) 2011  Imran M Yousuf (imyousuf@smartitengineering.com)
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
package com.smartitengineering.cms.api.impl.content.template;

import com.smartitengineering.cms.api.content.CollectionFieldValue;
import com.smartitengineering.cms.api.content.Content;
import com.smartitengineering.cms.api.content.Field;
import com.smartitengineering.cms.api.exception.InvalidTemplateException;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.type.FieldValueType;
import com.smartitengineering.cms.api.workspace.RepresentationTemplate;
import com.smartitengineering.cms.api.workspace.ResourceTemplate;
import com.smartitengineering.cms.api.content.template.RepresentationGenerator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.JRTextExporter;
import net.sf.jasperreports.engine.export.JRTextExporterParameter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.export.oasis.JROdsExporter;
import net.sf.jasperreports.engine.export.oasis.JROdtExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class JasperRepresentationGenerator extends AbstractTypeRepresentationGenerator {

  public static final String OUTPUT = "output";
  public static final String POJO_FIELD = "pojo_field";
  public static final String PDF = "pdf";
  public static final String XML = "xml";
  public static final String XLS = "xls";
  public static final String RTF = "rtf";
  public static final String CSV = "csv";
  public static final String ODT = "odt";
  public static final String ODS = "ods";
  public static final String DOCX = "docx";
  public static final String TXT = "txt";
  public static final String SUBREPORT_PREFIX = "subreport_";

  public RepresentationGenerator getGenerator(RepresentationTemplate template) throws InvalidTemplateException {
    return new JasperTemplateRepresentationGenerator(template);
  }

  static abstract class AbstractJasperTemplateGenerator {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());
    protected final JasperReport report;
    protected final ResourceTemplate resourceTemplate;

    public AbstractJasperTemplateGenerator(ResourceTemplate template) throws InvalidTemplateException {
      try {
        this.resourceTemplate = template;
        report = getJasperReport(template);
      }
      catch (Exception ex) {
        logger.warn(ex.getMessage(), ex);
        throw new InvalidTemplateException(ex);
      }
    }

    protected final JasperReport getJasperReport(ResourceTemplate template) throws JRException {
      return JasperCompileManager.compileReport(new ByteArrayInputStream(template.getTemplate()));
    }

    protected byte[] generateReport(Map<String, String> params, final JRDataSource jRBeanCollectionDataSource) throws
        RuntimeException {
      final String output;
      output = getOutputFormat(params);
      try {
        final JasperPrint print = JasperFillManager.fillReport(report, getParams(params), jRBeanCollectionDataSource);
        return export(output, print);
      }
      catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    }

    protected String getOutputFormat(Map<String, String> params) {
      String output;
      if (params.containsKey(OUTPUT)) {
        output = params.get(OUTPUT);
        params.remove(OUTPUT);
      }
      else {
        output = PDF;
      }
      return output;
    }

    protected byte[] export(final String output, final JasperPrint print) throws IllegalArgumentException, JRException {
      if (StringUtils.equalsIgnoreCase(output, PDF)) {
        return JasperExportManager.exportReportToPdf(print);
      }
      else if (StringUtils.equalsIgnoreCase(output, XML)) {
        return org.apache.commons.codec.binary.StringUtils.getBytesUtf8(JasperExportManager.exportReportToXml(print));
      }
      else if (StringUtils.equalsIgnoreCase(output, XLS)) {
        JRExporter exporter = new JRXlsExporter();
        exporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);
        exporter.setParameter(JRXlsExporterParameter.IS_DETECT_CELL_TYPE, Boolean.TRUE);
        exporter.setParameter(JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND, Boolean.FALSE);
        exporter.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.TRUE);
        return exportJasperPrint(exporter, print);
      }
      else if (StringUtils.equalsIgnoreCase(output, RTF)) {
        JRExporter exporter = new JRRtfExporter();
        return exportJasperPrint(exporter, print);
      }
      else if (StringUtils.equalsIgnoreCase(output, DOCX)) {
        JRExporter exporter = new JRDocxExporter();
        return exportJasperPrint(exporter, print);
      }
      else if (StringUtils.equalsIgnoreCase(output, TXT)) {
        JRExporter exporter = new JRTextExporter();
        exporter.setParameter(JRTextExporterParameter.CHARACTER_WIDTH, 2.0f);
        exporter.setParameter(JRTextExporterParameter.CHARACTER_HEIGHT, 2.0f);
        return exportJasperPrint(exporter, print);
      }
      else if (StringUtils.equalsIgnoreCase(output, ODS)) {
        JRExporter exporter = new JROdsExporter();
        return exportJasperPrint(exporter, print);
      }
      else if (StringUtils.equalsIgnoreCase(output, ODT)) {
        JRExporter exporter = new JROdtExporter();
        return exportJasperPrint(exporter, print);
      }
      else if (StringUtils.equalsIgnoreCase(output, CSV)) {
        JRExporter exporter = new JRCsvExporter();
        return exportJasperPrint(exporter, print);
      }
      else {
        throw new IllegalArgumentException("Unrecognized output format " + output);
      }
    }

    protected byte[] exportJasperPrint(JRExporter exporter, final JasperPrint print) {
      ByteArrayOutputStream outStream = new ByteArrayOutputStream();
      exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
      exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, outStream);
      try {
        exporter.exportReport();
      }
      catch (Exception ex) {
        throw new RuntimeException(ex);
      }
      return outStream.toByteArray();
    }

    protected Map<String, Object> getParams(Map<String, String> params) {
      Map<String, String> clonedParams = new LinkedHashMap<String, String>(params);
      clonedParams.remove(OUTPUT);
      clonedParams.remove(POJO_FIELD);
      LinkedHashMap<String, Object> parameters = new LinkedHashMap<String, Object>();
      Iterator<Entry<String, String>> itarator = clonedParams.entrySet().iterator();
      while (itarator.hasNext()) {
        Entry<String, String> entry = itarator.next();
        if (entry.getKey().startsWith(SUBREPORT_PREFIX)) {
          try {
            ResourceTemplate internalTemplateData = getInternalResourceTemplate(entry.getValue());
            JasperReport subreport = getJasperReport(internalTemplateData);
            parameters.put(entry.getKey().substring(SUBREPORT_PREFIX.length()), subreport);
          }
          catch (Exception ex) {
            logger.warn("Invalid subreport..", ex);
            throw new IllegalArgumentException("Invalid subreport specified!");
          }
          itarator.remove();
        }
      }
      parameters.putAll(clonedParams);
      return parameters;

    }

    protected abstract ResourceTemplate getInternalResourceTemplate(String value);
  }

  static class JasperTemplateRepresentationGenerator extends AbstractJasperTemplateGenerator implements
      RepresentationGenerator {

    public JasperTemplateRepresentationGenerator(ResourceTemplate template) throws InvalidTemplateException {
      super(template);
    }

    @Override
    public byte[] getRepresentationForContent(Content content, Map<String, String> params) {
      final Field field;
      if (params.get(POJO_FIELD) == null) {
        field = null;
      }
      else {
        field = content.getField(params.get(POJO_FIELD));
      }
      final JRDataSource jRBeanCollectionDataSource;
      if (field == null) {
        jRBeanCollectionDataSource = new JRBeanCollectionDataSource(Collections.singleton(content));
      }
      else {
        if (field.getFieldDef().getValueDef().getType().equals(FieldValueType.COLLECTION)) {
          CollectionFieldValue collectionFieldValue = (CollectionFieldValue) field.getValue();
          jRBeanCollectionDataSource = new JRBeanCollectionDataSource(collectionFieldValue.getValue());
        }
        else {
          jRBeanCollectionDataSource = new JRBeanCollectionDataSource(Collections.singleton(field.getValue()));
        }
      }
      return generateReport(params, jRBeanCollectionDataSource);
    }

    @Override
    protected ResourceTemplate getInternalResourceTemplate(String value) {
      return SmartContentAPI.getInstance().getWorkspaceApi().getRepresentationTemplate(resourceTemplate.getWorkspaceId(),
                                                                                       value);
    }
  }
}
