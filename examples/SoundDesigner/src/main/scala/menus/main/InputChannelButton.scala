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

import processing.core.PGraphics

import ui.menus._
import ui.paths.types._
import ui.paths._
import ui.events._
import ui.util._
import ui._

object InputChannelButton {
  
  def apply(app: Application, menu: Menu, center: Vector3D, channelNumber: Int) = {
      new InputChannelButton(app, menu, center, channelNumber)
  }  
 
  
}


class InputChannelButton(app: Application, menu: Menu, center: Vector3D, channelNumber: Int) extends Button(app, menu, center) {
  
  
  
  override def drawComponent(g: PGraphics) = {
    super.drawComponent(g)
    this.drawSymbol(g)
  }  

  def drawSymbol(g: PGraphics) = {
    val center = this.getCenterPointLocal()
    val cx = center.getX()
    val cy = center.getY()  
    
    g.noStroke()
    g.fill(this.itemForegroundColor.getR, this.itemForegroundColor.getG, this.itemForegroundColor.getB, this.itemForegroundColor.getAlpha * this.opacity)

    if (channelNumber == 0) {
      g.ellipse(cx, cy, 8, 8)
    }
    else {
      (1 to channelNumber + 1).foreach(item => {
        val (x,y) = Functions.positionOnCircle(cx, cy, 0.5f * this.radius, 2*math.Pi.toFloat, item, channelNumber + 1)
        g.ellipse(x, y, 8, 8)
      })
    }    
    
  } 
  
  override def clicked() = {
    super.clicked()
    if (!ChannelMenu.isMenuVisible(channelNumber)) {
      val menu = ChannelMenu(app, center, channelNumber)
      ChannelMenu += menu
      Ui += menu
    }
    else {
      val menuOption = ChannelMenu.menuFromChannelNumber(channelNumber)
      menuOption.foreach(menu => {
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
