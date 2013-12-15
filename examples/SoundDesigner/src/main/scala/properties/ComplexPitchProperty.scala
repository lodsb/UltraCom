package ui.properties

import org.mt4j.util.MTColor

import processing.opengl.PGraphicsOpenGL
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
class ComplexPitchProperty(connection: ManipulableBezierConnection, numberOfBuckets: Int) extends ComplexProperty(connection, numberOfBuckets) {
 
  /**
  * Draws this property.
  *
  * As a technical side note, this method uses many small rectangles aligned with the associated bezier curve to represent this property,
  * as opposed to a single complex polygon. This is due to Processing not being able to reliably fill complex polygons correctly.
  */  
  override def draw(g: PGraphicsOpenGL) = {
    import ComplexVolumeProperty._
    val (playbackIndex, playbackT) = this.connection.associatedPath match {case Some(path) => path.playbackPosition case None => (0,0.0f)}
    val thisIndex = this.connection.associatedPath match {case Some(path) => path.indexOf(this.connection) case None => -1}

    var highlightedAreas = Set[(Float, Float, Float)]() //the areas on the connection which are to be highlighted, each given by a center curve parameter and two curve parameters specifiying the left/right bound of the area
    this.connection.associatedPath match {
      case Some(path) => {
        val toolRegistryEntries = path.toolRegistryEntries
        toolRegistryEntries.foreach(entry => { //look at every registered tool
          val tool = entry._1
          if (tool.propertyType == this.propertyType) {
            entry._2.foreach(value => { //entry._2 is of form (connection, curveParameter, manipulationRadius)
              val connection = value._1
              val curveParameter = value._2
              val manipulationRadius = value._3
              if (this.connection == connection) { //if this connection is affected
                val bound = Bezier.pointsWithArcDistance(this.connection.apply)(curveParameter)(manipulationRadius) //we obtain the left and right bounds of the affected area on the connection
                highlightedAreas += ((bound(0), curveParameter, bound(1))) //assuming there are always exactly two bounds, which is OK here
              }
            })
          }
        })
      }
      case None => {}
    }

    g.noStroke()
    val steps = (this.buckets - 1) * RectanglesPerBucket //number of rectangles used to approximate each bezier curve on the path
    (0 to steps-1).foreach(step => { //iteratively draw the rectangles
      val t = this.connection.toCurveParameter(step / steps.toFloat)
      val t2 = this.connection.toCurveParameter((step+1)/steps.toFloat)
      
      val (fromX, fromY) = this.connection(t)
      val (toX, toY) = this.connection(t2)      
      val (tFromX, tFromY) = this.connection.tangent(t)
      val (tToX, tToY) = this.connection.tangent(t2)
          
      val aFrom = math.atan2(tFromY, tFromX) - HALF_PI
      val aTo = math.atan2(tToY, tToX) - HALF_PI
      
      val hasBeenPlayed = (thisIndex < playbackIndex || thisIndex == playbackIndex && t < playbackT)
      
      val red = this.propertyType.PropertyColor.getR //* (if (hasBeenPlayed) 0.0f else 1.0f)
      val green = this.propertyType.PropertyColor.getG //* (if (hasBeenPlayed) 0.0f else 1.0f)
      val blue = this.propertyType.PropertyColor.getB //* (if (hasBeenPlayed) 0.0f else 1.0f)
      var alpha: Float = if (hasBeenPlayed) this.propertyType.ProgressColorAlpha else this.propertyType.ColorAlpha
      
      val highlightedArea = if (highlightedAreas.size > 0) Some(highlightedAreas.minBy(area => math.abs(area._2 - t))) else None //get the area whose center is closest to t, if there is one
      
      highlightedArea.foreach(area => { //if there is an area
        if (area._1 <= t && area._3 >= t) { //and if said area encloses t
          val curveRadius = (area._3 - area._1)/2
          val curveDistToCenter = math.abs(t - area._2)
          val normDeviation = curveDistToCenter/curveRadius
          alpha = this.propertyType.HighlightedColorAlpha * (1 - normDeviation) + alpha * normDeviation //we adjust the alpha value to highlight that particular area
        }    
      })
      
      g.fill(red, green, blue, alpha)
      
      g.beginShape() //defines a small rectangle which fits the corresponding segment on the bezier curve
      g.vertex(fromX, fromY)
      g.vertex((math.cos(aFrom)*(-this.mappedValues(step/RectanglesPerBucket)) + fromX).toFloat, (math.sin(aFrom)*(-this.mappedValues(step/RectanglesPerBucket)) + fromY).toFloat)
      g.vertex((math.cos(aTo)*(-this.mappedValues((step+1)/RectanglesPerBucket)) + toX).toFloat, (math.sin(aTo)*(-this.mappedValues((step+1)/RectanglesPerBucket)) + toY).toFloat)     
      g.vertex(toX, toY)
      g.endShape(CLOSE)
    })    
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
  
  def propertyType = {
    PitchPropertyType
  } 

  override def toString = {
    "ComplexPitchProperty"
  }
  
  
}
