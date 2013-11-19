package ui.input

import org.mt4j.{Application, Scene}

import org.mt4j.input.inputProcessors.IGestureEventListener
import org.mt4j.input.inputProcessors.MTGestureEvent
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragProcessor 
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent

import org.mt4j.components.ComponentImplicits._
import org.mt4j.util.SessionLogger

import org.mt4j.types.Vec3d

import ui._
import ui.util._
import ui.menus.main._

class ChannelMenuTapListener(channelMenu: ChannelMenu) extends IGestureEventListener{
	
	override def processGestureEvent(gestureEvent: MTGestureEvent): Boolean = {
			gestureEvent match {
				case tapEvent: TapEvent => {
					if (tapEvent.getTapID == TapEvent.BUTTON_DOWN) {
					  println("channel menu down")
					}
					else if (tapEvent.getTapID == TapEvent.BUTTON_UP) {
					  println("channel menu up")
					}
					else if (tapEvent.getTapID == TapEvent.BUTTON_CLICKED) {
					  println("channel clicked")
					}
          else if (tapEvent.getTapID == TapEvent.BUTTON_DOUBLE_CLICKED) {
            println("channel menu double tapped")
            SessionLogger.log("Deactivated: channel menu with channel " + channelMenu.channelNumber, SessionLogger.SessionEvent.Event, this, null, null)            
            ChannelMenu -= channelMenu
            Ui -= channelMenu
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
