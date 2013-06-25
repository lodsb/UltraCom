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
import ui.input._
import ui.paths._

/**
* This is the isolated node type.
* A node associated with this type is not part of any path, i.e. it is isolated.
* It can be used to explore the timbre space by dragging it around.
*/
object IsolatedNodeType extends BasicNodeType{

  private val SymbolColor = Color(0, 0, 0, 200)  
  val Vicinity = this.radius * 2.0f  
  val Size = 0.8f
  
  protected override def setupInteractionImpl(app: Application, node: Node) = {
    node.setScale(Size)    
    
    //register input processors
    node.registerInputProcessor(new DragProcessor(app))
    val tapProcessor = new TapProcessor(app)
    tapProcessor.setEnableDoubleTap(true)
    node.registerInputProcessor(tapProcessor)
    
    //add gesture listeners
    node.addGestureListener(classOf[DragProcessor], new PlayTimbreDragAction(node))   
    node.addGestureListener(classOf[DragProcessor], new InertiaDragAction(200, .95f, 17)) //interesting feature =)        
    node.addGestureListener(classOf[TapProcessor], new NodeDeletionListener(node))
  }  
  
  
  override def drawComponent(g: PGraphics, node: Node) = {
    super.drawComponent(g, node)
    this.drawSymbol(g, node)
  }   
  
  
  def drawSymbol(g: PGraphics, node: Node) = {
    val center = node.getCenterPointLocal()
    val cx = center.getX()
    val cy = center.getY()  
    val r = 0.50f * this.radius

    val color = SymbolColor
    g.stroke(color.getR(), color.getG(), color.getB(), color.getAlpha())
    g.strokeWeight(4)
    
    /*g.line(cx-r, cy, cx+r, cy)
    g.line(cx, cy-r, cx, cy+r)
    */
    g.line(cx - 11*r/8, cy, cx - 4, cy)
    g.line(cx + 11*r/8, cy, cx + 4, cy)
    g.line(cx, cy - 11*r/8, cx, cy - 4)
    g.line(cx, cy + 11*r/8, cx, cy + 4)
    
  }
  
  override def toString = {
    "IsolatedNode"
  }
  
  override def vicinity = {
    Vicinity
  }  
    
  
}
