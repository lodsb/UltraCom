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

object BasicNodeType {
  protected[types] val FillColor = Color(0, 0, 0, 0)
  protected[types] val StrokeColor = Color(0, 0, 0, 100)
  val StrokeWeight = 1 //change to value relative to Ui size later // Ui.width/500.0f
}

/**
* This is the abstract basic node type.
* It sets up the basic appearance of certain nodes.
*/
abstract class BasicNodeType extends NodeType {

  override def drawComponent(g: PGraphics, node: Node) = {
    import BasicNodeType._
    val center = node.getCenterPointLocal()
    val cx = center.getX()
    val cy = center.getY()
    g.noFill()
    g.strokeWeight(StrokeWeight)
    g.stroke(StrokeColor.getR, StrokeColor.getG, StrokeColor.getB, StrokeColor.getAlpha)
    g.ellipse(cx, cy, this.radius*2, this.radius*2)
  } 

}
