package main.scala.TreeQuencer

import actors.Actor
import Actor.State._

/**
 * This source code is licensed as GPLv3 if not stated otherwise.
 * NO responsibility taken for ANY harm, damage done to you, your data, animals, etc.
 * 
 * Last modified:  23.05.13 :: 11:05
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
 
 
 object Stopwatch extends Actor {

  var startTime = 0l
  var currentTime = 0l
  var running = false

  def act() {
    println("!!!!!!!!!!! Stopwatch Starting")
    running = true
    startTime = System.currentTimeMillis()
    while (running) {
      print("")
      currentTime = System.currentTimeMillis() - startTime
    }
    println("!!!!!!!!!!! Stopwatch Stops!")
    printRunningTime
  }

  override def start(): Actor = {
    if (running) stop()
    Thread.sleep(10l)
    getState match {
      case Terminated => super.restart()
      case New => super.start()
      case _ =>
    }
    this
  }

  def stop() {
    println("!!!!!!!!!!! Stopwatch Stopping... (running="+running+")")
    running = false
  }

  def printRunningTime {
    println("!!!!!!!!!!! Stopwatch currentTime: "+currentTime)
  }

  def apply = this
}
