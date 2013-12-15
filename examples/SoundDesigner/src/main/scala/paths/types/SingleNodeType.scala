package ui.paths.types

import org.mt4j.Application
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragProcessor 
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragEvent
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
import ui.input._

/**
* This is the single node type.
* A node associated with this type provides a means to play back a single timbre by tapping
* and allows for the auditory exploration of the timbre space by dragging.
*/
object SingleNodeType extends EndNodeType with StartNodeType {

  val Vicinity = this.radius * 2.5f  
  val Size = 0.9f
  val Symbol = Speaker
  
  protected override def setupInteractionImpl(app: Application, node: Node) = {
    super.setupInteractionImpl(app, node)

    node.addGestureListener(classOf[DragProcessor], new PlayTimbreDragAction(node))
    
    val tapProcessor = new TapProcessor(app)
    tapProcessor.setEnableDoubleTap(true)
    node.registerInputProcessor(tapProcessor)    
    
    node.addGestureListener(classOf[TapProcessor], new NodeDeletionListener(node))
    
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
                node match {case singleNode: SingleNode => singleNode ! UiEvent("IGNORE_IGNORE_NEXT_TOGGLE_PLAYBACK")}
              }
              else if (tapEvent.getTapID == TapEvent.BUTTON_CLICKED) {
                println("play clicked")
                node.setTapped(false)
                node match {
                  case singleNode: SingleNode => {
                    singleNode ! UiEvent("TOGGLE_PLAYBACK") 
                  }
                  case otherNode => {
                    val (uiX, uiY) = (node.position._1, node.position._2)
                    val (x, y) = (uiX/Ui.width, uiY/Ui.height)

                    println("!!!!! FALLBACK singlenodetype othernode !!!!!!")

                    //Ui.audioInterface ! PlayAudioEvent(otherNode.id, x, y, PitchPropertyType.mean, VolumePropertyType.mean, Array(0,1,2,3))
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
  
  
  override def toString = {
    "SingleNode"
  }
  
  override def vicinity = {
    this.Vicinity
  }  
  
  override def symbol = {
    Some(this.Symbol)
  }
  
  override def size = {
    this.Size
  }  
  
}



