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

import org.mt4j.input.IMTInputEventListener
import org.mt4j.input.inputData.MTInputEvent
import org.mt4j.input.inputData.MTFingerInputEvt

import org.mt4j.input.gestureAction.DefaultDragAction
import org.mt4j.input.gestureAction.InertiaDragAction

import org.mt4j.util.math.Vector3D
import org.mt4j.util.math.Vertex
import processing.opengl.PGraphicsOpenGL
import processing.core.PConstants._

import org.mt4j.util.Color

import ui._
import ui.paths._

/**
* This is the virtual node type.
* A node of this type is not part of a path but used to visually afford new connections between nodes.
*/
object VirtualNodeType extends NodeType{

  protected val VirtualBackgroundColor = Color(200, 200, 200, 150)
  protected val VirtualStrokeColor = Color(0, 0, 0, 0)
  val Size = 0.7f
  
  protected override def setupInteractionImpl(app: Application, node: Node) = {
    node.setScale(Size)
    node.setPickable(false)
  }    
  
  override def toString = {
   "VirtualNode"
  }
  
  override def backgroundColor = {
   this.VirtualBackgroundColor
  }
  
  override def strokeColor = {
   this.VirtualStrokeColor
  }
  
  override def size = {
    this.Size
  }  

}
