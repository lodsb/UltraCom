/*
 ++1>>  This source code is licensed as GPLv3 if not stated otherwise.
    >>  NO responsibility taken for ANY harm, damage done
    >>  to you, your data, animals, etc.
    >>
  +2>>
    >>  Last modified:  2013 - 2 - 11 :: 8 : 41
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

package org.mt4j.components.visibleComponents.widgets

import org.mt4j.MTApplication
import org.mt4j.util.math.Vector3D
import processing.core.PImage

object Image {
	def apply(img: PImage): MTImage = {
		val app = MTApplication.getInstance();
		val x = app.width / 2f;
		val y = app.height / 2f;

		val image = new MTImage(app,img);
		image.setPositionGlobal(new Vector3D(app.width / 2f, app.height / 2f));


		image
	}

}
