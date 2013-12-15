package ui.properties

import processing.opengl.PGraphicsOpenGL
import org.mt4j.util.MTColor

import ui.persistence._


/**
* This abstract class represents a property.
* Every property has a value range and can be drawn onto the screen.
*/
abstract class Property extends Persistability {

  /**
  * Draws this property.
  */
  def draw(g: PGraphicsOpenGL)


  /**
  * Returns the range of this property.
  */
  def range: (Float, Float)
  
  /**
  * Returns the visual width of this property, that is, the maximum width with which
  * this property is visually represented. 
  * Note that this value may be different from the maximum width returned by method maxWidth.
  */
  def visualWidth: Float
  
  /**
  * Returns the maximum width of this property, which is used to determine the interaction area of this property.z
  */
  def maxWidth: Float
}
