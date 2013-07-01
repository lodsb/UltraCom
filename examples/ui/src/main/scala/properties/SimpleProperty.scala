package ui.properties

import processing.core.PGraphics
import org.mt4j.util.MTColor


/**
* This abstract class represents a simple property.
* A simple property manages exactly one value.
*/
abstract class SimpleProperty extends Property {
  val (min, max) = this.range
  var value = (max - min)/2 + min
  
  /**
  * Returns the value of this property.
  */
  def apply() = {
    this.value
  }
  
  /**
  * Updates this property using the specified new value.
  */
  def update(newValue: Float) = {
    val (min, max) = this.range        
    this.value = math.max(math.min(max, newValue), min)
  }

  /**
  * Returns the maximum visual width of this property.
  */
  def visualWidth: Float
  
  /**
  * Maps the property value to a drawable range, that is, the range [0, visualWidth].
  */
  protected def mappedValue = {
    val (min, max) = this.range
    ((this.value - min)/(max - min)) * this.visualWidth
  }
  
}