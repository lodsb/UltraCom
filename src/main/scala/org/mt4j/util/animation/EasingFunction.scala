package org.mt4j.util.animation

/**
 * Created by lodsb on 12/11/13.
 */
abstract class EasingFunction {
  protected def easeIn(t:Float,a:Float,b:Float,c:Float) : Float
  protected def easeOut(t:Float,a:Float,b:Float,c:Float) : Float
  protected def easeInOut(t:Float,a:Float,b:Float,c:Float) : Float

}
