package ui.input

import org.mt4j.{Application, Scene}

import org.mt4j.input.inputProcessors.IGestureEventListener
import org.mt4j.input.inputProcessors.MTGestureEvent

import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent
import org.mt4j.input.inputProcessors.componentProcessors.tapAndHoldProcessor.TapAndHoldProcessor
import org.mt4j.input.inputProcessors.componentProcessors.tapAndHoldProcessor.TapAndHoldEvent

import org.mt4j.components.ComponentImplicits._

import scala.actors._

import ui._
import ui.paths._
import ui.paths.types._
import ui.util._
import ui.events._
import ui.menus.context._

/**
* This class realizes a listener for tap and hold actions on nodes eliciting channel context menus.
*/
class ChannelContextMenuListener(node: Node) extends IGestureEventListener() with Actor {

  private val TapAndHoldMaxDist = 5 //maximum distance between the start and end point of a tap and hold touch event
  private val TapAndHoldTime = NodeContextMenu.Delay //time in milliseconds before a tap and hold is triggered
  this.start()
  
  override def processGestureEvent(gestureEvent: MTGestureEvent): Boolean = {
    gestureEvent match {
      case tapEvent: TapEvent => {
          if (tapEvent.getId == MTGestureEvent.GESTURE_DETECTED) {
            println("tap detected")
            if (!NodeContextMenu.isMenuVisible(node)) {
              node.giveFeedback(FeedbackEvent("START_TIMER", NodeContextMenu.Delay))
              this ! "START_TIMER"
            }
          }
          else if (tapEvent.getId == MTGestureEvent.GESTURE_ENDED) {
            println("tap ended")
            node.giveFeedback(FeedbackEvent("STOP_TIMER"))
            this ! "STOP_TIMER"
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


  def act() = {
    var keepChecking = false
    var initTime: Long = 0
    
    while (node.inExistence) {
      receive {
        case "START_TIMER" => {
          initTime = System.nanoTime()
          keepChecking = true
          this ! "CHECK"
        }
        case "STOP_TIMER" => {
          keepChecking = false
        }
        case "CHECK" => {
          if (keepChecking) {
            if ((System.nanoTime() - initTime)/1000000.0f >= TapAndHoldTime) { //tap and hold completed
              if (!NodeContextMenu.isMenuVisible(node)) {
                println("tap and hold completed")
                node.associatedPath.foreach(_ ! UiEvent("IGNORE_NEXT_TOGGLE_PLAYBACK")) //ignore next tap
                node match {case manipulableNode: ManipulableNode => manipulableNode ! UiEvent("IGNORE_NEXT_TOGGLE_PLAYBACK") case otherNode => {}}
                val menu = ChannelContextMenu(Ui, node)
                NodeContextMenu += menu
                node.addChild(menu)
              }              
              keepChecking = false
            }
            Thread.sleep(30)  
            //println("checking...")
            this ! "CHECK"
          }
        }
        case "STOP_ACTING" => {
          exit()
        }
      }
    }
    
  }    
  
  
} 
