package ui.input

import org.mt4j.Application

import org.mt4j.input.gestureAction.DefaultDragAction
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragEvent

import org.mt4j.input.inputProcessors.IGestureEventListener
import org.mt4j.input.inputProcessors.MTGestureEvent

import scala.actors._

import ui.paths._
import ui.events._

class NotifyingDragAction(node: Node) extends DefaultDragAction {
  
  	override def processGestureEvent(gestureEvent: MTGestureEvent): Boolean = {  
  	  val returnValue = super.processGestureEvent(gestureEvent)
  	  node.associatedPath match {
  	    case Some(path) => {
          path ! NodeMoveEvent(node)
  	    }
  	    case None => {}
  	  }
  	  returnValue
  	}
  	
}
