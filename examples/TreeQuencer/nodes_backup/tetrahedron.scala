import de.sciss.synth._
import ugen._
import org.mt4j.output.audio.{Changed1, AudioServer}

SynthDef("BlockSynth") {

  val gate = "gate".kr(1)
  val volume = "volume".kr(1)
  val rotationZ = "rotationZ".kr(0)

  /**
   * Returns a parameterised function, that maps a GE to another GE,
   * but mainly a Float to a Float.
   * The function is a line, which takes the wanted zeros as parameters (xZero or yZero).
   * Only the absolute value of the function is taken (i.e. always > 0)
   * Before the function calculates anything, it makes the input positive.
   *
   * The function looks like the following: (symmetrical to x-Axis)
   * \              /
   *  \            /
   *   \          /
   *    \________/
   *
   * @param xZero Wanted zero of a function on the x axis
   * @param yZero Wanted zero of a function on the y axis
   * @return GE The result
   */
  def f(xZero: Float = Float.MinValue, yZero: Float = Float.MinValue): GE=>GE = (x: GE) => {

    // input values should at first always be positive
    val greaterZero = x * (0 < x)
    val smallerZero = x * (x < 0) * (-1)
    val z = greaterZero + smallerZero

    var channel: GE = null
    // two functions, but both are the same, just different parameterisation:
    // pass xZero for a linear function, which is a line through the point (xZero,0)
    // pass yZero for a linear function, which is a line through the point (0,yZero)
    if (xZero != Float.MinValue) {
      channel = 1 - (z-360)/(xZero-360)
    } else {
      channel = 1 + (1-yZero)*(z/360-1)
    }

    // output out should match the following conditions:
    // if out>1   -> out=1
    // if out<0   -> out=0
    // if 0<out<1 -> out
    channel * (0<channel) * (channel<1) + (1<channel)
  }


  val (tone1, noiseVol, filterStart, filterEnd, speed, overlap, atk, globAmp) =
    ("tone1".kr(5), "noiseVol".kr(0.005), "filterStart".kr(300), "filterEnd".kr(1000), "speed".kr(1), "overlap".kr(1.5), "atk".kr(5), "globAmp".kr(0.02))

  val (frq1, frq5, frq3, frq7, frq9, frq11, frq13) =
    ("frq1".kr(400), "frq5".kr(600), "frq3".kr(500), "frq7".kr(750), "frq9".kr(900), "frq11".kr(1066), "frq13".kr(1333))

  val (amp1, amp5, amp3, amp7, amp9, amp11, amp13) =
    (f(yZero=0.5f)(rotationZ), f(xZero=0.0f)(rotationZ), f(xZero=40f)(rotationZ), f(xZero=80f)(rotationZ), f(xZero=120f)(rotationZ), f(xZero=160f)(rotationZ), f(xZero=200f)(rotationZ))

  val percussiveEnvelope = Env.perc(speed/atk, speed*overlap, 20)

  val gateChanger = Changed1.kr(gate)

  val w1 = volume * RLPF.ar(Blip.ar(frq1,tone1).madd(amp1*globAmp,0) + PinkNoise.ar(noiseVol), Line.kr(filterStart,filterEnd,3),0.4) *
    EnvGen.kr(percussiveEnvelope, gateChanger, doneAction=0)
  val w2 = volume * RLPF.ar(Blip.ar(frq5,tone1).madd(amp5*globAmp,0) + PinkNoise.ar(noiseVol), Line.kr(filterStart,filterEnd,3),0.4) *
    EnvGen.kr(percussiveEnvelope, gateChanger, doneAction=0)
  val w3 = volume * RLPF.ar(Blip.ar(frq3,tone1).madd(amp3*globAmp,0) + PinkNoise.ar(noiseVol), Line.kr(filterStart,filterEnd,3),0.4) *
    EnvGen.kr(percussiveEnvelope, gateChanger, doneAction=0)
  val w4 = volume * RLPF.ar(Blip.ar(frq7,tone1).madd(amp7*globAmp,0) + PinkNoise.ar(noiseVol), Line.kr(filterStart,filterEnd,3),0.4) *
    EnvGen.kr(percussiveEnvelope, gateChanger, doneAction=0)
  val w5 = volume * RLPF.ar(Blip.ar(frq9,tone1).madd(amp9*globAmp,0) + PinkNoise.ar(noiseVol), Line.kr(filterStart,filterEnd,3),0.4) *
    EnvGen.kr(percussiveEnvelope, gateChanger, doneAction=0)
  val w6 = volume * RLPF.ar(Blip.ar(frq11,tone1).madd(amp11*globAmp,0) + PinkNoise.ar(noiseVol), Line.kr(filterStart,filterEnd,3),0.4) *
    EnvGen.kr(percussiveEnvelope, gateChanger, doneAction=0)
  val w7 = volume * RLPF.ar(Blip.ar(frq13,tone1).madd(amp13*globAmp,0) + PinkNoise.ar(noiseVol), Line.kr(filterStart,filterEnd,3),0.4) *
    EnvGen.kr(percussiveEnvelope, gateChanger, doneAction=0)

  AudioServer.attach((w1 + w2 + w3 + w4 + w5 + w6 + w7))

}
