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
import ui.util._
import ui.events._

/**
* This is the pause node type.
* A node associated with this type indicates that the playback of its associated path can be paused by tapping.
*/
object PauseNodeType extends StartNodeType {  
 
  protected override def setupInteractionImpl(app: Application, node: Node) = {
    super.setupInteractionImpl(app, node)
    node.addGestureListener(classOf[TapProcessor], new IGestureEventListener() {
      override def processGestureEvent(gestureEvent: MTGestureEvent): Boolean = {
        gestureEvent match {
          case tapEvent: TapEvent => {
              if (tapEvent.getTapID == TapEvent.BUTTON_DOWN) {
                println("pause down")
              }
              else if (tapEvent.getTapID == TapEvent.BUTTON_UP) {
                println("pause up")
              }
              else if (tapEvent.getTapID == TapEvent.BUTTON_CLICKED) {
                println("pause clicked")
                node.associatedPath match {
                  case Some(path) => {
                    path ! UiEvent("PAUSE_PLAYBACK")
                  }
                  case None => {}
                }
              }
              true
          }
          case someEvent => {
              println("I can't process this particular event: " + someEvent.toString)
              false
          }
        }
      }
    })    
  }  
  
  def drawSymbol(g: PGraphics, node: Node) = {
    import StartNodeType._
    val center = node.getCenterPointLocal()    
    val cx = center.getX()
    val cy = center.getY()  
    val r = 0.4f * this.radius
    
    val (p1x, p1y) = (cx - r, cy - r)
    val (p2x, p2y) = (cx - r/3, cy - r)
    val (p3x, p3y) = (cx - r/3, cy + r)
    val (p4x, p4y) = (cx - r, cy + r)  
    
    val (q1x, q1y) = (cx + r, cy - r)
    val (q2x, q2y) = (cx + r/3, cy - r)
    val (q3x, q3y) = (cx + r/3, cy + r)
    val (q4x, q4y) = (cx + r, cy + r)  
    
    //rotating rectangles relative to tangent at first point of path
    val (tx, ty) = node.associatedPath match {case Some(path) => {path.connections.head.tangent(0)} case None => {(0.0f, 0.0f)}} //get tangent at start point of path    
    val atan2 = math.atan2(tx, ty)
    val radAngle = if (atan2 > 0) atan2 - math.Pi/2 else 1.5*math.Pi + atan2 //get radians for tangent
    val (lx1, ly1) = ((cx + (p1x-cx)*math.cos(-radAngle) - (p1y-cy)*math.sin(-radAngle)).toFloat, (cy + (p1x-cx)*math.sin(-radAngle) + (p1y-cy)*math.cos(-radAngle)).toFloat)
    val (lx2, ly2) = ((cx + (p2x-cx)*math.cos(-radAngle) - (p2y-cy)*math.sin(-radAngle)).toFloat, (cy + (p2x-cx)*math.sin(-radAngle) + (p2y-cy)*math.cos(-radAngle)).toFloat)
    val (lx3, ly3) = ((cx + (p3x-cx)*math.cos(-radAngle) - (p3y-cy)*math.sin(-radAngle)).toFloat, (cy + (p3x-cx)*math.sin(-radAngle) + (p3y-cy)*math.cos(-radAngle)).toFloat)
    val (lx4, ly4) = ((cx + (p4x-cx)*math.cos(-radAngle) - (p4y-cy)*math.sin(-radAngle)).toFloat, (cy + (p4x-cx)*math.sin(-radAngle) + (p4y-cy)*math.cos(-radAngle)).toFloat)    

    val (rx1, ry1) = ((cx + (q1x-cx)*math.cos(-radAngle) - (q1y-cy)*math.sin(-radAngle)).toFloat, (cy + (q1x-cx)*math.sin(-radAngle) + (q1y-cy)*math.cos(-radAngle)).toFloat)
    val (rx2, ry2) = ((cx + (q2x-cx)*math.cos(-radAngle) - (q2y-cy)*math.sin(-radAngle)).toFloat, (cy + (q2x-cx)*math.sin(-radAngle) + (q2y-cy)*math.cos(-radAngle)).toFloat)
    val (rx3, ry3) = ((cx + (q3x-cx)*math.cos(-radAngle) - (q3y-cy)*math.sin(-radAngle)).toFloat, (cy + (q3x-cx)*math.sin(-radAngle) + (q3y-cy)*math.cos(-radAngle)).toFloat)
    val (rx4, ry4) = ((cx + (q4x-cx)*math.cos(-radAngle) - (q4y-cy)*math.sin(-radAngle)).toFloat, (cy + (q4x-cx)*math.sin(-radAngle) + (q4y-cy)*math.cos(-radAngle)).toFloat)    
      
    g.noStroke()
    val color = SymbolColor
    g.fill(color.getR(), color.getG(), color.getB(), color.getAlpha())
    g.quad(lx1, ly1, lx2, ly2, lx3, ly3, lx4, ly4)
    g.quad(rx1, ry1, rx2, ry2, rx3, ry3, rx4, ry4)
    
  }
  
}
