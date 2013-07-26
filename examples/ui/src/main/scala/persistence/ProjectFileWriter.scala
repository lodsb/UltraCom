package ui.persistence

import java.io._

import ui._
import ui.paths._

object ProjectFileWriter {

  def save() = {
    val out = new java.io.FileWriter("project.xml")
    out.write(Ui.toXML)
    out.close    
  }

}
