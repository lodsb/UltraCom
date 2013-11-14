package ui.input

import org.mt4j.Application

import org.mt4j.input.gestureAction.DefaultDragAction
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragEvent

import org.mt4j.input.inputProcessors.IGestureEventListener
import org.mt4j.input.inputProcessors.MTGestureEvent

import scala.actors._

import ui.paths._
import ui.events._
import ui.menus.main._
import ui._
import org.mt4j.util.SessionLogger

class NotifyingDragAction(node: Node) extends BoundedDragAction(Menu.Space, Menu.Space, Ui.width - Menu.Space, Ui.height - Menu.Space) {
  
  	override def processGestureEvent(gestureEvent: MTGestureEvent): Boolean = {  
  	  val returnValue = super.processGestureEvent(gestureEvent)
  	  node.associatedPath match {
  	    case Some(path) => {
          path ! NodeMoveEvent(node)
  	    }
  	    case None => {
  	      node match {
  	        case singleNode: SingleNode => {
              singleNode ! NodeMoveEvent(singleNode)

            } //send move event to self in order to update time connections
  	        case otherNode => {}
  	      }
  	    }
  	  }
  	  returnValue
  	}
  	
}
