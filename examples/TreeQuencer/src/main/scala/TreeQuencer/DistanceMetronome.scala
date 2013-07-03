package main.scala.TreeQuencer

import collection.mutable

/**
 * This source code is licensed as GPLv3 if not stated otherwise.
 * NO responsibility taken for ANY harm, damage done to you, your data, animals, etc.
 * 
 * Last modified:  08.06.13 :: 16:59
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


object DistanceMetronome extends Metronome {
  val granulation = 8 // how many parts within two beat signals ~ solution
  var step = granulation // increments each round up to granulation and then restarts
  val nodeGrid = new Array[NodeSet[Node]](granulation)
  for (i <- 0 to granulation-1) {
    nodeGrid(i) = new NodeSet[Node]
  }
  val firstNodes = new mutable.HashMap[Int, Int]()
  def waitingTime = math.round(Metronome().duration()/granulation.toFloat)

  def distanceToSteps(node: Node): Int = {
    var distance = node.position.distance2D(SourceNode.position)
    if (distance<=app.innerCircleRadius) {
      distance = 0f
    } else {
      distance -= app.innerCircleRadius
    }
    val maxDistance = List(app.width,app.height).min/2f

    var triggerTime = math.round(distance/maxDistance*(granulation-1) + 1)
    if (triggerTime > granulation) {
      triggerTime = granulation
    }
    triggerTime
  }

  def act() {
    running = true
    while (running) {

      val nodesToBeAnimated = new NodeSet[Node]
      val firstNodesToBeAnimated = new mutable.HashMap[Int, NodeSet[Node]]()
      var nodesToBePlayed = new NodeSet[Node]

      step = if (step<granulation) step+1 else 1

      // for each node on the field
      app.globalNodeSet.foreach( node => { if (!node.isSourceNode) {
        // is node a child of the SourceNode?
        if (node.ancestor != null && node.ancestor.isSourceNode) { // yes
          if (!firstNodes.contains(node.id) || firstNodes(node.id) == 1) {
            val distanceInSteps = distanceToSteps(node)
            firstNodes(node.id) = distanceInSteps
            if (!firstNodesToBeAnimated.contains(distanceToSteps(node))) {
              firstNodesToBeAnimated(distanceInSteps) = new NodeSet[Node]
            }
            firstNodesToBeAnimated(distanceInSteps) += node
            nodesToBePlayed += node
          } else {
            firstNodes(node.id) -= 1
          }
        } else if (firstNodes.contains(node.id)) {
          firstNodes.remove(node.id)
        }
      }})

      nodesToBePlayed ++= nodeGrid(step-1)

      nodesToBePlayed.foreach( node => {
        node.play()
        nodeGrid(step-1) -= node
      })

      nodesToBePlayed.foreach( node => {
        nodesToBeAnimated ++= node.asInstanceOf[NodeSet[Node]]
        nodeGrid(step-1) ++= node.asInstanceOf[NodeSet[Node]]
      })

      LineAnimator.startLineAnimations(nodesToBeAnimated)
      firstNodesToBeAnimated.foreach( tuple => { val distanceInSteps = tuple._1; val nodes = tuple._2
        LineAnimator.startLineAnimations(nodes, distanceInSteps/granulation.toFloat)
      })
      Thread.sleep(waitingTime)
    }
  }

  def removeNode(node: Node) {}

  def get = this

  def durationFactor(node: Node) = {
    if (firstNodes.contains(node.id)) {
      distanceToSteps(node)/granulation.toFloat
    } else {
      1f
    }
  }
}
