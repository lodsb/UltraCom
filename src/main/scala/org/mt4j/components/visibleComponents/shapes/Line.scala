/*
 ++1>>  This source code is licensed as GPLv3 if not stated otherwise.
    >>  NO responsibility taken for ANY harm, damage done
    >>  to you, your data, animals, etc.
    >>
  +2>>
    >>  Last modified:  2013 - 3 - 17 :: 10 : 58
    >>  Origin: mt4j (project) / UltraCom (module)
    >>
  +3>>
    >>  Copyright (c) 2013:
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

package org.mt4j.components.visibleComponents.shapes

import processing.core.PImage
import org.mt4j.components.visibleComponents.widgets.buttons.MTImageButton
import org.mt4j.MTApplication
import org.mt4j.util.math.{Vertex, Vector3D}

object Line {
	def apply(start: Vertex, end: Vertex): MTLine = {

		val app = MTApplication.getInstance();

		val line = new MTLine(app, start, end)

		line
	}

	def apply(start: Vector3D, end: Vector3D): MTLine = {

		val app = MTApplication.getInstance();

		val line = new MTLine(app, new Vertex(start), new Vertex(end))

		line
	}

	def apply(): MTLine = {

		val app = MTApplication.getInstance();

		val start = new Vector3D(app.width / 2f, app.height / 2f)
		val end = new Vector3D(app.width / 2f, app.height / 2f)
		val line = new MTLine(app, new Vertex(start), new Vertex(end))

		line
	}



}
