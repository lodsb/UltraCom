package main.scala.TreeQuencer

import collection.mutable.ArrayBuffer
import actors.Actor
import collection.mutable
import org.lodsb.reakt.async.VarA
import org.lodsb.reakt.sync.VarS
import org.mt4j.util.SessionLogger
import org.mt4j.util.SessionLogger.SessionEvent

/**
 * This source code is licensed as GPLv3 if not stated otherwise.
 * NO responsibility taken for ANY harm, damage done to you, your data, animals, etc.
 * 
 * Last modified:  18.03.13 :: 19:12
 * Copyright (c) 2013: Gerhard Hagerer (Email: ghagerer@gmail.com)
 * 
 * Made in Bavaria by tons of eager fast pixies - since 1986.
 * 
 *  ^     ^
 *   ^   ^
 *   (o o)
 *  {  |  }
 *     "
 *  (Wong)
 * Don't eat the pills!
 */
 
object Metronome {
  def apply(): Metronome = if (app.game == app.TIMESHIFT_GAME) DistanceMetronome else NodeMetronome
}

trait Metronome extends Actor {
  protected var running: Boolean = false
  var beatsPerMinute = 60
  val duration = new VarS[Float](bpmToDuration)
  def bpmToDuration = {
    math.round(60f / beatsPerMinute * 1000) // in milliSecs
  }
  def durationToBPM = {
    math.round(60000f/duration())
  }
  def setBPM(bpm: Int) {
    beatsPerMinute = bpm
    duration() = bpmToDuration
  }
  def setDuration(dur: Float) {
    duration() = dur
    beatsPerMinute = durationToBPM
  }
  def removeNode(node: Node)
  def get: Metronome
  def stop() {
    running = false
  }
}


object NodeMetronome extends NodeSet[Node] with Metronome {

  var nodesToBeAnimated = new NodeSet[Node]()

  private def notifyNodes() {

    // the lines of these get animated via running dots later...
    nodesToBeAnimated = new NodeSet[Node]()

    copy.foreach( node => {
      if (node != null) {
        // play the sound of each registered node
        node.play()
        this -= node

        val nextNodes = new ArrayBuffer[Node]()

        if(!node.isEmpty){
          // if the node has children, register each one of them for the next round
          node.foreach(child => nextNodes += child.asInstanceOf[Node])

        } else if (app.game == app.RANDOM_GAME && !node.containsRunningSignal) {

          // if there is no beat signal within the tree of node anymore,
          // start a new beat signal from the firstNodeInTree
          nextNodes += node.firstNodeInTree.asInstanceOf[Node]

        }

        nextNodes.foreach( node => {
          // put the following nodes into the Metronome() for the next round/beat...
          this += node

          // animate the lines of the children
          if (node.lineToAncestor != null) {
            nodesToBeAnimated += node
          }
        })

      }
    })

    LineAnimator.startLineAnimations(nodesToBeAnimated.copy)
  }

  def act() {
    running = true

    // start Metronome()
    while (running) {
      // every some millisecs notify some nodes, that a beat took place
      notifyNodes()
      wait(duration().toInt)
    }
  }

  private def wait(time: Int) {
    try { Thread.sleep(time.toLong) }
    catch { case e: InterruptedException => }
  }

  def removeNode(node: Node) {
    this -= node
  }

  def get = this

  def apply = this

}
