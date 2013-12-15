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

import ui.paths.types._
import ui._
import ui.properties.types._
import ui.util._


object TimeConnection {

  private val MaxDotNumber = 500
  private val StrokeAlpha = 180
  
  /**
  * Constructs a time connection between two nodes, with the first one being a time node.
  */
  def apply(app: Application, timeNode: TimeNode, startNode: Node) = {
    new TimeConnection(app, timeNode, this.createTimeConnectionNode(app, timeNode, startNode), startNode)
  }

  def createTimeConnectionNode(app: Application, timeNode: TimeNode, startNode: Node) = {
    Node(app, TimeConnectionNodeType, timeNode.associatedPath, center(timeNode, startNode))
  } 
  
  /**
  * Returns the center between two nodes.
  */
  def center(firstNode: Node, secondNode: Node) = {
    val center1 = firstNode.getCenterPointGlobal
    val center2 = secondNode.getCenterPointGlobal
    new Vector3D((center1.getX+center2.getX)/2, (center1.getY+center2.getY)/2)
  }  
  
  
}


/**
* This class realizes a connection between a time node and a start node.
*/
class TimeConnection(app: Application, val timeNode: TimeNode, val connectionNode: Node, val startNode: Node) extends Connection(app, List(timeNode, connectionNode, startNode)) {
  this.setPickable(false)
  
  /**
  * Returns the point on this line corresponding to the specified parameter.
  * Not implemented.
  */
  override def apply(parameter: Float): (Float, Float) = {
    timeNode.position //TODO implement correctly when needed
  }  
  
  /**
  * Not implemented.
  */  
  override def tangent(parameter: Float): (Float, Float) = {
    timeNode.position //TODO implement correctly when needed
  }
  
  /**
  * Not implemented.
  */ 
  override def closestPoint(position: Vector3D): (Float, Float) = {
    timeNode.position //TODO implement correctly when needed
  }
  
  /**
  * Returns the parameter in [0,1] which yields the point on this line closest to the specified coordinate.
  * Not implemented.
  */
  override def parameterizedClosestPoint(position: Vector3D): Float = {    
    0.5f //TODO implement correctly when needed
  }
  
  /**
  * Draws this connection.
  */
  override def drawComponent(g: PGraphicsOpenGL) = {
    val (x1, y1) = this.timeNode.position
    val (x2, y2) = this.startNode.position

    g.noFill()
    g.strokeWeight(1)    
    val color = SpeedPropertyType.color
    g.stroke(color.getR, color.getG, color.getB, TimeConnection.StrokeAlpha)
    
    val timeNodeVec = Vec3d(x1, y1)
    val timeNodePoint = (timeNodeVec.getX, timeNodeVec.getY)
    val startNodePoint = (x2, y2)
    val distance = Vector.euclideanDistance(timeNodePoint, startNodePoint)
    val dots = math.round(distance/Ui.width * TimeConnection.MaxDotNumber)
    val line = Functions.line(timeNodePoint, startNodePoint)_
    (0 to dots - 1).foreach(dot => {
      val (x,y) = line(dot/dots.toFloat)
      g.point(x, y)
    })    
  }   
  
  
  def updateConnectionNode() = {
    val nodeSize = this.connectionNode.nodeType.size               
    Ui.getCurrentScene.registerPreDrawAction(new RepositionNodeActionThreadSafe(this.connectionNode, TimeConnection.center(this.timeNode, this.startNode)))
    //this.connectionNode.globalPosition := TimeConnection.center(this.timeNode, this.startNode)
    /*
    println("pos global = " + this.connectionNode.getCenterPointGlobal)
    println("pos local = " + this.connectionNode.getCenterPointLocal)
    val matrix = this.connectionNode.getLocalMatrix
    var translation = Vec3d(0,0)
    var rotation = Vec3d(0,0)
    var scaling = Vec3d(0,0)
    matrix.decompose(translation, rotation, scaling)
    println("matrix local = " + matrix)
    println("trans = "+translation+ ", rotation = "+rotation+", scaling = "+scaling) */
    /*
    val currentPosition = this.connectionNode.getCenterPointGlobal
    val newPosition = TimeConnection.center(this.timeNode, this.startNode)
    val translation = Vec3d(newPosition.getX - currentPosition.getX, newPosition.getY - currentPosition.getY)
    this.connectionNode.translateGlobal(translation)
    */
  }
  
  
  override def destroy() = {
    this.connectionNode.destroy()
    super.destroy()
  }
  
  
  override def toXML = {
    val start = "<connection type = 'TimeConnection'>"
    val nodes = "<nodes>" + this.nodes.map(_.toXML).foldLeft("")((n1, n2) => n1 + " " + n2) + "</nodes>"
    val end = "</connection>"
    start + nodes + end
  }  
  
}
