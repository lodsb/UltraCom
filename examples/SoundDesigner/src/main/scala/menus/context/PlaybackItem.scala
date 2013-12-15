package ui.menus.context

import org.mt4j.Application


import org.mt4j.util.MTColor
import org.mt4j.util.math.Vector3D
import org.mt4j.util.math.Vertex
import org.mt4j.types.{Vec3d}

import processing.opengl.PGraphicsOpenGL
import processing.core.PConstants._

import ui.menus._
import ui.paths.types._
import ui.events._
import ui.usability._
import ui._
import ui.util._

object PlaybackItem {

  val Radius = Ui.width/150
  val StrokeWeight = 0
  
  private val StrokeColor = new MTColor(0, 0, 0, 0)
  private val BackgroundColor = new MTColor(0, 20, 80, 50)
  private val ForegroundColor = new MTColor(0, 0, 0, 150)
  
  def apply(app: Application, menu: NodeContextMenu, center: Vector3D, nodeType: EndNodeType) = {
      new PlaybackItem(app, menu, center, nodeType)
  }  
  
}


class PlaybackItem(app: Application, menu: NodeContextMenu, center: Vector3D, val nodeType: EndNodeType) extends MenuItem(app, menu, center, PlaybackItem.Radius) {

  override def clicked() = {
    menu.node.associatedPath.foreach(_ ! PathPlaybackTypeEvent(this.nodeType))
    menu.remove()
  }
  
  override def up() = {}
  
  override def down() = {}  
  
  override def radius = {
    PlaybackItem.Radius
  }
  
  override def itemForegroundColor = {
    PlaybackItem.ForegroundColor
  }
  
  override def itemBackgroundColor = {
    PlaybackItem.BackgroundColor
  }
  
  override def itemStrokeColor = {
    PlaybackItem.StrokeColor
  }
  
  override def itemStrokeWeight = {
    PlaybackItem.StrokeWeight
  }
  
  override def drawComponent(g: PGraphicsOpenGL) = {
    val center = this.getCenterPointLocal
    val nodeCenter = menu.node.getCenterPointLocal
    val gradient = Functions.gradient((center.getX, center.getY), (nodeCenter.getX, nodeCenter.getY))
    val cx = center.getX()
    val cy = center.getY()    
    val r = this.radius * 0.5f
    
    g.fill(this.itemBackgroundColor.getR, this.itemBackgroundColor.getG, this.itemBackgroundColor.getB, this.opacity * this.itemBackgroundColor.getA)
    g.noStroke()
    g.ellipse(cx, cy, 2*this.radius, 2*this.radius)     

    this.nodeType.symbol match {
        case Some(symbol) => {    
          val symbolColor = this.itemForegroundColor  
          g.fill(symbolColor.getR(), symbolColor.getG(), symbolColor.getB(), this.opacity * symbolColor.getA)
          g.noStroke()
          g.beginShape()
          val precision = 128
          (1 to precision).foreach(value => { 
            val (x,y) = symbol((cx,cy), r)(value.toFloat/precision)
            val (rotatedX, rotatedY) = Functions.transform((center.getX, center.getY), gradient, (x,y))
            g.vertex(rotatedX, rotatedY)
          })
          g.endShape(CLOSE)       
        }
        case None => {}
    }
    
  }    
  
}
