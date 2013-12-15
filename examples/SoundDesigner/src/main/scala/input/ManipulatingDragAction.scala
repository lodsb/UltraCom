package ui.input

import org.mt4j.Application

import org.mt4j.input.gestureAction.DefaultDragAction
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragEvent

import org.mt4j.input.inputProcessors.IGestureEventListener
import org.mt4j.input.inputProcessors.MTGestureEvent

import org.mt4j.sceneManagement.Iscene
import org.mt4j.components.TransformSpace
import org.mt4j.types.Vec3d
import org.mt4j.util.math.Vector3D

import processing.opengl.PGraphicsOpenGL

import scala.actors._

import ui._
import ui.tools._
import ui.paths._
import ui.util._
import ui.properties._
import ui.properties.types._
import ui.events._

object ManipulatingDragAction {
  
  /* arc length of path segment which is manipulated */
  val ManipulationRange = Ui.width/80 //amounts to 24 in case of full HD
}

/**
* This class realizes a listener for manipulations on a path through dragging of a tool.
*/
class ManipulatingDragAction(app: Application, tool: Tool) extends AligningDragAction(app, tool) {
  
  private var lastObjectOption: Option[Actor] = None
  
	override def processGestureEvent(gestureEvent: MTGestureEvent): Boolean = {
	  val returnValue = super.processGestureEvent(gestureEvent) //first align with path

	  // obtain tip of tool               
	  val tipPoint = tool.tipPoint
	 
	  /* evaluate edit mode */
    if (gestureEvent.getId() == MTGestureEvent.GESTURE_DETECTED || gestureEvent.getId() == MTGestureEvent.GESTURE_UPDATED) {
      val touchPoint = gestureEvent match {case dragEvent: DragEvent => (dragEvent.getTo.getX, dragEvent.getTo.getY) case otherEvent => (Float.NaN, Float.NaN)}
      val localTouchPoint = tool.globalToLocal(Vec3d(touchPoint._1, touchPoint._2))
      tool.isEditing = tool.pointInEditArea((localTouchPoint.getX, localTouchPoint.getY))   
    }
    else if (gestureEvent.getId() == MTGestureEvent.GESTURE_ENDED || gestureEvent.getId() == MTGestureEvent.GESTURE_CANCELED) {
      tool.isEditing = false
    }
  
    val pathPositionOption = Ui.closestPath(tipPoint) //finding closest point on closest path from tip of tool
    val nodeOption = Ui.closestManipulableNode(tipPoint) //finding closest point on closest node from tip of tool
    (pathPositionOption, nodeOption) match {
      case (None, None) => {}
      case (Some(pathPosition), None) => {
        this.evaluateLastObject(pathPosition._1)
        this.lastObjectOption = Some(pathPosition._1)
        this.processPathManipulation(pathPosition._1, pathPosition._2, pathPosition._3, tipPoint, tool.isEditing)
      }
      case (None, Some(node)) => { 
        node match {
          case manipulableNode: ManipulableNode => {
            this.evaluateLastObject(manipulableNode) 
            this.lastObjectOption = Some(manipulableNode)
            this.processNodeManipulation(manipulableNode, tipPoint, tool.isEditing)
          }
          case otherNode => {}
        }
      }
      case (Some(pathPosition), Some(node)) => {
        val (path, connection, parameter) = (pathPosition._1, pathPosition._2, pathPosition._3)
        val pathDist = Vector.euclideanDistance(connection(parameter), tipPoint)
        val nodeDist = Vector.euclideanDistance(node.position, tipPoint)
        if (pathDist < nodeDist) {
          this.evaluateLastObject(path)
          this.lastObjectOption = Some(path)
          this.processPathManipulation(path, connection, parameter, tipPoint, tool.isEditing) 
        }
        else { 
          node match {
            case manipulableNode: ManipulableNode => {
              this.evaluateLastObject(manipulableNode)        
              this.lastObjectOption = Some(manipulableNode)
              this.processNodeManipulation(manipulableNode, tipPoint, tool.isEditing)
            }
            case otherNode => {}
          }
        }
      }
    }
    returnValue
  }
  
  /**
  * Evaluates whether the last object has to receive an unregister tool event since it is no longer in focus.
  */
  private def evaluateLastObject(newObject: Actor) {
    //println("evaluating last object")
    this.lastObjectOption match {
      case Some(lastObject) => {
        if (lastObject != newObject) {
          lastObject ! UnregisterToolEvent(tool)
        }
      }
      case None => {}
    }    
  }
  
  private def processPathManipulation(path: Path, connection: Connection, parameter: Float, point: (Float, Float), execute: Boolean) = { 
    import ManipulatingDragAction._                         
    val manipulatedPoint = connection(parameter) //this is the closest point    
    //DebugOutput.setPoint("1", manipulatedPoint)
    val distancePointToPath = Vector.euclideanDistance(point, manipulatedPoint)
    val propertyWidth = tool.propertyType.width
    val propertyVicinity = tool.propertyType.vicinity

    val (tx,ty) = connection.tangent(parameter) //calculate tangent at closest point
    val (dx,dy) = Vector(connection(parameter), point) //get vector from the closest point on the path to the specified point
    val side = math.signum(Vector.crossProduct((tx, ty, 0.0f), (dx, dy, 0.0f))._3) //determine on which side of the path the tool is; this is indicated by the third component of the cross product 
    val propertySide = tool.propertyType match {case VolumePropertyType => -1*side case PitchPropertyType => side case otherPropertyType => 0}
    
    connection match {
      case connection: ManipulableBezierConnection => {
        if (distancePointToPath <= propertyVicinity && propertySide >= 0) {//make sure tool is in vicinity of property and on the right side
          if (execute) path ! PathManipulationEvent(connection, parameter, tool, ManipulationRange/2, math.min(propertyWidth, distancePointToPath))
          else path ! RegisterToolWithLocationEvent(tool, connection, parameter, ManipulationRange/2)
        }
        else {
          path ! UnregisterToolEvent(tool)
        }
      }
      case otherConnection => {}
    }   
  }
  
  
  private def processNodeManipulation(node: ManipulableNode, point: (Float, Float), execute: Boolean) = {
    val distancePointToNode = Vector.euclideanDistance(point, node.position) - node.radius
    val propertyWidth = tool.propertyType.width
    val side = if (point._2 > node.position._2) -1 else 1
    val propertySide = tool.propertyType match {case VolumePropertyType => -1*side case PitchPropertyType => side case otherPropertyType => 0}
           
    if (distancePointToNode <= propertyWidth && propertySide >= 0) { //make sure tool is actually close enough to manipulate property and also on the right side
      if (execute) node ! ManipulationEvent(tool, math.min(propertyWidth, distancePointToNode))  
      else node ! RegisterToolEvent(tool)
    }
    else {
      node ! UnregisterToolEvent(tool)
    } 
  }
  
}
