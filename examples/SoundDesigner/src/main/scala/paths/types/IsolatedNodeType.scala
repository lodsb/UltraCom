package ui.paths.types

import org.mt4j.Application
import org.mt4j.components.visibleComponents.shapes.MTEllipse
import org.mt4j.components.visibleComponents.shapes.MTPolygon
import org.mt4j.components.visibleComponents.StyleInfo

import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragProcessor 
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragEvent
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapProcessor 
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent
import org.mt4j.input.inputProcessors.componentProcessors.tapAndHoldProcessor.TapAndHoldProcessor
import org.mt4j.input.inputProcessors.componentProcessors.tapAndHoldProcessor.TapAndHoldEvent
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
import ui.util._
import ui.input._
import ui.paths._
import ui.usability._
import ui.menus.context._

/**
* This is the isolated node type.
* A node associated with this type is not part of any path, i.e. it is isolated.
* It can be used to explore the timbre space by dragging it around.
*/
object IsolatedNodeType extends NodeType{

  val Vicinity = this.radius * 2.5f  
  val Size = 0.8f
  val Symbol = Speaker
  
  protected override def setupInteractionImpl(app: Application, node: Node) = {
    node.setScale(Size)    
    
    //register input processors
    node.registerInputProcessor(new DragProcessor(app))
    
    val tapProcessor = new TapProcessor(app)
    tapProcessor.setEnableDoubleTap(true)
    node.registerInputProcessor(tapProcessor)
    
    val tapAndHoldProcessor = new TapAndHoldProcessor(app, NodeContextMenu.Delay)
    tapAndHoldProcessor.setMaxFingerUpDist(5) 
    node.registerInputProcessor(tapAndHoldProcessor)
    
    
    //add gesture listeners
    node.addGestureListener(classOf[DragProcessor], new PlayTimbreDragAction(node))   
    node.addGestureListener(classOf[TapAndHoldProcessor], new ChannelContextMenuListener(node))
    //node.addGestureListener(classOf[DragProcessor], new InertiaDragAction(200, .95f, 17)) //interesting feature =)        
    node.addGestureListener(classOf[TapProcessor], new NodeDeletionListener(node))
    
    node.addGestureListener(classOf[TapProcessor], new IGestureEventListener() {
      override def processGestureEvent(gestureEvent: MTGestureEvent): Boolean = {
        gestureEvent match {
          case tapEvent: TapEvent => {
              if (tapEvent.getTapID == TapEvent.BUTTON_DOWN) {
                node.setTapped(true)
                println("play down")
              }
              else if (tapEvent.getTapID == TapEvent.BUTTON_UP) {
                println("play up")
                node.setTapped(false)
              }
              else if (tapEvent.getTapID == TapEvent.BUTTON_CLICKED) {
                println("play clicked")
                node.setTapped(false)
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
    "IsolatedNode"
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
