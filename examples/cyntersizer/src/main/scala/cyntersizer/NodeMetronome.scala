package cyntersizer

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
 
 
class NodeMetronome extends Actor with mutable.Set[Node]{
  var running = false
  var beatsPerMinute: Int = 60

  // ---------- Set part START ---------- //
  private val registeredNodes = new ArrayBuffer[Node]()

  override def +=(node: Node): NodeMetronome.this.type = {
    // only do something, if node isn't already within the Set
    if (!this.contains(node)) {
      // add node to children from this
      for (i <- 0 to registeredNodes.size - 1) {
        if (registeredNodes(i) == null) {
          registeredNodes(i) = node
        }
      }
    }
    if (!this.contains(node)) {
      registeredNodes += node
    }

    this
  }

  override def -=(node: Node): NodeMetronome.this.type = {
    // nothing special, just an ordinary removal from the Set
    for (registeredNode <- registeredNodes) {
      if(registeredNode eq node) {
        registeredNodes -= registeredNode
      }
    }
    this
  }

  override def contains(node: Node): Boolean = {
    for (registeredNode <- registeredNodes) {
      if(registeredNode eq node) {
        return true
      }
    }
    false
  }

  override def iterator: Iterator[Node] = {
    registeredNodes.iterator
  }
  // ---------- Set part END ---------- //


  def bpmInMillisecs = {
    60f / beatsPerMinute * 1000
  }

  var nodesToBeAnimated = ArrayBuffer[Node]()
  def notifyNodes() {

    // the lines of these get animated via running dots later...
    nodesToBeAnimated = ArrayBuffer[Node]()

    registeredNodes.clone().map(node => {
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
    LineAnimator().start(nodesToBeAnimated)
  }

  def act() {
    running = true

    // add all children of SourceNode() to Metronome
    SourceNode().map(node => this += node.asInstanceOf[Node])

    // start Metronome()
    while (running) {
      // every some millisecs notify some nodes, that a beat took place
      notifyNodes()
      try { Thread.sleep(bpmInMillisecs.toLong) }
      catch { case interrupted: InterruptedException => {} }
    }
  }

  def stop() {
    running = false
  }

}

object Metronome {
  private val metronome = new NodeMetronome()
  def apply() = metronome
}