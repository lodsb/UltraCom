package org.mt4j.util.animation

import org.mt4j.util.easing._

/**
 * Created by lodsb on 12/11/13.
 */
abstract class EasingFunction {
  protected def easeIn(t:Float,a:Float,b:Float,c:Float) : Float
  protected def easeOut(t:Float,a:Float,b:Float,c:Float) : Float
  protected def easeInOut(t:Float,a:Float,b:Float,c:Float) : Float


  def calcEasing(t:Float,a:Float,b:Float,c:Float) : Float

  private var lastT : Float = 0.0f;
  def apply(t: Float) : Float = {
    val delta = if(lastT > t) {
      // wrapAround
      t
    } else {
      t - lastT
    }

    lastT = t

    calcEasing(t*100, 0f, 1, 100f)
  }

  def map[T](f: Float => T) : Float => T = {x => f(apply(x))}

}

object LinearEasing {
  def apply() = new Linear
}

object QuadIn {
  def apply() = new Quad (AniConstants.IN)
}

object QuadOut {
  def apply() = new Quad (AniConstants.OUT)
}
 
object QuadInOut {
  def apply() = new Quad (AniConstants.IN_OUT)
}

object CubicIn {
  def apply() = new Cubic (AniConstants.IN)
}

object CubicOut {
  def apply() = new Cubic (AniConstants.OUT)
}

object CubicInOut {
  def apply() = new Cubic (AniConstants.IN_OUT)
}

object QuartIn {
  def apply() = new Quart (AniConstants.IN)
}

object QuartOut {
  def apply() = new Quart (AniConstants.OUT)
}

object QuartInOut {
  def apply() = new Quart (AniConstants.IN_OUT)
}

object QuintIn {
  def apply() = new Quint (AniConstants.IN)
}

object QuintOut {
  def apply() = new Quint (AniConstants.OUT)
}

object QuintInOut {
  def apply() = new Quint (AniConstants.IN_OUT)
}

object SineIn {
  def apply() = new Sine (AniConstants.IN)
}

object SineOut {
  def apply() = new Sine (AniConstants.OUT)
}

object SineInOut {
  def apply() = new Sine (AniConstants.IN_OUT)
}

object CircIn {
  def apply() = new Circ (AniConstants.IN)
}

object CircOut {
  def apply() = new Circ (AniConstants.OUT)
}

object CircInOut {
  def apply() = new Circ (AniConstants.IN_OUT)
}

object ExpoIn {
  def apply() = new Expo (AniConstants.IN)
}

object ExpoOut {
  def apply() = new Expo (AniConstants.OUT)
}

object ExpoInOut {
  def apply() = new Expo (AniConstants.IN_OUT)
}

object BackIn {
  def apply() = new Back (AniConstants.IN)
}

object BackOut {
  def apply() = new Back (AniConstants.OUT)
}

object BackInOut {
  def apply() = new Quart (AniConstants.IN_OUT)
}

object BounceIn {
  def apply() = new Bounce (AniConstants.IN)
}

object BounceOut {
  def apply() = new Bounce (AniConstants.OUT)
}

object BounceInOut {
  def apply() = new Bounce (AniConstants.IN_OUT)
}

object ElasticIn {
  def apply() = new Elastic (AniConstants.IN)
}

object ElasticOut {
  def apply() = new Elastic (AniConstants.OUT)
}

object ElasticInOut {
  def apply() = new Elastic (AniConstants.IN_OUT)
}