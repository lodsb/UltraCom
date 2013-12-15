package ui.menus.context

import org.mt4j.Application


import org.mt4j.util.MTColor
import org.mt4j.util.math.Vector3D
import org.mt4j.util.math.Vertex
import org.mt4j.types.{Vec3d}

import processing.opengl.PGraphicsOpenGL

import scala.actors._

import ui.audio._
import ui.menus._
import ui.paths._
import ui.paths.types._
import ui.events._
import ui._

object OutputChannelItem {

  val Radius = Ui.width/150
  val Alpha = 180
  val StrokeWeight = 0  
  
  private val StrokeColor = new MTColor(0, 0, 0, 0)
  private val ForegroundColor = new MTColor(0, 0, 0, 0)
  
  def apply(app: Application, menu: NodeContextMenu, center: Vector3D, channelNumber: Int) = {
      new OutputChannelItem(app, menu, center, channelNumber)
  }  
  
}


class OutputChannelItem(app: Application, menu: NodeContextMenu, center: Vector3D, val channelNumber: Int) extends MenuItem(app, menu, center, OutputChannelItem.Radius) {

  override def clicked() = {
    menu.node.associatedPath match {
      case Some(path) => {
        path ! ToggleOutputChannelEvent(channelNumber)
      }
      case None => {
        menu.node match {
          case manipulableNode: ManipulableNode => manipulableNode ! ToggleOutputChannelEvent(channelNumber)
          //case isolatedNode: IsolatedNode => isolatedNode.toggleChannel(channelNumber)
          case otherNode => {}
        }
      }
    }   
  }
  
  override def up() = {}
  
  override def down() = {}  
  
  override def radius = {
    OutputChannelItem.Radius
  }
  
  override def itemForegroundColor = {
    OutputChannelItem.ForegroundColor
  }
  
  override def itemBackgroundColor = {
    AudioOutputChannels.colorFromIndex(channelNumber)
  }
  
  override def itemStrokeColor = {
    OutputChannelItem.StrokeColor
  }
  
  override def itemStrokeWeight = {
    OutputChannelItem.StrokeWeight
  }
  
  override def drawComponent(g: PGraphicsOpenGL) = {
    val channelOn = menu.node.associatedPath match {
      case Some(path) => {
        path.isOutputChannelOpen(channelNumber)
      }
      case None => {
        menu.node match {
          case withChannels: AudioOutputChannels => {
            withChannels.isOutputChannelOpen(channelNumber)
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
    g.fill(this.itemBackgroundColor.getR * saturationMultiplier + saturationConstant, this.itemBackgroundColor.getG * saturationMultiplier + saturationConstant, this.itemBackgroundColor.getB * saturationMultiplier + saturationConstant, this.opacity * OutputChannelItem.Alpha)
    g.noStroke()
    g.ellipse(cx, cy, 2*this.radius, 2*this.radius)  
  }    
  
}
