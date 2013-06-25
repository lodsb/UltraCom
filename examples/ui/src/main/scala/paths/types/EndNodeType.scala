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
import processing.core.PGraphics
import processing.core.PConstants._

import org.mt4j.util.Color

import ui._
import ui.paths._
import ui.input._
import ui.events._

object EndNodeType {
  protected[types] val SymbolColor = Color(0, 0, 0, 200)  
  val Size = 1.0f
}

/**
* This is the end node type trait.
* A node associated with this type is always the last node in a sequence of nodes.
*/
trait EndNodeType extends BasicNodeType{
  
  protected override def setupInteractionImpl(app: Application, node: Node) = {
    import EndNodeType._
    node.setScale(Size)
    
    //register input processors
    node.registerInputProcessor(new DragProcessor(app))
    node.registerInputProcessor(new TapProcessor(app))
    
    //add gesture listeners
    node.addGestureListener(classOf[DragProcessor], new NotifyingDragAction(node))   
    //node.addGestureListener(classOf[DragProcessor], new InertiaDragAction(200, .95f, 17)) //interesting feature =)         
    node.addGestureListener(classOf[TapProcessor], new IGestureEventListener() {
        override def processGestureEvent(gestureEvent: MTGestureEvent): Boolean = {
              gestureEvent match {
                  case tapEvent: TapEvent => {
                      if (tapEvent.getTapID == TapEvent.BUTTON_DOWN) {
                        println("stop down")
                      }
                      else if (tapEvent.getTapID == TapEvent.BUTTON_UP) {
                        println("stop up")
                      }
                      else if (tapEvent.getTapID == TapEvent.BUTTON_CLICKED) {
                        println("stop clicked")
                        node.associatedPath match {
                          case Some(path) => {
                            path ! UiEvent("STOP_PLAYBACK")
                          }
                          case None => {}
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
 
  
  override def drawComponent(g: PGraphics, node: Node) = {
    super.drawComponent(g, node)
    this.drawSymbol(g, node)
  } 
  
  
  def drawSymbol(g: PGraphics, node: Node)

  override def toString = {
    "EndNode"
  }  
  
  override def vicinity = {
    this.radius * 2.0f
  }    
  
}
