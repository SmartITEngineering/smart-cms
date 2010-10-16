obj = {
  getRepresentationForContent: function (content) {
    return content.getField("test").value.value.toString();
  }
}
r = new com.smartitengineering.cms.spi.content.template.RepresentationGenerator(obj);
