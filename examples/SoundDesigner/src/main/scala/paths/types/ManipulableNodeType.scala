package ui.paths.types

import org.mt4j.Application
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapProcessor 
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent
import org.mt4j.input.inputProcessors.IGestureEventListener
import org.mt4j.input.inputProcessors.MTGestureEvent

import org.mt4j.components.visibleComponents.shapes.MTEllipse
import org.mt4j.util.Color
import org.mt4j.types.{Vec3d}

import processing.opengl.PGraphicsOpenGL
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
                node.setTapped(true)
              }
              else if (tapEvent.getTapID == TapEvent.BUTTON_UP) {
                println("play up")
                node.setTapped(false)
                node match {case manipulableNode: ManipulableNode => manipulableNode ! UiEvent("IGNORE_IGNORE_NEXT_TOGGLE_PLAYBACK")}
              }
              else if (tapEvent.getTapID == TapEvent.BUTTON_CLICKED) {
                println("play clicked")
                node.setTapped(false)
                node match {
                  case manipulableNode: ManipulableNode => {
                    manipulableNode ! UiEvent("TOGGLE_PLAYBACK") 
                  }
                  case otherNode => {
                    val (uiX, uiY) = (node.position._1, node.position._2)
                    val (x, y) = (uiX/Ui.width, uiY/Ui.height)

                    println("!!!!! FALLBACK manipulablenodetype othernode !!!!!!")

                    //Ui.audioInterface ! PlayAudioEvent(otherNode.id, x, y, PitchPropertyType.mean, VolumePropertyType.mean, otherNode.activeInputChannel, Array(0,1,2,3))
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

  override def vicinity = {
    0f
  }   

  override def toString = {
    "ManipulableNode"
  }
  
}



