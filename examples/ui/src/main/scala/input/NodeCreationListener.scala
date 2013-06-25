package ui.input

import org.mt4j.{Application, Scene}

import org.mt4j.input.inputProcessors.IGestureEventListener
import org.mt4j.input.inputProcessors.MTGestureEvent

import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent

import org.mt4j.components.ComponentImplicits._

import ui._
import ui.paths._
import ui.paths.types._

/**
* This class handles the creation of isolated nodes.
*/
class NodeCreationListener extends IGestureEventListener {
  override def processGestureEvent(gestureEvent: MTGestureEvent): Boolean = {  
        gestureEvent match {
            case tapEvent: TapEvent => {
                if (tapEvent.getTapID == TapEvent.BUTTON_DOUBLE_CLICKED) {
                  Ui += Node(Ui, IsolatedNodeType, None, tapEvent.getLocationOnScreen)
                }
                true
            }
            case someEvent => {
                println("I can't process this particular event: " + someEvent.toString)
                false
            }
        }
  }
}
