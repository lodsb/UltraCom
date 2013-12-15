package ui.paths.types

import org.mt4j.Application

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
import ui.properties.types._
import ui.usability._

/**
* This is the time connection node type.
* A node of this type can be used to remove a single time connection by double tapping.
* It does not exhibit dragging capability.
*/
object TimeConnectionNodeType extends NodeType{

  private val color = SpeedPropertyType.color
  protected val TimeBackgroundColor = Color(color.getR, color.getG, color.getB, 80)
  protected val TimeStrokeColor = Color(color.getR, color.getG, color.getB, 0)
  protected val TimeForegroundColor = Color(color.getR, color.getG, color.getB, 220)
  val Size = 0.7f
  val Symbol = Triangle
  
  protected override def setupInteractionImpl(app: Application, node: Node) = {
    node.setScale(Size)

    val tapProcessor = new TapProcessor(app)
    tapProcessor.setEnableDoubleTap(true)
    node.registerInputProcessor(tapProcessor)
    
    //node.addGestureListener(classOf[DragProcessor], new InertiaDragAction(200, .95f, 17)) //interesting feature =)  
    node.addGestureListener(classOf[TapProcessor], new NodeDeletionListener(node))
  }    
  

  override def size = {
    this.Size
  }  
   
  override def backgroundColor = {
   this.TimeBackgroundColor
  }
  
  override def strokeColor = {
   this.TimeStrokeColor
  }   
  
  override def foregroundColor = {
    this.TimeForegroundColor
  }
  
  override def symbol = {
    Some(this.Symbol)
  }  
  
  override def toString = {
   "TimeConnectionNode"
  }  

}
