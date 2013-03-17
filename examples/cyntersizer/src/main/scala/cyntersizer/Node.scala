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
 * ^   ^
 * (o o)
 * {  |  }                  (Wong)
 * "
 *
 * Don't eat the pills!
 */


abstract class Node extends DragableNode {
  var synthesizer: NodeSynthesizer = null
  var radius: Float = 10f
  var rotationAngle: Float = 0f

  // abstract methods
  def isLastWithinTree: Boolean = true
  def play {}
  def prepare {}
}


class Circle extends Node {
  form = new CircleForm(radius, new MTColor(255,0,0))
  synthesizer = new NodeSynthesizer()
}

class Triangle extends Node {
  form = new TriangleForm(2*radius,new MTColor(0,255,0))
  synthesizer = new NodeSynthesizer()
}

class Square extends Node {
  form = new SquareForm(2*radius,new MTColor(0,0,255))
  synthesizer = new NodeSynthesizer()
}

object SourceNode {
  val sourceNode = new Node() {
    form = new CircleForm(radius,new MTColor(255,255,255))
  }
  def apply(): Node = sourceNode
}


object NewRandomNode {
  def apply(): Node = {
    val random = new Random().nextInt(3)
    random match {
      case 0 => new Circle()
      case 1 => new Triangle()
      case _ => new Square()
    }
  }
}