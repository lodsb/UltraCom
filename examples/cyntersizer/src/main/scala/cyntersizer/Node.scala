package cyntersizer

import org.mt4j.util.MTColor


import scala.util.Random


/**
 * This source code is licensed as GPLv3 if not stated otherwise.
 * NO responsibility taken for ANY harm, damage done to you, your data, animals, etc.
 *
 * Last modified:  17.03.13 :: 17:11
 * Copyright (c) 2013: Gerhard Hagerer (Email: ghagerer@gmail.com)
 *
 * Made in Bavaria by tons of eager fast pixies - since 1986.
 *
 * ^     ^
 *  ^   ^
 *  (o o)
 * {  |  }                  (Wong)
 *    "
 *
 * Don't eat the pills!
 */


abstract class Node extends DragableNode {

  var synthesizer: NodeSynthesizer = null

  def play() {}
  def prepare() {}

}


class Circle extends Node {
  form = new CircleForm(radius, new MTColor(255,0,0))
  synthesizer = new NodeSynthesizer()
}

class Triangle extends Node {
  form = new TriangleForm(radius,new MTColor(0,255,0))

  println("Triangle:"+form.getCenterPointGlobal().toString)
  synthesizer = new NodeSynthesizer()
}

class Square extends Node {
  form = new SquareForm(radius,new MTColor(0,0,255))
  synthesizer = new NodeSynthesizer()
}

object SourceNode {
  val sourceNode = new Node() {
    form = new CircleForm(radius,new MTColor(255,255,255))
    form.unregisterAllInputProcessors()
    form.removeAllGestureEventListeners()
  }
  def apply(): Node = sourceNode

  /**
   * Checks if there's a node lying on SourceNode()
   * @return Boolean if yes or no
   */
  def isOccupied: Boolean = {
    var isOccupied = false
    app.globalNodeSet.foreach(node => {
      if(
        !node.eq(SourceNode()) &&
        node.position.getX == app.center.getX &&
        node.position.getY == app.center.getY
      ) {
        isOccupied = true
      }
    })

    isOccupied
  }
}


object NewRandomNode {
  def apply(): Node = {
    val random = new Random().nextInt(3)
    random match {
      case 0 => new Circle()
      case 1 => new Square()
      case _ => new Triangle()
    }
  }
}