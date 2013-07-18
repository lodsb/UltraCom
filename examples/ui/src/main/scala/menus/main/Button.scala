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

import processing.core.PGraphics

import scala.actors._

import ui.menus._
import ui.paths.types._
import ui._

object Button {
  
  val Radius = Ui.width/100
  private val StrokeColor = new MTColor(0, 0, 0, 100)
  private val BackgroundColor = new MTColor(255, 255, 255)
  private val ForegroundColor = new MTColor(0, 0, 0, 200)
  val StrokeWeight = 1
  
}


abstract class Button(app: Application, menu: Actor, center: Vector3D) extends MenuItem(app, menu, center, Button.Radius) {
  
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
  
  override def drawComponent(g: PGraphics) = {
    val center = this.getCenterPointLocal()
    val cx = center.getX()
    val cy = center.getY()    
    g.fill(this.itemBackgroundColor.getR, this.itemBackgroundColor.getG, this.itemBackgroundColor.getB, this.alphaValue)
    g.stroke(this.itemStrokeColor.getR, this.itemStrokeColor.getG, this.itemStrokeColor.getB, this.alphaValue)
    g.strokeWeight(this.itemStrokeWeight)
    g.ellipse(cx, cy, 2*this.radius, 2*this.radius)  
  }  

}
  
