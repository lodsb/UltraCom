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


object SimpleConnection {
  
  /**
  * Constructs a simple straight connection between two nodes.
  */
  def apply(app: Application, firstNode: Node, secondNode: Node) = {
    new SimpleConnection(app, firstNode, secondNode)
  }

}


class SimpleConnection(app: Application, node1: Node, node2: Node) extends Connection(app, List(node1, node2)) {
  this.setPickable(false)
  
  /**
  * Returns the point on this line corresponding to the specified parameter.
  * Not implemented.
  */
  override def apply(parameter: Float): (Float, Float) = {
    node1.position //TODO implement correctly when needed
  }  
  
  /**
  * Not implemented.
  */  
  override def tangent(parameter: Float): (Float, Float) = {
    node1.position //TODO implement correctly when needed
  }
  
  /**
  * Not implemented.
  */ 
  override def closestPoint(position: Vector3D): (Float, Float) = {
    node1.position //TODO implement correctly when needed
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
    //g.fill(0,0,0)
    g.noFill()
    g.strokeWeight(1)
    g.stroke(0,0,0,40)
    val (x1, y1) = firstNode.position
    val (x2, y2) = secondNode.position
    g.line(x1, y1, x2, y2)
  }   
  
  def firstNode = {
    this.node1
  }
  
  def secondNode = {
    this.node2
  }
  
  override def toString = {
    "Connection(" + this.firstNode + " " + this.secondNode + ")"
  }
  
  
  override def toXML = {
    "<SimpleConnection></SimpleConnection>"
  }  
  
}
