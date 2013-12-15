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
import ui.persistence._


/**
* This abstract class represents a connection between two (not necessarily distinct) nodes,
* possibly with further nodes altering the shape and/or behaviour of the connection.
*/
abstract class Connection(app: Application, val nodes: List[Node]) extends AbstractVisibleComponent(app) with Persistability {
  
  this.setPickable(false)
  
  /**
  * Returns the point on this connection corresponding to the specified connection parameter.
  */  
  def apply(parameter: Float): (Float, Float)
  
  /**
  * Returns the tangent at the point corresponding to the specified connection parameter.
  */
  def tangent(parameter: Float): (Float, Float)
  
  /**
  * Returns the path associated with this connection.
  */
  def associatedPath = {
    this.nodes.head.associatedPath
  }
 
  
  /** Returns the parameter in [0,1] which yields the point on this connection closest to the specified coordinate.
  */  
  def parameterizedClosestPoint(position: Vector3D): Float
 
  /**
  * Returns the point on this connection closest to the specified coordinate.
  */
  def closestPoint(position: Vector3D): (Float, Float)
  
  override def toString = {
    "(" + this.nodes.map(_ + "").reduce((n1, n2) => n1 + " ~ " + n2) + ")"
  }
  
}
