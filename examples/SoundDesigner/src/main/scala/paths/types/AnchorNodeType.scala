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
import ui.util._
import ui.input._
import ui.paths._
import ui.usability._


/**
* This is the anchor node type.
* A node of this type defines a point in the timbre space through which its associated path shall pass.
*/
object AnchorNodeType extends NodeType{

  val Size = 0.65f //size relative to basic node type
  val Symbol = Circle
  
  protected override def setupInteractionImpl(app: Application, node: Node) = {
    //scale //TODO this is also visual appearance, not just interaction, so... there...
    node.setScale(Size) //TODO REVISIT radius, distance, scale....
    
    //register input processors
    node.registerInputProcessor(new DragProcessor(app))
    val tapProcessor = new TapProcessor(app)
    tapProcessor.setEnableDoubleTap(true)
    node.registerInputProcessor(tapProcessor)
    
    //add gesture listeners
    node.addGestureListener(classOf[DragProcessor], new NotifyingDragAction(node))   
    //node.addGestureListener(classOf[DragProcessor], new InertiaDragAction(200, .95f, 17)) //interesting feature =)        
    node.addGestureListener(classOf[TapProcessor], new NodeDeletionListener(node))
  }    
 
  
  override def toString = {
   "AnchorNode"
  }  
  
  override def symbol = {
    Some(this.Symbol)
  }
  
  override def size = {
    this.Size
  }
  
}
