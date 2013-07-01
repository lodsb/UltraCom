package main.scala.TreeQuencer

import org.mt4j.util.math.Vector3D


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


class Node extends DragableNode {

  val id = app.idCounter

  // all nodes are globally stored in globalNodeSet
  // this is for reconnection (see DragableNode.nearestPossibleAncestor)
  if (app.game == app.SEQUENCE_GAME) {
    // in sequence game only still nodes are possible ancestors
    if (isInstanceOf[StillNode]) {
      app.globalNodeSet += this
    }
  } else {
    app.globalNodeSet += this
  }

  var synthesizer: NodeSynthesizer = null

  def play() {
    if(synthesizer != null && !isSourceNode) {
      if(app.game != app.SEQUENCE_GAME) {
        if (!isNearToCenter) {
          synthesizer.play
        }
      } else {
        synthesizer.play
      }
    }
  }

}

object SourceNode extends Node {
  if (app.game != app.SEQUENCE_GAME) {
    form = new NodeForm(FileImporter.sourceNodeFormFile)
    form.scaleGlobal(0.5f, 0.5f, 0.2f, position)
  }

  def apply(): Node = this

  /**
   * Checks if there's a node lying on SourceNode()
   * @return Boolean if yes or no
   */
  def isOccupied: Boolean = {
    app.globalNodeSet.foreach( node => {
      if (!node.isSourceNode && node.isNearToCenter) {
        return true
      }
    })
    false
  }
}


object RandomNode {
  def apply(): Node = {
    new Node {
      form = Import.form(FileImporter.randomFormFile)
      synthesizer = Import.synthesizer(this, FileImporter.randomSynthiFile)
    }
  }
  def apply(pos: Vector3D): Node = {
    val node = apply()
    node.form.setPositionGlobal(pos)
    node
  }
}

class StillNode(position: Vector3D) extends Node {
  form = new NodeForm(FileImporter.sourceNodeFormFile)
  form.scaleGlobal(0.4f, 0.4f, 0.4f, position) // make it still
  form.setPositionGlobal(position)
}