obj = {
  getRepresentationForContent: function (content) {
    return content.fields.get("test").value.value.toString();
  }
}
r = new com.smartitengineering.cms.api.content.template.RepresentationGenerator(obj);
