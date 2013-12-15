package ui.paths

import org.mt4j.Application

import org.mt4j.components.MTComponent
import org.mt4j.components.visibleComponents.shapes.MTPolygon
import org.mt4j.components.visibleComponents.AbstractVisibleComponent

import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragProcessor 
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragEvent
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapProcessor 
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent
import org.mt4j.input.inputProcessors.IGestureEventListener
import org.mt4j.input.inputProcessors.MTGestureEvent
import org.mt4j.input.gestureAction.DefaultDragAction
import org.mt4j.input.gestureAction.InertiaDragAction

import org.mt4j.util.Color
import org.mt4j.util.math.Vector3D
import org.mt4j.util.math.Vertex
import org.mt4j.types.Vec3d

import processing.opengl.PGraphicsOpenGL
import processing.core.PConstants._

import ui._
import ui.util._
import ui.paths._
import ui.paths.types._
import ui.properties._
import ui.properties.types._


object ManipulableBezierConnection {

  protected[paths] val LineColor = Color(0, 130, 130, 150)  
  val LineNumber = 100 //number of lines used to approximate the bezier curve
  val MaxDotNumber = 500 //maximum number of dots between a control point and its curve
  val LineWeight = 1
  
  /**
  * Constructs a new manipulable bezier connection with the control node placed in the middle of the two specified nodes.
  */
  def apply(app: Application, startNode: Node, endNode: Node) = {
    new ManipulableBezierConnection(app, startNode, BezierConnection.createControlNode(app, startNode, endNode), endNode)
  }   
  
  /**
  * Constructs a new manipulable bezier connection with the control node already defined by the caller.
  */
  def apply(app: Application, startNode: Node, controlNode: Node, endNode: Node) = {
    new ManipulableBezierConnection(app, startNode, controlNode, endNode)
  } 

}


/**
* This class represents a manipulable connection between two nodes on a path based on a quadratic bezier curve.
* The following manipulations are supported:
* <ul>
*   <li> Change of pitch over time </li>
*   <li> Change of volume over time </li>
*   <li> Change of speed over time </li>
* </ul>
*/
class ManipulableBezierConnection(app: Application, startNode: Node, controlNode: Node, endNode: Node) extends BezierConnection(app, startNode, controlNode, endNode) {
  private var properties: Map[PropertyType, ComplexProperty] =
    Map(
      (VolumePropertyType -> ComplexVolumeProperty(this)),
      (SpeedPropertyType -> ComplexSpeedProperty(this)),
      (PitchPropertyType -> ComplexPitchProperty(this))
    )              
  
  private var curveParameterFunction = Bezier.toCurveParameter(this.apply) //holds a function which returns for each arc length parameter the corresponding (approximated) curve parameter
 
   def setProperties(propertyMap: Map[PropertyType, ComplexProperty]) = {
     this.properties = propertyMap
   }
  
  /**
  * Updates the specified property.
  */
  protected[paths] def updateProperty(propertyIdentifier: PropertyType, curveParameter: Float, radius: Float, value: Float) = {
    if (this.properties.contains(propertyIdentifier)) {
      val property = this.properties(propertyIdentifier)
      property.update(valueFunction(property, curveParameter, radius, value))
    }
  }
  
  /**
  * Returns the mean value of the specified property over all of its buckets, or -1 if the property does not exist.
  */
  protected[paths] def propertyMean(propertyIdentifier: PropertyType): Float = {
    if (this.properties.contains(propertyIdentifier)) this.properties(propertyIdentifier).mean else -1
  }
  
  /**
  * Returns the sum of the specified property over all of its buckets, or -1 if the property does not exist.
  */
  protected[paths] def propertySum(propertyIdentifier: PropertyType): Float = {
    if (this.properties.contains(propertyIdentifier)) this.properties(propertyIdentifier).sum else -1
  }
  
  /**
  * Returns the partial sum of the specified property up to the given bucket, or -1 if the property does not exist.
  */
  protected[paths] def partialPropertySum(propertyIdentifier: PropertyType, bucket: Int): Float = {
    if (this.properties.contains(propertyIdentifier)) this.properties(propertyIdentifier).partialSum(bucket) else -1   
  }
  
  /**
  * Returns the value of the specified property at the given bucket, or -1 if the property does not exist.
  *
  * @throws ArrayIndexOutOfBoundsException if the specified bucket is out of range
  */
  protected[paths] def propertyValue(propertyIdentifier: PropertyType, bucket: Int): Float = {
    if (this.properties.contains(propertyIdentifier)) this.properties(propertyIdentifier).apply(bucket) else -1
  }
  
  /**
  * Returns the number of buckets of the specified property, or 0 if the property does not exist.
  */
  protected[paths] def propertyBuckets(propertyIdentifier: PropertyType): Int = {
    if (this.properties.contains(propertyIdentifier)) this.properties(propertyIdentifier).buckets else 0
  }  
  
