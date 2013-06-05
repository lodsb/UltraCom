package main.scala.TreeQuencer

import java.io.File
import org.mt4j.output.audio.AudioServer._
import org.lodsb.reakt.async.VarA
import de.sciss.synth._


/**
 * This source code is licensed as GPLv3 if not stated otherwise.
 * NO responsibility taken for ANY harm, damage done to you, your data, animals, etc.
 * 
 * Last modified:  15.05.13 :: 12:08
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
 
 
class NodeSynthesizer(val node: main.scala.TreeQuencer.Node, val file: File) {
  var schalter = 1
  var synthesizer = null.asInstanceOf[Synth]
  val synthDef = FileImporter.cacheSynthDef(file)


  def play() {
    if (synthesizer != null) {
      println("Playing Synthesizer")
      schalter = if(schalter==1) 0 else 1
      synthesizer.parameters() = ("gate",schalter)
    }
  }

  def bind (value: VarA[Float], parameter: String, function: Float => Float) {
    if (synthesizer != null) {
      value.map( z => {
        synthesizer.parameters() = (parameter,function(z))
      })
    }
  }

  var x1: Float = 0
  var x2: Float = 0
  var x3: Float = 0

  def init: Boolean = {
    if(synthesizer == null) {
      println("Initializing Synthesizer")
      synthesizer = synthDef.play

      // volume <-> color of node
      var max = 0f
      var maxMem = 0f
      synthesizer.amplitude.map( x => { if (node.isWithinField) {
        synthesizer.setAmplitudeUpdateDivisions(0)
        if (!(x1==0&&x2==0&&x3==0&&x==0&&maxMem<0.005)) {
          x1 = x2
          x2 = x3
          x3 = x.abs*11
          max = List(x1,x2,x3).max
          maxMem = if (max<maxMem) (max+maxMem)/2 else max
          node.form.meshes.foreach(mesh => {
            mesh.getMaterial.setAmbient(Array(maxMem, maxMem, maxMem, 1f))
          })
        }
      }})
      return true
    }
    false
  }
}
