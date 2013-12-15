package ui.properties.types

import org.mt4j.util.MTColor

import processing.opengl.PGraphicsOpenGL
import processing.core.PConstants._

import ui._
import ui.properties._
import ui.tools.Tool
import ui.paths._
import ui.util._


object PitchPropertyType extends PropertyType{
  val Width = Ui.width/100
  val Vicinity = 2* Width
  val PropertyColor = new MTColor(0, 50, 130)
  val ColorAlpha = 100 //alpha value for non-playing path
  val ProgressColorAlpha = 150 //alpha value for played back path
  val HighlightedColorAlpha = 200 //alpha value for path segments wich are highlighted
  val Range = (0.0f, 1.0f) //range of a pitch property bucket; 
  
  protected val SymbolWidth = Tool.Width/6.5f //width of note
  protected val SymbolHeight = Tool.Height/5.0f //height of note
  protected val NoteHeight = SymbolHeight/3.0f

  override def range = {
    this.Range
  }
  
  override def width = {
    this.Width
  }
  
  override def vicinity = {
    this.Vicinity
  }  
  
  override def color = { 
    new MTColor(this.PropertyColor.getR, this.PropertyColor.getG, this.PropertyColor.getB, this.PropertyColor.getA)
  }    
  
  override def drawSymbol(g: PGraphicsOpenGL, center: (Float, Float), color: MTColor) = {
    val (cx, cy) = center
    g.noFill()
    g.strokeWeight(SymbolWeight)
    g.stroke(color.getR, color.getG, color.getB, color.getA)
    g.beginShape() //drawing a note-like symbol using bezier curves
    g.vertex(cx + SymbolWidth/2, cy - SymbolHeight/2) //from top right
    g.bezierVertex(cx + SymbolWidth/2, cy, cx + SymbolWidth/2, cy, cx + SymbolWidth/2, cy + SymbolHeight/2 - NoteHeight/2) //to bottom right (induces straight line downwards)
    g.bezierVertex(cx + 5, cy + SymbolHeight/2, cx - 5, cy + SymbolHeight/2, cx - SymbolWidth/2, cy + SymbolHeight/2 - NoteHeight/2) //to bottom left
    g.bezierVertex(cx - 5, cy + SymbolHeight/2 - NoteHeight, cx + 5, cy + SymbolHeight/2 - NoteHeight, cx + SymbolWidth/2, cy + SymbolHeight/2 - NoteHeight/2) //back to bottom right
    g.endShape()   
  } 
  
}
