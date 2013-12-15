package ui.paths.types

import org.mt4j.Application
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapProcessor 
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent
import org.mt4j.input.inputProcessors.IGestureEventListener
import org.mt4j.input.inputProcessors.MTGestureEvent

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


object DeleteManipulableNodeType extends NodeType {  
  private val DeleteBackgroundColor = Color(0, 20, 80, 70)
  private val DeleteStrokeColor = Color(0, 0, 0, 0)
  val Size = 0.5f
  
  protected override def setupInteractionImpl(app: Application, deletionNode: Node) = {
    val manipulableNodeOption = deletionNode match {case node: DeleteNode => Some(node.manipulableNode) case otherNode => None}  
    deletionNode.setScale(this.size)  
    
    val tapProcessor = new TapProcessor(app)
    tapProcessor.setEnableDoubleTap(true)
    deletionNode.registerInputProcessor(tapProcessor)      
      
    deletionNode.addGestureListener(classOf[TapProcessor], new IGestureEventListener() {
      override def processGestureEvent(gestureEvent: MTGestureEvent): Boolean = {
        gestureEvent match {
          case tapEvent: TapEvent => {
              if (tapEvent.getTapID == TapEvent.BUTTON_DOUBLE_CLICKED) {
                manipulableNodeOption.foreach(manipulableNode => {
                  manipulableNode ! UiEvent("REMOVE_TIME_CONNECTIONS")
                  Ui -= manipulableNode
                  Ui += IsolatedNode(Ui, manipulableNode.position)
                })
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

    
  override def size = {
    this.Size
  }   
  
  override def backgroundColor = {
    this.DeleteBackgroundColor
  }
  
  override def strokeColor = {
    this.DeleteStrokeColor
  }
  
  override def toString = {
    "DeleteManipulableNode"
  }
  
    
}  
