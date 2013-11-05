package ui.menus.context

import org.mt4j.Application

import org.mt4j.util.MTColor
import org.mt4j.util.math.Vector3D
import org.mt4j.util.math.Vertex
import org.mt4j.types.{Vec3d}

import processing.core.PGraphics

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
  val Alpha = 50
  val StrokeWeight = 0  
  
  private val StrokeColor = new MTColor(0, 0, 0, 0)
  private val BackgroundColor = new MTColor(0, 0, 0, 50)
  private val ForegroundColor = new MTColor(0, 0, 0, 150)
  
  def apply(app: Application, menu: NodeContextMenu, center: Vector3D, channelNumber: Int) = {
      new InputChannelItem(app, menu, center, channelNumber)
  }  
  
}


class InputChannelItem(app: Application, menu: NodeContextMenu, center: Vector3D, val channelNumber: Int) extends MenuItem(app, menu, center, InputChannelItem.Radius) {

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
  
  override def drawComponent(g: PGraphics) = {
    val channelOn = menu.node.associatedPath match {
      case Some(path) => {
        path.isInputChannelOpen(channelNumber)
      }
      case None => {
        menu.node match {
          case withChannels: MIDIInputChannels => {
            withChannels.isInputChannelOpen(channelNumber)
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
    g.ellipse(cx, cy, 2*this.radius, 2*this.radius)  
    
    (1 to channelNumber + 1).foreach(item => {
      val (x,y) = this.positionOnCircle(cx, cy, 0.6f * this.radius, 2*math.Pi.toFloat, item, channelNumber + 1)
      g.ellipse(x, y, 4, 4)
    })
    
    
  }    

  /**
  * Returns for a given item (starting with 0) out of a fixed number of items the position on an arc segment with specified center, radius and length (in radians),
  * with the premise that the items are equidistantly distributed on the arc segment.
  * Use (2 * math.Pi) if you are aiming for a whole circle.
  */
  protected def positionOnCircle(x:Float, y: Float, radius: Float, arc: Float, item: Int, items: Int) = {
    Functions.circle((x,y), radius)(math.Pi.toFloat/2 + arc * ((2*item+1).toFloat/(2*items)))
  }

  
}
