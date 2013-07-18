package ui.input

import org.mt4j.{Application, Scene}

import org.mt4j.input.inputProcessors.IGestureEventListener
import org.mt4j.input.inputProcessors.MTGestureEvent

import org.mt4j.input.inputProcessors.componentProcessors.tapAndHoldProcessor.TapAndHoldProcessor
import org.mt4j.input.inputProcessors.componentProcessors.tapAndHoldProcessor.TapAndHoldEvent

import org.mt4j.components.ComponentImplicits._
import org.mt4j.types.Vec3d

import ui._
import ui.paths._
import ui.events._
import ui.menus.context._

class ToolMenuListener(app: Application) extends IGestureEventListener {
  
  override def processGestureEvent(gestureEvent: MTGestureEvent): Boolean = {
    gestureEvent match {
      case tahEvent: TapAndHoldEvent => {
          if (tahEvent.getId == MTGestureEvent.GESTURE_DETECTED) {
            println("tap and hold detected")
          }
          else if (tahEvent.getId == MTGestureEvent.GESTURE_ENDED) {
            println("tap and hold ended")
            if (tahEvent.isHoldComplete && !ToolContextMenu.isMenuInProximity(tahEvent.getLocationOnScreen) && !CursorProcessor.isCursorInUse(tahEvent.getCursor)) {
              println("tap and hold completed")
              val menu = ToolContextMenu(app, tahEvent.getLocationOnScreen)
              ToolContextMenu += menu
              Ui += menu
            }
          }
          //else if (tahEvent.getId == MTGestureEvent.GESTURE_UPDATED) {
            //println("tap and hold updated")
          //}
          true
      }
      case someEvent => {
          println("I can't process this particular event: " + someEvent.toString)
          false
      }
    }
  }
  
}
