package ui.menus.main

import org.mt4j.Application

import org.mt4j.components.TransformSpace
import org.mt4j.components.MTComponent
import org.mt4j.components.visibleComponents.shapes.MTEllipse

import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapProcessor 
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent
import org.mt4j.input.inputProcessors.IGestureEventListener
import org.mt4j.input.inputProcessors.MTGestureEvent

import org.mt4j.util.MTColor
import org.mt4j.util.math.Vector3D
import org.mt4j.util.math.Vertex
import org.mt4j.types.{Vec3d}

import processing.opengl.PGraphicsOpenGL

import scala.actors._

import ui.menus._
import ui.paths.types._
import ui._
import processing.opengl.PGraphicsOpenGL

object Button {
  
  val Radius = Ui.width/100
  private val StrokeColor = new MTColor(0, 0, 0, 0)
  private val BackgroundColor = new MTColor(0, 20, 80, 50)
  private val ForegroundColor = new MTColor(0, 0, 0, 200)
  private val TapColor = new MTColor(0, 20, 80, 100)
  val StrokeWeight = 1
  
}


abstract class Button(app: Application, menu: Actor, center: Vector3D) extends MenuItem(app, menu, center, Button.Radius) {
  
  protected var currentColor = this.itemBackgroundColor
  
  override def radius = {
    Button.Radius
  }
  
  override def itemForegroundColor = {
    Button.ForegroundColor
  }
  
  override def itemBackgroundColor = {
    Button.BackgroundColor
  }
  
  override def itemStrokeColor = {
    Button.StrokeColor
  }
  
  override def itemStrokeWeight = {
    Button.StrokeWeight
  }
  
  def itemTapColor = {
    Button.TapColor
  }
  
  override def clicked() = {
    this.setTapped(false)
  }
  
  override def up() = {
    this.setTapped(false)
  }
  
  override def down() = {
    this.setTapped(true) 
  }  
  
  override def drawComponent(g: PGraphicsOpenGL) = {
    super.drawComponent(g)
    val center = this.getCenterPointLocal()
    val cx = center.getX()
    val cy = center.getY()    
    g.fill(this.currentColor.getR, this.currentColor.getG, this.currentColor.getB, this.currentColor.getA * this.opacity)
    g.stroke(this.itemStrokeColor.getR, this.itemStrokeColor.getG, this.itemStrokeColor.getB, this.itemStrokeColor.getA * this.opacity)
    g.strokeWeight(this.itemStrokeWeight)
    g.ellipse(cx, cy, 2*this.radius, 2*this.radius)  
  }  
  
  def setTapped(isTapped: Boolean) = {
    this.currentColor = if (isTapped) this.itemTapColor else this.itemBackgroundColor
  }

}
  
