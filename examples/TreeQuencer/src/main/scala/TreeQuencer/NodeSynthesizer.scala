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

  def init: Boolean = {
    if(synthesizer == null) {
      println("Initializing Synthesizer")
      synthesizer = synthDef.play

      // volume <-> color of node
      synthesizer.setAmplitudeUpdateDivisions(1)
      synthesizer.amplitude.map( x => {
        var z = x.abs
        node.form.meshes.foreach(mesh => {
          mesh.getMaterial.setAmbient(Array(5*z, z, z, 1f))
        })
      })
      return true
    }
    false
  }

/*
  val synthi = new Synthi {
    val node: Node = node
    val synthiDef: SynthDef = evaluateFile[SynthDef](synthiFile)
  }


  node.form.

  mySynth.parameters <~ couplingSlider.value.map { x => ( "oscfreq" -> x ) }

  node.form.globalPosition.observe {
    pos =>{
      mySynth.parameters() = ("modfreq" -> pos.x)
      mySynth.parameters() = ("coupling" -> 5*pos.y)
      true
  }}*/
}
