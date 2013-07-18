package ui.properties.types

import org.mt4j.util.MTColor

import processing.core.PGraphics

import ui.tools.Tool

/**
* This abstract class represents a property type.
*/
abstract class PropertyType {
  protected val SymbolWeight = 1
  
  def color: MTColor

  def range: (Float, Float)
  
  def width: Float
  
  def vicinity: Float
  
  def mean = {
    val (min, max) = this.range
    (max - min)/2 + min
  }
  
  /**
  * Draws the symbol associated with this property type at the specified position using the given color.
  */
  def drawSymbol(g: PGraphics, center: (Float, Float), color: MTColor)
}
