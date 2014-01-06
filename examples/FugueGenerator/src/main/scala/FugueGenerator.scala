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

package de.ghagerer.FugueGenerator

import org.mt4j.{Scene, Application}
import org.mt4j.types.Vec3d
import org.mt4j.components.ComponentImplicits._
import org.mt4j.util.MTColor
import scala.collection.mutable.ArrayBuffer
import org.mt4j.components.visibleComponents.widgets.{MTImage, MTBackgroundImage, Slider}
import org.mt4j.output.audio.AudioServer
import java.beans.{PropertyChangeEvent, PropertyChangeListener}
import org.mt4j.components.visibleComponents.shapes.MTRectangle


object app extends Application {

  // some global variables
  var scene = null.asInstanceOf[Scene]
  val TRANSPARENT = new MTColor(0,0,0,0)
  def center = Vec3d(width/2f,height/2f)

  // get all ControllerCanvas in an ArrayBuffer
  val allControllerCanvas = new ArrayBuffer[ControllerCanvas]()

	def main(args: Array[String]): Unit = {
		execute(false)
	}

	override def startUp() = {
		addScene(new FugueGeneratorScene)
	}

}


class FugueGeneratorScene extends Scene(app, "FugueGenerator") {

  // setting scene for global access
  app.scene = this

  // start AudioServer
  AudioServer.start(synchronously = true)

	// Show touches
	showTracer(show = true)

  // set harmony complexity
  //Harmony.progressionComplexity(0.57f)


  // -- add first player modules

  // add a ControllerCanvas, which contains the sound-controller
  val controllerCanvas1: ControllerCanvas = new ControllerCanvas(300f, 200f, 16)
  app.scene.canvas += controllerCanvas1
  app.allControllerCanvas += controllerCanvas1

  // add local arousal slider for this ControllerCanvas
  val slider1 = Slider(0f, 1f, height=20f)
  slider1.value.map { x =>

    // get a value between 0 and 1
    val percent = x / slider1.getValueRangeVar

    // set the arousal/activity of the corresponding ControllerCanvas
    controllerCanvas1.activity(percent)

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

  // add local arousal slider for this ControllerCanvas
  val slider2 = Slider(0f, 1f, height=20f)
  slider2.value.map { x =>
    // get a value between 0 and 1
    val percent = x / slider2.getValueRangeVar
    // set the arousal/activity of the corresponding ControllerCanvas
    controllerCanvas2.activity(percent)
  }
  app.scene.canvas += slider2

  // set positions
  controllerCanvas2.setPositionGlobal(Vec3d(app.center.getX, app.height-150f))
  slider2.setPositionGlobal(Vec3d(app.center.getX, app.height-25f))


  val durationVotingListener = new PropertyChangeListener {
    override def propertyChange(evt: PropertyChangeEvent) {
      synchronized {

        // 0.0 -> 50, 0.5 -> 100, 1.0 -> 150

        val newValue = 50 + 100 * (1-evt.getNewValue.asInstanceOf[Float])
        val oldValue = 50 + 100 * (1-evt.getOldValue.asInstanceOf[Float])
        val otherValue = 2*Metronome.duration() - oldValue
        Metronome.duration() = (newValue + otherValue) / 2
        println("newValue="+newValue+", oldValue="+oldValue+", otherValue="+otherValue+", duration="+Metronome.duration())
      }
    }
  }
  slider1.addPropertyChangeListener("value", durationVotingListener)
  slider2.addPropertyChangeListener("value", durationVotingListener)




  // -- add global slider in the middle

  // add slider
  val valenceSlider = Slider(0f, 1f, height=40f, width=400f)
  valenceSlider.value.map { x =>
    val percent = x / valenceSlider.getValueRangeVar

    // set complexity of harmonies
    //Harmony.complexity(percent)

    // set the active scale, depending on how valence was adjusted
    val index = math.round(percent * Scales.size)
    Scales.activeScale(index)

    controllerCanvas1.valence(x)
    controllerCanvas2.valence(x)
  }

  // add slider to canvas
  app.scene.canvas += valenceSlider

  // set position
  valenceSlider.setPositionGlobal(app.center)


  // -- add bass slider for selecting who plays bass melody

  val bassSlider = Slider(0f, 1f, height=40f, width=400f)
  bassSlider.value.map { x =>
    val percent = x / bassSlider.getValueRangeVar
    var i = 0
    app.allControllerCanvas.foreach( controllerCanvas => {
      controllerCanvas.synthi.bass() = if(i==0) percent else 1-percent
      i += 1
    })
  }
  app.scene.canvas += bassSlider
  bassSlider.rotateZGlobal(app.center, 90f)
  bassSlider.setPositionGlobal(Vec3d(200f, app.height/2))


  canvas += Icon("arousal_low.png",   Vec3d(app.width/2f-130, app.height-20f),  0.1f)
  canvas += Icon("arousal_high.png",  Vec3d(app.width/2f+130, app.height-20f),  0.1f)
  canvas += Icon("arousal_low.png",   Vec3d(app.width/2f+130, 20f),             0.1f, true)
  canvas += Icon("arousal_high.png",  Vec3d(app.width/2f-130, 20f),             0.1f, true)
  canvas += Icon("valence_low.png",  Vec3d(app.width/2f+230f, app.height/2f),  0.15f)
  canvas += Icon("valence_high.png",   Vec3d(app.width/2f-230f, app.height/2f),  0.15f)


  // start Metronome
  Metronome() ! "start"

}
