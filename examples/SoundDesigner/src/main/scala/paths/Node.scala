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

import org.mt4j.util.{SessionLogger, MTColor, Color}
import org.mt4j.util.math.Vector3D
import org.mt4j.util.math.Vertex
import org.mt4j.util.math.Matrix
import org.mt4j.types.{Vec3d}

import org.mt4j.util.animation.Animation
import org.mt4j.util.animation.AnimationEvent
import org.mt4j.util.animation.IAnimationListener
import org.mt4j.util.animation.MultiPurposeInterpolator

import processing.opengl.PGraphicsOpenGL
import processing.core.PConstants._

import ui._
import ui.util._
import ui.paths.types._
import ui.events._
import ui.usability._
import ui.audio._
import ui.persistence._



object Node {
  
  def apply(app: Application, typeOfNode: NodeType, associatedPath: Option[Path], center: Vector3D) = {
    new Node(app, typeOfNode, associatedPath, center)
  }
  
  def apply(app: Application, typeOfNode: NodeType, associatedPath: Option[Path], center: (Float, Float)) = {
    new Node(app, typeOfNode, associatedPath, Vec3d(center._1, center._2))
  }  
  
}

/**
* This abstract class represents a node.
* Each node is associated with a specific node type defining the visual appearance and interactive behaviour of the node, 
* both of which can be altered at runtime simply by changing the node type.
*
*/
class Node(val app: Application, var typeOfNode: NodeType, var associatedPath: Option[Path], center: Vector3D) extends MTEllipse(app, Vec3d(0,0), NodeType.Radius, NodeType.Radius) with NodeFeedback with NodeFeedforward with Persistability with Identifier {
    protected[paths] var inEx = true
    private var scaleFactor = 1.0f //the current scale of this node
    private var rotationAngle = 0.0f //the current rotation angle (in degrees) of this node
    private var currentColor = this.nodeType.backgroundColor
    private var clockValue = 0 //the current clock value, which lies between 0 and 360
    
    this.setup()
    
    def inExistence: Boolean = {
      this.inEx
    }
    
    protected[paths] def setup(): Unit = {
      this.globalPosition := center
      this.nodeType = typeOfNode //overloaded assignment operator =, sets up interation on this node in the process
      this.feedforwardNodeType = this.nodeType
    }
    
