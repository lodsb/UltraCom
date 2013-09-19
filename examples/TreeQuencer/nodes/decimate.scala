import de.sciss.synth._
import sun.rmi.transport.proxy.RMIHttpToPortSocketFactory
import ugen._
import org.mt4j.output.audio.{Changed1, AudioServer}
import de.sciss.synth.Ops
import de.sciss.synth.Ops._
import de.sciss.synth._
import de.sciss.synth

SynthDef("Decimate") {

	// PARAMETERS  -----
	val gate = "gate".kr(0)
	val volume = "volume".kr(0.083)
	val rotationZ = "rotationZ".kr(0)
	val rotationY = "rotationY".kr(0)
	val rotationX = "rotationX".kr(0)
	val beatDuration = "beatDuration".kr(1000)

  val fakeGate:GE = 1.0;

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
		Clip.kr(rotation.abs / 360, 0, 1)
	}


	// CREATING SOUND -----	
	var root:GE = ((12*scaledRot(rotationZ))+55).floor
	var trigger = Changed1.kr(gate)
	var imp = EnvGen.ar(Env.perc(0.2, 0.5), trigger)
  var imp2 = EnvGen.ar(Env.perc(0.3, 0.7), trigger)
	var freqs:GE = Seq(root-24,root+7,root+15)
	freqs = freqs.midicps
	var freqs2:GE = Seq(root,root+12,root+3)
	freqs2 = freqs2.midicps
	var osc = Pulse.ar(freqs, SinOsc.ar(0.2*LFNoise0.kr(0.2)).abs+0.05)+ Saw.ar(freqs2)+(scaledRot(rotationX)*WhiteNoise.ar);


  //val fadeIn =  1.0 - EnvGen.ar(Env.perc(0.0, 1.5), fakeGate)



	var sig:GE = LeakDC.ar(BLowPass.ar(osc, (imp*(scaledRot(rotationY+90)*10000))+50.0));
  sig = sig * imp;
//  sig = BHiPass.ar(sig, 50)
	sig = 0.35*DelayL.ar(sig,0.17,0.17)+sig
	sig = 0.35*DelayL.ar(sig,0.27,0.27)+sig
	sig = 0.35*DelayL.ar(sig,0.45,0.45)+sig
	sig = 0.35*DelayL.ar(sig,0.75,0.75)+sig
//  sig = BHiPass.ar(sig, 50)
  sig = LeakDC.ar((FreeVerb.ar(SplayAz.ar(2,Limiter.ar(volume*sig*0.45)),0.9, 0.4)));
//  sig = LeakDC.ar(Limiter.ar(volume*sig*0.45));

	AudioServer.attach(sig)

}
