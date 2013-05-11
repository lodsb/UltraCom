import de.sciss.synth._
import ugen._
import org.mt4j.output.audio.AudioServer
import de.sciss.synth._

/*
SynthDef("mySynth") {

	val oscFreq = "oscfreq".kr(440)
	val modFreq = "modfreq".kr(440)
	val coupling= "coupling".kr(0.0)
	
	val imp = Impulse.kr(2)
	val envelope = EnvGen.ar(Env.perc(0.1, 0.6, 1.0, curveShape(-4)),imp )

	val modulation = SinOsc.ar(modFreq)*coupling*envelope


	val signal = SinOsc.ar(oscFreq + modulation)

	AudioServer.attach(signal)
	
}
*/

SynthDef("DropsSynth") {
  var freq = 440
  var dec = 1.35
  val sin = SinOsc.ar(freq, 0).madd(0.5,0) * EnvGen.kr(Env.perc(0.01, dec), doneAction=2)
  //Out.ar(0, sin ! 2)
  AudioServer.attach(sin)
}