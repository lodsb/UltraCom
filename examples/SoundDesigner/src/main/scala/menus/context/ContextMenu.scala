package ui.menus.context

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


object ContextMenu {
  val Timeout = 5000 //number of milliseconds without interaction before the menu fades out 
  val FadeInTime = 750
  val FadeOutTime = 1500  
}

/**
* This class represents an abstract context menu.
* A context menu can be used to display an arbitrary number of selectable options, arranged in a circular fashion.
*/
abstract class ContextMenu(app: Application) extends MTComponent(app) with Actor {
  
  var inExistence = true
  this.setup()  
  
  private def setup() = {
    import ContextMenu._
    
    this.unregisterAllInputProcessors()
    this.removeAllGestureEventListeners()
    
    this.fadeInAnimation(0, FadeInTime).start() 
    this.start()
    this ! "CHECK_TIMEOUT"
  }
  
  
  def act() = {
    import ContextMenu._
    
    var lastTime = System.nanoTime()
    var currentTime = System.nanoTime()  
    var timeDiff = 0.0f
    var fadeIn = this.fadeInAnimation(0, FadeInTime)
    var fadeOut = this.fadeOutAnimation(255, FadeOutTime)
    var isFadingOut = false
    
    while(this.inExistence) {
      receive {
        case "CHECK_TIMEOUT" => {
          currentTime = System.nanoTime()
          timeDiff = (currentTime - lastTime)/1000000.0f //passed time in milliseconds
          if (timeDiff > Timeout) {
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
            fadeIn = this.fadeInAnimation(fadeOut.getValue(), FadeInTime)
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
        me.getChildren.collect({case item: MenuItem => item}).foreach(item => {
          item.setOpacity(ae.getValue/255f)
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
          me.remove()
        }
        else {
          me.getChildren.collect({case item: MenuItem => item}).foreach(item => {
            item.setOpacity(ae.getValue/255f)
          })
        }
      }
    })
    animation
  }
  
  
  protected[context] def remove() = {
    this.inExistence = false
    Ui -= this   
  }
  
  
}
