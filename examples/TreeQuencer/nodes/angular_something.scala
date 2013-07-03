import de.sciss.synth._
import de.sciss.synth.Env.Seg;
import de.sciss.synth.Env.Seg._;
import org.w3c.dom.ls.LSParserFilter
import ugen._
import org.mt4j.output.audio.{Changed1, AudioServer}

SynthDef("angular") {

	// PARAMETERS  -----
	val gate = "gate".kr(1)
	val volume = "volume".kr(0.083)
	val rotationZ = "rotationZ".kr(0)
	val rotationY = "rotationY".kr(0)
	val rotationX = "rotationX".kr(0)
	val beatDuration = "beatDuration".kr(1000)


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
		Clip.kr(rotation.abs / 360, 0, 1);
	}




	// CREATING SOUND -----

	//var envc1, envc2, outc, noisec1, noisec2, envs0, envs1, envs1m, envs2: GE;
	//var oscs, noises, outs:GE;
	//var mix, freq:GE;


		var freqs = Seq( 102.83503357415, 271.87123588774, 165.08083327752, 178.25018273397, 323.11964925229, 116.86829443705, 134.95738247509, 200.00001372448, 502.37729319678, 597.07655656184 );
		var resos = Seq( 0.10743826856584, 0.037808643263063, 0.042253087508826, 0.037808643263063, 0.0625, 0.10027777526114, 0.10027777526114, 0.059753101151667, 0.040000001192093, 0.042253087508826 );
		var amps = Seq( 0.23080432814507, 0.049036562352402, 0.072009509795199, 0.080804783787226, 0.082371636160548, 0.25568279423097, 0.2300671314532, 0.59489172046932, 0.29811618978107, 0.30389314914497 );


		var trig = Changed1.kr(gate);
		var env = EnvGen.ar(Env.perc(0.001, 0.1), trig);

    var delayLenMod = (scaledRot(rotationX)*2.0)+1;
    var delayMod = (scaledRot(rotationY)*2.0)+1;


		var sig:GE = Klank.ar(Zip(freqs, resos, amps), env*WhiteNoise.ar()*SinOsc.ar(1000*env), Rand(0.01, 5), Rand(0.01, 2.0));
    sig = BLowPass.ar(sig, (1.0-scaledRot(rotationZ))*18000+100)


		var aux:GE = LeakDC.ar(CombC.ar(sig, 1, delayLenMod*LFTri.kr(0.25*delayMod, iphase= 0.34).abs*0.5, 1));
		sig = (0.750*sig)+(0.125*aux);
		aux = LeakDC.ar(CombC.ar(sig, 0.5, delayLenMod*LFTri.kr(0.17*delayMod).abs*0.33, 0.5));
		sig = (0.750*sig)+(0.125*aux);
		aux = LeakDC.ar(CombC.ar(sig, 1, delayLenMod*LFTri.kr(0.47*delayMod).abs*0.25, 1));
		sig = (0.750*sig)+(0.125*aux);
		aux = LeakDC.ar(CombC.ar(sig, 1, delayLenMod*LFTri.kr(0.35*delayMod, iphase= 0.34).abs*0.8, 1));
		sig = (0.750*sig)+(0.125*aux);
		aux = LeakDC.ar(CombC.ar(sig, 0.5, delayLenMod*LFTri.kr(0.11*delayMod).abs*0.133, 0.5));
		sig = (0.750*sig)+(0.125*aux);
		aux = LeakDC.ar(CombC.ar(sig, 1, delayLenMod*LFTri.kr(0.67*delayMod).abs*0.66, 1));
		sig = (0.750*sig)+(0.125*aux);


		sig = Pan2.ar(SplayAz.ar(2,Limiter.ar(volume*sig)));






	AudioServer.attach(sig)

}
