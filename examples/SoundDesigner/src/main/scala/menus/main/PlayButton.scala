package ui.menus.main

import org.mt4j.Application

import org.mt4j.components.TransformSpace
import org.mt4j.components.MTComponent
import org.mt4j.components.visibleComponents.shapes.MTEllipse

import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapProcessor 
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent
import org.mt4j.input.inputProcessors.IGestureEventListener
import org.mt4j.input.inputProcessors.MTGestureEvent

import org.mt4j.util.Color
import org.mt4j.util.math.Vector3D
import org.mt4j.util.math.Vertex
import org.mt4j.types.{Vec3d}

import processing.opengl.PGraphicsOpenGL

import ui.menus._
import ui.paths.types._
import ui.paths._
import ui.events._
import ui._

object PlayButton {
  
  def apply(app: Application, menu: Menu, center: Vector3D) = {
      new PlayButton(app, menu, center)
  }  
 
  
}


class PlayButton(app: Application, menu: Menu, center: Vector3D) extends Button(app, menu, center) {
  
  override def drawComponent(g: PGraphicsOpenGL) = {
    super.drawComponent(g)
    if (Playback.isPlaying) drawPauseSymbol(g) else drawPlaySymbol(g)
  }  

  def drawPlaySymbol(g: PGraphicsOpenGL) = {
    val center = this.getCenterPointLocal()
    val cx = center.getX()
    val cy = center.getY()  
    
    //defining an equilateral triangle circumscribed by a circle of radius r centered around (cx, cy)
    val r = 0.50f * this.radius
    val a = (3 * r/math.sqrt(3)).toFloat //side length of triangle
    val h = (math.sqrt(3)/2 * a).toFloat //height of triangle
    val segment = (math.cos(math.toRadians(60)) * r).toFloat //trigonometric function to calculate the length of one of the segments induced by the orthocenter of the triangle; here, the greater of the two segment lengths is obtained
    val (p1x, p1y) = (cx - segment, cy - a/2)
    val (p2x, p2y) = (cx - segment, cy + a/2)
    val (p3x, p3y) = (cx + h - segment, cy)   

    g.noStroke()
    g.fill(this.itemForegroundColor.getR, this.itemForegroundColor.getG, this.itemForegroundColor.getB, this.itemForegroundColor.getA * this.opacity)
    g.triangle( p1x, p1y,
                p2x, p2y,
                p3x, p3y)
  }

  
  def drawPauseSymbol(g: PGraphicsOpenGL) = {
    val center = this.getCenterPointLocal()
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
           
    g.noStroke()
    g.fill(this.itemForegroundColor.getR, this.itemForegroundColor.getG, this.itemForegroundColor.getB, this.itemForegroundColor.getA * this.opacity)
    g.quad(p1x, p1y, p2x, p2y, p3x, p3y, p4x, p4y)
    g.quad(q1x, q1y, q2x, q2y, q3x, q3y, q4x, q4y)
  }
 
  
  override def clicked() = {
    super.clicked()
    if (!Playback.isPlaying) {
      Ui.paths.foreach(_ ! UiEvent("START_GLOBAL_PLAYBACK"))
      Ui.nodes.collect({case m: ManipulableNode => m}).foreach(_ ! UiEvent("START_GLOBAL_PLAYBACK"))
    }
    else {
      Ui.paths.foreach(_ ! UiEvent("PAUSE_PLAYBACK"))
      Ui.nodes.collect({case m: ManipulableNode => m}).foreach(_ ! UiEvent("STOP_GLOBAL_PLAYBACK"))
    }
  }
  
  override def up() = {
    super.up()
  }
  
  override def down() = {
    super.down() 
  }
  
  
}
  
