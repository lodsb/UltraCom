package ui.paths

import org.mt4j.Application

import org.mt4j.components.TransformSpace
import org.mt4j.components.MTComponent
import org.mt4j.components.visibleComponents.shapes.MTEllipse

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
import org.mt4j.types.{Vec3d}

import processing.core.PGraphics

import ui.paths.types._


object Node {
  
  def apply(app: Application, nodeType: NodeType, center: (Float, Float)): Node = {
      new Node(app, nodeType, None, Vec3d(center._1, center._2))
  }
  
  def apply(app: Application, nodeType: NodeType, center: Vector3D): Node = {
      new Node(app, nodeType, None, center)
  }
  
  def apply(app: Application, nodeType: NodeType, associatedPath: Option[Path], center: Vector3D): Node = {
      new Node(app, nodeType, associatedPath, center)
  }
  
  def apply(app: Application, nodeType: NodeType, associatedPath: Option[Path], center: (Float, Float)): Node = {
      new Node(app, nodeType, associatedPath, Vec3d(center._1, center._2))
  }  
 
  
}

/**
* This class represents a node.
* Each node is associated with a specific node type defining the visual appearance and interactive behaviour of the node, 
* both of which can be altered at runtime simply by changing the node type.
*
*/
class Node(val app: Application, var typeOfNode: NodeType, var associatedPath: Option[Path], center: Vector3D) extends MTEllipse(app, center, NodeType.Radius, NodeType.Radius) {
    var scaleFactor = 1.0f //the current scale of this node
    this.setup()
    
    protected def setup(): Unit = {
      this.nodeType = typeOfNode //overloaded assignment operator =, sets up interation on this node in the process
    }
    
    /**
    * Returns whether this node is nearby the specified coordinate. More precisely, returns true iff the specified coordinate is in this node's vicinity.
    */
    def isNearby(x: Float, y: Float): Boolean = {
      this.distance(x,y) <= this.vicinity
    }
    
    def isNearby(position: Vector3D): Boolean = {
      this.isNearby(position.getX, position.getY)
    }
    
    override def toString = {
      val (cx, cy) = this.position 
      this.nodeType + ""//+ "(" + cx + ", " + cy +")"
    }
    
    /**
    * Returns the current position of this node as a tuple.
    */
    def position: (Float, Float) = {
      val center = this.getCenterPointGlobal()
      return (center.getX(), center.getY())
    }
    
    /**
    * Returns the current position of this node as a Vertex.
    */
    def positionAsVertex: Vertex = {
      val center = this.getCenterPointGlobal()
      return new Vertex(center.getX(), center.getY())
    }
    
    /**
    * Returns the distance of the specified coordinate from this node.
    */
    def distance(x: Float, y: Float): Float = {
      val (cx, cy) = this.position 
      math.sqrt((x-cx)*(x-cx) + (y-cy)*(y-cy)).toFloat - this.radius
    }
 
    /**
    * Returns the distance of the specified coordinate from this node.
    */    
    def distance(position: Vector3D): Float = {
      this.distance(position.getX, position.getY)
    }
    
    /**
    * Returns the radius of this node.
    */
    def radius: Float = {
      this.nodeType.radius
    }
    
    /**
    * Returns the maximum distance from the node which is still considered vicinity.
    */
    def vicinity: Float = {
      this.nodeType.vicinity
    }
    
    override def drawComponent(g: PGraphics) = {
      this.nodeType.drawComponent(g, this)
    }
    
    def nodeType = {
      this.typeOfNode
    }
    
    def nodeType_=(newType: NodeType) = {
       this.typeOfNode = newType
       this.nodeType.setupInteraction(app, this)
       this.nodeType
    }
    
     /**
    * Sets the scale factor of this node.
    */
    def setScale(scale: Float): Unit = {
      this.scale(1/this.scaleFactor, 1/this.scaleFactor, 1/this.scaleFactor, this.getCenterPointLocal(), TransformSpace.LOCAL) //reset scale
      this.scale(scale, scale, scale, this.getCenterPointLocal(), TransformSpace.LOCAL)
      this.scaleFactor = scale //update current scale factor
    }   
    
}
