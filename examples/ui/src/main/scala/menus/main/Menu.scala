package ui.menus.main

import org.mt4j.Application
import org.mt4j.components.MTComponent
import org.mt4j.components.visibleComponents.shapes.MTRectangle
import org.mt4j.components.TransformSpace

import org.mt4j.util.math.Vector3D
import org.mt4j.util.math.Vertex
import org.mt4j.types.{Vec3d}

import org.mt4j.util.MTColor

import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapProcessor 
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent
import org.mt4j.input.inputProcessors.IGestureEventListener
import org.mt4j.input.inputProcessors.MTGestureEvent

import org.mt4j.util.animation.Animation
import org.mt4j.util.animation.AnimationEvent
import org.mt4j.util.animation.IAnimationListener
import org.mt4j.util.animation.MultiPurposeInterpolator

import scala.actors._

import ui.menus._
import ui._
import ui.util._


/**
* This object manages all menues currently visible to the users.
*/
object Menu {
  val Space = Ui.width/30.0f //width of area around the timbre space which is reserved for menu interaction
  val Padding = Space/6.0f
  val Width = Ui.width/5.0f
  val Height = Space
  val StrokeColor = new MTColor(200, 200, 200, 0)
  val Timeout = 5000 //number of milliseoncds without interaction before the menu fades out 
  val FadeInTime = 1000
  val FadeOutTime = 1500

  private var registry = Set[Menu]()
  
  def +=(menu: Menu) = {
    this.registry += menu
  }
  
  def -=(menu: Menu) = {
    this.registry -= menu
  }
  
  def apply(app: Application, center: Vector3D, rotationAngle: Float) = {
    new Menu(app, center, rotationAngle)
  }
  
  def isMenuInProximity(center: Vector3D, rotationAngle: Float) = {
    this.registry.filter(_.rotationAngle == rotationAngle).exists(menu => Vector.euclideanDistance((menu.center.getX, menu.center.getY), (center.getX, center.getY)) < Width)
  }
  
}

/**
* This class represents the application's main menu.
* It provides means to create, load, save and play back sounds,
* and fades in and out smoothly based on user interaction (or the lack thereof).
*/
class Menu(app: Application, val center: Vector3D, val rotationAngle: Float) extends MTRectangle(app, center.getX - Menu.Width/2, center.getY - Menu.Height/2, Menu.Width, Menu.Height) with Actor {
  
  this.setup()  
  
  private def setup() = {
    import Menu._
    
    this.unregisterAllInputProcessors()
    this.removeAllGestureEventListeners()
    
    val me = this
    this.registerInputProcessor(new TapProcessor(app))         
    this.addGestureListener(classOf[TapProcessor], new IGestureEventListener() {
        override def processGestureEvent(gestureEvent: MTGestureEvent): Boolean = {
              gestureEvent match {
                  case tapEvent: TapEvent => {
                      if (tapEvent.getTapID == TapEvent.BUTTON_DOWN) {
                        me ! "RESET_TIMER"
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
    
    val (cx, cy) = (center.getX, center.getY)    
    this.setNoFill(true)
    this.setNoStroke(true)
    this.addChild(FastForwardButton(app, this, Vec3d(cx + 6 * Button.Radius + 3*Padding, cy)))
    this.addChild(PlayButton(app, this, Vec3d(cx + 4 * Button.Radius + 2*Padding, cy)))
    this.addChild(StopButton(app, this, Vec3d(cx + 2 * Button.Radius + Padding, cy)))
    this.addChild(RewindButton(app, this, Vec3d(cx, cy)))
    this.addChild(SaveProjectButton(app, this, Vec3d(cx - 2 * Button.Radius - Padding, cy)))
    this.addChild(LoadProjectButton(app, this, Vec3d(cx - 4 * Button.Radius - 2*Padding, cy)))
    this.addChild(NewProjectButton(app, this, Vec3d(cx - 6 * Button.Radius - 3*Padding, cy)))
    this.rotateZ(Vec3d(center.getX, center.getY), rotationAngle, TransformSpace.GLOBAL) //then apply new rotation
    
    this.fadeInAnimation(0, FadeInTime).start() 
    this.start()
    this ! "CHECK_TIMEOUT"
  }
  
  
  def act() = {
    
    var lastTime = System.nanoTime()
    var currentTime = System.nanoTime()  
    var timeDiff = 0.0f
    var fadeIn = this.fadeInAnimation(0, Menu.FadeInTime)
    var fadeOut = this.fadeOutAnimation(255, Menu.FadeOutTime)
    var isFadingOut = false
    
    while (true) {
      receive {
        case "CHECK_TIMEOUT" => {
          currentTime = System.nanoTime()
          timeDiff = (currentTime - lastTime)/1000000.0f //passed time in milliseconds
          if (timeDiff > Menu.Timeout) {
            fadeOut.restart()
            isFadingOut = true
          }
          else {
            this ! "CHECK_TIMEOUT"
            Thread.sleep(10) 
          }
        }
        case "RESET_TIMER" => {
          lastTime = System.nanoTime()
          if (isFadingOut) {
            isFadingOut = false
            fadeOut.stop()
            fadeIn = this.fadeInAnimation(fadeOut.getValue(), Menu.FadeInTime)
            fadeIn.start()
            this ! "CHECK_TIMEOUT"
          }
        }
      }
    }
  }
  
  
  private def fadeInAnimation(startValue: Float, time: Float) = {
    val me = this
    val fadeInInterpolator = new MultiPurposeInterpolator(startValue, 255, time, 1.0f, 1.0f, 1) //accelerated fadein in 1,5 secs
    val animation = new Animation("FADE_IN", fadeInInterpolator, this)    
    animation.addAnimationListener(new IAnimationListener() { 
      def processAnimationEvent(ae: AnimationEvent) {
        val componentFillColor = me.getFillColor()
        val componentStrokeColor = me.getStrokeColor()
        componentFillColor.setAlpha(ae.getValue)
        componentStrokeColor.setAlpha(ae.getValue)
      
        me.setStrokeColor(componentStrokeColor)
        me.setFillColor(componentFillColor)
        
        me.getChildren.collect({case item: MenuItem => item}).foreach(item => {
          item.setAlpha(ae.getValue)
        })
      }
    })
    animation
  }
  
  
  
  private def fadeOutAnimation(startValue: Float, time: Float) = {
    val me = this
    val fadeOutInterpolator = new MultiPurposeInterpolator(startValue, 0, time, 0.0f, 0.0f, 1) //decelerated fadeout in time milliseconds
    val animation = new Animation("FADE_OUT", fadeOutInterpolator, this)
    animation.addAnimationListener(new IAnimationListener() { 
      def processAnimationEvent(ae: AnimationEvent) {
        if(ae.getId() == AnimationEvent.ANIMATION_ENDED){ //if the animation has been played back uninterrupted
          Menu -= me
          Ui -= me
          println("ended")
        }
        else {
          val componentFillColor = me.getFillColor()
          val componentStrokeColor = me.getStrokeColor()
          componentFillColor.setAlpha(ae.getValue)
          componentStrokeColor.setAlpha(ae.getValue)
        
          me.setStrokeColor(componentStrokeColor)
          me.setFillColor(componentFillColor)
          
          me.getChildren.collect({case item: MenuItem => item}).foreach(item => {
            item.setAlpha(ae.getValue)
          })
        }
      }
    })
    animation
  }
  
  
}
