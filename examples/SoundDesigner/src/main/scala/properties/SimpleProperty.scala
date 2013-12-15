package ui.properties

import processing.opengl.PGraphicsOpenGL
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
  * Maps the property value to a drawable range, that is, the range [0, visualWidth].
  */
  protected def mappedValue = {
    val (min, max) = this.range
    ((this.value - min)/(max - min)) * this.visualWidth
  }
  
  override def toXML = {
    "<property type = '" + this.toString + "'><buckets number = '" + 1 + "'><bucket index = '0'>" + this.value + "</bucket></buckets></property>"
  } 
  
}
