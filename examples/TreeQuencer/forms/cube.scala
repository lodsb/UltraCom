import de.sciss.synth._
import ugen._
import org.mt4j.output.audio.{Changed1, AudioServer}

SynthDef("Cube") {

  val gate = "gate".kr(1)
  val volume = "volume".kr(1)
  val rotationZ = "rotationZ".kr(0)

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

  val dec = 1.35
  val sin = volume * SinOsc.ar(rotationToHalftones(rotationZ)).madd(0.5,0) * EnvGen.kr(Env.perc(0.01, dec), Changed1.kr(gate), doneAction=0)
  AudioServer.attach(sin)
}