/*
 ++1>>  This source code is licensed as GPLv3 if not stated otherwise.
    >>  NO responsibility taken for ANY harm, damage done
    >>  to you, your data, animals, etc.
    >>
  +2>>
    >>  Last modified:  2013 - 2 - 11 :: 6 : 0
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
import com.jogamp.graph.font.FontFactory
import org.mt4j.components.visibleComponents.font.{Font, FontManager}
import java.io.File
import org.mt4j.components.MTComponent
import processing.opengl.PGraphicsOpenGL

/*
 ++1>>  This source code is licensed as GPLv3 if not stated otherwise.
    >>  NO responsibility taken for ANY harm, damage done
    >>  to you, your data, animals, etc.
    >>
  +2>>
    >>  Last modified:  2013 - 2 - 11 :: 5 : 54
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


object TextArea {
	def apply(): MTTextArea = {
		val app = MTApplication.getInstance();
		val x = app.width / 2f;
		val y = app.height / 2f;

		val textArea = new MTTextArea(app, Font());
		textArea.setPositionGlobal(new Vector3D(app.width / 2f, app.height / 2f));
		textArea.setNoFill(true);

		textArea
	}

  def apply(s: String): MTTextArea = {
    val t = this.apply()
    t.setText(s)

    t
  }

	//TODO: add textarea with fixed size and textclipping

}
