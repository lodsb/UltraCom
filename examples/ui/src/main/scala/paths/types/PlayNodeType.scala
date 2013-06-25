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
* This is the play node type.
* A node associated with this type provides a means to play back its associated path by tapping.
*/
object PlayNodeType extends StartNodeType{  

  protected override def setupInteractionImpl(app: Application, node: Node) = {
    super.setupInteractionImpl(app, node)
    node.addGestureListener(classOf[TapProcessor], new IGestureEventListener() {
      override def processGestureEvent(gestureEvent: MTGestureEvent): Boolean = {
        gestureEvent match {
          case tapEvent: TapEvent => {
              if (tapEvent.getTapID == TapEvent.BUTTON_DOWN) {
                println("play down")
              }
              else if (tapEvent.getTapID == TapEvent.BUTTON_UP) {
                println("play up")
              }
              else if (tapEvent.getTapID == TapEvent.BUTTON_CLICKED) {
                println("play clicked")
                node.associatedPath match {
                  case Some(path) => {
                    path ! UiEvent("START_PLAYBACK")
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
    val r = 0.50f * this.radius
    
    //defining an equilateral triangle circumscribed by a circle of radius r centered around (cx, cy)
    val a = (3 * r/math.sqrt(3)).toFloat //side length of triangle
    val h = (math.sqrt(3)/2 * a).toFloat //height of triangle
    val segment = (math.cos(math.toRadians(60)) * r).toFloat //trigonometric function to calculate the length of one of the segments induced by the orthocenter of the triangle; here, the greater of the two segment lengths is obtained
    val (p1x, p1y) = (cx - segment, cy - a/2)
    val (p2x, p2y) = (cx - segment, cy + a/2)
    val (p3x, p3y) = (cx + h - segment, cy)   

    //rotating triangle relative to tangent at first point of path
    val (tx, ty) = node.associatedPath match {case Some(path) => {path.connections.head.tangent(0)} case None => {(0.0f, 0.0f)}} //get tangent at start point of path    
    val atan2 = math.atan2(tx, ty)
    val radAngle = if (atan2 > 0) atan2 - math.Pi/2 else 1.5*math.Pi + atan2 //get radians for tangent
    val point1 = ((cx + (p1x-cx)*math.cos(-radAngle) - (p1y-cy)*math.sin(-radAngle)).toFloat, (cy + (p1x-cx)*math.sin(-radAngle) + (p1y-cy)*math.cos(-radAngle)).toFloat)
    val point2 = ((cx + (p2x-cx)*math.cos(-radAngle) - (p2y-cy)*math.sin(-radAngle)).toFloat, (cy + (p2x-cx)*math.sin(-radAngle) + (p2y-cy)*math.cos(-radAngle)).toFloat)
    val point3 = ((cx + (p3x-cx)*math.cos(-radAngle) - (p3y-cy)*math.sin(-radAngle)).toFloat, (cy + (p3x-cx)*math.sin(-radAngle) + (p3y-cy)*math.cos(-radAngle)).toFloat)
    
    g.noStroke()
    val color = SymbolColor
    g.fill(color.getR(), color.getG(), color.getB(), color.getAlpha())
    g.triangle( point1._1, point1._2,
                point2._1, point2._2,
                point3._1, point3._2)
  }
  
}
