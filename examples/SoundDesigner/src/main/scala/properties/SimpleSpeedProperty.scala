package ui.properties

import org.mt4j.util.MTColor

import processing.opengl.PGraphicsOpenGL
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
  
  override def draw(g: PGraphicsOpenGL) = {
  }    
  
  override def range = {
    this.propertyType.range
  }
  
  override def visualWidth = {
    this.propertyType.width
  }  

  override def maxWidth = {
    this.propertyType.width
  }  

  def propertyType = {
    SpeedPropertyType
  }  
  
  override def toString = {
    "SimpleSpeedProperty"
  }  
  
}
