package ui.properties

import org.mt4j.util.MTColor

import processing.core.PGraphics
import processing.core.PConstants._

import ui._
import ui.properties.types._
import ui.tools.Tool
import ui.paths._
import ui.util._


object ComplexPitchProperty{
  val Buckets = 100 //default number of buckets
  val RectanglesPerBucket = 4
  
  def apply(connection: ManipulableBezierConnection) = {
     new ComplexPitchProperty(connection, Buckets)
  }
  
  def apply(connection: ManipulableBezierConnection, numberOfBuckets: Int) = {
     new ComplexPitchProperty(connection, numberOfBuckets)
  }
}

/**
* This class represents a complex pitch property.
*/
class ComplexPitchProperty(connection: ManipulableBezierConnection, numberOfBuckets: Int) extends ComplexProperty {
 
  /**
  * Draws this property.
  *
  * As a technical side note, this method uses many small rectangles aligned with the associated bezier curve to represent this property,
  * as opposed to a single complex polygon. This is due to Processing not being able to reliably fill complex polygons correctly.
  */  
  override def draw(g: PGraphics) = {
    import ComplexVolumeProperty._
    val (playbackIndex, playbackT) = connection.associatedPath match {case Some(path) => path.playbackPosition case None => (0,0.0f)}
    val thisIndex = connection.associatedPath match {case Some(path) => path.indexOf(connection) case None => -1}
    g.noStroke()
    val steps = (this.buckets - 1) * RectanglesPerBucket //number of rectangles used to approximate each bezier curve on the path
    (0 to steps-1).foreach(step => { //iteratively draw the rectangles
      val t = connection.toCurveParameter(step / steps.toFloat)
      val t2 = connection.toCurveParameter((step+1)/steps.toFloat)
      
      val (fromX, fromY) = connection(t)
      val (toX, toY) = connection(t2)      
      val (tFromX, tFromY) = connection.tangent(t)
      val (tToX, tToY) = connection.tangent(t2)
          
      val aFrom = math.atan2(tFromY, tFromX) - HALF_PI
      val aTo = math.atan2(tToY, tToX) - HALF_PI
      
      val red = PitchPropertyType.PropertyColor.getR
      val green = PitchPropertyType.PropertyColor.getG
      val blue = PitchPropertyType.PropertyColor.getB
      val alpha = if (thisIndex < playbackIndex || thisIndex == playbackIndex && t < playbackT) PitchPropertyType.ProgressColorAlpha else PitchPropertyType.ColorAlpha     
      g.fill(red, green, blue, alpha)
      
      g.beginShape() //defines a small rectangle which fits the corresponding segment on the bezier curve
      g.vertex(fromX, fromY)
      g.vertex((math.cos(aFrom)*(-this.mappedValues(step/RectanglesPerBucket)) + fromX).toFloat, (math.sin(aFrom)*(-this.mappedValues(step/RectanglesPerBucket)) + fromY).toFloat)
      g.vertex((math.cos(aTo)*(-this.mappedValues((step+1)/RectanglesPerBucket)) + toX).toFloat, (math.sin(aTo)*(-this.mappedValues((step+1)/RectanglesPerBucket)) + toY).toFloat)     
      g.vertex(toX, toY)
      g.endShape(CLOSE)
    })    
  }  
  
  override def buckets = {
    numberOfBuckets
  }
  
  override def range = {
    PitchPropertyType.range
  }
  
  override def visualWidth = {
    PitchPropertyType.width
  }

  override def maxWidth = {
    PitchPropertyType.width
  }  
  
}
