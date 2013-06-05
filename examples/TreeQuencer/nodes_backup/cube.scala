import de.sciss.synth._
import ugen._
import org.mt4j.output.audio.{Changed1, AudioServer}

SynthDef("Cube") {


  val gate = "gate".kr(1)
  val volume = "volume".kr(0.083)
  val rotationZ = "rotationZ".kr(0)
  val rotationY = "rotationY".kr(0)
  val rotationX = "rotationX".kr(0)

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
    x.abs/360
  }

  val dec = 1.35
  val sin = volume * SinOsc.ar(rotationToHalftones(rotationZ)).madd(0.5,0) * EnvGen.kr(Env.perc(0.01, dec), Changed1.kr(gate), doneAction=0)
  var local = LocalIn.ar(2) + sin

  for(i <- 1 to 10) {
    local = AllpassN.ar(local, 0.01, i*0.01*scaleDown(rotationX), scaleMoreDown(rotationX))
  }


  LocalOut.ar(local*scaleDown(rotationX))
  AudioServer.attach(sin + local)

}