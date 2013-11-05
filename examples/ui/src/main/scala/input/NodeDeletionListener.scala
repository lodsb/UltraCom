package ui.input

import org.mt4j.{Application, Scene}

import org.mt4j.input.inputProcessors.IGestureEventListener
import org.mt4j.input.inputProcessors.MTGestureEvent

import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent

import org.mt4j.components.ComponentImplicits._

import ui._
import ui.audio._
import ui.paths._
import ui.events._
import ui.paths.types._

/**
* This class handles the deletion of nodes of arbitrary type.
*/
class NodeDeletionListener(node: Node) extends IGestureEventListener {
  override def processGestureEvent(gestureEvent: MTGestureEvent): Boolean = {  
    gestureEvent match {
      case tapEvent: TapEvent => {
          if (tapEvent.getTapID == TapEvent.BUTTON_DOUBLE_CLICKED) {
            println("double tapped - node is being deleted")
            node.associatedPath match {
              case Some(path) => { //node is part of a path
                path ! NodeDeletionEvent(node) //remove node from path (Ui will automatically be updated with possible new paths)
              }
              case None => {
                //if (node.nodeType == DeleteManipulableNodeType) {         
                Ui -= node
                println("handling deletion of isolated node")
              }
            }
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