    /**
    * Returns whether this node is nearby the specified coordinate. More precisely, returns true iff the specified coordinate is in this node's vicinity.
    */
    def isNearby(x: Float, y: Float): Boolean = {
      this.distance(x,y) <= this.nodeType.size * (this.vicinity - this.radius)
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
    *
    * Note that a point on the outline of this node has a distance of 0 and
    * a point inside the node has a negative distance.
    */
    def distance(x: Float, y: Float): Float = {
      val (cx, cy) = this.position 
      math.sqrt((x-cx)*(x-cx) + (y-cy)*(y-cy)).toFloat - this.nodeType.size * this.radius
    }
 
    /**
    * Returns the distance of the specified coordinate from this node.
    *
    * Note that a point on the outline of this node has a distance of 0 and
    * a point inside the node has a negative distance.
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
    
    /**
    * Returns the relative size of this node.
    */
    def size: Float = {
      this.nodeType.size
    }
    
    override def drawComponent(g: PGraphicsOpenGL) = {
      val center = this.getCenterPointLocal()
      val cx = center.getX()
      val cy = center.getY()
      val fillColor = this.color
      val strokeColor = this.nodeType.strokeColor 
      
      //draw vicinity
      if (this.vicinity > 0) {
        g.noStroke()
        g.fill(0, 20, 80, 15)
        g.ellipse(cy, cy, this.vicinity*2, this.vicinity*2)
      }      
      
      //draw basic node appearance
      g.fill(fillColor.getR, fillColor.getG, fillColor.getB, fillColor.getA)
      g.stroke(strokeColor.getR, strokeColor.getG, strokeColor.getB, strokeColor.getA)
      g.strokeWeight(this.nodeType.strokeWeight)
      g.ellipse(cx, cy, this.radius*2, this.radius*2)      
      
      //draw clock
      g.noFill()
      g.stroke(0, 0, 0, 100)
      g.strokeWeight(1)
      g.arc(cx, cy, this.radius*2 + 20, this.radius*2 + 20, 0, this.clockValue/360f * 2*PI)
      
      //optionally draw symbol
      this.drawSymbol(g)
    }
    
    private def drawSymbol(g: PGraphicsOpenGL) = {
      this.nodeType.symbol match {
        case Some(symbol) => {
          val center = this.getCenterPointLocal()
          val cx = center.getX()
          val cy = center.getY()
          val r = 0.50f * this.radius    
              
          val feedforwardSymbol = this.feedforwardNodeType.symbol match {
            case Some(ffSymbol) => {ffSymbol}
            case None => {symbol}
          }
          
          val symbolColor = this.nodeType.foregroundColor  
          g.fill(symbolColor.getR(), symbolColor.getG(), symbolColor.getB(), symbolColor.getA())
          g.noStroke()
          g.beginShape()
          val precision = 256 
          (1 to precision).foreach(value => { 
            val (x,y) = SymbolInterpolator.interpolate(symbol((cx,cy),r), feedforwardSymbol((cx,cy),r), value.toFloat/precision, this.feedforwardValue)
            val (rotatedX, rotatedY) = this.transformPoint((x,y))
            g.vertex(rotatedX, rotatedY)
          })
          g.endShape(CLOSE)     
        }
        case None => {}
      }     
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
    * Applies a coordinate transformation to the specified point.
    * More precisely, this node's center becomes the center of the coordinate system and the axes correspond to this node's associated paths's tangent,
    * if there is an associated path. Otherwise, the tangent is set to (0,0).
    */
    private def transformPoint(point: (Float, Float)): (Float, Float) = {
      val center = (this.getCenterPointLocal.getX, this.getCenterPointLocal.getY)
      val gradient = this.associatedPath match {
        case Some(path) => path.tangent(this)
        case None => (0.0f, 0.0f)
      }  
      Functions.transform(center, gradient, point)          
    }
    

     /**
    * Sets the scale factor of this node in local space.
    */
    def setScale(scale: Float): Unit = {
      this.scale(1/this.scaleFactor, 1/this.scaleFactor, 1/this.scaleFactor, this.getCenterPointLocal, TransformSpace.LOCAL) //reset scale
      this.scale(scale, scale, scale, this.getCenterPointLocal, TransformSpace.LOCAL)
      this.scaleFactor = scale //update current scale factor
    }   
    
    def getScaleFactor: Float = {
      this.scaleFactor
    }


    /**
    * Sets the rotation angle (in degrees) of this node in local space, with the rotation point being the center of this node.
    */
    def setRotation(angle: Float): Unit = {
      /*this.rotateZ(this.getCenterPointLocal, -this.rotationAngle, TransformSpace.LOCAL) //reset rotation of this node
      this.rotateZ(this.getCenterPointLocal, angle, TransformSpace.LOCAL) //then apply new rotation
      this.rotationAngle = angle //finally update current rotation angle */
    }
 
    
    def updateRotation() = {
      /*val (tx,ty) = this.associatedPath match {
        case Some(path) => path.tangent(this) //calculate tangent at node
        case None => (0f, 0f)
      }
      val atan2 = math.atan2(ty,tx).toFloat //get angle for mirrored vector
      val angle = ((if (atan2 > 0) atan2 else (2*math.Pi + atan2)) * 360 / (2*math.Pi)).toFloat //convert to degrees
      this.setRotation(angle) //set rotation of node to the calculated angle  // */
    }
  
    
    /**
    * Not used anymore, threading issues have been dealt with.
    * 
    * Sets the local matrix back to a legal state.
    *
    * This is necessary because MT4J simply messes up the local matrix at some point for no apparent reason,
    * which then produces weird results.
    */
    def resetLocalMatrix() = {
      val nodeSize = this.scaleFactor  
      val localMatrix =   Array(nodeSize, 0f,        0f,       0f,
                              0f,       nodeSize,  0f,       0f,
                              0f,       0f,        nodeSize, 0f,
                              this.getCenterPointLocal.getX, this.getCenterPointLocal.getY, 0f, 1f
                          )   
      this.setLocalMatrix(new Matrix(localMatrix)) //correcting local matrix, which seems necessary for some unknown reason...      
    }
    
    
    /**
    * Returns a deep copy of this node's current color.
    */
    def color() = {
      new MTColor(this.currentColor.getR, this.currentColor.getG, this.currentColor.getB, this.currentColor.getA)
    }
    
    def setColor(col: MTColor) = {
      this.currentColor = Color.fromMtColor(col)
    }
    
    def setClock(value: Int) = {
      this.clockValue = value 
    }
    
    
    def setTapped(isTapped: Boolean) = {
      this.setColor(if (isTapped) this.nodeType.tapColor else this.nodeType.backgroundColor)
    }
    
    /**
    * Sets the global position of this node after resetting the local matrix.
    */
    /*override def setPositionGlobal(position: Vector3D) = {
      //this.resetLocalMatrix()
      super.setPositionGlobal(position)
    }*/
    
    
    override def toXML = {
      val (x,y) = this.position
      "<node type = '" + this.nodeType.toString + "' x = '" + x + "' y = '" + y + "'/>"
    }    

}
