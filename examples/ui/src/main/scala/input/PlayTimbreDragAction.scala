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
import ui.menus.main._
import ui._

class PlayTimbreDragAction(node: Node) extends BoundedDragAction(Menu.Space, Menu.Space, Ui.width - Menu.Space, Ui.height - Menu.Space) {
  
  	override def processGestureEvent(gestureEvent: MTGestureEvent): Boolean = {  
  	  val returnValue = super.processGestureEvent(gestureEvent)
  	  node match {
  	    case withChannels: AudioChannels => {
  	      Synthesizer ! AudioEvent(withChannels.collectOpenChannels, math.round(withChannels.position._1), math.round(withChannels.position._2), PitchPropertyType.mean, VolumePropertyType.mean)   
        }
        case withoutChannels => {
          Synthesizer ! AudioEvent(Array(0,1,2,3), math.round(withoutChannels.position._1), math.round(withoutChannels.position._2), PitchPropertyType.mean, VolumePropertyType.mean)   
        }
      }
  	  returnValue
  	}
  	
}
