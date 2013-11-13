package ui.input

import org.mt4j.{Application, Scene}

import org.mt4j.input.inputProcessors.IGestureEventListener
import org.mt4j.input.inputProcessors.MTGestureEvent

import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent
import org.mt4j.input.inputProcessors.componentProcessors.tapAndHoldProcessor.TapAndHoldProcessor
import org.mt4j.input.inputProcessors.componentProcessors.tapAndHoldProcessor.TapAndHoldEvent

import org.mt4j.components.ComponentImplicits._

import ui._
import ui.paths._
import ui.paths.types._
import ui.util._
import ui.events._
import ui.menus.context._

/**
* This class realizes a listener for tap and hold actions on nodes eliciting playback context menus. 
*/
class PlaybackContextMenuListener(node: Node) extends IGestureEventListener() {
  override def processGestureEvent(gestureEvent: MTGestureEvent): Boolean = {
    gestureEvent match {
      case tahEvent: TapAndHoldEvent => {
          if (tahEvent.getId == MTGestureEvent.GESTURE_DETECTED) {
            println("tap and hold detected")
            if (!NodeContextMenu.isMenuVisible(node)) {
              node.giveFeedback(FeedbackEvent("START_TIMER", NodeContextMenu.Delay))
            }            
          }
          else if (tahEvent.getId == MTGestureEvent.GESTURE_ENDED) {
            println("tap and hold ended")
            node.giveFeedback(FeedbackEvent("STOP_TIMER"))
            if (tahEvent.isHoldComplete && !NodeContextMenu.isMenuVisible(node)) {
              println("tap and hold completed")
              node.associatedPath.foreach(_ ! UiEvent("IGNORE_NEXT_STOP_PLAYBACK")) //ignore next tap
              node match {case manipulableNode: ManipulableNode => manipulableNode ! UiEvent("IGNORE_NEXT_TOGGLE_PLAYBACK") case otherNode => {}}
              val menu = PlaybackContextMenu(Ui, node)
              NodeContextMenu += menu
              node.addChild(menu)
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
