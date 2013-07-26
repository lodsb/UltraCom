package ui.persistence

import java.io._

import ui._
import ui.paths._

object SoundFileWriter {

  def save() = {
    val out = new java.io.FileWriter("project1.txt")
    //out.write("hello file!")
    
    //out.write(Ui.toXML())
    
    /*
    Ui.nodes.foreach(node => out.write(node.asXML))
    Ui.paths.foreach(path => out.write(path.asXML))
    */
    out.close    
  }

}
