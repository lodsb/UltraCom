package cyntersizer

import actors.Actor
import collection.mutable.ArrayBuffer
import org.mt4j.components.visibleComponents.shapes.MTEllipse
import org.mt4j.util.MTColor
import org.mt4j.util.math.Vector3D

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
 
 
 object LineAnimator extends Actor {

  var nodesToBeAnimated = ArrayBuffer[Node]()

  var duration = Metronome().bpmInMillisecs
  var startTime = 0l
  def expiredTime = System.currentTimeMillis() - startTime
  def expiredTimeFactor = expiredTime/duration.toFloat

  /**
   * Calculates the new position for the moving circle of the line from node
   * @param node
   * @return Vector3D The new position
   */
  def circlePosition(node:DragableNode) = {
    val connectionLine = node.position.getSubtracted(node.ancestor.position)
    node.ancestor.position.getAdded(connectionLine.getScaled(LineAnimator().expiredTimeFactor))
  }

  /**
   * One single thread for the dot animation on the lines.
   * Saves a lot of CPU power....
   */
  def act() {

    // make all the dots on the lines visible...
    nodesToBeAnimated.foreach(node => {
      node.lineToAncestor.movingCircle.setVisible(true)
    })

    // start animation
    startTime = System.currentTimeMillis()
    while (expiredTime < duration) {
      // move all the dots one step forward
      nodesToBeAnimated.foreach(node => {
        node.lineToAncestor.movingCircle.setPositionGlobal(circlePosition(node))
      })
    }

    // make the dots invisible
    nodesToBeAnimated.foreach(node => {
      node.lineToAncestor.movingCircle.setVisible(false)
    })

  }

  def start(nodes: ArrayBuffer[Node]) {
    nodesToBeAnimated = nodes

    getState match {
      case Actor.State.Terminated => restart()
      case _ => start()
    }
  }

  def apply() = this
}
