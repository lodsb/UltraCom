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

/**
* This is the stop node type.
* A node associated with this type functions as a stop signal for the playing back of a path,
* that is, once the ende node is reached, playback stops.
*/
object StopNodeType extends EndNodeType{
  
  override def drawSymbol(g: PGraphics, node: Node) = {
    import EndNodeType._
    val center = node.getCenterPointLocal()
    val cx = center.getX()
    val cy = center.getY()
    val r = 0.35f * this.radius
    
    g.noStroke()
    val color = SymbolColor
    g.fill(color.getR(), color.getG(), color.getB(), color.getAlpha())    
    
    //defining a rectangle with width = height = 2*r and center (cx, cy)
    val (p1x, p1y) = (cx - r, cy - r)
    val (p2x, p2y) = (cx - r, cy + r)
    val (p3x, p3y) = (cx + r, cy + r)
    val (p4x, p4y) = (cx + r, cy - r)
    
    //rotating triangle relative to tangent at last point of path
    val (tx, ty) = node.associatedPath match {case Some(path) => {path.connections.last.tangent(1)} case None => {(0.0f, 0.0f)}} //get tangent at end point of path   
    val atan2 = math.atan2(tx, ty)
    val radAngle = if (atan2 > 0) atan2 else 2*math.Pi + atan2 //get radians for tangent
    val point1 = ((cx + (p1x-cx)*math.cos(-radAngle) - (p1y-cy)*math.sin(-radAngle)).toFloat, (cy + (p1x-cx)*math.sin(-radAngle) + (p1y-cy)*math.cos(-radAngle)).toFloat)
    val point2 = ((cx + (p2x-cx)*math.cos(-radAngle) - (p2y-cy)*math.sin(-radAngle)).toFloat, (cy + (p2x-cx)*math.sin(-radAngle) + (p2y-cy)*math.cos(-radAngle)).toFloat)
    val point3 = ((cx + (p3x-cx)*math.cos(-radAngle) - (p3y-cy)*math.sin(-radAngle)).toFloat, (cy + (p3x-cx)*math.sin(-radAngle) + (p3y-cy)*math.cos(-radAngle)).toFloat)
    val point4 = ((cx + (p4x-cx)*math.cos(-radAngle) - (p4y-cy)*math.sin(-radAngle)).toFloat, (cy + (p4x-cx)*math.sin(-radAngle) + (p4y-cy)*math.cos(-radAngle)).toFloat)     
    
    g.quad(point1._1, point1._2, point2._1, point2._2, point3._1, point3._2, point4._1, point4._2)
  }  
  
}
