class MyValGen
  include Java::com.smartitengineering.cms.api.content.template.FieldValidator
  def isValidFieldValue(field, params)
    return field.value.value != "content";
  end
end
MyValGen.new
