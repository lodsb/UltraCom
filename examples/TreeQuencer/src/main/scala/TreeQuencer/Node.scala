package main.scala.TreeQuencer


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

  var synthesizer: NodeSynthesizer = null

  def play() {
    if(synthesizer != null && !isSourceNode && !isNearToCenter) {
      if (synthesizer.init) {
        synthesizer.bind(form.rotationZ, "rotationZ", (x:Float) => {x})
        synthesizer.bind(form.rotationY, "rotationY", (x:Float) => {x})
        synthesizer.bind(form.rotationX, "rotationX", (x:Float) => {x})
        synthesizer.bind(NodeMetronome.duration, "beatDuration", (x:Float) => {x})
        synthesizer.bind(form.scaleFactor, "volume", (x:Float) => {if(x<0.8f) 0f else if(3f<x) 1f else 5/12f*x-1f/3})
      }
      synthesizer.play
    }
  }

}

object SourceNode extends Node {
  form = new NodeForm(FileImporter.sourceNodeFormFile)
  form.scaleGlobal(0.5f, 0.5f, 0.2f, position)

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


object NewRandomNode {
  def apply(): Node = {
    new Node {
      form = Import.form(FileImporter.randomFormFile)
      synthesizer = Import.synthesizer(this, FileImporter.randomSynthiFile)
    }
  }
}