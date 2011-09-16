import com.smartitengineering.cms.api.content.Field;
import com.smartitengineering.cms.api.content.MutableField;
import com.smartitengineering.cms.api.content.MutableStringFieldValue;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
/**
 *
 * @author imyousuf
 */
class SampleCoProcessor implements com.smartitengineering.cms.api.content.template.ContentCoProcessor {
  public void processContent(com.smartitengineering.cms.api.content.MutableContent content, Map<String, String> params) {
    Field copySrcField = content.getField("directEnumField");
    if(copySrcField != null) {
      String val = copySrcField.getValue().toString();
      MutableField newField = SmartContentAPI.getInstance().getContentLoader().createMutableField(content.getContentId(), content.getContentDefinition().getFieldDefs().get("directEnumFieldCopy"));
      MutableStringFieldValue mutVal = SmartContentAPI.getInstance().getContentLoader().createStringFieldValue();
      mutVal.setValue(val);
      newField.setValue(mutVal);
      content.setField(newField);
      newField = SmartContentAPI.getInstance().getContentLoader().createMutableField(content.getContentId(), content.getContentDefinition().getFieldDefs().get("dynaField"));
      mutVal = SmartContentAPI.getInstance().getContentLoader().createStringFieldValue();
      mutVal.setValue(Long.toString(new java.util.Date().getTime()));
      newField.setValue(mutVal);
      content.setField(newField);
    }
  }
}

