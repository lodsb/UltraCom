/*
 ++1>>  This source code is licensed as GPLv3 if not stated otherwise.
    >>  NO responsibility taken for ANY harm, damage done
    >>  to you, your data, animals, etc.
    >>
  +2>>
    >>  Last modified:  2012 - 4 - 20 :: 8 : 15
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

import org.lodsb.reakt.async.{ValA, VarA}
import de.sciss.synth._
import collection.immutable.{IndexedSeq => Vec}
//import language.implicitConversions
import de.sciss.osc
import de.sciss.synth.Ops
import de.sciss.synth.Ops._

import org.lodsb.reakt.sync.VarS
import org.lodsb.reakt.sync.ValS

class Synthesizer(val synth: Synth) {
	val amplitude: ValS[Float] = new ValS[Float](0f)
	val parameters: VarS[ControlSetMap] = new VarS[ControlSetMap](("_"->0.0));

  // for debugging
  var currentAmp = 0.0f;
  var maxAmplitude = 0.0f;

	//parameters.observe({ x => synth.set(x); true;})
	synth.set("freq1" -> 0.1)

	def setAmplitudeUpdateDivisions(divisions: Int) = {
		synth.set( "__pulseDivision" -> divisions  )
	}

}



