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
import ui.paths._

/**
* This is the virtual node type.
* A node of this type is not part of a path but used to visually afford new connections between nodes.
*/
object VirtualNodeType extends NodeType{

  val Vicinity = 0
  protected val StrokeColor = Color(0, 0, 0, 40)
  val StrokeWeight = 2
  val Size = 0.5f
  
  protected override def setupInteractionImpl(app: Application, node: Node) = {
    node.setScale(Size)
    node.setPickable(false)
    //register input processors
    /*node.registerInputProcessor(new DragProcessor(app))
    
    //add input listener
    node.addInputListener(new IMTInputEventListener[MTFingerInputEvt]() {
        override def processInputEvent(inputEvent: MTFingerInputEvt): Boolean = {
            val x = inputEvent.getScreenX
            val y = inputEvent.getScreenY
            println(x + " " + y)
            true
        }
    })	
    
    //add gesture listeners
    node.addGestureListener(classOf[DragProcessor], new DefaultDragAction())
    */
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
     "VirtualNode"
   }
   
   override def vicinity = {
     Vicinity
   }

}
