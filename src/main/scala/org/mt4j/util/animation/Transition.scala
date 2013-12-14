package org.mt4j.util.animation

import org.lodsb.reakt.property.Property
import org.mt4j.util.math.Vector3D

/**
 * Created by lodsb on 12/11/13.
 * a transition is a one-shot tween, after having finished the signals are being disconnected
 *
 */


abstract class Transition[T](transitionName: String, property: Property[T], from:T, to: T, protected val tween: Tween, easing: EasingFunction)
  extends Tweening {

  property <~ this.tween.step.map(x => easing.map(y => this.interpolation(y, from, to))(x))

  tween.stop <~ this.stop
  tween.start<~ this.start

  tween.stop.observe({ x =>
    if(x) {
      this.destroy

      this.synchronized {
        if(successor.isDefined) {
          successor.get.start() = true
        }
      }
    }

    true
  })

  def name: String = transitionName

  def destroy: Unit = {
    tween.destroy
  }

  def interpolation(v: Float, from: T, to: T) : T
}

// todo: transitions for several items
case class Vector3DTransition(property: Property[Vector3D],
                              from: Vector3D, to: Vector3D,
                              duration: Float,
                              easing: EasingFunction = QuartInOut())
  extends Transition[Vector3D]("PositionTransition", property, from, to, Tween(duration, LoopRepetitions(1)), easing) {

  def interpolation(v: Float, from: Vector3D, to: Vector3D): Vector3D = Interpolation(v, from, to)
}

// color transition
// rotation transition
// vertex transition
