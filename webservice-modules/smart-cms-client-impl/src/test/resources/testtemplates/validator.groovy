/**
 *
 * @author imyousuf
 */
class InternalValidator implements com.smartitengineering.cms.api.content.template.FieldValidator {
	public boolean isValidFieldValue(com.smartitengineering.cms.api.content.Field field, Map<String, String> params) {
    boolean valid = true;
    if(field.getFieldDef().getValueDef().getType() != com.smartitengineering.cms.api.type.FieldValueType.STRING) {
      return valid;
    }
    if(params.containsKey("max")) {
      try {
        int max = Integer.parseInt(params["max"]);
        String str = (String) field.getValue().getValue();
        if(str != null && str.length() > max) {
          valid = false;
        }
      }
      catch(Exception ex) {
        ex.printStackTrace();
      }
    }
    if(params.containsKey("min")) {
      try {
        int min = Integer.parseInt(params["min"]);
        String str = (String) field.getValue().getValue();
        if(min > 0 && (str == null || str.length() < min)) {
          valid = false;
        }
      }
      catch(Exception ex) {
        ex.printStackTrace();
      }
      
    }
    return valid;
  }
}

