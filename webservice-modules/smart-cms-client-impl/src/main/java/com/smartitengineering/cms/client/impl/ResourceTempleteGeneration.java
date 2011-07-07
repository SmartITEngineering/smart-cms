/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smartitengineering.cms.client.impl;

import com.smartitengineering.cms.ws.common.domains.ResourceTemplateImpl;
import java.io.StringWriter;
import javax.swing.JOptionPane;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author kaisar
 */
public class ResourceTempleteGeneration {

  public static final String[] options = {"VELOCITY", "RUBY", "GROOVY", "JAVASCRIPT"};

  public static void main(String args[]) {

    String name = JOptionPane.showInputDialog("Enter Resource Template Name : ");
    String temp = JOptionPane.showInputDialog("Enter Resource Template (This will Converted to bytes automaticly) : ");
    String tempType = (String) JOptionPane.showInputDialog(null, "Choose Templete Type", "Template Type",
                                                           JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

    ObjectMapper mapper = new ObjectMapper();
    ResourceTemplateImpl template = new ResourceTemplateImpl();
    template.setName(name);
    final byte[] bytes = temp.getBytes();
    template.setTemplate(bytes);
    template.setTemplateType(tempType);
    StringWriter writer = new StringWriter();
    try {
      mapper.writeValue(writer, template);
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    System.out.println(writer.toString());
  }
}
