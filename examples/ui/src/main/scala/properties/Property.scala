package ui.properties

import processing.core.PGraphics
import org.mt4j.util.MTColor


/**
* This abstract class represents a property.
* Every property has a value range and can be drawn onto the screen.
*/
abstract class Property {

  /**
  * Draws this property.
  */
  def draw(g: PGraphics)


  /**
  * Returns the range of this property.
  */
  def range: (Float, Float)
  
  /**
  * Returns the maximum visual width of this property.
  */
  def visualWidth: Float
  
  /**
  * Returns the maximum width of this property, which is used to determine when the property takes its maximum.
  */
  def maxWidth: Float
}
