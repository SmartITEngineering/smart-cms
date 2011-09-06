obj = {
  isValidFieldValue: function (field) {
    return field.value.value.toString() != "content";
  }
}
r = new com.smartitengineering.cms.api.content.template.FieldValidator(obj);
