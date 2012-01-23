obj = {
  getRepresentationForContent: function (content, params) {
    return content.fields.get("test").value.value.toString();
  }
}
r = new com.smartitengineering.cms.api.content.template.RepresentationGenerator(obj);
