package org.mt4j.util.animation

import org.lodsb.reakt.TVal
import de.looksgood.ani.Ani
import org.mt4j.util.animation._
import org.lodsb.reakt.async.VarA

/**
 * Created by lodsb on 12/11/13.
 */
// should be used like
// tween.map.EasingFunction.map.Interpolator(SomeType)
// wrappertype: transition

abstract class Repetitions(val repetitions: Int)
case class LoopRepetitions(override val repetitions: Int) extends Repetitions(repetitions)
case object InfLoopRepetitions extends Repetitions(-1)

abstract class TweenEvent
case class TweenStart(name: String) extends TweenEvent
case class TweenStop(name: String)  extends TweenEvent
case class TweenUpdate(name: String) extends TweenEvent

trait Tweening[T] {
  val start : VarA[Boolean] = new VarA[Boolean](false)
  val stop : VarA[Boolean] = new VarA[Boolean](true)

  def name : String

  def startValue: T

  val step: org.lodsb.reakt.TVar[(TweenEvent, T)] = {
    new org.lodsb.reakt.async.VarA[(TweenEvent,T)]((TweenStop(name),startValue))
  }
}

// generic interpolation classes?

class Tween(tweenName: String, from: Float, to: Float, duration: Float,
            reps: Repetitions, accelerationEndTime: Float = 0.1f,
            decelerationStartTime: Float = 0.1f)
            extends Tweening[Float] with IAnimationListener {

  def startValue = from
  def name = tweenName

  private val interpolator = new MultiPurposeInterpolator(from, to, duration,
    accelerationEndTime, decelerationStartTime, reps.repetitions)

  val animation = new Animation(name, interpolator, this)
  animation.addAnimationListener(this)

  start.observe({x => println(x); if(x) animation.start; true})
  stop.observe({x => if(x) animation.stop; true})

  def processAnimationEvent(ae: AnimationEvent): Unit = {
    val te =ae.getId match {
      case AnimationEvent.ANIMATION_STARTED => TweenStart(name)
      case AnimationEvent.ANIMATION_UPDATED => TweenUpdate(name)
      case AnimationEvent.ANIMATION_ENDED => TweenStop(name)
    }

    println(ae.getValue)
    step.emit((te, ae.getValue))
  }

  // playmodes? pendulum etc...

}

object Tween {
  def apply(duration: Float = 1.0f, reps: Repetitions = LoopRepetitions(1)) = {
    new Tween("unnamed tween", 0f, 1f, duration, reps)
  }

  // todo sequence

}
