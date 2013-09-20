/*
 ++1>>  This source code is licensed as GPLv3 if not stated otherwise.
    >>  NO responsibility taken for ANY harm, damage done
    >>  to you, your data, animals, etc.
    >>
  +2>>
    >>  Last modified:  2012 - 4 - 25 :: 6 : 6
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

package org.mt4j.components.visibleComponents.widgets

import org.mt4j.components.visibleComponents.shapes.MTRectangle
import processing.core.{PGraphics, PApplet}
import org.lodsb.reakt.sync.VarS
import org.mt4j.MTApplication

class Scope(applet: PApplet, x: Int, y: Int, width: Int, height: Int, noSigPoints: Int = 100)
	extends MTRectangle(applet, x, y, width, height) {

	val plot: VarS[Float] = new VarS[Float](0.0f);

	private var signalPoints = new Array[Float](noSigPoints);
	private var currentArrayIndex = 0;

	plot.observe({
		x => signalPoints(currentArrayIndex) = x;
		currentArrayIndex = (currentArrayIndex + 1) % noSigPoints;
		true;
	})

	override def drawComponent(g: PGraphics): Unit = {
		val width = this.getWidthLocal();
		val height = this.getHeightLocal();

		val pixPerPointOffset: Float = width / noSigPoints;

		val renderer = this.getRenderer();

		renderer.pushMatrix();

		renderer.translate(this.x, this.y + this.height / 2)

		renderer.noFill();
		renderer.stroke(0f, 200f, 255f)
		renderer.rect(0, -height, width, height)

		var lastY: Float = 0.0f;
		var lastX: Float = 0.0f;

		for (i <- 0 to noSigPoints - 1) {
			val curY = -signalPoints(i) * height / 2;
			val curX: Float = i.toFloat * pixPerPointOffset;

			renderer.line(lastX, lastY, curX, curY);

			lastX = curX;
			lastY = curY;
		}


		renderer.popMatrix();

	}
}

object Scope {
	def apply(): Scope = {
		val app = MTApplication.getInstance();
		val x = app.width / 2;
		val y = app.height / 2;

		val scope = new Scope(app, x, y, 100, 100);
		scope
	}

	def apply(x: Int, y: Int): Scope = {
		val app = MTApplication.getInstance();
		val scope = new Scope(app, x, y, 100, 100);
		scope
	}

	def apply(x: Int, y: Int,w:Int, h:Int): Scope = {
		val app = MTApplication.getInstance();
		val scope = new Scope(app, x, y, w, h);
		scope
	}

}

