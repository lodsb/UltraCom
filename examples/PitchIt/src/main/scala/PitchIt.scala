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
import org.mt4j.types.Vec3d
import org.mt4j.components.ComponentImplicits._
import org.mt4j.util.MTColor
import scala.collection.mutable.ArrayBuffer
import org.mt4j.components.visibleComponents.widgets.Slider


object app extends Application {

  // some global variables
  var scene = null.asInstanceOf[Scene]
  val TRANSPARENT = new MTColor(0,0,0,0)
  def center = Vec3d(width/2f,height/2f)

  // get all ControllerCanvas in an ArrayBuffer
  def allControllerCanvas = {
    val children = app.scene.canvas().getChildren
    val canvas = new ArrayBuffer[ControllerCanvas]()
    children.foreach {
      case c: ControllerCanvas => canvas += c
      case _ =>
    }
    canvas
  }

	def main(args: Array[String]): Unit = {
		this.execute(false)
	}

	override def startUp() = {
		addScene(new PitchItScene)
	}

}


class PitchItScene extends Scene(app, "PitchIt Scene") {

  // setting scene for global access
  app.scene = this

	// Show touches
	showTracer(show = true)

  // add a ControllerCanvas, which contains the sound-controller
  val controllerCanvas = new ControllerCanvas(300f, 100f, 16)
  app.scene.canvas() += controllerCanvas
  controllerCanvas.setPositionGlobal(Vec3d(app.width/2f, app.height/2f))

  val slider = Slider(2f, 16f, height=20f)
  slider.value.map { x =>
    val z = math.round(x)
    if (z==2 || z==4 || z==8 || z==16) {
      controllerCanvas.initializeControllers(z)
    }
  }
  app.scene.canvas() += slider
  slider.globalPosition() = app.center.getAdded(Vec3d(0f,80f))

  // start Metronome
  Metronome() ! "start"

}
