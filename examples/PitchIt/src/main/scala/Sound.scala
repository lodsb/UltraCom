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


object Sound {

  val synthDef = SynthDef("") {

    // parameters
    val gate = "gate".kr(1)
    val volume = "volume".kr(0.9)
    val pitch = "pitch".kr(0)

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

      (primePos + secondPos + thirdPos + fourthPos + fifthPos + sixthPos + seventhPos + eigthPos + seventhNeg + sixthNeg + fifthNeg + fourthNeg + thirdNeg + secondNeg + negNeg)

    }

    def pitch2Scale = {
      440 * 2.pow((pitch.abs*12).floor/12)
    }

    // tone
    val dec = 1.35
    var bing = volume * SinOsc.ar(pitch2Tone).madd(0.5,0) * EnvGen.kr(Env.perc(attack=0.01, release=dec), Changed1.kr(gate), doneAction=1)
    bing = Pan2.ar(SplayAz.ar(2, bing/0.325))

    bing = Limiter.ar(0.5f*bing)

    // put it out
    AudioServer.attach(bing)

  }

  var synthesizer: Synth = synthDef.play()
  synthesizer.run(flag = false)
  private var _switch = 1f


  def apply() = this
  def switch = {_switch = if(_switch==1f) 0f else 1f; _switch}
  def play(pitch: Float) {
    synthesizer.parameters() = ("gate",switch)
    synthesizer.parameters() = ("pitch",pitch)
    synthesizer.run()
  }


}




