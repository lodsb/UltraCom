/*
 ++1>>  This source code is licensed as GPLv3 if not stated otherwise.
    >>  NO responsibility taken for ANY harm, damage done
    >>  to you, your data, animals, etc.
    >>
  +2>>
    >>  Last modified:  2012 - 5 - 7 :: 7 : 50
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

import de.sciss.synth.SynthDef
import io.Source
import tools.nsc.io.Directory

object EEMDSynthDef {
	def apply(directory: String): SynthDef = {
		null
	}

	private def loadEEMDFileDir(dirName: String) {
		val dir = Directory(dirName);
		val fileRegex = """sound_([a-z]+)(\d*)(.*)""".r

		if (dir.isValid) {
			var loadedFiles = dir.list.map(
				x=> x.toString() match {
					case fileRegex(typeStr, numStr) => {
						val num = java.lang.Integer.valueOf(numStr);

						(num, typeStr, readCSVFile(x.toString()))
					}

					case _ => (Nil, Nil, Nil)
				}
			)

			//loadedFiles  = loadedFiles sort ({(x:(Int, String, Seq[Float]),y:(Int, String, Seq[Float])) => x._1 < y._1});

		} else null
	}

	private def readCSVFile(filename: String) {

		val src = Source.fromFile(filename)
		src.reset.getLines().map(java.lang.Float.valueOf(_)).toSeq
	}
}
