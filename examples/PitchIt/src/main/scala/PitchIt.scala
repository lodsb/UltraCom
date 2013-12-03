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
import org.mt4j.output.audio.AudioServer
import org.mt4j.components.MTComponent


object app extends Application {

  // some global variables
  var scene = null.asInstanceOf[Scene]
  val TRANSPARENT = new MTColor(0,0,0,0)
  def center = Vec3d(width/2f,height/2f)

  // get all ControllerCanvas in an ArrayBuffer
  val allControllerCanvas = new ArrayBuffer[ControllerCanvas]()

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

  // start AudioServer
  AudioServer.start(synchronously = true)

	// Show touches
	showTracer(show = true)

  val component1 = new MTComponent(app)
  app.scene.canvas += component1

  // add a ControllerCanvas, which contains the sound-controller
  val controllerCanvas1: ControllerCanvas = new ControllerCanvas(300f, 200f, 16)
  component1 += controllerCanvas1
  app.allControllerCanvas += controllerCanvas1
  controllerCanvas1.setPositionGlobal(Vec3d(app.width/2f, app.height/2f))

  // add slider
  val slider1 = Slider(0f, 1f, height=20f)
  slider1.value.map { x =>
    val percent = x / slider1.getValueRangeVar
    val numberOfControllers = math.pow(2,1+(percent / 0.25).toInt).toInt
    controllerCanvas1.initializeControllers(numberOfControllers)
    controllerCanvas1.synthi.activity() = percent
  }
  component1 += slider1
  slider1.globalPosition() = app.center.getAdded(Vec3d(0f,120f))

  //controllerCanvas1.rotate180

/*

  val component2 = new MTComponent(app)
  app.scene.canvas += component2

  // add a second ControllerCanvas, which contains the sound-controller
  val controllerCanvas2 = new ControllerCanvas(300f, 200f, 16)
  component2 += controllerCanvas2
  controllerCanvas2.setPositionGlobal(Vec3d(app.width/2f, app.height/2f))

  // add slider
  val slider2 = Slider(0f, 1f, height=20f)
  slider2.value.map { x =>
    val percent = x / slider2.getValueRangeVar
    val numberOfControllers = math.pow(2,1+(percent / 0.25).toInt).toInt
    controllerCanvas2.initializeControllers(numberOfControllers)
    controllerCanvas2.synthi.activity() = percent
  }
  app.scene.canvas() += slider2
  slider2.globalPosition() = app.center.getAdded(Vec3d(0f,120f))
*/

  // start Metronome
  Metronome() ! "start"

}
