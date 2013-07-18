package ui.paths.types

import org.mt4j.Application
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapProcessor 
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent
import org.mt4j.input.inputProcessors.IGestureEventListener
import org.mt4j.input.inputProcessors.MTGestureEvent

import org.mt4j.components.visibleComponents.shapes.MTEllipse
import org.mt4j.util.Color
import org.mt4j.types.{Vec3d}

import processing.core.PGraphics
import processing.core.PConstants._

import ui._
import ui.util._
import ui.paths._
import ui.audio._
import ui.events._
import ui.properties.types._
import ui.usability._

/**
* This is the manipulable node type.
* A node associated with this type provides a means to play back a single timbre by tapping.
*/
object ManipulableNodeType extends EndNodeType with StartNodeType {

  val Symbol = Triangle
  
  protected override def setupInteractionImpl(app: Application, node: Node) = {
    super.setupInteractionImpl(app, node)
 
    val tapProcessor = new TapProcessor(app)
    tapProcessor.setEnableDoubleTap(true)
    node.registerInputProcessor(tapProcessor)    
    
    node.addGestureListener(classOf[TapProcessor], new IGestureEventListener() {
      override def processGestureEvent(gestureEvent: MTGestureEvent): Boolean = {
        gestureEvent match {
          case tapEvent: TapEvent => {
              if (tapEvent.getTapID == TapEvent.BUTTON_DOWN) {
                println("play down")
              }
              else if (tapEvent.getTapID == TapEvent.BUTTON_UP) {
                println("play up")
                node match {case manipulableNode: ManipulableNode => manipulableNode ! UiEvent("IGNORE_IGNORE_NEXT_TOGGLE_PLAYBACK")}
              }
              else if (tapEvent.getTapID == TapEvent.BUTTON_CLICKED) {
                println("play clicked")
                node match {
                  case manipulableNode: ManipulableNode => {
                    manipulableNode ! UiEvent("START_PLAYBACK") 
                  }
                  case otherNode => {
                    Synthesizer ! AudioEvent(Array(0,1,2,3), math.round(node.position._1), math.round(node.position._2), PitchPropertyType.mean, VolumePropertyType.mean)   
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
    })    
  } 
  
  
  override def symbol = {
    Some(this.Symbol)
  }    
  
}