  /**
  * Returns a function which can be used to update the buckets of the specified property.
  * More precisely, the returned function yields for every property bucket a value.
  * Note that currently this method will not work with a bucket number lower than 2.
  */
  private def valueFunction(property: ComplexProperty, curveParameter: Float, radius: Float, value: Float): Int => Float = {
    val (min, max) = property.range
    val arcLength = Bezier.arcLength(this.apply)
    val arcLengthParameter = Bezier.quadraticCurveToArcLengthParameter(this.startNode.position, this.controlNode.position, this.endNode.position)(curveParameter)
    var leftEdge = (arcLengthParameter - radius/arcLength) * (property.buckets - 1) //the first bucket in the property which is affected
    var rightEdge = (arcLengthParameter + radius/arcLength) * (property.buckets - 1) //the last bucket in the property which is affected
    val leftBucket = math.round(leftEdge)
    val rightBucket = math.round(rightEdge)
    if (leftEdge.toInt == rightEdge.toInt) {  //if leftEdge and rightEdge do not encompass a bucket (that is, the bucket is 'inbetween'), expand one of the edges
      if (leftEdge - leftBucket > 0) leftEdge = leftBucket - 1
      else if (rightEdge - rightBucket < 0) rightEdge = rightBucket + 1
    }

    x => {
      val xMapped = (x - leftEdge)/(rightEdge - leftEdge) //mapping from [leftEdge, rightEdge] to [0,1]
      property(x) * (1-Functions.mirroredSigmoid(xMapped)) + Functions.mirroredSigmoid(xMapped) * ((value/property.maxWidth) * (max - min) + min)
        /*
        explanation:
          if x mapped onto [0,1] is 0.5, we want to discard the old value completely since x is in the center of the manipulation range
          if x mapped onto [0,1] is less than 0 or greater than 1, we want to keep the old value since x is not in the manipulation range
          inbetween, we interpolate using a mirrored sigmoid function because this is smooth and just looks nice ;)
        */
    }
  }
  
  /**
  * Updates the arc length parameter to curve parameter mapping of this curve.
  * This must be done every time a node of this connection is moved.
  */
  protected[paths] def updateCurveParameters() = {
    this.curveParameterFunction = Bezier.toCurveParameter(this.apply)
  }
  
  /**
  * Returns the curve parameter corresponding to the specified arc length parameter.
  */
  def toCurveParameter(value: Float) = {
    this.curveParameterFunction(value)
  }

  
  /**
  * Draws this connection.
  */
  override def drawComponent(g: PGraphicsOpenGL) = {
    val sync = this.associatedPath match {case Some(path) => path case None => this}
    sync.synchronized {
      this.properties.values.foreach(_.draw(g))
      
      import ManipulableBezierConnection._
      g.stroke(LineColor.getR, LineColor.getG, LineColor.getB, LineColor.getA)
      g.strokeWeight(LineWeight)
      val steps = LineNumber - 1 //number of lines used to approximate each bezier curve on the path
      val p1 = this.nodes(0).position
      val pc = this.nodes(1).position
      val p2 = this.nodes(2).position
      val toCurveParameter = Bezier.toCurveParameter(this.apply)
      (0 to steps-1).foreach(step => { //iteratively draw the rectangles 
        val t = toCurveParameter(step / steps.toFloat)
        val t2 = toCurveParameter((step+1)/steps.toFloat)      
        val (fromX, fromY) = Bezier.quadraticCurve(p1, pc, p2)(t)
        val (toX, toY) = Bezier.quadraticCurve(p1, pc, p2)(t2)  
        g.line(fromX, fromY, toX, toY)
      }) 
      
      //draw dotted line from control node to connection
      g.stroke(0, 0, 0, LineColor.getA)
      val closestPoint = this.closestPoint(Vec3d(pc._1, pc._2))
      val distance = Vector.euclideanDistance(pc, closestPoint)
      val dots = math.round(distance/Ui.width * MaxDotNumber)
      val line = Functions.line(pc, closestPoint)_
      (0 to dots - 1).foreach(dot => {
        val (x,y) = line(dot/dots.toFloat)
        g.point(x, y)
      })
    }
  }   
  
 
  override def toXML = {
    val start = "<connection type = 'ManipulableBezierConnection'>"
    val nodes = "<nodes>" + this.nodes.map(_.toXML).foldLeft("")((n1, n2) => n1 + " " + n2) + "</nodes>"
    val properties = "<properties>" + this.properties.values.map(_.toXML).foldLeft("")((p1, p2) => p1 + " " + p2) + "</properties>"
    val end = "</connection>"
    start + nodes + properties + end
  }
  
}
 
