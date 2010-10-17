obj = {
  isValidFieldValue: function (field) {
    return field.value.value.toString() != "content";
  }
}
r = new com.smartitengineering.cms.spi.content.template.FieldValidator(obj);
