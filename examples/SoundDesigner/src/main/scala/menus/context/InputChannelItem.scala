package ui.menus.context

import org.mt4j.Application

import org.mt4j.util.MTColor
import org.mt4j.util.math.Vector3D
import org.mt4j.util.math.Vertex
import org.mt4j.types.{Vec3d}
import org.mt4j.components.bounds.BoundsZPlaneRectangle
import org.mt4j.components.visibleComponents.shapes.MTRoundRectangle

import processing.opengl.PGraphicsOpenGL

import scala.actors._

import ui.audio._
import ui.menus._
import ui.paths._
import ui.paths.types._
import ui.events._
import ui.util._
import ui._

object InputChannelItem {

  val Radius = Ui.width/150
  val DotWidth = Radius/2f
  val EdgeWidth = Radius/2f
  val Alpha = 130
  val StrokeWeight = 0  
  
  private val StrokeColor = new MTColor(0, 0, 0, 0)
  private val BackgroundColor = new MTColor(0, 20, 80, 50)
  private val ForegroundColor = new MTColor(0, 0, 0, 150)
  
  def apply(app: Application, menu: NodeContextMenu, center: Vector3D, channelNumber: Int) = {
      new InputChannelItem(app, menu, center, channelNumber)
  }  
  
}


class InputChannelItem(app: Application, menu: NodeContextMenu, center: Vector3D, val channelNumber: Int) extends MenuItem(app, menu, center, InputChannelItem.Radius) {

  this.setBounds(new BoundsZPlaneRectangle(
    new MTRoundRectangle(app, center.getX - InputChannelItem.Radius, center.getY - InputChannelItem.Radius, 0, 2*InputChannelItem.Radius, 2*InputChannelItem.Radius, 5, 5)
  ))
  
  override def clicked() = {
    menu.node.associatedPath match {
      case Some(path) => {
        path ! ToggleInputChannelEvent(channelNumber)
      }
      case None => {
        menu.node match {
          case manipulableNode: ManipulableNode => manipulableNode ! ToggleInputChannelEvent(channelNumber)
          case otherNode => {}
        }
      }
    }   
  }
  
  override def up() = {}
  
  override def down() = {}  
  
  override def radius = {
    InputChannelItem.Radius
  }
  
  override def itemForegroundColor = {
    InputChannelItem.ForegroundColor
  }
  
  override def itemBackgroundColor = {
    InputChannelItem.BackgroundColor
  }
  
  override def itemStrokeColor = {
    InputChannelItem.StrokeColor
  }
  
  override def itemStrokeWeight = {
    InputChannelItem.StrokeWeight
  }
  
  override def drawComponent(g: PGraphicsOpenGL) = {
    val channelOn = menu.node.associatedPath match {
      case Some(path) => {
        path.isInputChannelActive(channelNumber)
      }
      case None => {
        menu.node match {
          case withChannels: MIDIInputChannels => {
            withChannels.isInputChannelActive(channelNumber)
          }         
          case somethingElse => false
        }
      }
    }     
    val saturationMultiplier = if (channelOn) 1.0f else 0.15f
    val saturationConstant = if (channelOn) 0 else 180
      
    val center = this.getCenterPointLocal()
    val cx = center.getX()
    val cy = center.getY()    
    g.fill(this.itemBackgroundColor.getR * saturationMultiplier + saturationConstant, this.itemBackgroundColor.getG * saturationMultiplier + saturationConstant, this.itemBackgroundColor.getB * saturationMultiplier + saturationConstant, this.opacity * InputChannelItem.Alpha)
    g.noStroke()
    g.rect(cx - this.radius, cy  - this.radius, 2*this.radius, 2*this.radius, InputChannelItem.EdgeWidth, InputChannelItem.EdgeWidth, InputChannelItem.EdgeWidth, InputChannelItem.EdgeWidth)
    
    if (channelNumber == 0) {
      g.ellipse(cx, cy, InputChannelItem.DotWidth, InputChannelItem.DotWidth)
    }
    else {
      (1 to channelNumber + 1).foreach(item => {
        val (x,y) = Functions.positionOnCircle(cx, cy, 0.6f * this.radius, 2*math.Pi.toFloat, item, channelNumber + 1)
        g.ellipse(x, y, InputChannelItem.DotWidth, InputChannelItem.DotWidth)
      })
    }

  }   

  
}
