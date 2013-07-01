/*
	 ++1>>  This source code is licensed as GPLv3 if not stated otherwise.
		>>  NO responsibility taken for ANY harm, damage done
		>>  to you, your data, animals, etc.
		>>
	  +2>>
		>>  Last modified:  2012 - 4 - 17 :: 7 : 25
		>>  Origin: mt4j (project) / mt4j_mod (module)
		>>
	  +3>>
		>>  Copyright (c) 2012:
		>>
		>>     |             |     |
		>>     |    ,---.,---|,---.|---.
		>>     |    |   ||   |`---.|   |
		>>     `---'`---'`---'`---'`---'
		>>                    // Niklas Klügel
		>>
	  +4>>
		>>  Made in Bavaria by fat little elves - since 1983.
	 */

package org.mt4j.output.audio

import de.sciss.synth.ugen._
import de.sciss.synth._
import de.sciss.osc.Message
import scala.collection.mutable.Map
import de.sciss.synth.NodeManager.NodeEnd
import de.sciss.synth.osc._
import org.mt4j.util.MT4jSettings


object AudioServer {
	implicit def synth2Synthesizer(s: Synth):Synthesizer = AudioServer.lookupOrCreateAndRegister(s)
	implicit def jstringjDoubleControlSet( tup: (java.lang.String, java.lang.Double) )            = ControlSetMap.Single( tup._1, tup._2.toFloat )
	implicit def jstringjFloatControlSet( tup: (java.lang.String, java.lang.Float) )            = ControlSetMap.Single( tup._1, tup._2.toFloat )

	private val timerBus = 511;
	private val timerFreq = 500
	private val amplitudeTriggerID = 63
	private val ctrlBusChannels = 512
	private val defaultTriggerDivisions = 20

  private val audioOutBusFirstChannel = 0
  private val audioOutBusses = Seq(audioOutBusFirstChannel,audioOutBusFirstChannel+1)

	private val mapLock = new Object;
	private var idToSynthMap = Map[Int, Synthesizer]();

	private var serverBooted = false

	private def timerTriggerSynth: SynthDef = {
		SynthDef("triggerSynth") {
			Out.kr(timerBus, Impulse.kr(timerFreq))
		}
	}

  private def globalLimiter: SynthDef = {
 		SynthDef("globalLimiter") {
       ReplaceOut.ar(audioOutBusses, Limiter.ar(In.ar(audioOutBusFirstChannel, 2), 0.3))
 		}
 	}

	object NodeListener extends Model.Listener {
		def isDefinedAt(v: AnyRef) = {
			v match {
				case NodeEnd(x, y) => true;
				case _ => false;
			}
		}

		def apply(v: AnyRef) = {
			v match {
				case NodeEnd(x, y) => unregisterSynth(x.id);
			}
		}
	}

	private[audio] def lookupOrCreateAndRegister(s: Synth): Synthesizer = {
		var ret: Synthesizer = null;
		mapLock.synchronized{
			ret = idToSynthMap.getOrElse(s.id, null);
		}
		if(ret == null) {
			ret = new Synthesizer(s)
			registerSynth(ret);
		}
		ret
	}

	private def registerSynth(s: Synthesizer) = {
		mapLock.synchronized {
			idToSynthMap += (s.synth.id -> s)
		}
	}

	private def unregisterSynth(id: Int) = {
		mapLock.synchronized {
			idToSynthMap -= id
		}
	}

	object ServerResponder extends PartialFunction[Message, Unit] {
		def isDefinedAt(v: Message): Boolean = {
			v match {
				case Message("/tr", _, _, _) => true
				case _ => false
			}
		}


		def apply(v: Message) = {
			v match {
				case Message("/tr", x, y, AudioServer.amplitudeTriggerID) => {
					var s: Synthesizer = null
					mapLock.synchronized {
						s = idToSynthMap.getOrElse(x.asInstanceOf[Int], null)
					}

					if (s != null) {
            val synth = s.synth
            val amp = y.asInstanceOf[Int] / 1000.0f

            if (Math.abs(amp) > s.maxAmplitude) {
              s.maxAmplitude = Math.abs(amp)
              println(synth+" max amplitude "+s.maxAmplitude)
            }
						s.amplitude.emit(y.asInstanceOf[Int] / 1000.0f)
					}
				}
			}
		}
	}

	// once you made your processing graph, dont forget to call attach
	// to add its output and _RECEIVE_ ui updates (amplitude)
	def attach(graph : GE) : GE = {
		val pdiv = "__pulseDivision".kr(defaultTriggerDivisions)
		Out.ar(audioOutBusses, graph)
		SendTrig.kr(PulseDivider.kr(In.kr(timerBus),pdiv), amplitudeTriggerID, graph*1000f)//1000 * Amplitude.kr(graph))
	}

