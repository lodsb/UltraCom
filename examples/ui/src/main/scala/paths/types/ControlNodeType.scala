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
import processing.core.PGraphics
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

  val Vicinity = 0
  protected[types] val StrokeColor = Color(0, 0, 0, 80)
  val StrokeWeight = 1
  val Size = 0.4f
  
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
  
  override def drawComponent(g: PGraphics, node: Node) = {
    val center = node.getCenterPointLocal()
    val cx = center.getX()
    val cy = center.getY()
    g.noFill()
    g.strokeWeight(StrokeWeight)
    g.stroke(StrokeColor.getR, StrokeColor.getG, StrokeColor.getB, StrokeColor.getAlpha)
    g.ellipse(cx, cy, this.radius*2, this.radius*2)
  } 
  
   override def toString = {
     "ControlNode"
   }
   
   override def vicinity = {
     Vicinity
   }

}
