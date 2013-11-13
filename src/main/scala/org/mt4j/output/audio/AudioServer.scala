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
import de.sciss.osc._
import Implicits._
import scala.collection.mutable.Map
import de.sciss.synth.NodeManager.NodeEnd
import de.sciss.model._
import de.sciss.synth.message._
import de.sciss.synth.Ops._
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

	object NodeListener extends Model.Listener[de.sciss.synth.NodeManager.Update] {
		def isDefinedAt(v: de.sciss.synth.NodeManager.Update) = {
			v match {
				case NodeEnd(x, y) => true;
				case _ => false;
			}
		}

		def apply(v: de.sciss.synth.NodeManager.Update) = {
			v match {
				case NodeEnd(x, y) => unregisterSynth(x.id);
			}
		}
	}

	private[audio] def lookupOrCreateAndRegister(s: Synth): Synthesizer = {
		var ret: Synthesizer = null;
		mapLock.synchronized{
			if(s != null) {
						ret = idToSynthMap.getOrElse(s.id, null);
			} else {
				println("SYNTH == NULL")
			}
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

	object TriggerResponder extends PartialFunction[Message, Unit] {
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

	def triggerResponder(v: Message) = {
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

	// once you made your processing graph, dont forget to call attach
	// to add its output and _RECEIVE_ ui updates (amplitude)
	def attach(graph : GE, extendChannels : Boolean = true) : GE = {
		val pdiv = "__pulseDivision".kr(defaultTriggerDivisions)
		if(extendChannels) {
			//stereo
			Out.ar(audioOutBusses, graph)
		} else {
			Out.ar(audioOutBusFirstChannel,graph)
		}
		SendTrig.kr(PulseDivider.kr(In.kr(timerBus),pdiv), amplitudeTriggerID, graph*1000f)//1000 * Amplitude.kr(graph))
	}


  import org.mt4j.MTApplication
  import org.mt4j.util.MT4jSettings

	def start( func: => Unit ) = {
		val cfg = de.sciss.synth.Server.Config();
		cfg.controlBusChannels = ctrlBusChannels;

		//cfg.memorySize  = 65536*2;


		cfg.programPath = MT4jSettings.getInstance().getScSynthPath();

		var audioDev = MT4jSettings.getInstance().getDefaultAudioDevice()

		if(audioDev != "") {
			MTApplication.logInfo("Using Audio Device "+audioDev)
			cfg.deviceName = Some(audioDev);
		}

		de.sciss.synth.Server.run(cfg) { s =>
			s.nodeManager.addListener(NodeListener)

			// for visual feedback
			timerTriggerSynth.play(s)

			// limit output if necessary
			if(MT4jSettings.getInstance().isLimiterEnabled()){
				globalLimiter.play(s, addAction = addToTail)
			}

			de.sciss.synth.message.Responder.add(s)(TriggerResponder)
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
    de.sciss.synth.Server.default.quit // quit supercollider server scsynth
  }

}


/// pseudo u-gens
object Changed1 {
   def kr( in: GE, thresh: GE = 0 ) : GE = HPZ1.kr( in ).abs > thresh
   def ar( in: GE, thresh: GE = 0 ) : GE = HPZ1.ar( in ).abs > thresh
}
