package ui.paths.types

import org.mt4j.Application

import org.mt4j.components.TransformSpace
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
import ui.input._
import ui.paths._

/**
* This is the control node type.
* A node of this type shapes its associated path and can be dragged around.
*/
object ControlNodeType extends NodeType{

  protected[types] val ControlStrokeColor = Color(0, 0, 0, 80)
  val Size = 0.50f
  
  protected override def setupInteractionImpl(app: Application, node: Node) = {
    node.setScale(Size)
    
    node.registerInputProcessor(new DragProcessor(app))
    val tapProcessor = new TapProcessor(app)
    tapProcessor.setEnableDoubleTap(true)
    node.registerInputProcessor(tapProcessor)
    
    node.addGestureListener(classOf[DragProcessor], new NotifyingDragAction(node)) 
    //node.addGestureListener(classOf[DragProcessor], new InertiaDragAction(200, .95f, 17)) //interesting feature =)  
    node.addGestureListener(classOf[TapProcessor], new NodeDeletionListener(node))
  }    
  
   override def toString = {
     "ControlNode"
   }
   
   override def strokeColor = {
     this.ControlStrokeColor
   }
   
  override def size = {
    this.Size
  }   

}
