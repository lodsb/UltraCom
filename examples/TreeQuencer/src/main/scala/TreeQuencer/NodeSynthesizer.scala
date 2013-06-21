package main.scala.TreeQuencer

import java.io.File
import org.mt4j.output.audio.AudioServer._
import org.lodsb.reakt.async.VarA


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
  private var _switch = new VarA[Float](0f)
  def switch = {_switch() = if(_switch()==1) 0f else 1f}
  var synthesizer = FileImporter.cacheSynth(file)
  synthesizer.run(flag = false)

  def play() {
    switch
    synthesizer.run
  }

  def free() {
    synthesizer.run(flag = false)
    synthesizer.free
    synthesizer = null
  }

  def bind (value: VarA[Float], parameter: String, function: Float => Float = (x:Float) => {x}) {
    value.map( z => {
      synthesizer.parameters() = (parameter,function(z))
    })
  }

  bind(_switch, "gate")
  switch

  bind(node.form.rotationZ, "rotationZ", (x:Float) => {x})
  bind(node.form.rotationY, "rotationY", (x:Float) => {x})
  bind(node.form.rotationX, "rotationX", (x:Float) => {x})
  bind(Metronome().duration, "beatDuration", (x:Float) => {x})
  bind(node.form.scaleFactor, "volume", (x:Float) => {if(x<0.8f) 0f else if(3f<x) 1f else 5/12f*x-1f/3})


  var x1: Float = 0
  var x2: Float = 0
  var x3: Float = 0

  // volume <-> color of node
  var max = 0f
  var maxMem = 0f
  val r = node.form.materialCopy.getAmbient(0)
  val b = node.form.materialCopy.getAmbient(1)
  val g = node.form.materialCopy.getAmbient(2)
  var colorArray = Array(r,b,g,1f)
  synthesizer.amplitude.map( x => { if (node.isWithinField) {
    synthesizer.setAmplitudeUpdateDivisions(1)
    if (!(x1==0&&x2==0&&x3==0&&x==0&&maxMem<0.005)) {
      x1 = x2
      x2 = x3
      x3 = x.abs*500
      max = List(x1,x2,x3).max
      maxMem = if (max<maxMem) (max+maxMem)/2 else max
      if(!node.form.isGrey) {
        colorArray = Array(
          if(maxMem>r) maxMem else r,
          if(maxMem>b) maxMem else b,
          if(maxMem>g) maxMem else g,
          1f
        )
        node.form.material.setAmbient(colorArray)
        node.form.material.setDiffuse(colorArray)
        node.form.material.setSpecular(colorArray)
      }
    }
  }})

}
