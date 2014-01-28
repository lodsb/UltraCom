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
  val r = new scala.util.Random
  def rrand(l: Double, h: Double) = (r.nextFloat()*(h-l))+l

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
    val activity = "activity".kr(0.5)
    val valenceIn = 1-"valence".kr(0.5)
    val valence = valenceIn+0.0001
    val frequency = "frequency".kr(0)
    val bass = "bass".kr(0.5)
    val beatDuration = ( "beatDuration".kr(100) / 1000.0 ) // beat duration in seconds
    val timeSignature = "timeSignature".kr(8)

    // note length is set by activity + step size
    //
    val invActivity = (2.0 - activity)   // > 1.0 so the decay wont be too short and we get overlapping tones on low
    val invValence = 1-valence
                                          // activity
    val noteLength = ( beatDuration*(32.0/timeSignature) ) // slightly overlapping notes
    // staccato: change 0.5 to something else
    val dec = ( noteLength * invActivity * LinLin(invValence, 0,1,0.5,2.0));
    val atk = (0.02 * invActivity)

    // adds a slight slur/legato playing to the notes if valence is high (sad)
    val slurTime = LinLin(valence+0.1, 0.1, 1.4, 0.0, noteLength*0.125); //valence,0,1, 0,noteLength)

    val freq = Lag.kr(frequency, slurTime)


    // fm synthesis
    var modulator = (5*freq* (activity+2*invValence)/3  )*SinOsc.ar(freq * ( 4*invValence + 1 ).roundTo(1.0) )+0.01
    var osc = SinOsc.ar(freq+modulator).madd(0.5,0)
    var bing = volume * osc * EnvGen.kr(Env.perc(atk, dec), Changed1.kr(gate), doneAction=1)

    //distortion
    val numChorusDelays = 36;
    val chorusIn = bing* 1.0/numChorusDelays;
    val chorusModulators = (0 to numChorusDelays).map {i => LFPar.kr(0.5* rrand(0.64, 1.06), 0.5 * i)* 0.5 + 0.08}
    val	chorusSig = (bing+2*Mix.mono(DelayC.ar(chorusIn, chorusModulators)))/3;

    val distFade = (activity-0.01)*(valence-0.01)
    val distIn = HPF.ar(chorusSig, activity*440+10)*8
    val amount = distFade*EnvGen.kr(Env.perc(0.001, dec*1.7,curve= Curve.sine), Changed1.kr(gate), doneAction=1)
    val amCoef = (2.0*amount/(1.0-amount))
    var dist = MidEQ.ar(LPF.ar((amCoef+1.0)*distIn/((amCoef*distIn.abs)+1.0), Seq(3800, 3900))*0.5, 120, 0.7, 8);
    dist = MidEQ.ar(LPF.ar((amCoef+1.0)*dist/((amCoef*dist.abs)+1.0), Seq(1800, 1900))*0.5, 120, 0.7, 8);
    dist = MidEQ.ar(LPF.ar((amCoef+1.0)*dist/((amCoef*dist.abs)+1.0), Seq(2500, 2600))*0.5, 120, 0.7, 8);
    val distOut = (dist + dist.tanh + dist.cos)/3.5

    // comment this out
    //var asrEnv = Env.perc(0.005, dec*1.8, curve= Curve.sine);
    //val distOut = 1.5*dista *EnvGen.kr(asrEnv, Changed1.kr(gate), doneAction = 1)
    // 'till here

    bing = XFade2.ar(SplayAz.ar(2, bing/0.325), SplayAz.ar(2, distOut/0.325), 2*(distFade)-1)

    bing = LeakDC.ar(Pan2.ar(bing))



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
  val activity = new VarS[Float](0.5f)
  bind(activity, "activity")

  val valence = new VarS[Float](0.5f)
  bind(valence, "valence")


  // the bass value in percent (0-1)
  // its about how deep the tonality/pitch is
  // 0 = bass
  // 1 = high / treble
  val bass = new VarS[Float](0.5f)
  bind(bass, "bass")

  // witch time signature does the rhythm have
  // 2, 4, 8 or 16
  var timeSignature = new VarS[Float](8f)
  bind(timeSignature, "timeSignature")
  //timeSignature.observe({x=> println("sig "+x); true})
  //activity.observe({x=> println("act "+x); true})
  //valence.observe({x=> println("val "+x); true})


  // beat duration is the duration between two 32th beats
  bind(Metronome.duration, "beatDuration")

  def bind (value: VarS[Float], parameter: String, function: Float => Float = (x:Float) => {x}) {
    value.observe( z => {
      if(synthesizer != null) {
        synthesizer.synth.set(parameter -> function(z))
      }
    true })
  }

}




