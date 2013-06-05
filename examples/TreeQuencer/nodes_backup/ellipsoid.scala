import de.sciss.synth._
import ugen._
import org.mt4j.output.audio.AudioServer
import de.sciss.synth._

SynthDef("DropsSynth") {
  var freq = 660
  var dec = 1.35
  val sin = SinOsc.ar(freq, 0).madd(0.5,0) * EnvGen.kr(Env.perc(0.01, dec), doneAction=2)
  //Out.ar(0, sin ! 2)
  AudioServer.attach(sin)
}
