class MyRepGen
  include Java::com.smartitengineering.cms.spi.content.template.RepresentationGenerator
  def getRepresentationForContent(content, params)
    return content.fields["test"].value.value;
  end
end
MyRepGen.new
