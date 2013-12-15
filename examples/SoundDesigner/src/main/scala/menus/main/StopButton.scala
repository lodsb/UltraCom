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
import ui.paths._
import ui.paths.types._
import ui.events._
import ui._

object StopButton {
  
  def apply(app: Application, menu: Menu, center: Vector3D) = {
      new StopButton(app, menu, center)
  }  
 
  
}


class StopButton(app: Application, menu: Menu, center: Vector3D) extends Button(app, menu, center) {
  
  override def drawComponent(g: PGraphicsOpenGL) = {
    super.drawComponent(g)
    val center = this.getCenterPointLocal()
    val cx = center.getX()
    val cy = center.getY()              
    val r = 0.35f * this.radius
    g.noStroke()
    g.fill(this.itemForegroundColor.getR, this.itemForegroundColor.getG, this.itemForegroundColor.getB, this.itemForegroundColor.getA * this.opacity)
    g.rect(cx - r, cy - r, 2*r, 2*r)
  }  

  override def clicked() = {
    super.clicked()
    Ui.paths.foreach(path => path ! UiEvent("STOP_GLOBAL_PLAYBACK"))
    Ui.nodes.collect({case m: ManipulableNode => m}).foreach(_ ! UiEvent("STOP_GLOBAL_PLAYBACK"))
  }

  override def up() = {
    super.up()
  }
  
  override def down() = {
    super.down() 
  }
  
}
