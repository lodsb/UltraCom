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


  // -- add first player modules

  // add a ControllerCanvas, which contains the sound-controller
  val controllerCanvas1: ControllerCanvas = new ControllerCanvas(300f, 200f, 16)
  app.scene.canvas += controllerCanvas1
  app.allControllerCanvas += controllerCanvas1

  // add slider
  val slider1 = Slider(0f, 1f, height=20f)
  slider1.value.map { x =>
    val percent = x / slider1.getValueRangeVar
    val numberOfControllers = math.pow(2,1+(percent / 0.25).toInt).toInt
    controllerCanvas1.initializeControllers(numberOfControllers)
    controllerCanvas1.synthi.activity() = percent
  }
  app.scene.canvas += slider1

  // set positions and rotate
  controllerCanvas1.rotate180
  controllerCanvas1.setPositionGlobal(Vec3d(app.center.getX, 150f))
  slider1.rotateZ(app.center, 180f)
  slider1.setPositionGlobal(Vec3d(app.center.getX, 25f))


  // -- add second player modules

  // add a ControllerCanvas, which contains the sound-controller
  val controllerCanvas2: ControllerCanvas = new ControllerCanvas(300f, 200f, 16)
  app.scene.canvas += controllerCanvas2
  app.allControllerCanvas += controllerCanvas2

  // add slider
  val slider2 = Slider(0f, 1f, height=20f)
  slider2.value.map { x =>
    val percent = x / slider2.getValueRangeVar
    val numberOfControllers = math.pow(2,1+(percent / 0.25).toInt).toInt
    controllerCanvas2.initializeControllers(numberOfControllers)
    controllerCanvas2.synthi.activity() = percent
  }
  app.scene.canvas += slider2


  // set positions
  controllerCanvas2.setPositionGlobal(Vec3d(app.center.getX, app.height-150f))
  slider2.setPositionGlobal(Vec3d(app.center.getX, app.height-25f))


  // -- add global slider in the middle

  // add slider
  val slider3 = Slider(0f, 1f, height=40f, width=300f)
  slider3.value.map { x =>
    println("within slider3.value.map")
    val percent = x / slider3.getValueRangeVar
    app.allControllerCanvas.foreach( controllerCanvas => {
      val scaleNumber = math.round(percent * controllerCanvas.synthi.scales.size)
      controllerCanvas.synthi.activeScale = scaleNumber
    })
  }
  app.scene.canvas += slider3

  // set position
  slider3.setPositionGlobal(app.center)

  // start Metronome
  Metronome() ! "start"

}
