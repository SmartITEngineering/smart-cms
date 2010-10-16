class MyRepGen
  include Java::com.smartitengineering.cms.spi.content.template.RepresentationGenerator
  def getRepresentationForContent(content)
    return content.getField("test").value.value;
  end
end
MyRepGen.new
