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

import ui.paths.types._
import ui._

object Button {
  
  val Radius = Ui.width/100
  val StrokeColor = Color(0, 0, 0, 0)
  val StrokeWeight = 1
  
}


abstract class Button(app: Application, menu: Menu, center: Vector3D) extends MTEllipse(app, center, Button.Radius, Button.Radius) {

  protected var alphaValue = 0.0f
  
  this.setup()
  
  def setup() = {  
    this.unregisterAllInputProcessors()
    this.removeAllGestureEventListeners()
    
    //register input processors
    this.registerInputProcessor(new TapProcessor(app))
    
    this.addGestureListener(classOf[TapProcessor], new IGestureEventListener() {
    	override def processGestureEvent(gestureEvent: MTGestureEvent): Boolean = {
        gestureEvent match {
          case tapEvent: TapEvent => {
            if (tapEvent.getTapID == TapEvent.BUTTON_DOWN) {
            }
            else if (tapEvent.getTapID == TapEvent.BUTTON_UP) {
            }
            else if (tapEvent.getTapID == TapEvent.BUTTON_CLICKED) {
              clicked()
              menu ! "RESET_TIMER"
            }
            true
          }
          case someEvent => {
            println("I can't process this particular event: " + someEvent.toString)
            false
          }
        }
			}
	  })
    
  }
  
  def clicked()
  
  def setAlpha(alpha: Float) = {
    this.alphaValue = alpha
  }
  
  override def drawComponent(g: PGraphics) = {
    val center = this.getCenterPointLocal()
    val cx = center.getX()
    val cy = center.getY()    
    g.noFill()
    g.stroke(Button.StrokeColor.getR, Button.StrokeColor.getG, Button.StrokeColor.getB, this.alphaValue)
    g.strokeWeight(Button.StrokeWeight)
    g.ellipse(cx, cy, 2*Button.Radius, 2*Button.Radius)  
  }  

}
  
