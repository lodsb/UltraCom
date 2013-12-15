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
import org.mt4j.util.SessionLogger

import processing.opengl.PGraphicsOpenGL

import ui.menus._
import ui.paths.types._
import ui.paths._
import ui.events._
import ui.util._
import ui._

object InputChannelButton {
 
  final val OnOpacity = 1.0f
  final val OffOpacity = 0.2f
  final val DotWidth = Button.Radius/2.5f
  
  def apply(app: Application, menu: Menu, center: Vector3D, channelNumber: Int) = {
      new InputChannelButton(app, menu, center, channelNumber)
  }  
 
  
}


class InputChannelButton(app: Application, menu: Menu, center: Vector3D, channelNumber: Int) extends Button(app, menu, center) {
  
  this.setOpacity(InputChannelButton.OffOpacity)
  
  override def drawComponent(g: PGraphicsOpenGL) = {
    this.drawComponentImpl(g)
    this.drawSymbol(g)
  }  
  
  def drawComponentImpl(g: PGraphicsOpenGL) = {
    //super.drawComponent(g)
    val center = this.getCenterPointLocal()
    val cx = center.getX()
    val cy = center.getY() 
    val visibility = if (ChannelMenu.isMenuVisible(channelNumber)) InputChannelButton.OnOpacity else InputChannelButton.OffOpacity
    
    g.fill(this.currentColor.getR, this.currentColor.getG, this.currentColor.getB, this.currentColor.getA * this.opacity * visibility)
    g.stroke(this.itemStrokeColor.getR, this.itemStrokeColor.getG, this.itemStrokeColor.getB, this.itemStrokeColor.getA * this.opacity * visibility)
    g.strokeWeight(this.itemStrokeWeight)
    g.ellipse(cx, cy, 2*this.radius, 2*this.radius)  
  }    

  def drawSymbol(g: PGraphicsOpenGL) = {
    val center = this.getCenterPointLocal()
    val cx = center.getX()
    val cy = center.getY()  
    val visibility = if (ChannelMenu.isMenuVisible(channelNumber)) InputChannelButton.OnOpacity else InputChannelButton.OffOpacity
    
    g.noStroke()
    g.fill(this.itemForegroundColor.getR, this.itemForegroundColor.getG, this.itemForegroundColor.getB, this.itemForegroundColor.getA * this.opacity * visibility)

    if (channelNumber == 0) {
      g.ellipse(cx, cy, InputChannelButton.DotWidth, InputChannelButton.DotWidth)
    }
    else {
      (1 to channelNumber + 1).foreach(item => {
        val (x,y) = Functions.positionOnCircle(cx, cy, 0.5f * this.radius, 2*math.Pi.toFloat, item, channelNumber + 1)
        g.ellipse(x, y, InputChannelButton.DotWidth, InputChannelButton.DotWidth)
      })
    }    
    
  } 
  
  override def clicked() = {
    super.clicked()
    if (!ChannelMenu.isMenuVisible(channelNumber)) {
      val newGlobalVector = this.localToGlobal(Vec3d(this.getCenterPointLocal.getX, this.getCenterPointLocal.getY - ChannelMenu.Height/2f - Menu.Space))
      val menu = ChannelMenu(app, newGlobalVector, channelNumber)
      SessionLogger.log("Activated: channel menu with channel " + channelNumber,SessionLogger.SessionEvent.Event, this, null, null)
      ChannelMenu += menu
      Ui += menu
    }
    else {
      val menuOption = ChannelMenu.menuFromChannelNumber(channelNumber)
      menuOption.foreach(menu => {
        SessionLogger.log("Deactivated: channel menu with channel " + channelNumber,SessionLogger.SessionEvent.Event, this, null, null)        
        ChannelMenu -= menu
        Ui -= menu
      })
    }
  }
  
  override def up() = {
    super.up()
  }
  
  override def down() = {
    super.down() 
  }
  
  
}
