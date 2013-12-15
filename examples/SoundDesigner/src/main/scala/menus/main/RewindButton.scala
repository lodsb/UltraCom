package ui.menus.main

import org.mt4j.Application

import org.mt4j.components.TransformSpace
import org.mt4j.components.MTComponent
import org.mt4j.components.visibleComponents.shapes.MTEllipse

import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapProcessor 
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent
import org.mt4j.input.inputProcessors.componentProcessors.tapAndHoldProcessor.TapAndHoldProcessor
import org.mt4j.input.inputProcessors.componentProcessors.tapAndHoldProcessor.TapAndHoldEvent
import org.mt4j.input.inputProcessors.IGestureEventListener
import org.mt4j.input.inputProcessors.MTGestureEvent

import org.mt4j.util.Color
import org.mt4j.util.math.Vector3D
import org.mt4j.util.math.Vertex
import org.mt4j.types.{Vec3d}

import processing.opengl.PGraphicsOpenGL

import ui.menus._
import ui.paths.types._
import ui.events._
import ui._

object RewindButton {

  def apply(app: Application, menu: Menu, center: Vector3D) = {
      new RewindButton(app, menu, center)
  }  
 
  
}


class RewindButton(app: Application, menu: Menu, center: Vector3D) extends Button(app, menu, center) {
  
  override def drawComponent(g: PGraphicsOpenGL) = {
    super.drawComponent(g)
    val center = this.getCenterPointLocal()
    val r = 0.4f * this.radius
    val a = (3 * r/math.sqrt(3)).toFloat //side length of triangle
    val h = (math.sqrt(3)/2 * a).toFloat //height of triangle
    val segment = (math.cos(math.toRadians(60)) * r).toFloat //trigonometric function to calculate the length of one of the segments induced by the orthocenter of the triangle; here, the greater of the two segment lengths is obtained       
    
    val (c1x, c1y) = (center.getX() - h/2, center.getY)
    val (c2x, c2y) = (center.getX() + h/2, center.getY)
    
    //defining an equilateral triangle circumscribed by a circle of radius r centered around (c1x, c1y)
    val (p1x, p1y) = (c1x + h - 2*segment, c1y - a/2)
    val (p2x, p2y) = (c1x + h - 2*segment, c1y + a/2)
    val (p3x, p3y) = (c1x - 2*segment, c1y)   
    
    //defining an equilateral triangle circumscribed by a circle of radius r centered around (c2x, c2y)
    val (p4x, p4y) = (c2x + h - 2*segment, c2y - a/2)
    val (p5x, p5y) = (c2x + h - 2*segment, c2y + a/2)
    val (p6x, p6y) = (c2x - 2*segment, c2y)     

    g.noStroke()
    g.fill(this.itemForegroundColor.getR, this.itemForegroundColor.getG, this.itemForegroundColor.getB, this.itemForegroundColor.getA * this.opacity)
    g.triangle( p1x, p1y,
                p2x, p2y,
                p3x, p3y)
    g.triangle( p4x, p4y,
                p5x, p5y,
                p6x, p6y)
  }   
  
  override def clicked() = {
    super.clicked()
  }
  
  override def up() = {
    super.up()
  }
  
  override def down() = {
    super.down()
    Ui.paths.foreach(_ ! PathRewindEvent(100)) 
  }  
  
  
  override def setup() = {  
    this.unregisterAllInputProcessors()
    this.removeAllGestureEventListeners()
    
    //register input processors
    val tapAndHoldProcessor = new TapAndHoldProcessor(app, Int.MaxValue) //choosing Int.MaxValue so GESTURE_UPDATED events are sent for a long time to come (approximately 600h, which should be sufficient ;))
    tapAndHoldProcessor.setMaxFingerUpDist(this.radius) 
    this.registerInputProcessor(tapAndHoldProcessor)
    
    this.addGestureListener(classOf[TapAndHoldProcessor], new IGestureEventListener() {
    	override def processGestureEvent(gestureEvent: MTGestureEvent): Boolean = {
    	  gestureEvent match {
          case tahEvent: TapAndHoldEvent => {
              if (tahEvent.getId == MTGestureEvent.GESTURE_DETECTED) {
                println("tap and hold detected")
                down()
                menu ! "RESET_TIMER"
              }
              else if (tahEvent.getId == MTGestureEvent.GESTURE_UPDATED) {
                println("tap and hold updated")
                down()
                menu ! "RESET_TIMER"
              }
              else if (tahEvent.getId == MTGestureEvent.GESTURE_CANCELED) {
                println("tap and hold canceled")
                up()                
              }
              else if (tahEvent.getId == MTGestureEvent.GESTURE_ENDED) {
                println("tap and hold ended")
                up()                
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
  
}
