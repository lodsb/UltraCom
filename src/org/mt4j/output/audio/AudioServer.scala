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


object AudioServer {
	implicit def synth2Synthesizer(s: Synth):Synthesizer = AudioServer.lookupOrCreateAndRegister(s)

	private val timerBus = 511;
	private val timerInterval = 10
	private val amplitudeTriggerID = 63
	private val ctrlBusChannels = 512

	private val mapLock = new Object;
	private var idToSynthMap = Map[Int, Synthesizer]();

	private def timerTriggerSynth: SynthDef = {
		SynthDef("triggerSynth") {
			Out.kr(timerBus, Impulse.kr(timerInterval))
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
			idToSynthMap.remove(id)
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
						s.amplitude.emit(y.asInstanceOf[Int] / 1000.0f)
					}
				}
			}
		}
	}

	def tt:SynthDef = {SynthDef("test") {
						val amp = "amp".kr(0.0)
						val f = LFSaw.kr(0.4).madd(24, LFSaw.kr(Seq(8, 7.23)).madd(3, 80)).midicps
						val signal = amp * (CombN.ar(SinOsc.ar(f) * 0.04, 0.2, 0.2, 4))
						Out.ar(0, signal)
						SendTrig.kr(In.kr(timerBus), amplitudeTriggerID, 1000 * Amplitude.kr(signal))
					}}

	def start( func: () => Unit ) = {
		val cfg = Server.Config();
		cfg.controlBusChannels = ctrlBusChannels;

		// TODO make config for this
		// FIXME: set up to your own system path pointing to scsynth
		cfg.programPath = "/usr/local/bin/scsynth";

		//Server.boot("UltraCom-Audio", cfg);
		Server.run(cfg) {

			s =>
				s.nodeMgr.addListener(NodeListener)
				timerTriggerSynth.play
				Responder.add(ServerResponder)
				func()
		}
	}

}
