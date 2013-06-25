package ui.input

import org.mt4j.Application

import org.mt4j.input.gestureAction.DefaultDragAction
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragEvent

import org.mt4j.input.inputProcessors.IGestureEventListener
import org.mt4j.input.inputProcessors.MTGestureEvent

import scala.actors._

import ui.paths._
import ui.events._
import ui.audio._
import ui.properties.types._

class PlayTimbreDragAction(node: Node) extends DefaultDragAction {
  
  	override def processGestureEvent(gestureEvent: MTGestureEvent): Boolean = {  
  	  val returnValue = super.processGestureEvent(gestureEvent)
      Synthesizer ! AudioEvent(0, math.round(node.position._1), math.round(node.position._2), PitchPropertyType.mean, VolumePropertyType.mean)                	  
  	  returnValue
  	}
  	
}
