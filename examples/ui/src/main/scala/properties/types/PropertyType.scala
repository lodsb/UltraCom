package ui.properties.types

import org.mt4j.util.MTColor

import processing.core.PGraphics

import ui.tools.Tool

/**
* This abstract class represents a property type.
*/
abstract class PropertyType {
  protected val SymbolAlpha = 150
  protected val SymbolColor = new MTColor(0,0,0,SymbolAlpha)
  protected val SymbolWeight = 1 //TODO change to Ui-relative value
  
  def color: MTColor

  def range: (Float, Float)
  
  def width: Float
  
  def vicinity: Float
  
  def mean = {
    val (min, max) = this.range
    (max - min)/2 + min
  }
  
  /**
  * Draws the symbol associated with a property of this type on the specified tool.
  */
  def drawSymbol(g: PGraphics, tool:Tool)
}
