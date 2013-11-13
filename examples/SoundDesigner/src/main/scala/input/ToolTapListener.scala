package ui.tools

import org.mt4j.{Application, Scene}

import org.mt4j.input.inputProcessors.IGestureEventListener
import org.mt4j.input.inputProcessors.MTGestureEvent
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragProcessor 

import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent

import org.mt4j.components.ComponentImplicits._

import org.mt4j.types.Vec3d

import ui._
import ui.util._

class ToolTapListener(tool: Tool) extends IGestureEventListener{
	
	override def processGestureEvent(gestureEvent: MTGestureEvent): Boolean = {
			gestureEvent match {
				case tapEvent: TapEvent => {
					if (tapEvent.getTapID == TapEvent.BUTTON_DOWN) {
            val touchPoint = (tapEvent.getCursor.getCurrentEvtPosX(), tapEvent.getCursor.getCurrentEvtPosY())
            val localTouchPoint = tool.globalToLocal(Vec3d(touchPoint._1, touchPoint._2))
            tool.isEditing = tool.pointInEditArea((localTouchPoint.getX, localTouchPoint.getY))   
					  println("tool down")
					}
					else if (tapEvent.getTapID == TapEvent.BUTTON_UP) {
					  tool.isEditing = false
					  println("tool up")
					}
					else if (tapEvent.getTapID == TapEvent.BUTTON_CLICKED) {
					  //tool.isEditing = !tool.isEditing
					  //println("tool is editing: " + tool.isEditing)
					}
          else if (tapEvent.getTapID == TapEvent.BUTTON_DOUBLE_CLICKED) {
            println("tool double tapped")
            Ui.getCurrentScene.getCanvas -= tool
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
