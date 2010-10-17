class MyValGen
  include Java::com.smartitengineering.cms.spi.content.template.FieldValidator
  def isValidFieldValue(field)
    return field.value.value != "content";
  end
end
MyValGen.new
