import de.sciss.synth._
import sun.rmi.transport.proxy.RMIHttpToPortSocketFactory
import ugen._
import org.mt4j.output.audio.{Changed1, AudioServer}

SynthDef("Decimate") {

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
	
	def scaledRot(rotation:GE): GE = {
		Clip.kr(rotation / 720, 0, 1)
	}


	// CREATING SOUND -----	
	var root:GE = 50+(12*scaledRot(rotationZ))
	var trigger = Changed1.kr(gate)
	var imp = EnvGen.ar(Env.perc(0.2, 0.5), trigger)
	var freqs = Seq(root-24,root+7,root+15).midicps
	var freqs2 = Seq(root,root+12,root+3).midicps
	var osc = Pulse.ar(freqs, SinOsc.ar(0.2*LFNoise0.kr(0.2)))+ Saw.ar(freqs2)+(scaledRot(rotationX)*WhiteNoise.ar);
	 
	 
	 
	var sig:GE = LeakDC.ar(BLowPass.ar(osc,(imp*(50+(scaledRot(rotationY)*10000)))));
	sig = 0.35*DelayL.ar(sig,0.17,0.17)+sig
	sig = 0.35*DelayL.ar(sig,0.27,0.27)+sig
	sig = 0.35*DelayL.ar(sig,0.45,0.45)+sig
	sig = 0.35*DelayL.ar(sig,0.75,0.75)+sig
  sig = HPF.ar(sig, 20)
	sig = volume*LeakDC.ar(Limiter.ar(FreeVerb.ar(SplayAz.ar(2,sig),0.9, 0.4))*0.8);

	AudioServer.attach(sig)

}
