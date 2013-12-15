package ui.input

import org.mt4j.Application

import org.mt4j.input.gestureAction.DefaultDragAction
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragEvent

import org.mt4j.input.inputProcessors.IGestureEventListener
import org.mt4j.input.inputProcessors.MTGestureEvent

import org.mt4j.sceneManagement.Iscene
import org.mt4j.components.TransformSpace
import org.mt4j.types.Vec3d

import processing.opengl.PGraphicsOpenGL

import ui._
import ui.tools._
import ui.paths._
import ui.util._
import ui.menus.main._

/**
* This class realizes a listener for drag actions which aligns the associated tool with the closest path.
* More precisely, the listener determines the path which minimizes the distance between the user's touch point and itself
* and subsequently sets the tool's rotation in such a way that the tool's axis is orthogonal to the tangent at the closest point on that path.
*/
class AligningDragAction(app: Application, tool: Tool) extends BoundedDragAction(0, 0, Ui.width, Ui.height) {

	override def processGestureEvent(gestureEvent: MTGestureEvent): Boolean = {  
	  import Vector._  
    val returnValue = super.processGestureEvent(gestureEvent)
	  
	  val eventPoint = gestureEvent match {
	    case dragEvent: DragEvent => {
	      (dragEvent.getTo.getX, dragEvent.getTo.getY) //position after dragging
	    }
      case otherEvent => {
        (Float.NaN, Float.NaN) //undefined and/or unimportant
      }
    }
		
		val pathPositionOption = Ui.closestPath(eventPoint)  
		val nodeOption = Ui.closestManipulableNode(eventPoint)  
	
	  (pathPositionOption, nodeOption) match {
	    case (None, None) => {println("none both")} //nothing to align to thus do nothing
	    case (Some(pathPosition), None) => { //align to closest point on path
	      this.alignToPath(pathPosition._1, pathPosition._2, pathPosition._3, eventPoint)
	    }
	    case (None, Some(node)) => { //align to node
	      this.alignToNode(node, eventPoint)
	    }
	    case (Some(pathPosition), Some(node)) => {
	      val (path, connection, parameter) = (pathPosition._1, pathPosition._2, pathPosition._3)
	      val pathDist = Vector.euclideanDistance(connection(parameter), eventPoint)
	      val nodeDist = Vector.euclideanDistance(node.position, eventPoint)
	      if (pathDist < nodeDist) this.alignToPath(path, connection, parameter, eventPoint) else this.alignToNode(node, eventPoint)
	    }
	  }
		      
    /*
    some notes: 
      - this method deliberately does not use the tip of the tool as the rotation point since the tool has to stay under the user's finger at all times,
      which cannot be accomplished if the tool is rotated around the tip
      
      - TODO: maybe only rotate in path vicinity?
      - TODO: keep drag vector to avoid alignment jumps between different paths (especially when manipulating the path)
    */
		
		returnValue
	}
	
	
	
	def alignToPath(path: Path, connection: Connection, parameter: Float, point: (Float, Float)) = {
    val (tx,ty) = connection.tangent(parameter) //calculate tangent at closest point
    val (dx,dy) = Vector(connection(parameter), point) //get vector from the closest point on the path to the specified point
    val side = math.signum(Vector.crossProduct((tx, ty, 0.0f), (dx, dy, 0.0f))._3) //determine on which side of the path the tool is; this is indicated by the third component of the cross product 
    val (ox,oy) = (ty,tx) //calculate orthogonal vector to bezier tangent //actually, since there is no minus here, this vector is NOT orthogonal, but it works... TODO go over why there is no minus needed here
    val atan2 = math.atan2(ox,oy).toFloat //get angle for that vector
    val angle = ((if (atan2 > 0) atan2 else (2*math.Pi + atan2)) * 360 / (2*math.Pi)).toFloat //convert to degrees
    tool.setRotation(if (side >= 0) angle else angle + 180, point) //set rotation of tool to the calculated angle    
	}
	
	
	def alignToNode(node: Node, point: (Float, Float)) = {
    val (dx,dy) = Vector(node.position, point) //get vector from the closest point on the path to the specified point
    val (ox,oy) = (-dx,dy)
    val atan2 = math.atan2(ox,oy).toFloat //get angle for that vector
    val angle = ((if (atan2 > 0) atan2 else (2*math.Pi + atan2)) * 360 / (2*math.Pi)).toFloat //convert to degrees
    tool.setRotation(angle, point) //set rotation of tool to the calculated angle   	  	
	}
      
	
	
}
