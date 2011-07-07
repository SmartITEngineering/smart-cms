obj = {
  getVariationForField: function (field) {
    return field.value.value.toString();
  }
}
r = new com.smartitengineering.cms.spi.content.template.VariationGenerator(obj);
