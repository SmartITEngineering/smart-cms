#! /usr/bin/python

from com.smartitengineering.cms.api.content.template import VariationGenerator

class MyVar(VariationGenerator):
  def getVariationForField(self, field, params):
    return field.value.value

if __name__ == "__main__":
    rep = MyVar

