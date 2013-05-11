package TreeQuencer

import collection.mutable.ArrayBuffer
import actors.Actor
import collection.mutable

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
 
 
object NodeMetronome extends NodeSetAternative[Node] with Actor {
  var animationsAreRunning = false
  var beatsPerMinute = 60


  def bpmInMillisecs = {
    math.round(60f / beatsPerMinute * 1000)
  }

  var nodesToBeAnimated = ArrayBuffer[Node]()
  def notifyNodes() {

    // the lines of these get animated via running dots later...
    nodesToBeAnimated = ArrayBuffer[Node]()

    copy().foreach( node => {
      if (node != null) {

        // play the sound of each registered node
        node.play()
        this -= node

        val nextNodes = new ArrayBuffer[Node]()

        if(!node.isEmpty){
          // if the node has children, register each one of them for the next round
          node.map(child => nextNodes += child.asInstanceOf[Node])

        } else if (!node.containsRunningSignal) {

          // if there is no beat signal within the tree of node anymore,
          // start a new beat signal from the firstNodeInTree
          nextNodes += node.firstNodeInTree.asInstanceOf[Node]
        }

        nextNodes.foreach(node => {
          // put the following nodes into the Metronome() for the next round/beat...
          this += node

          // animate the lines of the children
          if (node.lineToAncestor != null) {
            nodesToBeAnimated += node
          }
        })

      }
    })

    LineAnimator().reset()
  }

  def act() {
    animationsAreRunning = true

    // add all children of SourceNode() to Metronome
    SourceNode().foreach( node => {
      this += node.asInstanceOf[Node]
    })

    // start Metronome()
    while (animationsAreRunning) {
      // every some millisecs notify some nodes, that a beat took place
      notifyNodes()
      wait(bpmInMillisecs)
    }
  }

  def wait(time: Int) {
    try { Thread.sleep(time.toLong) }
    catch { case interrupted: InterruptedException => {} }
  }

  def stop() {
    animationsAreRunning = false
  }

  def apply() = this

}
