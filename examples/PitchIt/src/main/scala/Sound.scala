/**
 * Created with so called Intelligence.
 * User: ghagerer
 * Date: 10.11.13
 * Time: 16:16
 */

package PitchIt

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

  def frequency(height: Float): Double = {
    var degree = math.round(4*height+4)
    var factor = 1d

    if (4 <= degree) {
      degree -= 4
      factor = 2d
      if (4 <= degree) {
        degree -= 4
        factor = 4d
      }
    }

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


    // further parameters
    // scale is the tonalic scale as number
    val scale = "scale".kr(0)

    // harmony is the scale degree as number
    // the actual chord is determined by this
    val harmony = "harmony".kr(0)


    def pitch2Tone = {
      val tone = pitch.signum * (pitch.abs * 8).floor

      val primePos: GE =   (tone sig_== 0) * 440.0
      val secondPos: GE =  (tone sig_== 1) * 493.9
      val thirdPos: GE =   (tone sig_== 2) * 523.2
      val fourthPos: GE =  (tone sig_== 3) * 587.3
      val fifthPos: GE =   (tone sig_== 4) * 659.2
      val sixthPos: GE =   (tone sig_== 5) * 698.4
      val seventhPos: GE = (tone sig_== 6) * 784.0
      val eigthPos: GE =   (tone sig_== 7) * 880.0

      val seventhNeg: GE =  (tone sig_== -1) * 392.0
      val sixthNeg: GE =   (tone sig_== -2) * 349.2
      val fifthNeg: GE =  (tone sig_== -3) * 329.6
      val fourthNeg: GE =   (tone sig_== -4) * 293.6
      val thirdNeg: GE =   (tone sig_== -5) * 261.6
      val secondNeg: GE = (tone sig_== -6) * 246.9
      val negNeg: GE =   (tone sig_== -7) * 220.0

      (
        primePos
          + secondPos
          + thirdPos
          + fourthPos
          + fifthPos
          + sixthPos
          + seventhPos
          + eigthPos
          + seventhNeg
          + sixthNeg
          + fifthNeg
          + fourthNeg
          + thirdNeg
          + secondNeg
          + negNeg
       )

    }

    def pitch2Pentatone = {
      val tone = pitch.signum * (pitch.abs * 6).floor

      val primePos: GE =   (tone sig_== 0) * 440.0
      //val secondPos: GE =  (tone sig_== 1) * 493.9
      val thirdPos: GE =   (tone sig_== 1) * 523.2
      val fourthPos: GE =  (tone sig_== 2) * 587.3
      val fifthPos: GE =   (tone sig_== 3) * 659.2
      //val sixthPos: GE =   (tone sig_== 5) * 698.4
      val seventhPos: GE = (tone sig_== 4) * 784.0
      val eigthPos: GE =   (tone sig_== 5) * 880.0

      val seventhNeg: GE =  (tone sig_== -1) * 392.0
      //val sixthNeg: GE =   (tone sig_== -2) * 349.2
      val fifthNeg: GE =  (tone sig_== -2) * 329.6
      val fourthNeg: GE =   (tone sig_== -3) * 293.6
      val thirdNeg: GE =   (tone sig_== -4) * 261.6
      //val secondNeg: GE = (tone sig_== -6) * 246.9
      val negNeg: GE =   (tone sig_== -5) * 220.0

      (
        primePos
          //+ secondPos
          + thirdPos
          + fourthPos
          + fifthPos
          //+ sixthPos
          + seventhPos
          + eigthPos
          + seventhNeg
          //+ sixthNeg
          + fifthNeg
          + fourthNeg
          + thirdNeg
          //+ secondNeg
          + negNeg
        )

    }

    def pitch2Scale = {
      440 * 2.pow((pitch.abs*12).floor/12)
    }

    // tone
    val dec = 1.35
    var bing = volume * SinOsc.ar(frequency).madd(0.5,0) * EnvGen.kr(Env.perc(attack=0.01, release=dec), Changed1.kr(gate), doneAction=1)
    bing = Pan2.ar(SplayAz.ar(2, bing/0.325))

    bing = Limiter.ar(0.5f*bing)

    // put it out
    AudioServer.attach(bing)

  }

  // initialization stuff
  var synthesizer: Synth = synthDef.play()
  synthesizer.run(flag = false)

  // logic for playing the synthesizer sound
  private var _switch = 1f
  def switch = {_switch = math.abs(_switch-1f); _switch}
  def play(pitch: Float) {
    synthesizer.parameters() = ("frequency",frequency(pitch))
    synthesizer.parameters() = ("gate",switch)
    //synthesizer.parameters() = ("pitch",pitch)
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

  def bind (value: VarS[Float], parameter: String, function: Float => Float = (x:Float) => {x}) {
    value.map( z => {
      if(synthesizer != null) {
        synthesizer.synth.set(parameter -> function(z))
      }
    })
  }

}




