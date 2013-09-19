import de.sciss.synth._
import ugen._
import de.sciss.synth.Ops
import de.sciss.synth.Ops._
import de.sciss.synth._
import de.sciss.synth
import org.mt4j.output.audio.{Changed1, AudioServer}

SynthDef("tetra") {

  // parameters
  val gate = "gate".kr(1)
  val volume = "volume".kr(0.083)
  val rotationZ = "rotationZ".kr(0)
  val rotationY = "rotationY".kr(0)
  val rotationX = "rotationX".kr(0)

  // functions

  /**
   * Returns a parameterised function, that maps a GE to another GE,
   * but mainly a Float to a Float.
   * The function maps a rotation value in degrees to a frequency.
   * 360 degrees get mapped to the 12 halftones.
   *
   * @return GE The result
   */
  def rotationToHalftones(x: GE): GE = {
    440 * 2.pow((x.abs/30).floor/12)
  }
  def scaleDown(x: GE): GE = {
    val a = (x.abs/360) < 0.9
    val b = (x.abs/360) > 0.9
    a*(x.abs/360)+b*0.9
  }
  def scaleMoreDown(x: GE): GE = {
    x.abs/180
  }
  def delay(signal: GE, index: Int): GE = {
    val count = (rotationY.abs/30).floor
    val delay = 2/count
    0.8*CombN.ar(signal, 1, delay, 0)*(count>index)
  }

  def scaledRot(rotation:GE): GE = {
 		Clip.kr(rotation / 360, 0, 1);
 	}

  // tone
  val dec = 1.35
  var bing = volume * SinOsc.ar(rotationToHalftones(rotationZ)*(scaledRot(rotationX)*5*Saw.ar(440.0*scaledRot(rotationY)))+1.0).madd(0.5,0) * EnvGen.kr(Env.perc(attack=0.01, release=dec), Changed1.kr(gate), doneAction=1)
	bing = Pan2.ar(SplayAz.ar(2, bing/0.325));

  // reverb
  //var reverb = LocalIn.ar(2) + bing
  //for(i <- 1 to 15) {
  //  reverb = AllpassN.ar(reverb, 0.01, i*0.01*scaleDown(rotationX), scaleMoreDown(rotationX))
  //}
  //LocalOut.ar(reverb*scaleDown(rotationX))

  /**
   * A function that fulfills the following conditions:
   * f(0) = 0
   * lim x->infinite of f(x) = 1
   * @param x
   * @return
   */
  def f(x: GE): GE = {
    1-1/((x/200)+1)
  }

  bing = FreeVerb.ar(bing, f(rotationX), f(rotationX), f(rotationX))

  // hall

  val echo1  = delay(bing, 1)
  val echo2  = delay(echo1, 2)
  val echo3  = delay(echo2, 3)
  val echo4  = delay(echo3, 4)
  val echo5  = delay(echo4, 5)
  val echo6  = delay(echo5, 6)
  val echo7  = delay(echo6, 7)
  val echo8  = delay(echo7, 8)
  val echo9  = delay(echo8, 9)
  val echo10 = delay(echo9, 10)
  val echo11 = delay(echo10, 11)

  bing += echo1 + echo2 + echo3 + echo4 + echo5 + echo6 + echo7 + echo8 + echo9 + echo10 + echo11
  bing = Limiter.ar(0.5f*bing)

  // put it out
  AudioServer.attach(bing)

}
