package ui.input

import org.mt4j.input.inputProcessors.IGestureEventListener
import org.mt4j.input.inputProcessors.MTGestureEvent

import org.mt4j.components.ComponentImplicits._

import org.mt4j.input.inputProcessors.componentProcessors.unistrokeProcessor.UnistrokeProcessor
import org.mt4j.input.inputProcessors.componentProcessors.unistrokeProcessor.UnistrokeEvent
import org.mt4j.input.inputProcessors.componentProcessors.unistrokeProcessor.UnistrokeUtils
import org.mt4j.input.inputProcessors.componentProcessors.unistrokeProcessor.UnistrokeUtils.UnistrokeGesture
import org.mt4j.input.inputProcessors.componentProcessors.unistrokeProcessor.UnistrokeUtils.Direction

import ui._
import ui.paths._
import ui.paths.types._
import ui.tools._
import ui.properties._
import ui.properties.types._

/**
* This class handles unistroke gestures on the canvas.
*/
class UnistrokeListener extends IGestureEventListener {
  
  override def processGestureEvent(gestureEvent: MTGestureEvent): Boolean = {
    gestureEvent match {
      case unistrokeEvent: UnistrokeEvent => {
        //if (!CursorProcessor.isCursorInUse(unistrokeEvent.getCursor)){
          unistrokeEvent.getGesture match {
            case UnistrokeUtils.UnistrokeGesture.CIRCLE => {
              val gestureVisualization = unistrokeEvent.getVisualization
              val position = gestureVisualization.getCenterPointGlobal
              Ui += IsolatedNode(Ui, (position.getX, position.getY))
            }
            case UnistrokeUtils.UnistrokeGesture.DELETE => {
              val gestureVisualization = unistrokeEvent.getVisualization
              val position = gestureVisualization.getCenterPointGlobal
              Ui += Tool(Ui, (position.getX, position.getY), SpeedPropertyType)
            }
            case UnistrokeUtils.UnistrokeGesture.TRIANGLE => {
              val gestureVisualization = unistrokeEvent.getVisualization
              val position = gestureVisualization.getCenterPointGlobal
              Ui += Tool(Ui, (position.getX, position.getY), VolumePropertyType)
            }
            case UnistrokeUtils.UnistrokeGesture.PIGTAIL => {
              val gestureVisualization = unistrokeEvent.getVisualization
              val position = gestureVisualization.getCenterPointGlobal
              Ui += Tool(Ui, (position.getX, position.getY), PitchPropertyType)
            }          
            case somethingElse => {}
          }
          true
       //}
       //else false
      }
      case someEvent => {
        println("I can't process this particular event: " + someEvent.toString)
        false
      }
    }
  }
  
}
