#! /usr/bin/python

from com.smartitengineering.cms.api.content.template import RepresentationGenerator

class MyGen(RepresentationGenerator):
  def getRepresentationForContent(self, content,params):
    return content.getField("test").value.value

if __name__ == "__main__":
    rep = MyGen

