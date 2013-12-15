package ui.properties

import org.mt4j.util.MTColor

import processing.opengl.PGraphicsOpenGL
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
  override def draw(g: PGraphicsOpenGL) = {
    import ComplexSpeedProperty._
 
    val (playbackIndex, playbackT) = this.connection.associatedPath match {case Some(path) => path.playbackPosition case None => (0,0.0f)} //playback position as (connection, curve parameter) pair
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
    
    g.noFill()
    g.strokeWeight(LineWeight)
    val steps = this.buckets
    (0 to steps - 1).foreach(step => { //iteratively draw the lines
      val t = this.connection.toCurveParameter((step+0.5f)/steps.toFloat)
      val lineLength = this.mappedValues(step)
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
        
      g.stroke(red, green, blue, alpha)           
      val (x,y) = this.connection(t)
      val (tx, ty) = this.connection.tangent(t)
      val a = math.atan2(ty, tx) - HALF_PI
      g.beginShape(LINES) //draws lines which are orthogonal to the bezier curve at the point of intersection
      g.vertex((-math.cos(a) * lineLength + x).toFloat, (-math.sin(a) * lineLength + y).toFloat)
      g.vertex((math.cos(a) * lineLength + x).toFloat, (math.sin(a) * lineLength + y).toFloat)
      g.endShape()
    })    
  }
  
  override def range = {
    SpeedPropertyType.range
  }

  override def visualWidth = {
    PitchPropertyType.width
  }
  
  override def maxWidth = {
    SpeedPropertyType.width
  }
  
  def propertyType = {
    SpeedPropertyType
  }
  
  /**
  * Maps property values to opacity values in the range (0,255), 255 being completely opaque.
  * Not used anymore.
  */
  /*override protected def mappedValues(index: Int) = {
    //OUTDATED
    //0->0, 1->1, 2->3, 3->9, 4->27...
    //this mapping was chosen because it leads to lines being added inbetween,
    //which means there is no jumping of lines if the user manipulates the values of this property
    //
    //val (min, max) = this.range
    //val value = this.values(index)
    //println("value is " + value)
    //if (value == 0) 0 else (math.pow(3, math.round(value) - 1)).toFloat
    //(math.pow(2, this.values(index)) - 1).toFloat
    
    val (min, max) = this.range
    (this.values(index) - min)/(max - min) * 255
  }*/
  
  override def toString = {
    "ComplexSpeedProperty"
  }  
  
}
