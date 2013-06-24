package main.scala.TreeQuencer

import actors.Actor
import actors.Actor.State._
import org.mt4j.input.inputProcessors.componentProcessors.rotateProcessor.RotateEvent
import org.mt4j.input.inputProcessors.MTGestureEvent._
import org.mt4j.input.inputProcessors.MTGestureEvent
import org.mt4j.input.inputProcessors.componentProcessors.rotate3DProcessor.Rotate3DEvent

/**
 * This source code is licensed as GPLv3 if not stated otherwise.
 * NO responsibility taken for ANY harm, damage done to you, your data, animals, etc.
 * 
 * Last modified:  21.06.13 :: 22:44
 * Copyright (c) 2013: Gerhard Hagerer (Email: ghagerer@gmail.com)
 * 
 * Made in Bavaria by tons of eager fast pixies - since 1986.
 * 
 *  ^     ^
 *   ^   ^
 *   (o o)
 *  {  |  }                  (Wong)
 *     "
 * 
 * Don't eat the pills!
 */
 

object xRotator {
  def apply(form: NodeForm): AutoRotation = new AutoRotation(form, "x")
}
object yRotator {
  def apply(form: NodeForm): AutoRotation = new AutoRotation(form, "y")
}
object zRotator {
  def apply(form: NodeForm): AutoRotation = new AutoRotation(form, "z")
}

class AutoRotation(val form: NodeForm, val axis: String) extends Actor {

  var lastDegrees = 0f
  var timestamps = new Array[Long](2)
  timestamps(0) = System.currentTimeMillis()
  timestamps(1) = System.currentTimeMillis()
  var speed = 0d
  private var active = false
  val waitingTime = 15l

  def degrees(degrees: Float) {
    stop()
    lastDegrees = degrees
    timestamps(0) = timestamps(1)
    timestamps(1) = System.currentTimeMillis()
  }

  def run() {println("AutoRotation.run()")
    stop()
    getState match {
      case New => start()
      case Terminated => restart()
      case _ =>
    }
  }

  def act() {println("AutoRotation.act()")

    speed = lastDegrees/(timestamps(1)-timestamps(0)).toDouble/5d
    if (speed.abs == Double.PositiveInfinity) {
      speed = 0.5
    }

    active = true
    while (active) {println("AutoRotation.act(), while")
      val degrees = waitingTime * speed
      if (axis != "" && speed.abs > 0.01) {
        axis match {
          case "x" => form.rotateXGlobal(form.position, degrees.toFloat)
          case "y" => form.rotateYGlobal(form.position, degrees.toFloat)
          case "z" => form.rotateZGlobal(form.position, degrees.toFloat)
          case _ =>
        }
        println("AutoRotation.act(), speed="+speed)
        if (speed>0) {
          speed -= 0.01
        } else {
          speed += 0.01
        }
        Thread.sleep(waitingTime)
      } else {
        active = false
      }
    }
  }

  def stop() {
    while (getState == Runnable) {
      active = false
      Thread.sleep(waitingTime)
    }
  }

}
