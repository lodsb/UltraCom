package ui.input

import org.mt4j.Application

import org.mt4j.components.MTComponent
import org.mt4j.components.interfaces.IMTComponent3D
import org.mt4j.components.visibleComponents.shapes.AbstractShape

import org.mt4j.input.gestureAction.ICollisionAction 
import org.mt4j.input.gestureAction.DefaultDragAction
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragEvent

import org.mt4j.input.inputProcessors.IGestureEventListener
import org.mt4j.input.inputProcessors.MTGestureEvent

import org.mt4j.sceneManagement.Iscene
import org.mt4j.components.TransformSpace
import org.mt4j.types.Vec3d
import org.mt4j.util.math.Vector3D
import org.mt4j.util.math.ToolsGeometry

import processing.opengl.PGraphicsOpenGL

import ui._
import ui.tools._
import ui.paths._
import ui.util._
import ui.menus.main._
import ui.events._

/**
* This class realizes a listener for drag actions on time nodes which may only be moved along a given path.
* Note that the specified path has to take care of the bounded movement.
*/
class PathBoundedDragAction(node: TimeNode) extends IGestureEventListener with ICollisionAction {

    private var lastEvent: Option[MTGestureEvent] = None

    /**
     * The gesture aborted.
     */
    private var gestureAbortedVar: Boolean = false

    
    override def processGestureEvent(gestureEvent: MTGestureEvent) = {
      gestureEvent match {
        case dragEvent: DragEvent => {
          this.lastEvent = Some(dragEvent)
          
          dragEvent.getId match {
            case MTGestureEvent.GESTURE_DETECTED => {}
            case MTGestureEvent.GESTURE_RESUMED => {
              this.translateBounded(dragEvent)
            }
            case MTGestureEvent.GESTURE_UPDATED => {
              this.translateBounded(dragEvent)
            }
            case MTGestureEvent.GESTURE_CANCELED => {}
            case MTGestureEvent.GESTURE_ENDED => {}
            case somethingElse => {}
          }
        }
      }
      false
    }  
    
    
    private def translateBounded(dragEvent: DragEvent) = {
      if (!gestureAborted) {      
        node.associatedPath.foreach(_ ! TimeNodeMoveEvent(node, dragEvent.getTo.getX, dragEvent.getTo.getY))
      } 
    }
  
  

    override def gestureAborted = {
      this.gestureAbortedVar
    }


    override def setGestureAborted(aborted: Boolean) = {
      this.gestureAbortedVar = aborted
    }  
    
    override def getLastEvent = {
      this.lastEvent match {
        case Some(event) => event
        case None => null
      }
    }
  
  
}
