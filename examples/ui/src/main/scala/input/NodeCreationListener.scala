package ui.input

import org.mt4j.{Application, Scene}

import org.mt4j.input.inputProcessors.IGestureEventListener
import org.mt4j.input.inputProcessors.MTGestureEvent

import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent
import org.mt4j.types.Vec3d
import org.mt4j.components.ComponentImplicits._

import ui._
import ui.paths._
import ui.paths.types._
import ui.util._
import ui.events._

/**
* This class handles the creation of nodes in the specified node space.
*/
class NodeCreationListener(nodeSpace: NodeSpace) extends IGestureEventListener {
  
  override def processGestureEvent(gestureEvent: MTGestureEvent): Boolean = {  
    gestureEvent match {
      case tapEvent: TapEvent => {
          if (tapEvent.getTapID == TapEvent.BUTTON_DOUBLE_CLICKED) {
            this.evaluateTap(tapEvent) 
          }
          true
      }
      case someEvent => {
          println("I can't process this particular event: " + someEvent.toString)
          false
      }
    }
  }
  
  
  private def evaluateTap(tapEvent: TapEvent) = {
    val position = (tapEvent.getLocationOnScreen.getX, tapEvent.getLocationOnScreen.getY)
    val closestPathPositionOption = Ui.closestPath(position)
    closestPathPositionOption match {
      case Some(closestPathPosition) => {
        val path = closestPathPosition._1
        val connection = closestPathPosition._2
        val parameter = closestPathPosition._3
        val pathPosition = connection(parameter)
        if (Vector.euclideanDistance(pathPosition, position) <= 10) {
          path ! TimeNodeAddEvent(TimeNode(Ui, closestPathPosition))
        }
        else {
          if (nodeSpace.contains(tapEvent.getLocationOnScreen)) Ui += IsolatedNode(Ui, tapEvent.getLocationOnScreen)    
        }
      } 
      case None => {
        if (nodeSpace.contains(tapEvent.getLocationOnScreen)) Ui += IsolatedNode(Ui, tapEvent.getLocationOnScreen)
      }       
    } 
  }
  
  
}
