package main.scala.TreeQuencer

import java.io.File
import org.mt4j.output.audio.AudioServer._
import org.lodsb.reakt.async.VarA
import org.lodsb.reakt.sync.VarS
import de.sciss.synth.Synth
import de.sciss.synth._
import ugen._
import de.sciss.synth.Ops
import de.sciss.synth.Ops._

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
  private val _switch = new VarA[Float](1f)
  def switch = {_switch() = if(_switch()==1) 0f else 1f; _switch()}
  var synthesizer: Synth = FileImporter.cacheSynth(file)
  synthesizer.run(flag = false)
  synthesizer.setAmplitudeUpdateDivisions(1)

  // uncomment to see params sent to the synth
  //synthesizer.parameters.observe { x=> println("synth parm "+this+" "+x); true}

  def play() {
    if(synthesizer != null) {
      synthesizer.parameters() = ("gate",switch)
      synthesizer.run()
    }
  }

  def free() {
    synthesizer.run(flag = false)
    synthesizer.free
    synthesizer = null
  }

  def bind (value: VarS[Float], parameter: String, function: Float => Float = (x:Float) => {x}) {
    value.map( z => {
      //synthesizer.parameters() = (parameter,function(z))
      //println("from bind")
      // just to be sure, send the parameters directly, it should work with synthesizer.parameters() as well,
      // it is a synchronous Var
      if(synthesizer != null) {
        synthesizer.synth.set(parameter -> function(z))
      }
    })
  }


  bind(node.form.rotationZ, "rotationZ", (x:Float) => {x})
  bind(node.form.rotationY, "rotationY", (x:Float) => {x})
  bind(node.form.rotationX, "rotationX", (x:Float) => {x})
  bind(Metronome().duration, "beatDuration", (x:Float) => {x})
  bind(node.form.scaleFactor, "volume", (x:Float) => {if(x<0.8f) 0f else if(3f<x) 1f else 5/12f*x-1f/3})

  val floatStack = new FloatStack(5)

  // volume <-> color of node
  // kind of a low pass filter
  var max = 0f
  var maxMem = 0f

  // emission in grayscales
  val grayEmission = node.form.materialCopy.getEmission(0)

  //var colorArray = Array(grayEmission,grayEmission,grayEmission,grayEmissionAlpha)

  synthesizer.amplitude.map( x => { if (node.isWithinField) {
    if (!(floatStack.isZero&&x==0&&maxMem<0.005)) {
      floatStack.push(x.abs*25)
      max = floatStack.max
      maxMem = if (max<maxMem) (max+maxMem)/2 else max
      if(!node.form.isGrey) {
        val emission = if(reduce(maxMem)> grayEmission) reduce(maxMem) else grayEmission
        val emissionAlpha = if(reduce(maxMem)> grayEmission) 1-reduce(maxMem) else 1f
        val colorArray = Array(
          emission,emission,emission, emissionAlpha
        )
        node.form.material.setEmission(colorArray)
        //node.form.material.setDiffuse(colorArray)
        //node.form.material.setSpecular(colorArray)
      }
    }
  }})

  def reduce(value: Float): Float = {
    if(value>10) {
       0.9f+value/1000f
    } else if(value>1) {
       0.8f+value/100f
    } else if (value>0.8) {
       0.8f
    } else {
      value
    }
  }

}