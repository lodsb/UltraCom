package main.scala.TreeQuencer

import actors.Actor

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

object LineAnimator {

 /**
   * Resets the nodes, which have to be animated.
   * Starts the thread also if not started or terminated
   */
  def startLineAnimations(nodes: NodeSet[Node], durationFactor: Float = 1f) {
    if (nodes.size==0) return
    /**
     * One single thread for the dot animation on the lines.
     * Saves a lot of CPU power....
     */
    new Actor() {

      // the start time, from when the line animation begins
      var startTime = 0l

      // Measures the time left from startTime
      def expiredTime = System.currentTimeMillis() - startTime

      // how much percent from the whole duration between two beat signals expired
      def expiredTimeFactor = expiredTime/duration

      // the dureation between two beat signals
      def duration = Metronome().duration()*durationFactor

      /**
       * Calculates the new position for the moving circle of the line to node
       * @param node The node to which the line goes to
       * @return Vector3D The new position
       */
      def circlePosition(node: DragableNode) = {
        val connectionLine = node.position.getSubtracted(node.ancestor.position)
        node.ancestor.position.getAdded(connectionLine.getScaled(expiredTimeFactor))
      }

      def act() {
        // make all the dots on the lines visible...
        nodes.foreach( node => {
          node.lineToAncestor.createAnimationCircle(this)
        })

        // start animation
        startTime = System.currentTimeMillis()
        while (expiredTime < duration) {
          // move all the dots one step forward
          nodes.foreach( node => {
            node.lineToAncestor.moveCircle(this,circlePosition(node))
          })
          Thread.sleep(13l) // this is the time between two animation processes
        }

        // make all the dots on the lines invisible and remove them
        nodes.foreach( node => {
          node.lineToAncestor.destructAnimationCircle(this)
        })
      }

    }.start()
  }

  def apply = this
}

