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
import processing.core.PGraphics
import processing.core.PConstants._

import org.mt4j.util.Color

import ui._
import ui.paths._
import ui.input._
import ui.util._
import ui.events._


object StartNodeType {
  protected[types] val SymbolColor = Color(0, 0, 0, 200)  
  val Size = 1.0f
}

/**
* This is the start node type trait.
* A node associated with this type is always the first node in a sequence of nodes.
*/
trait StartNodeType extends BasicNodeType{
  
  protected override def setupInteractionImpl(app: Application, node: Node) = {
    import StartNodeType._
    node.setScale(Size)    
    
    //register input processors
    node.registerInputProcessor(new DragProcessor(app))
    
    val tapProcessor = new TapProcessor(app)
    tapProcessor.setEnableDoubleTap(true)
    node.registerInputProcessor(tapProcessor)   
    
    val tapAndHoldProcessor = new TapAndHoldProcessor(app, 2000)
    tapAndHoldProcessor.setMaxFingerUpDist(5) 
    node.registerInputProcessor(tapAndHoldProcessor)
    
    
    //add gesture listeners
    node.addGestureListener(classOf[DragProcessor], new NotifyingDragAction(node))
    node.addGestureListener(classOf[TapAndHoldProcessor], new IGestureEventListener() {
      override def processGestureEvent(gestureEvent: MTGestureEvent): Boolean = {
        gestureEvent match {
          case tahEvent: TapAndHoldEvent => {
              if (tahEvent.getId == MTGestureEvent.GESTURE_DETECTED) {
                println("tap and hold detected")
              }
              else if (tahEvent.getId == MTGestureEvent.GESTURE_ENDED) {
                if (tahEvent.getElapsedTime >= tahEvent.getHoldTime) {
                  println("tap and hold completed")
                  
                }
                println("tap and hold ended")
              }
              //else if (tahEvent.getId == MTGestureEvent.GESTURE_UPDATED) {
                //println("tap and hold updated")
              //}
              true
          }
          case someEvent => {
              println("I can't process this particular event: " + someEvent.toString)
              false
          }
        }
      }
    })    
    //node.addGestureListener(classOf[DragProcessor], new InertiaDragAction(200, .95f, 17)) //interesting feature =)         
  }  
  
  
  override def drawComponent(g: PGraphics, node: Node) = {
    super.drawComponent(g, node)
    this.drawSymbol(g, node)
  }   
  
  
  def drawSymbol(g: PGraphics, node: Node)
  
  override def toString = {
    "StartNode"
  }
  
  override def vicinity = {
    this.radius * 2.0f
  }  
    
  
}
