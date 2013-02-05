/*
 ++1>>  This source code is licensed as GPLv3 if not stated otherwise.
    >>  NO responsibility taken for ANY harm, damage done
    >>  to you, your data, animals, etc.
    >>
  +2>>
    >>  Last modified:  2012 - 10 - 18 :: 8 : 16
    >>  Origin: mt4j (project) / prototaip (module)
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


package prototaip

import de.sciss.synth._
import de.sciss.synth.{Buffer, SynthDef}
import org.mt4j.output.audio.{Changed1, AudioServer}
import de.sciss.synth.ugen._

object WaveSynth {
	def apply(filename: String) : SynthDef = {
		createSynth(filename)
	}

	private def createSynth(filename: String) : SynthDef = {
		val buf = Buffer.read(path = filename);
		SynthDef("WaveSynth") {
			val pos = "pos".kr(1.0)
			val trigger = "trig".kr(1.0)
			val changeTrigger = Changed1.kr(trigger)
			val playBuf = PlayBuf.ar(1, buf.id, BufRateScale.kr(buf.id), changeTrigger, pos);
			val signal = playBuf;
			AudioServer attach signal;
		}
	}

}
