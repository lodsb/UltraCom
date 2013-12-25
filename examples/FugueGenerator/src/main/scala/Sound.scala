/**
 * Created with so called Intelligence.
 * User: ghagerer
 * Date: 10.11.13
 * Time: 16:16
 */

package de.ghagerer.FugueGenerator

import org.mt4j.output.audio.{Changed1, AudioServer}
import org.mt4j.output.audio.AudioServer._
import de.sciss.synth._
import ugen._
import de.sciss.synth.Ops._
import org.lodsb.reakt.sync.VarS
import scala.collection.mutable.ArrayBuffer
import org.lodsb.scales.Conversions._
import org.lodsb.scales.Transformations._
import org.lodsb.scales._


class Synthi {

  private var _activeHarmony : org.lodsb.scales.Pitch = org.lodsb.scales.Pitch(0)
  def activeHarmony_=(value: org.lodsb.scales.Pitch) {
    _activeHarmony = value
  }

  /**
   * translates a pitch value ranging from 0-1 to an acoustic frequency
   * @param pitch value between -1 and 1
   * @return a frequency value in Hz
   */
  def frequency(pitch: Float): Double = {

    val chordLength = Harmony.activeChord.length

    // calculate dynamic pitchRange, dependent from activity/arousal value
    val pitchRange = math.round(activity()*4+2)

    // degree. which degree to choose within a chord
    var degree = math.round(pitchRange*pitch)

    // factor for octavisation
    var factor = 1d

    // if the degree is to high (greater than chordLength) reduce it and
    // change the factor for octavisation accordingly (or vice versa if to small)
    if (chordLength <= degree) {
      while (chordLength <= degree) {
        degree -= chordLength
        factor *= 2d
      }
    } else if (degree < 0) {
      while (degree < 0) {
        degree += chordLength
        factor *= 0.5d
      }
    }

    // the bassFactor takes the bass/treble property respectively slider into account
    var bassFactor: Float = bass()
    bassFactor = if(bassFactor<1/3f) 0.5f else if(2/3f<bassFactor) 2f else 1f

    bassFactor * factor * Harmony.activeChord(degree).frequency
  }

  // synthesizer definition
  val synthDef = SynthDef("") {

    // parameters
    val gate = "gate".kr(1)
    val volume = "volume".kr(0.9)
    val pitch = "pitch".kr(0)
    val activity = "activity".kr(0)
    val frequency = "frequency".kr(0)
    val bass = "bass".kr(0)
    val beatDuration = "beatDuration".kr(0)
    val timeSignature = "timeSignature".kr(0)

    // tone
    val dec = 1.35
    var bing = volume * SinOsc.ar(frequency).madd(0.5,0) * EnvGen.kr(Env.perc(attack=0.01, release=dec), Changed1.kr(gate), doneAction=1)
    bing = Pan2.ar(SplayAz.ar(2, bing/0.325))

    bing = Limiter.ar(0.1f*bing)

    // put it out
    AudioServer.attach(bing)

  }

  // initialization stuff
  var synthesizer: Synth = synthDef.play()
  synthesizer.run(flag = false)

  // logic for playing the synthesizer sound
  private var _switch = 1f
  def switch = {_switch = math.abs(_switch-1f); _switch}
  def play(pitch: Float, toneDuration: Float) {
    synthesizer.parameters() = ("frequency", frequency(pitch))
    synthesizer.parameters() = ("gate", switch)
    //synthesizer.parameters() = ("toneDuration", toneDuration)
    synthesizer.run()
  }

  // the staccato value in percent (0-1)
  // 0 = legato / long
  // 1 = staccato / short
  val activity = new VarS[Float](1f)
  bind(activity, "activity")

  // the bass value in percent (0-1)
  // its about how deep the tonality/pitch is
  // 0 = bass
  // 1 = high / treble
  val bass = new VarS[Float](1f)
  bind(bass, "bass")

  // witch time signature does the rhythm have
  // 2, 4, 8 or 16
  var timeSignature = new VarS[Float](1f)
  bind(timeSignature, "timeSignature")

  // beat duration is the duration between two 32th beats
  bind(Metronome.duration, "beatDuration")

  def bind (value: VarS[Float], parameter: String, function: Float => Float = (x:Float) => {x}) {
    value.map( z => {
      if(synthesizer != null) {
        synthesizer.synth.set(parameter -> function(z))
      }
    })
  }

}




