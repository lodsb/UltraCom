import de.sciss.synth._
import ugen._
import org.mt4j.output.audio.{Changed1, AudioServer}

SynthDef("cube") {

	// PARAMETERS  -----
	val gate = "gate".kr(1)
	val volume = "volume".kr(0.083)
	val rotationZ = "rotationZ".kr(0)
	val rotationY = "rotationY".kr(0)
	val rotationX = "rotationX".kr(0)
	val beatDuration = "beatDuration".kr(1000)

	val amp = "amp".kr(1)
	val cf = "cf".kr(100)
	val t_bd = "t_bd".kr(0)
	val t_sd = "t_sd".kr(0)
	val pw = "pw".kr(0.4)


	// FUNCTIONS -----
	def range(signal: GE, low: GE, high: GE): GE = {
		((high + low) / 2) + (signal * (high - low) / 2)
	}
	/**
	 * rotation to sound-frequence, discretised to the 12 halftones
	 * @param rotation in degrees
	 * @return GE sound-frequence in Hz
	 */
	def halftone(rotation: GE): GE = {
		55 * 2.pow((rotation.abs / 30).floor / 12)
	}
	/**
	 * rotation to duration of the sound
	 * @param rotation in degrees
	 * @return GE duration in seconds
	 */
	def duration(rotation: GE): GE = {
		(((rotation.abs) / 360).floor + 1) * (beatDuration / 1000)
	}
	/**
	 * rotation to wobbles per seconds
	 * 0 - 30  degrees -> 1
	 * 30 - 60  degrees -> 2
	 * 60 - 90  degrees -> 3 ...
	 * @param rotation in degrees
	 * @return GE wobbles per seconds
	 */
	def wobbles(rotation: GE): GE = {
		(rotation.abs / 30).floor + 1
	}


	// CREATING SOUND -----
	var base = RLPF.ar(Pulse.ar(Seq(0.99, 0.5, 1.01).map(_ * halftone(rotationZ)), pw), cf, 0.3).madd(range(SinOsc.kr(wobbles(rotationY)), 0.5, 4), 0).sin
	val env = EnvGen.kr(Env.perc(attack = 0.01, release = 3*duration(rotationX)), Changed1.kr(gate), doneAction = 1)
	base = (HPF.ar(base * env, 30) * 0.5 + base).tanh

	val sig = Pan2.ar(SplayAz.ar(2, volume * base * amp * env)/2.0)





	AudioServer.attach(sig)

}
