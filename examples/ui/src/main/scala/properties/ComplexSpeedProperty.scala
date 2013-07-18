package ui.properties

import org.mt4j.util.MTColor

import processing.core.PGraphics
import processing.core.PConstants._

import ui._
import ui.properties.types._
import ui.tools.Tool
import ui.paths._
import ui.util._


object ComplexSpeedProperty {
  val Buckets = 50 //default number of buckets
  val LineWeight = 1
  
  def apply(connection: ManipulableBezierConnection) = {
     new ComplexSpeedProperty(connection, Buckets)
  }
  
  def apply(connection: ManipulableBezierConnection, numberOfBuckets: Int) = {
     new ComplexSpeedProperty(connection, numberOfBuckets)
  }
}

class ComplexSpeedProperty(connection: ManipulableBezierConnection, numberOfBuckets: Int) extends ComplexProperty(connection, numberOfBuckets) {
  
  /**
  * Draws this property.
  */ 
  override def draw(g: PGraphics) = {
    import ComplexSpeedProperty._
    g.noFill()
    g.strokeWeight(LineWeight)
    val steps = this.buckets
    (0 to steps - 1).foreach(step => { //iteratively draw the lines
      val t = this.connection.toCurveParameter((step+0.5f)/steps.toFloat)
      val alphaValue = this.mappedValues(step)
      g.stroke(SpeedPropertyType.PropertyColor.getR, SpeedPropertyType.PropertyColor.getG, SpeedPropertyType.PropertyColor.getB, alphaValue)
      val (x,y) = this.connection(t)
      val (tx, ty) = this.connection.tangent(t)
      val a = math.atan2(ty, tx) - HALF_PI
      g.beginShape(LINES) //draws lines which are orthogonal to the bezier curve at the point of intersection
      g.vertex((-math.cos(a) * this.visualWidth + x).toFloat, (-math.sin(a) * this.visualWidth + y).toFloat)
      g.vertex((math.cos(a) * this.visualWidth + x).toFloat, (math.sin(a) * this.visualWidth + y).toFloat)
      g.endShape()
      /*g.noStroke()
      g.fill(0,0,0)
      g.ellipse(x.toInt,y.toInt,4,4)*/
    })    
  }
  
  override def range = {
    SpeedPropertyType.range
  }
  
  override def visualWidth = {
    SpeedPropertyType.width/4
  }  
  
  override def maxWidth = {
    SpeedPropertyType.width
  }
  
  /**
  * Maps property values to opacity values in the range (0,255), 255 being completely opaque.
  */
  override protected def mappedValues(index: Int) = {
    /* OUTDATED
    0->0, 1->1, 2->3, 3->9, 4->27...
    this mapping was chosen because it leads to lines being added inbetween,
    which means there is no jumping of lines if the user manipulates the values of this property
    */
    //val (min, max) = this.range
    //val value = this.values(index)
    //println("value is " + value)
    //if (value == 0) 0 else (math.pow(3, math.round(value) - 1)).toFloat
    //(math.pow(2, this.values(index)) - 1).toFloat
    
    val (min, max) = this.range
    (this.values(index) - min)/(max - min) * 255
  }
  
}