	def tt:SynthDef = {SynthDef("test") {
						val amp = "amp".kr(0.0)
						val freq = "freq".kr(200)
						val freq2 = "freq2".kr(200)
						val f = LFSaw.kr(freq2).madd(24, LFSaw.kr(Seq(8, 7.23)).madd(3, 80)).midicps
						val signal = amp * (CombN.ar(SinOsc.ar(f) * 0.04, 0.2, 0.2, 4)) * SinOsc.ar(freq);
						Out.ar(0, signal)
						SendTrig.kr(In.kr(timerBus), amplitudeTriggerID, 1000 * Amplitude.kr(signal))
					}}
	def tt3:SynthDef = {SynthDef("test") {
						val amp = "amp".kr(0.0)
						val freq = "freq".kr(200)
						val freq2 = "freq2".kr(200)
						val f = LFSaw.kr(freq2).madd(24, LFSaw.kr(Seq(8, 7.23)).madd(3, 80)).midicps
						val signal = amp * (CombN.ar(SinOsc.ar(f) * 0.04, 0.2, 0.2, 4)) * SinOsc.ar(freq);
						attach(signal)
					}}

	def tt2:SynthDef = {SynthDef("test2") {
							val fcar = "fcar".kr(200)
							val fmod = "fmod".kr(200)
							val idx = "idx".kr(1)
							val attack = "attack".kr(0.001)
							val decay = "decay".kr(0.001)
							val op = "operator".kr(1)
							val speed = "speed".kr(5)

							val imp = Impulse.ar(speed);
							val envelope = EnvGen.ar(Env.perc(attack, decay, 1.0, curveShape(-4)),imp );
							val algs = List( 	SinOsc.ar(fcar+ idx*envelope*SinOsc.ar(fmod)),
												Pulse.ar(fcar+ idx*envelope*SinOsc.ar(fmod)),
												Saw.ar(fcar+ idx*envelope*SinOsc.ar(fmod))
											)
							val sig1 = Select.ar(op*algs.size, algs)

							val fcar2 = "fcar2".kr(200)
							val fmod2 = "fmod2".kr(200)
							val idx2 = "idx2".kr(1)
							val attack2 = "attack2".kr(0.001)
							val decay2 = "decay2".kr(0.001)
							val op2 = "operator2".kr(1)
							val speed2 = "speed2".kr(5)  // ringmod?! oder zusätzlicher osci?! fx?!

							val envelope2 = EnvGen.ar(Env.perc(attack2, decay2, 1.0, curveShape(-4)),imp );


							val algs2 = List( 	SinOsc.ar(fcar2+ idx2*envelope2*sig1) ,
												Pulse.ar(fcar2+ idx2*envelope2*sig1) ,
												Saw.ar(fcar2+ (idx2*envelope2*sig1) )
											)

							val sig2 = Select.ar(op2*algs2.size, algs2)

							val sig3 = LinXFade2.ar(sig1, sig2, speed2);

							val attack3 = "fattack".kr(0.001)
							val decay3 = "fdecay".kr(0.001)


							val envelope3 = EnvGen.ar(Env.perc(attack3, decay3, 1.0, curveShape(-4)),imp );

							val op3 = "filtertype".kr(1)
							val filtermod = "filtermod".kr(1.0)
							val ffreq = "filterfreq".kr(100)*envelope3*filtermod
							val algs3 = List( 	BLowPass.ar(sig3, ffreq) ,
												BHiPass.ar(sig3, ffreq) ,
												BBandPass.ar(sig3, ffreq) ,
												BBandStop.ar(sig3, ffreq)
							)

							val signal = Select.ar(op3*algs3.size, algs3)

							attach(signal)
						}}

  import org.mt4j.MTApplication
  import org.mt4j.util.MT4jSettings

	def start( func: => Unit ) = {
		val cfg = Server.Config();
		cfg.controlBusChannels = ctrlBusChannels;

		//cfg.memorySize  = 65536*2;


		cfg.programPath = MT4jSettings.getInstance().getScSynthPath();

    var audioDev = MT4jSettings.getInstance().getDefaultAudioDevice()

		if(audioDev != "") {
			MTApplication.logInfo("Using Audio Device "+audioDev)
			cfg.deviceName = Some(audioDev);
		}

		Server.run(cfg) { s =>
      s.nodeMgr.addListener(NodeListener)

      // for visual feedback
      timerTriggerSynth.play

      // limit output if necessary
      if(MT4jSettings.getInstance().isLimiterEnabled()){
        globalLimiter.play(s, addAction = addToTail)
      }

      Responder.add(ServerResponder)
      serverBooted = true
      func
		}
	}

	def start(synchronously: Boolean) {
		start()
		if(synchronously) {
			while(!serverBooted) {
				Thread.sleep(100)
			}
		}
	}

  def quit {
    Server.default.quit // quit supercollider server scsynth
  }

}


/// pseudo u-gens
object Changed1 {
   def kr( in: GE, thresh: GE = 0 ) : GE = HPZ1.kr( in ).abs > thresh
   def ar( in: GE, thresh: GE = 0 ) : GE = HPZ1.ar( in ).abs > thresh
}
