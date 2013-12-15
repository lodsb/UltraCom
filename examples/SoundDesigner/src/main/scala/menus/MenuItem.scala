package ui.menus

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

import processing.opengl.PGraphicsOpenGL

import scala.actors._

import ui.paths.types._
import ui._


abstract class MenuItem(app: Application, menu: Actor, center: Vector3D, radius: Float) extends MTEllipse(app, center, radius, radius) {

  protected var opacity = 0f
  
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
              down()
              menu ! "RESET_TIMER"
            }
            else if (tapEvent.getTapID == TapEvent.BUTTON_UP) {
              up()
              menu ! "RESET_TIMER"
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
  
  def up()
  
  def down()
  
  def radius: Float
  
  def itemForegroundColor: MTColor
  
  def itemBackgroundColor: MTColor
  
  def itemStrokeColor: MTColor
  
  def itemStrokeWeight: Float
   
  def setOpacity(newOpacity: Float) = {
    this.opacity = newOpacity
  }
  
}
  
