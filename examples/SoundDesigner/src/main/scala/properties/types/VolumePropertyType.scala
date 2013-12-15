package ui.properties.types

import org.mt4j.util.MTColor

import processing.opengl.PGraphicsOpenGL
import processing.core.PConstants._

import ui._
import ui.properties._
import ui.tools.Tool
import ui.paths._
import ui.util._


object VolumePropertyType extends PropertyType {
  val Width = Ui.width/100 //maximum width of volume property
  val Vicinity = 2 * Width
  val PropertyColor = new MTColor(0, 130, 0)  
  val ColorAlpha = 100
  val ProgressColorAlpha = 150 
  val HighlightedColorAlpha = 200 //alpha value for path segments wich are highlighted  
  val Range = (0.0f, 1.0f) //range of a property bucket
  
  protected val SymbolWidth = Tool.Width/4.0f //width of volume control
  protected val SymbolHeight = Tool.Height/10.0f //height of volume control
  //val Convexity = -5 //convexity of diagonal line(s)  
  
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
    g.beginShape() //drawing a volume-control-like symbol using bezier curves
    
    //closed, hovering volume symbol
    g.vertex(cx + SymbolWidth/2, cy + SymbolHeight/2) //from bottom right
    g.bezierVertex(cx - SymbolWidth/2, cy, cx - SymbolWidth/2, cy, cx - SymbolWidth/2, cy) //to center left
    g.bezierVertex(cx + SymbolWidth/2, cy - SymbolHeight/2, cx + SymbolWidth/2, cy - SymbolHeight/2, cx + SymbolWidth/2, cy - SymbolHeight/2) //to top right
    g.bezierVertex(cx + SymbolWidth/2, cy + SymbolHeight/2, cx + SymbolWidth/2, cy + SymbolHeight/2, cx + SymbolWidth/2, cy + SymbolHeight/2) //back to bottom right  
    /* //closed, lying volume symbol
    g.vertex(cx - SymbolWidth/2, cy + SymbolHeight/2) //from bottom left
    g.bezierVertex(cx - Convexity, cy - Convexity, cx - Convexity, cy - Convexity, cx + SymbolWidth/2, cy - SymbolHeight/2) //to top right (induces diagonal line)
    g.bezierVertex(cx + SymbolWidth/2, cy + SymbolHeight/2, cx + SymbolWidth/2, cy + SymbolHeight/2, cx + SymbolWidth/2, cy + SymbolHeight/2) //to bottom right
    g.bezierVertex(cx - SymbolWidth/2, cy + SymbolHeight/2, cx - SymbolWidth/2, cy + SymbolHeight/2, cx - SymbolWidth/2, cy + SymbolHeight/2) //back to bottom left
    */
    g.endShape()
  }  
  
  
}
