/*
 ++1>>  This source code is licensed as GPLv3 if not stated otherwise.
    >>  NO responsibility taken for ANY harm, damage done
    >>  to you, your data, animals, etc.
    >>
  +2>>
    >>  Last modified:  2012 - 10 - 16 :: 10 : 16
    >>  Origin: mt4j (project) / prototaip (module)
    >>
  +3>>
    >>  Copyright (c) 2012:
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

package prototaip

import org.mt4j.MTApplication
import org.mt4j.sceneManagement.SimpleAbstractScene
import org.mt4j.util.MTColor
import org.mt4j.input.inputProcessors.globalProcessors.CursorTracer
import org.mt4j.components.visibleComponents.font.FontManager
import org.mt4j.components.visibleComponents.widgets._
import org.lodsb.reaktExt.animation.{SineInOut, InterpolatingAnimation}
import org.mt4j.input.midi.MidiCommunication
import org.mt4j.util.math.Vector3D
import org.mt4j.util.math.Vector3D
import org.mt4j.components.visibleComponents.font.FontManager
import org.mt4j.util.MTColor

import java.net.InetSocketAddress
import org.mt4j.components.ComponentImplicits._

import org.mt4j.input.inputData.osc.MTOSCControllerInputEvt
import org.mt4j.MTApplication
import org.mt4j.sceneManagement.SimpleAbstractScene
import org.mt4j.input.inputProcessors.globalProcessors.CursorTracer
import org.mt4j.stateMachine.StateMachine
import org.mt4j.eventSystem.{foo, bar, TrEventListener}
import org.mt4j.commandSystem.Command


import org.mt4j.input.midi.{MidiCommunication, MidiCtrlMsg}
import org.mt4j.input.kinect.KinectSkeletonSource
import org.mt4j.components.visibleComponents.widgets._

import org.lodsb.reakt.ConstantSignal._
import org.lodsb.reaktExt.animation._
import org.mt4j.output.audio.AudioServer
import de.sciss.synth.SynthDef
import de.sciss.synth.ugen._
import AudioServer._
import org.mt4j.types.{Rotation}
import org.mt4j.input.osc.OSCCommunication

class PrototaipScene(mtApplication: MTApplication, name: String)
	extends SimpleAbstractScene(mtApplication, name) {

	//Show touches
	var tracer = new CursorTracer(this, mtApplication);

	var cp = new TimbreSurface(	mtApplication, "/home/lodsb/testsettings.csv", 0, 0,
								mtApplication.getSize.getWidth.toInt,
								mtApplication.getSize.getHeight.toInt)


	canvas += cp;

	var textField = new MTTextArea(Prototaip);
	textField.setNoFill(true);
	textField.setPositionGlobal(new Vector3D(mtApplication.width / 2f, mtApplication.height / 2f));
	textField.text <~ cp.soundPicked +""

	//Add the textfield to our canvas
	canvas += textField;


	AudioServer.start(() => {})
}

object Prototaip extends MTApplication {

	def main(args: Array[String]): Unit = {
		this.execute(false)
	}

	override def startUp() = {
		System.out.println("SADSDFSDF");
		val fsm = new PrototaipScene(this, "FPPPP");
		this.addScene(fsm)
	}

}
