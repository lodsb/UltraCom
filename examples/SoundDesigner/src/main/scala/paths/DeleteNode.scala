package ui.paths

import org.mt4j.Application
import org.mt4j.util.math.Vector3D
import org.mt4j.types.{Vec3d}

import org.mt4j.util.Color

import processing.opengl.PGraphicsOpenGL

import ui.paths.types._
import ui._
import ui.util._


/**
* This class represents a delete node, which enables the user to transform a manipulable node back into an isolated one.
*/
class DeleteNode(app: Application, val manipulableNode: ManipulableNode, center: Vector3D) extends Node(app, DeleteManipulableNodeType, None, center) {   
  private val DeleteNodeRadius = NodeType.Radius * ControlNodeType.Size.toFloat
  private val LineColor = Color(0, 130, 130, 150)  
  private val DotNumber = 15
    
  override def drawComponent(g: PGraphicsOpenGL) = {
    super.drawComponent(g)
    
    //draw dotted line from button to node
    g.stroke(0, 0, 0, LineColor.getA)
    val deletableNodeVector = this.parentToLocal(manipulableNode.getCenterPointLocal)
    val deletableNodePosition = (deletableNodeVector.getX, deletableNodeVector.getY)
    val deleteNodePosition = (this.getCenterPointLocal.getX, this.getCenterPointLocal.getY)
    val distance = Vector.euclideanDistance(deletableNodePosition, deleteNodePosition)
    val line = Functions.line(deletableNodePosition, deleteNodePosition)_
    (0 until this.DotNumber).foreach(dot => {
      val (x,y) = line(dot/this.DotNumber.toFloat)
      g.point(x, y)
    })
  }   
} 
