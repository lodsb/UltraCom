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
import ui.paths._
import ui.input._
import ui.events._
import ui.menus.context._

object EndNodeType {
  val Size = 1.0f
}

/**
* This is the end node type trait.
* A node associated with this type is always the last node in a sequence of nodes.
*/
trait EndNodeType extends NodeType{
  
  protected override def setupInteractionImpl(app: Application, node: Node) = {
    import EndNodeType._
    node.setScale(this.size)
    
    //register input processors
    node.registerInputProcessor(new DragProcessor(app))
    
    val tapAndHoldProcessor = new TapAndHoldProcessor(app, NodeContextMenu.Delay)
    tapAndHoldProcessor.setMaxFingerUpDist(3) 
    node.registerInputProcessor(tapAndHoldProcessor)
        
    //add gesture listeners
    node.addGestureListener(classOf[DragProcessor], new NotifyingDragAction(node))
    node.addGestureListener(classOf[DragProcessor], new PlayTimbreDragAction(node))
    node.addGestureListener(classOf[TapAndHoldProcessor], new PlaybackContextMenuListener(node))       
       
    //node.addGestureListener(classOf[DragProcessor], new InertiaDragAction(200, .95f, 17)) //interesting feature =)         
  }    
 
  override def toString = {
    "EndNode"
  }  
  
  override def vicinity = {
    this.radius * 2.5f
  }    
  
  override def size = {
    EndNodeType.Size
  }  
  
}
