package ui

import org.mt4j.{Scene, Application}
import org.mt4j.components.visibleComponents.shapes.MTRectangle

import org.mt4j.util.MTColor
import org.mt4j.util.math.Vector3D
import org.mt4j.util.math.Vertex

import processing.core.PGraphics

import ui.menus.main._

object TimbreSpace {
  
  val BorderWeight = 1.0f
  val BorderColor = new MTColor(0,0,0,150)
  
  def apply(app: Application) = {
    new TimbreSpace(app)
  }
  
}

/**
* This class visually represents the timbre space on which this musical application is based.
* Users may explore the space auditorily by dragging nodes around or construct paths through the space, which correspond to sounds changing over time.
*/
class TimbreSpace(app: Application) extends MTRectangle(app, Menu.Space, Menu.Space, app.width - 2 * Menu.Space, app.height - 2 * Menu.Space) {
  this.setupRepresentation()
  this.setupInteraction()
 
  private def setupRepresentation() = {
    this.setStrokeWeight(TimbreSpace.BorderWeight)
    this.setStrokeColor(TimbreSpace.BorderColor)
  }
  
  private def setupInteraction() = {
    this.unregisterAllInputProcessors() //no default rotate, scale & drag processors
    this.removeAllGestureEventListeners() //no default listeners as well
    this.setPickable(false) //we don't want to get this component if we pick
  }
  
  override def drawComponent(g: PGraphics) = {
    super.drawComponent(g)
  }
  
  
}
