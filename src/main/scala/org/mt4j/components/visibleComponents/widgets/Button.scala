/*
 ++1>>  This source code is licensed as GPLv3 if not stated otherwise.
    >>  NO responsibility taken for ANY harm, damage done
    >>  to you, your data, animals, etc.
    >>
  +2>>
    >>  Last modified:  2013 - 2 - 11 :: 8 : 47
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

import buttons.{MTTextButton, MTSvgButton, MTImageButton}
import processing.core.PImage
import org.mt4j.MTApplication
import org.mt4j.util.math.Vector3D
import java.io.File
import org.mt4j.components.visibleComponents.font.Font

object Button {

	def apply(image: PImage): MTImageButton = {
		val app = MTApplication.getInstance();

		val imageButton = new MTImageButton(app,image);
		imageButton.setPositionGlobal(new Vector3D(app.width / 2f, app.height / 2f));


		imageButton
	}

	def apply(file:File): MTSvgButton = {
		val app = MTApplication.getInstance();
		val x = app.width / 2f;
		val y = app.height / 2f;

		val svg = new MTSvgButton(app,file.getAbsolutePath);
		svg.setPositionGlobal(new Vector3D(app.width / 2f, app.height / 2f));

		svg
	}

	def apply(text: String): MTTextButton = {
		val app = MTApplication.getInstance();
		val x = 200+app.width / 2f;
		val y = app.height / 2f;

		val textButton = new MTTextButton(app,text, Font());
		textButton.setPositionGlobal(new Vector3D(app.width / 2f, app.height / 2f));

		textButton

	}

	// fixed size and text clipping version of textbutton
	def apply(text: String, width: Float, height: Float): MTTextButton = {
		val app = MTApplication.getInstance();
		val x = app.width / 2f;
		val y = app.height / 2f;

		val textButton = new MTTextButton(app,text,x,y,width,height);	

		textButton

	}

}
