package ui.properties.types

import org.mt4j.util.MTColor

import processing.opengl.PGraphicsOpenGL
import processing.core.PConstants._

import ui._
import ui.properties._
import ui.tools.Tool
import ui.paths._
import ui.util._


object SpeedPropertyType extends PropertyType {
  val Width = Ui.width/100
  val Vicinity = 2 * Width
  val PropertyColor = new MTColor(0, 130, 130)  
  val ColorAlpha = 100
  val ProgressColorAlpha = 150
  val HighlightedColorAlpha = 200
  val Range = (5.0f, 325.0f) //range of a speed property bucket; in this context, number of milliseconds
  
  protected val SymbolWidth = Tool.Width/6.0f //width of hourglass
  protected val SymbolHeight = Tool.Height/6.0f //height of hourglass
  
  //more vals defining the hourglass coming up...
  protected val YConvexity = 0 //vertical convexity of crossing curves; higher value means more
  protected val XConvexity = 10 //horizontal convexity of crossing curves; higher value means more
  protected val YConvexityLine = 0 //vertical convexity of bottom and top lines; higher value means more (and thus a negative value means curvature towards the center)
  protected val XConvexityLine = 0 //not really a useful value at the moment, its semantics could be improved however if necessary   
  
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
    g.beginShape() //drawing an hourglass-like symbol using bezier curves
    g.vertex(cx + SymbolWidth/2, cy + SymbolHeight/2) //from bottom right
    g.bezierVertex(cx + XConvexityLine, cy + YConvexityLine + SymbolHeight/2, cx - XConvexityLine, cy + YConvexityLine + SymbolHeight/2, cx - SymbolWidth/2, cy + SymbolHeight/2) //to bottom left
    g.bezierVertex(cx - XConvexity, cy - YConvexity, cx + XConvexity, cy + YConvexity, cx + SymbolWidth/2, cy - SymbolHeight/2) //to top right
    g.bezierVertex(cx + XConvexityLine, cy - YConvexityLine - SymbolHeight/2, cx - XConvexityLine, cy - YConvexityLine - SymbolHeight/2, cx - SymbolWidth/2, cy - SymbolHeight/2) //to top left
    g.bezierVertex(cx - XConvexity, cy + YConvexity, cx + XConvexity, cy - YConvexity, cx + SymbolWidth/2, cy + SymbolHeight/2) //back to bottom right
    g.endShape()
  }  
  
}
