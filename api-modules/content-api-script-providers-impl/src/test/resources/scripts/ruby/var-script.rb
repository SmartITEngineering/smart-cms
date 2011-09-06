class MyVarGen
  include Java::com.smartitengineering.cms.api.content.template.VariationGenerator
  def getVariationForField(field, params)
    return field.value.value;
  end
end
MyVarGen.new
