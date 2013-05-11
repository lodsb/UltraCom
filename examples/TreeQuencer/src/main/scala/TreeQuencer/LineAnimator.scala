package TreeQuencer

import actors.Actor
import collection.mutable.ArrayBuffer

/**
 * This source code is licensed as GPLv3 if not stated otherwise.
 * NO responsibility taken for ANY harm, damage done to you, your data, animals, etc.
 * 
 * Last modified:  16.04.13 :: 12:25
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

/**
 * This object represents a thread, that is animating all the lines.
 * It's for the running dots.
 */
 object LineAnimator extends Actor {

  var nodesToBeAnimated = ArrayBuffer[Node]()

  var startTime = 0l
  def duration = NodeMetronome().bpmInMillisecs.toFloat
  def expiredTime = System.currentTimeMillis() - startTime
  def expiredTimeFactor = expiredTime/duration

  /**
   * Calculates the new position for the moving circle of the line to node
   * @param node The node to which the line goes to
   * @return Vector3D The new position
   */
  def circlePosition(node :DragableNode) = {
    val connectionLine = node.position.getSubtracted(node.ancestor.position)
    node.ancestor.position.getAdded(connectionLine.getScaled(LineAnimator().expiredTimeFactor))
  }

  /**
   * One single thread for the dot animation on the lines.
   * Saves a lot of CPU power....
   */
  def act() {

    while (expiredTime < duration) {
      moveCirclesForward()
      Thread.sleep(13l)
    }

  }

  /**
   * Resets the nodes, which have to be animated.
   * Starts the thread also if not started or terminated
   */
  def reset() {
    makeCirclesVisible(setVisible = false)
    nodesToBeAnimated = NodeMetronome().nodesToBeAnimated
    startTime = System.currentTimeMillis()
    makeCirclesVisible(setVisible = true)
    getState match {
      case Actor.State.Terminated => restart()
      case Actor.State.New => start()
      case _ =>
    }
  }

  def moveCirclesForward() {
    // move all the dots one step forward
    nodesToBeAnimated.foreach(node => {
      node.lineToAncestor.movingCircle.setPositionGlobal(circlePosition(node))
    })
  }
  def makeCirclesVisible(setVisible: Boolean) {
    // make all the dots on the lines visible...
    nodesToBeAnimated.foreach(node => {
      node.lineToAncestor.movingCircle.setVisible(setVisible)
    })
  }

  def framesPerSecond(frames: Long): Long = {
    1000l/frames
  }

  def apply() = this
}
