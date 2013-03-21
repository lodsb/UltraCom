/*
 ++1>>  This source code is licensed as GPLv3 if not stated otherwise.
    >>  NO responsibility taken for ANY harm, damage done
    >>  to you, your data, animals, etc.
    >>
  +2>>
    >>  Last modified:  2013 - 3 - 19 :: 2 : 57
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

import org.mt4j.util.math.Vector3D
import org.mt4j.MTApplication

object Ellipse {

	def apply(): MTEllipse = {

		val app = MTApplication.getInstance();

		val center = new Vector3D(app.width / 2f, app.height / 2f)

		val radiusX = 20.0f
		val radiusY = 20.0f

		val ellipse = new MTEllipse(app, center, radiusX, radiusY);

		ellipse
	}

	def apply(center:Vector3D): MTEllipse = {

		val app = MTApplication.getInstance();

		val radiusX = 20.0f
		val radiusY = 20.0f

		val ellipse = new MTEllipse(app, center, radiusX, radiusY);

		ellipse
	}


	def apply(radiusX: Float, radiusY: Float): MTEllipse = {

		val app = MTApplication.getInstance();

		val center = new Vector3D(app.width / 2f, app.height / 2f)

		val ellipse = new MTEllipse(app, center, radiusX, radiusY);

		ellipse
	}

	def apply(center:Vector3D, radiusX: Float, radiusY: Float): MTEllipse = {

		val app = MTApplication.getInstance();

		val ellipse = new MTEllipse(app, center, radiusX, radiusY);

		ellipse
	}


}
