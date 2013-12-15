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

import ui._
import ui.util._
import ui.paths.types._


object BezierConnection {
   
  /**
  * Returns the center between two nodes.
  */
  def center(firstNode: Node, secondNode: Node) = {
    val center1 = firstNode.getCenterPointGlobal
    val center2 = secondNode.getCenterPointGlobal
    new Vector3D((center1.getX+center2.getX)/2, (center1.getY+center2.getY)/2)
  }

  def createControlNode(app: Application, startNode: Node, endNode: Node, associatedPath: Path) = {
    Node(app, ControlNodeType, Some(associatedPath), center(startNode, endNode))
  }    
  
  /**
  * Creates a control node in the center of the two specified nodes,
  * with the associated path determined automatically.
  * That is, if one of the specified nodes has an associated path, it
  * is implicitly chosen as the control node's path.
  * Note that if both nodes have an associated path, the path of the start node is chosen.
  */
  def createControlNode(app: Application, startNode: Node, endNode: Node) = {
    if (startNode.associatedPath != None) {
      Node(app, ControlNodeType, startNode.associatedPath, center(startNode, endNode))
    }
    else{ //the associated path of the end node is either set or it is None
      Node(app, ControlNodeType, endNode.associatedPath, center(startNode, endNode))
    }
  }      
  
}


/**
* This class represents a connection between two nodes on a path, established by a quadratic bezier curve.
* A connection of this type may be shaped by the user by dragging either the start and end nodes or the control node of the curve.
*/
abstract class BezierConnection(app: Application, val startNode: Node, val controlNode: Node, val endNode: Node) extends Connection(app, List(startNode, controlNode, endNode)) {
  Ui += controlNode
  this.setPickable(true)
  
  
  override def apply(parameter: Float): (Float, Float) = {
    Bezier.quadraticCurve(this.startNode.position, this.controlNode.position, this.endNode.position)(parameter)
  }
  
  override def tangent(parameter: Float): (Float, Float) = {
    Bezier.quadraticCurveTangent(this.startNode.position, this.controlNode.position, this.endNode.position)(parameter)
  }
  
 
  override def closestPoint(position: Vector3D): (Float, Float) = {
    val (p0x, p0y) = this.startNode.position
    val (p1x, p1y) = this.controlNode.position
    val (p2x, p2y) = this.endNode.position
    Bezier.quadraticCurve((p0x, p0y), (p1x, p1y), (p2x, p2y))(this.parameterizedClosestPoint(position))    
  }
  
  
  /**
  * Returns the parameter in [0,1] which yields the point on this bezier curve closest to the specified coordinate.
  *
  * For a derivation, see e.g. http://blog.gludion.com/2009/08/distance-to-quadratic-bezier-curve.html
  */
  override def parameterizedClosestPoint(position: Vector3D): Float = {  
    val (p0x, p0y) = this.startNode.position
    val (p1x, p1y) = this.controlNode.position
    val (p2x, p2y) = this.endNode.position
    
    val (mx, my) = (position.getX, position.getY)
    val (m0x, m0y) = (p0x - mx, p0y - my) 
                                                             
    val (ax, ay) = (p1x - p0x, p1y - p0y) //(P1-P0)
    val (bx, by) = (p2x - p1x - ax, p2y - p1y - ay) //(P2-P1-A)
    
    val a = if (bx*bx + by*by == 0) 0.000000000000001f else bx*bx + by*by  //B^2 //ensuring a non-zero value for 'a' to avoid stupid exception in class Cubic which won't solve a quadratic equation
    val b = 3*(ax*bx + ay*by)//3A.B
    val c = 2*(ax*ax + ay*ay) + (m0x*bx + m0y*by)//2A^2+M'.B
    val d = m0x*ax + m0y*ay//M'.A
    
    val cubic = new Cubic() //create a cubic equation solver, a^3*t + b^2*t + c*t + d = 0
    cubic.solve(a,b,c,d) //solve for coefficients a,b,c,d - return values are saved in public fields x1, x2, x3 of Cubic object
    val (t0, t1, t2) = (math.max(0, math.min(1,cubic.x1)).toFloat, //make sure the value is between 0 and 1 since [0,1] is the domain of the quadratic bezier function
                        math.max(0, math.min(1,cubic.x2)).toFloat, //note that this also works if x2 and/or x3 are complex numbers and thus NaN
                        math.max(0, math.min(1,cubic.x3)).toFloat)  
       
    val curve = Bezier.quadraticCurve((p0x, p0y), (p1x, p1y), (p2x, p2y))_ //construct quadratic bezier curve 
    var argminT = t0
    
    val distT0 = Vector.euclideanDistance((mx, my), curve(t0)) //calculate distance from points on curve to specified coordinate
    val distT1 = Vector.euclideanDistance((mx, my), curve(t1))
    val distT2 = Vector.euclideanDistance((mx, my), curve(t2))
    
    argminT = t0   
    if(distT1 < distT0){ //if point[t1] is closer than point[t0]
      argminT = t1
      if(distT2 < distT1){ //if point[t2] is even closer than point[t1]
        argminT = t2
      }
    }
    else if(distT2 < distT0){ //else if point[t2] is closer than point[t0]
      argminT = t2
    }

    argminT
  }  
  
  
  
}
 
    
