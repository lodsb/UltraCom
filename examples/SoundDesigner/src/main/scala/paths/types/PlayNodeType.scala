package ui.paths.types

import org.mt4j.Application
import org.mt4j.components.visibleComponents.shapes.MTEllipse
import org.mt4j.components.visibleComponents.shapes.MTPolygon
import org.mt4j.components.visibleComponents.StyleInfo

import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragProcessor 
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragEvent
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapProcessor 
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent
import org.mt4j.input.inputProcessors.IGestureEventListener
import org.mt4j.input.inputProcessors.MTGestureEvent
import org.mt4j.input.gestureAction.DefaultDragAction
import org.mt4j.input.gestureAction.InertiaDragAction

import org.mt4j.util.math.Vector3D
import org.mt4j.util.math.Vertex
import processing.opengl.PGraphicsOpenGL
import processing.core.PConstants._

import org.mt4j.util.Color

import ui._
import ui.paths._
import ui.util._
import ui.events._
import ui.usability._

/**
* This is the play node type.
* A node associated with this type provides a means to play back its associated path by tapping.
*/
object PlayNodeType extends StartNodeType{  

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
                node.setTapped(true)
                println("tap play down")
              }
              else if (tapEvent.getTapID == TapEvent.BUTTON_UP) {
                println("tap play up")
                node.setTapped(false)
                node.associatedPath.foreach(_ ! UiEvent("IGNORE_IGNORE_NEXT_TOGGLE_PLAYBACK"))
              }
              else if (tapEvent.getTapID == TapEvent.BUTTON_CLICKED) {
                println("tap play clicked")
                node.setTapped(false)
                node.associatedPath.foreach(_ ! UiEvent("START_PLAYBACK"))
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
  

  override def toString = {
    "PlayNode"
  }  
  
  
}
