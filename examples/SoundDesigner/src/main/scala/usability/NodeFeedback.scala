package ui.usability

import org.mt4j.util.MTColor
import org.mt4j.util.Color
import org.mt4j.util.math.Vector3D
import org.mt4j.util.math.Vertex
import org.mt4j.types.{Vec3d}

import org.mt4j.util.animation.Animation
import org.mt4j.util.animation.AnimationEvent
import org.mt4j.util.animation.IAnimationListener
import org.mt4j.util.animation.MultiPurposeInterpolator

import ui.paths.types._
import ui.events._
import ui._


/**
* This trait realizes visual feedback for nodes.
*/
trait NodeFeedback extends Feedback {

  private var timer: Option[Animation] = None
  
  def setColor(col: MTColor)
  
  def setClock(value: Int)

  def color: MTColor
	
	override def giveFeedback(event: FeedbackEvent) = {
	  if (event.name == "START_TIMER") {
	    this.timer = Some(this.timerAnimation(event.value))
	    this.timer.foreach(_.start())
	    println("timer started")
	  }
	  else if (event.name == "STOP_TIMER") {
	    this.timer.foreach(_.stop())
	    this.setClock(0)
	    println("timer stopped") 
	  }
	  else {
	    this.wrongActionAnimation.start()
	  }
	}	
	
	private def timerAnimation(time: Float) = {
	  val me = this
	  val timerDelay = 500
	  
	  val interpolator = new MultiPurposeInterpolator(0.0f, 1.0f, timerDelay, 0.0f, 1.0f, 1) //(from, to, duration, accelerationEndTime, decelerationStartTime, loopCount)
	  val animation = new Animation("TIMER_DELAY", interpolator, this)
	  animation.addAnimationListener(new IAnimationListener() { 
		def processAnimationEvent(ae: AnimationEvent) {
        if(ae.getId() == AnimationEvent.ANIMATION_ENDED) { //if the animation has been played back uninterrupted
            val interpolator2 = new MultiPurposeInterpolator(0.0f, 1.0f, time - timerDelay, 0.0f, 1.0f, 1) //(from, to, duration, accelerationEndTime, decelerationStartTime, loopCount)
            val animation2 = new Animation("TIMER", interpolator2, this)
            animation2.addAnimationListener(new IAnimationListener() { 
            def processAnimationEvent(ae: AnimationEvent) {
              if(ae.getId() == AnimationEvent.ANIMATION_ENDED) { //if the animation has been played back uninterrupted
                println("animation ended")
                me.setClock(0)
              }
              else {
                me.setClock((ae.getValue * 360).toInt)
              }
            }
            })
            me.timer = Some(animation2)
            me.timer.foreach(_.start())
        }
		}
	  })	  
	  animation	  
	}
	
	
	private def wrongActionAnimation = {
	  val color = this.color
    val oldR = color.getR
    val oldG = color.getG
    val oldB = color.getB
    val oldAlpha = color.getA
    val min = 0.2f
	  val rDiff = 255 - oldR
	  val gDiff = 255*min - oldG
	  val bDiff = 255*min - oldB
	  val me = this
	  val interpolator = new MultiPurposeInterpolator(0.0f, 1.0f, 150, 0.0f, 1.0f, 1) //(from, to, duration, accelerationEndTime, decelerationStartTime, loopCount)
	  val animation = new Animation("WRONG ACTION FADE IN", interpolator, this)
	  animation.addAnimationListener(new IAnimationListener() { 
		def processAnimationEvent(ae: AnimationEvent) {
		  if(ae.getId() == AnimationEvent.ANIMATION_ENDED) { //if the animation has been played back uninterrupted
			  //then start animation back to default appearance of node
			  val interpolator2 = new MultiPurposeInterpolator(0.0f, 1.0f, 500, 0.0f, 1.0f, 1)
			  val animation2 = new Animation("WRONG ACTION FADE OUT", interpolator2, me)
			  animation2.addAnimationListener(new IAnimationListener() {
			    def processAnimationEvent(ae: AnimationEvent) {
			      me.setColor(new MTColor(oldR + rDiff*(1-ae.getValue), oldG + gDiff*(1-ae.getValue), oldB + bDiff*(1-ae.getValue), oldAlpha))
			    }
			  }).start()
		  }
		  else {
		    //(oldR, oldG, oldB) -> (255, 255*min, 255*min) 
		    me.setColor(new MTColor(oldR + rDiff*ae.getValue, oldG + gDiff*ae.getValue, oldB + bDiff*ae.getValue, oldAlpha))
		  }
		}
	  })
	  animation
	}    
	

}
