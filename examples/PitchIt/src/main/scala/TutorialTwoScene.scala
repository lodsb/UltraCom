/*
 ++1>>  This source code is licensed as GPLv3 if not stated otherwise.
    >>  NO responsibility taken for ANY harm, damage done
    >>  to you, your data, animals, etc.
    >>
  +2>>
    >>  Last modified:  2013 - 3 - 19 :: 0 : 54
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

package PitchIt

import org.mt4j.{Scene, Application}
import de.sciss.synth._

import ugen._
import org.mt4j.output.audio.AudioServer
import org.mt4j.output.audio.AudioServer._
import org.mt4j.components.visibleComponents.widgets.{Scope, Slider}
import org.mt4j.components.visibleComponents.shapes.Ellipse
import org.mt4j.types.Vec3d
import de.sciss.synth._
import de.sciss.synth.Ops._ 
import org.mt4j.components.ComponentImplicits._
import org.mt4j.util.{MTColor, Color}
import org.mt4j.sceneManagement.Iscene


object app extends Application {
	/*
			Settings, such as <b>the application name<b>, display properties, etc are set in Settings.txt
	 */

  var scene = null.asInstanceOf[Scene]
  val TRANSPARENT = new MTColor(0,0,0,0)

	def main(args: Array[String]): Unit = {
		this.execute(false)
	}

	override def startUp() = {
		addScene(new TutorialTwoScene)
	}

}


class TutorialTwoScene extends Scene(app, "PitchIt Scene") {
  app.scene = this
	// Show touches
	showTracer(true)

  // add a ControllerCanvas, which contains the sound-controller
  val controllerCanvas = new ControllerCanvas(200f, 100f, 4)
  app.scene.canvas() += controllerCanvas
  controllerCanvas.setPositionGlobal(Vec3d(app.width/2f, app.height/2f))
}
