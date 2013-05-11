package TreeQuencer

import org.mt4j.util.math.Vector3D
import de.sciss.synth.SynthDef
import java.io.File
import org.mt4j.output.audio.AudioServer


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

  var synthesizer: SynthDef = null

  def play() {
    if(synthesizer != null && 150 < position.distance2D(app.center)) {
      synthesizer.play
    }
  }
  def prepare() {}

}

object SourceNode extends Node {
  form = new ImportedForm(FileImporter.sourceNodeFormFile)
  form.scaleGlobal(0.5f, 0.5f, 0.2f, position)

  def apply(): Node = this

  /**
   * Checks if there's a node lying on SourceNode()
   * @return Boolean if yes or no
   */
  def isOccupied: Boolean = {
    app.globalNodeSet.foreach( node => {
      if (
        !node.isSourceNode &&
        node.position.distance(SourceNode.position) < 150
      ) {
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
      synthesizer = Import.synthesizer(FileImporter.randomSynthiFile)
    }
  }
}