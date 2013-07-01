package ui.properties

import org.mt4j.util.MTColor

import processing.core.PGraphics
import processing.core.PConstants._

import ui._
import ui.properties.types._
import ui.tools.Tool
import ui.paths._
import ui.util._

object SimpleVolumeProperty {

  def apply(node: ManipulableNode) = {
     new SimpleVolumeProperty(node)
  }

}

/**
* This class represents a simple volume property.
*/
class SimpleVolumeProperty(node: ManipulableNode) extends SimpleProperty {
  
  override def draw(g: PGraphics) = {
    val playbackT = node.playbackPosition
    val center = node.getCenterPointLocal
    val (x,y) = (center.getX, center.getY)
    val innerRadius = node.radius
    val outerRadius = innerRadius + this.mappedValue
    g.noStroke()

    val steps = 100 //number of rectangles to approximate a circle
    (0 to steps-1).foreach(step => { //iteratively draw rectangles 
      val t = step/steps.toFloat
      val t2 = (step+1)/steps.toFloat
      val (x1, y1) = ((innerRadius*math.cos(math.Pi*t) + x).toFloat, (innerRadius*math.sin(math.Pi*t) + y).toFloat)
      val (x2, y2) = ((outerRadius*math.cos(math.Pi*t) + x).toFloat, (outerRadius*math.sin(math.Pi*t) + y).toFloat)
      val (x3, y3) = ((outerRadius*math.cos(math.Pi*t2) + x).toFloat, (outerRadius*math.sin(math.Pi*t2) + y).toFloat)
      val (x4, y4) = ((innerRadius*math.cos(math.Pi*t2) + x).toFloat, (innerRadius*math.sin(math.Pi*t2) + y).toFloat) 
      
      val red = VolumePropertyType.PropertyColor.getR
      val green = VolumePropertyType.PropertyColor.getG
      val blue = VolumePropertyType.PropertyColor.getB
      val alpha = if (t < playbackT) VolumePropertyType.ProgressColorAlpha else VolumePropertyType.ColorAlpha     
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
    VolumePropertyType.range
  }
  
  override def visualWidth = {
    VolumePropertyType.width
  }  
  
  override def maxWidth = {
    VolumePropertyType.width
  }  
  
}