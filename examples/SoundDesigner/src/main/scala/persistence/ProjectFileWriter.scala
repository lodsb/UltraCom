package ui.persistence

import java.io._

import ui._
import ui.paths._

object ProjectFileWriter {

  def save() = {
    val out = new java.io.FileWriter("project.xml")
    out.write("<?xml version='1.0' encoding='UTF-8'?>" + Ui.toXML)
    out.close    
  }

}
