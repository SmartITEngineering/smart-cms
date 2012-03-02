#! /usr/bin/python

from com.smartitengineering.cms.api.content.template import FieldValidator

class MyVal(FieldValidator):
  def isValidFieldValue(self, field, params):
    return field.value.value != "content";

if __name__ == "__main__":
    rep = MyVal

