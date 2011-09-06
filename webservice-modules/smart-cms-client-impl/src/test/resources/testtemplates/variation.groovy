/**
 *
 * @author imyousuf
 */
class InternalVariation implements com.smartitengineering.cms.api.content.template.VariationGenerator {
  public String getVariationForField(com.smartitengineering.cms.api.content.Field field, Map<String, String> params) {
    String strVal = field.value.value.toString();
    int max = 10;
    if(params.containsKey("max")) {
      try {
        max = Integer.parseInt(params["max"]);
      }
      catch(Exception ex) {
        ex.printStackTrace();
      }
    }
    if(strVal.length() > max) {
      return strVal.substring(0, max);
    }
    return strVal;
  }
}

