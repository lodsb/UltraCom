package ui.properties

import org.mt4j.util.MTColor

import processing.core.PGraphics
import processing.core.PConstants._

import ui._
import ui.properties.types._
import ui.tools.Tool
import ui.paths._
import ui.util._

object SimpleSpeedProperty {

  def apply(node: ManipulableNode) = {
     new SimpleSpeedProperty(node)
  }

}

/**
* This class represents a simple speed property. Not used atm.
*/
class SimpleSpeedProperty(node: ManipulableNode) extends SimpleProperty {
  
  override def draw(g: PGraphics) = {       
  }    
  
  override def range = {
    SpeedPropertyType.range
  }
  
  override def visualWidth = {
    SpeedPropertyType.width
  }  

  override def maxWidth = {
    SpeedPropertyType.width
  }  
  
}
