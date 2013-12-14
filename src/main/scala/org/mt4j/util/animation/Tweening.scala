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

trait Tweening {
  val start : VarA[Boolean] = new VarA[Boolean](false)
  val stop : VarA[Boolean] = new VarA[Boolean](true)
  val restart : VarA[Boolean] = new VarA[Boolean](true)
  def name : String

  protected var successor : Option[Tweening] = None

  def before(successor: Tweening): Tweening = {
    this.synchronized {
      this.successor = Some(successor)
    }

    successor
  }

  def unchain: Unit = {
    this.synchronized {
      this.successor = None
    }
  }

  def destroy
}

trait GenericTweening[T] extends Tweening {

  def startValue: T
  val step: org.lodsb.reakt.TVar[T] = {
    new org.lodsb.reakt.async.VarA[T](startValue)
  }
}

// generic interpolation classes?
// playmodes? pendulum etc...

class Tween(tweenName: String, from: Float, to: Float, duration: Float,
            reps: Repetitions, accelerationEndTime: Float = 0.0f,
            decelerationStartTime: Float = 0.0f)
            extends GenericTweening[Float] with IAnimationListener {

  def startValue = from
  def name = tweenName

  private val interpolator = new MultiPurposeInterpolator(from, to, duration,
    accelerationEndTime, decelerationStartTime, reps.repetitions)

  val animation = new Animation(name, interpolator, this)
  animation.addAnimationListener(this)

  private var running = false;
  start.observe({x => if(x && ! running) animation.start; true})
  stop.observe({x => if(x && running){
    running = false
    println("STOPPED")
    animation.stop
  }; true})

  restart.observe({x => if(x) animation.restart(); true})

  private var doneRepetitions = 0;

  def processAnimationEvent(ae: AnimationEvent): Unit = {
    ae.getId match {
      case AnimationEvent.ANIMATION_STARTED => {
        doneRepetitions = 0;
        this.running = true;
      }
      case AnimationEvent.ANIMATION_ENDED =>
      if(reps != InfLoopRepetitions){
        doneRepetitions = doneRepetitions + 1

        if(reps.repetitions == doneRepetitions){
          this.stop() = true;

          this.synchronized {
            if(successor.isDefined) {
              successor.get.start() = true
            }
          }
        }
      }
      case _ =>
    }

    step.emit( ae.getValue)
  }
  def destroy: Unit = {
    if(!running) {
      start.disconnectAll
      stop.disconnectAll
      animation.removeAllAnimationListeners()
    }
  }
}

object Tween {
  def apply(duration: Float = 1.0f, reps: Repetitions = LoopRepetitions(1)) = {
    new Tween("unnamed tween", 0f, 1f, duration, reps)
  }

  // todo sequence

}
