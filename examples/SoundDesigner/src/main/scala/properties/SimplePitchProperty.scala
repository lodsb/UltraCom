package ui.properties

import org.mt4j.util.MTColor

import processing.opengl.PGraphicsOpenGL
import processing.core.PConstants._

import ui._
import ui.properties.types._
import ui.tools.Tool
import ui.paths._
import ui.util._

object SimplePitchProperty{

  def apply(node: ManipulableNode) = {
     new SimplePitchProperty(node)
  }
  
}

/**
* This class represents a simple pitch property.
*/
class SimplePitchProperty(node: ManipulableNode) extends SimpleProperty {
  
  override def draw(g: PGraphicsOpenGL) = {
    val playbackT = node.playbackPosition
    val isHighlighted = node.toolRegistryEntries.exists(entry => entry._1.propertyType == this.propertyType)
    
    val center = node.getCenterPointLocal
    val (x,y) = (center.getX, center.getY)
    val innerRadius = node.radius
    val outerRadius = innerRadius + this.mappedValue
    g.noStroke()

    val steps = 100 //number of rectangles to approximate a circle
    (0 to steps-1).foreach(step => { //iteratively draw rectangles 
      val t = step/steps.toFloat
      val t2 = (step+1)/steps.toFloat
      val (x1, y1) = ((innerRadius*math.cos(math.Pi*(t+1)) + x).toFloat, (innerRadius*math.sin(math.Pi*(t+1)) + y).toFloat)
      val (x2, y2) = ((outerRadius*math.cos(math.Pi*(t+1)) + x).toFloat, (outerRadius*math.sin(math.Pi*(t+1)) + y).toFloat)
      val (x3, y3) = ((outerRadius*math.cos(math.Pi*(t2+1)) + x).toFloat, (outerRadius*math.sin(math.Pi*(t2+1)) + y).toFloat)
      val (x4, y4) = ((innerRadius*math.cos(math.Pi*(t2+1)) + x).toFloat, (innerRadius*math.sin(math.Pi*(t2+1)) + y).toFloat)     
      
      val red = this.propertyType.PropertyColor.getR
      val green = this.propertyType.PropertyColor.getG
      val blue = this.propertyType.PropertyColor.getB
      val alpha = 
        if (isHighlighted) {
          if (t < playbackT) this.propertyType.HighlightedColorAlpha
          else this.propertyType.HighlightedColorAlpha * 0.7f
        }
        else {
          if (t < playbackT) this.propertyType.ProgressColorAlpha 
          else this.propertyType.ColorAlpha
        }
        
      g.fill(red, green, blue, alpha)
      
      g.beginShape() //defines a small rectangle which fits the corresponding segment on the circle
      g.vertex(x1, y1)
      g.vertex(x2, y2)
      g.vertex(x3, y3)     
      g.vertex(x4, y4)
      g.endShape(CLOSE)
    })    
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
    PitchPropertyType
  }   
  
  
  override def toString = {
    "SimplePitchProperty"
  }  
  
}
