class MyVarGen
  include Java::com.smartitengineering.cms.spi.content.template.VariationGenerator
  def getVariationForField(field, params)
    return field.value.value;
  end
end
MyVarGen.new
