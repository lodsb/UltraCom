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

/**
* This class realizes a listener for drag actions with fixed bounds.
*/
class BoundedDragAction(xLowerBound: Float, yLowerBound: Float, xUpperBound: Float, yUpperBound: Float) extends IGestureEventListener with ICollisionAction {

    private var lastEvent: Option[MTGestureEvent] = None

    /**
     * The gesture aborted.
     */
    private var gestureAbortedVar: Boolean = false

    
    override def processGestureEvent(gestureEvent: MTGestureEvent) = {
      gestureEvent match {
        case dragEvent: DragEvent => {
          this.lastEvent = Some(dragEvent)
          
          val dragTarget = dragEvent.getTarget

          dragEvent.getId match {
            case MTGestureEvent.GESTURE_DETECTED => {}
            case MTGestureEvent.GESTURE_RESUMED => {
              //Put target on top -> draw on top of others
              /*dragTarget match {
                case baseComp: MTComponent => baseComp.sendToFront()
              }*/
              
              this.translateBounded(dragTarget, dragEvent)
            }
            case MTGestureEvent.GESTURE_UPDATED => {
              this.translateBounded(dragTarget, dragEvent)
            }
            case MTGestureEvent.GESTURE_CANCELED => {}
            case MTGestureEvent.GESTURE_ENDED => {}
            case somethingElse => {}
          }
        }
      }
      false
    }  
    
    
    private def translateBounded(dragTarget: IMTComponent3D, dragEvent: DragEvent) = {
      dragTarget match {
        case shape: AbstractShape => {
          if (!gestureAborted) {             
            val width = shape.getWidthXY(TransformSpace.GLOBAL)
            val height = shape.getHeightXY(TransformSpace.GLOBAL)     
            val translationVector = dragEvent.getTranslationVect
            val toVector = dragEvent.getTo
            
            if (Vector.euclideanDistance((dragEvent.getFrom.getX, dragEvent.getFrom.getY), (shape.getCenterPointGlobal.getX, shape.getCenterPointGlobal.getY)) <= width/2.0f) { //if the cursor is inside the shape            
              val xDiff = if (toVector.getX <= xLowerBound) xLowerBound - shape.getCenterPointGlobal.getX //if we fall below the lower bound, we translate by the diff to that bound
              else if (toVector.getX >= xUpperBound) xUpperBound - shape.getCenterPointGlobal.getX //else if we exceed the upper bound we translate by the diff to that bound            
              else dragEvent.getTo.getX - dragEvent.getFrom.getX //else we translate by the drag distance
              
              val yDiff = if (toVector.getY <= yLowerBound) yLowerBound - shape.getCenterPointGlobal.getY //if we fall below the lower bound, we translate by the diff to that bound
              else if (toVector.getY >= yUpperBound) yUpperBound - shape.getCenterPointGlobal.getY //else if we exceed the upper bound we translate by the diff to that bound            
              else dragEvent.getTo.getY - dragEvent.getFrom.getY //else we translate by the drag distance
              
              val boundedTranslationVector = Vec3d(xDiff, yDiff)
              shape.translateGlobal(boundedTranslationVector)
            }
          } 
        }
        case somethingElse => {}
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
