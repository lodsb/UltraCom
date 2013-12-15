package ui.menus.context

import org.mt4j.Application


import org.mt4j.util.MTColor
import org.mt4j.util.math.Vector3D
import org.mt4j.util.math.Vertex
import org.mt4j.types.{Vec3d}
import org.mt4j.util.SessionLogger

import processing.opengl.PGraphicsOpenGL

import ui.menus._
import ui.paths.types._
import ui.events._
import ui._
import ui.properties.types._
import ui.tools._

object ToolItem {

  val Radius = Ui.width/100
  val StrokeWeight = 0 
  
  private val StrokeColor = new MTColor(0, 0, 0, 0)
  
  def apply(app: Application, menu: ToolContextMenu, center: Vector3D, propertyType: PropertyType) = {
      new ToolItem(app, menu, center, propertyType)
  }  
  
}


class ToolItem(app: Application, menu: ToolContextMenu, center: Vector3D, val propertyType: PropertyType) extends MenuItem(app, menu, center, ToolItem.Radius) {

  override def clicked() = {
    println("clicked!")
    SessionLogger.log("Created: Tool of type " + propertyType, SessionLogger.SessionEvent.Event, this, null, null)
    Ui += Tool(Ui, (menu.position.getX, menu.position.getY), propertyType)
    menu.remove()
  }
  
  override def up() = {
    println("up!")
  }
  
  override def down() = {
    println("down!")
  }  
  
  override def radius = {
    ToolItem.Radius
  }
  
  override def itemForegroundColor = {
    val color = this.propertyType.color
    new MTColor(color.getR, color.getG, color.getB, 200)
  }
  
  override def itemBackgroundColor = {
    val color = this.propertyType.color
    new MTColor(color.getR, color.getG, color.getB, 100)
  }
  
  override def itemStrokeColor = {
    ToolItem.StrokeColor
  }
  
  override def itemStrokeWeight = {
    ToolItem.StrokeWeight
  }
  
  override def drawComponent(g: PGraphicsOpenGL) = {
    val center = this.getCenterPointLocal()
    val cx = center.getX()
    val cy = center.getY()    
    g.fill(this.itemBackgroundColor.getR, this.itemBackgroundColor.getG, this.itemBackgroundColor.getB, this.opacity * this.itemBackgroundColor.getA)
    g.noStroke()
    g.ellipse(cx, cy, 2*this.radius, 2*this.radius)  
    
    val symbolColor = new MTColor(this.itemForegroundColor.getR, this.itemForegroundColor.getG, this.itemForegroundColor.getB, this.opacity * this.itemForegroundColor.getA)
    this.propertyType.drawSymbol(g, (cx, cy), symbolColor)
  }    
  
}
