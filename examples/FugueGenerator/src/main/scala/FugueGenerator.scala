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
import org.mt4j.util.{SessionLogger, MTColor}
import scala.collection.mutable.ArrayBuffer
import org.mt4j.components.visibleComponents.widgets.Slider
import org.mt4j.output.audio.AudioServer
import java.beans.{PropertyChangeEvent, PropertyChangeListener}
import java.net.{InetAddress, InetSocketAddress}


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



  //app.getInputManager.registerInputSource()



  // -- add first player modules

  // add a ControllerCanvas, which contains the sound-controller
  val controllerCanvas1 = new ControllerCanvas(600f, 400f, 16)
  app.scene.canvas += controllerCanvas1
  app.allControllerCanvas += controllerCanvas1

  // add local arousal slider for this ControllerCanvas
  val slider1 = Slider(0f, 1f, width=400f)
  slider1.value.map { x =>

    // get a value between 0 and 1
    val percent = x / slider1.getValueRangeVar

    // set the arousal/activity of the corresponding ControllerCanvas
    controllerCanvas1.activity(percent)

  }
  app.scene.canvas += slider1

  // set positions and rotate
  controllerCanvas1.rotate90
  controllerCanvas1.setPositionGlobal(Vec3d(300, app.center.getY))
  controllerCanvas1.setName("Canvas1")
  slider1.rotateZ(app.center, 90f)
  slider1.setPositionGlobal(Vec3d(60f, app.center.getY))


  // -- add second player modules

  // add a ControllerCanvas, which contains the sound-controller
  val controllerCanvas2 = new ControllerCanvas(600f, 400f, 16)
  app.scene.canvas += controllerCanvas2
  app.allControllerCanvas += controllerCanvas2

  // add local arousal slider for this ControllerCanvas
  val slider2 = Slider(0f, 1f, width=400f)
  slider2.value.map { x =>
    // get a value between 0 and 1
    val percent = x / slider2.getValueRangeVar
    // set the arousal/activity of the corresponding ControllerCanvas
    controllerCanvas2.activity(percent)
  }
  app.scene.canvas += slider2

  // set positions
  controllerCanvas2.globalPosition() = Vec3d(app.width-300, app.center.getY)
  controllerCanvas2.rotate180
  controllerCanvas2.rotate90
  controllerCanvas2.setName("Canvas2")
  slider2.globalPosition() = Vec3d(app.width-60, app.center.getY)
  slider2.rotateZ(app.center, 270f)


  // create the logic for democratic tempo setting
  // (the tempo is averaged between the two arousal settings)
  val durationVotingListener = new PropertyChangeListener {
    override def propertyChange(evt: PropertyChangeEvent) {
      synchronized {
        val newValue = 50 + 100 * (1-evt.getNewValue.asInstanceOf[Float])
        val oldValue = 50 + 100 * (1-evt.getOldValue.asInstanceOf[Float])
        val otherValue = 2*Metronome.duration() - oldValue
        Metronome.duration() = (newValue + otherValue) / 2
      }
    }
  }
  slider1.addPropertyChangeListener("value", durationVotingListener)
  slider2.addPropertyChangeListener("value", durationVotingListener)




  // -- add global slider for valence in the middle

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
  valenceSlider.rotateZ(app.center, 90f)


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
  bassSlider.setPositionGlobal(Vec3d(app.width/2, 40f))


  canvas += Icon("arousal_low.png",   Vec3d(65, app.height/2-230), 0.1f, 90f)
  canvas += Icon("arousal_high.png",  Vec3d(65, app.height/2+230), 0.1f, 90f)
  canvas += Icon("arousal_low.png",   Vec3d(app.width-65, app.height/2+230), 0.1f, 270f)
  canvas += Icon("arousal_high.png",  Vec3d(app.width-65, app.height/2-230), 0.1f, 270f)
  canvas += Icon("valence_high_mirrored.png",   Vec3d(app.width/2f, app.height/2f+230), 0.1f, 90f)
  canvas += Icon("valence_low_mirrored.png",  Vec3d(app.width/2f, app.height/2f-230), 0.1f, 90f)


  // start Metronome
  Metronome() ! "start"



  app.getInputManager.registerInputSource(new KinectInputSource())

  // Logging
  //
  //
    // Arousal

    slider1.value.observe({ x=>
      SessionLogger.log("Arousal 1", SessionLogger.SessionEvent.Event, this, slider1, x)
      true
    })

    slider2.value.observe({ x=>
      SessionLogger.log("Arousal 2", SessionLogger.SessionEvent.Event, this, slider2, x)
      true
    })

    // Valence

    valenceSlider.value.observe({ x=>
      SessionLogger.log("Valence", SessionLogger.SessionEvent.Event, this, valenceSlider, x)
      true
    })

    // Octaving
    bassSlider.value.observe({ x=>
      SessionLogger.log("Octave", SessionLogger.SessionEvent.Event, this, bassSlider, x)
      true
    })


  /*
  private val localhost = InetAddress.getLocalHost()
  private val port = 3336
  private val socketAddress = new InetSocketAddress(localhost, port)
  val oscReceive: SignalingOSCReceiver = OSCCommunication.createOSCReceiver(UDP, socketAddress)

  var textField2 = TextArea()
  textField2.globalPosition := Vec3d(100,300)
  var zaehler = 0
  oscReceive.receipt.observe(
    ((x: (Message, InetSocketAddress)) => {
      try {
        println("zaehler=" + zaehler+ ", " + x._1.toString())
      } catch {
        case e: ClassCastException => {
          val bundle = x._1.asInstanceOf[de.sciss.osc.Bundle]
          println("zaehler=" + zaehler + ", " + bundle.toString())
        }
      }
      zaehler += 1
      true
    })
  )
  textField2.text := "bar"

  canvas() += textField2

  */
}
